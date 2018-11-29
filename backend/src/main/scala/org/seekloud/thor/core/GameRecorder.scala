package org.seekloud.thor.core

import java.io.File

import akka.actor.typed.{Behavior, PostStop}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.{ActorContext, StashBuffer, TimerScheduler}
import org.seekloud.byteobject.MiddleBufferInJvm
import org.seekloud.essf.io.FrameOutputStream
import org.seekloud.thor.common.AppSettings
import org.seekloud.thor.models.SlickTables
import org.seekloud.thor.protocol.ReplayProtocol.{EssfMapJoinLeftInfo, EssfMapKey}
import org.seekloud.thor.shared.ptcl.protocol.ThorGame
import org.seekloud.thor.shared.ptcl.protocol.ThorGame.{GameInformation, ThorSnapshot, UserEnterRoom, UserLeftRoom}
import org.seekloud.utils.ESSFSupport.userMapEncode
import org.seekloud.thor.models.DAO.recordDao
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}


/**
  * User: TangYaruo
  * Date: 2018/11/25
  * Time: 16:51
  */
object GameRecorder {

  private final val log = LoggerFactory.getLogger(this.getClass)
  private final val maxRecordNum = 100

  import org.seekloud.byteobject.ByteObject._
  import org.seekloud.utils.ESSFSupport.initFileRecorder

  sealed trait Command

  final case class GameRecord(event: (List[ThorGame.WsMsgServer], Option[ThorGame.GameSnapshot])) extends Command

  final case class SaveData(stop: Int) extends Command

  final case class SaveEmpty(stop: Int, fileName: String) extends Command

  final case object Save extends Command

  final case object RoomClose extends Command

  final case object StopRecord extends Command

  private final case object BehaviorChangeKey

  private final case object SaveDataKey

  private final val saveTime = AppSettings.gameRecordTime.minute


  final case class SwitchBehavior(
    name: String,
    behavior: Behavior[Command],
    durationOpt: Option[FiniteDuration] = None,
    timeOut: TimeOut = TimeOut("busy time error")
  ) extends Command

  case class TimeOut(msg: String) extends Command

  final case class GameRecorderData(
    roomId: Long,
    fileName: String,
    fileIndex: Int,
    gameInformation: GameInformation,
    initStateOpt: Option[ThorGame.GameSnapshot],
    recorder: FrameOutputStream,
    var gameRecordBuffer: List[GameRecord]
  )

  private[this] def switchBehavior(ctx: ActorContext[Command],
    behaviorName: String, behavior: Behavior[Command], durationOpt: Option[FiniteDuration] = None, timeOut: TimeOut = TimeOut("busy time error"))
    (implicit stashBuffer: StashBuffer[Command],
      timer: TimerScheduler[Command]) = {
    timer.cancel(BehaviorChangeKey)
    durationOpt.foreach(timer.startSingleTimer(BehaviorChangeKey, timeOut, _))
    stashBuffer.unstashAll(ctx, behavior)
  }

  def create(fileName: String, gameInformation: GameInformation, initStateOpt: Option[ThorGame.GameSnapshot] = None, roomId: Long): Behavior[Command] = {
    Behaviors.setup { ctx =>
      log.info(s"${ctx.self.path} is starting..")
      implicit val stashBuffer: StashBuffer[Command] = StashBuffer[Command](Int.MaxValue)
      implicit val middleBuffer: MiddleBufferInJvm = new MiddleBufferInJvm(10 * 4096)
      Behaviors.withTimers[Command] { implicit timer =>
        val fileRecorder = initFileRecorder(fileName, 0, gameInformation, initStateOpt)
        val gameRecordBuffer: List[GameRecord] = List[GameRecord]()
        val data = GameRecorderData(roomId, fileName, 0, gameInformation, initStateOpt, fileRecorder, gameRecordBuffer)
        timer.startSingleTimer(SaveDataKey, Save, saveTime)
        val startFrame = initStateOpt.map(_.asInstanceOf[ThorSnapshot].state.f).getOrElse(0L)
        switchBehavior(ctx, "work", work(data, mutable.HashMap.empty[EssfMapKey, EssfMapJoinLeftInfo], mutable.HashMap.empty[String, String], mutable.HashMap.empty[String, String], startFrame, -1L))
      }
    }
  }

  private def work(
    gameRecordData: GameRecorderData,
    essfMap: mutable.HashMap[EssfMapKey, EssfMapJoinLeftInfo],
    userAllMap: mutable.HashMap[String, String],
    userMap: mutable.HashMap[String, String],
    startF: Long,
    endF: Long
  )(
    implicit stashBuffer: StashBuffer[Command],
    timer: TimerScheduler[Command],
    middleBuffer: MiddleBufferInJvm
  ): Behavior[Command] = {
    import gameRecordData._
    Behaviors.receive[Command] { (ctx, msg) =>
      msg match {
        case msg: GameRecord =>
          val wsMsg = msg.event._1
          wsMsg.foreach {
            case UserEnterRoom(playerId, name, adventurerState, frame) =>
              userAllMap.put(playerId, name)
              userMap.put(playerId, name)
              essfMap.put(EssfMapKey(playerId, name), EssfMapJoinLeftInfo(frame, -1L))
            case UserLeftRoom(playerId, name, frame) =>
              userMap.remove(playerId)
              if (essfMap.get(EssfMapKey(playerId, name)).isDefined) {
                val startF = essfMap(EssfMapKey(playerId, name)).joinF
                essfMap.put(EssfMapKey(playerId, name), EssfMapJoinLeftInfo(startF, frame))
              }
            case _ =>
          }
          gameRecordBuffer = msg :: gameRecordBuffer
          val newEndF = msg.event._2.get match {
            case thor: ThorSnapshot =>
              thor.state.f
          }

          if (gameRecordBuffer.size > maxRecordNum) {
            val buffer = gameRecordBuffer.reverse
            buffer.headOption.foreach { e =>
              recorder.writeFrame(e.event._1.fillMiddleBuffer(middleBuffer).result(), e.event._2.map(_.fillMiddleBuffer(middleBuffer).result()))
              buffer.tail.foreach { e =>
                if (e.event._1.nonEmpty) {
                  recorder.writeFrame(e.event._1.fillMiddleBuffer(middleBuffer).result())
                } else {
                  recorder.writeEmptyFrame()
                }
              }
            }
            gameRecordBuffer = List[GameRecord]()
            switchBehavior(ctx, "work", work(gameRecordData, essfMap, userAllMap, userMap, startF, newEndF))
          } else {
            switchBehavior(ctx, "work", work(gameRecordData, essfMap, userAllMap, userMap, startF, newEndF))
          }

        case Save =>
          log.info(s"${ctx.self.path} work get msg save")
          timer.startSingleTimer(SaveDataKey, Save, saveTime)
          val file = AppSettings.gameDataDirectoryPath + fileName + s"_$fileIndex"
          if (userAllMap.nonEmpty) {
            ctx.self ! SaveData(0)
          } else {
            ctx.self ! SaveEmpty(0, file)
          }
          switchBehavior(ctx, "save", save(gameRecordData, essfMap, userAllMap, userMap, startF, endF))

        case RoomClose =>
          log.info(s"${ctx.self.path} work get msg save, room close")
          val file = AppSettings.gameDataDirectoryPath + fileName + s"_$fileIndex"
          if (userAllMap.nonEmpty) {
            ctx.self ! SaveData(1)
          } else {
            ctx.self ! SaveEmpty(1, file)
          }
          switchBehavior(ctx, "save", save(gameRecordData, essfMap, userAllMap, userMap, startF, endF))

        case unknown =>
          log.warn(s"unknown msg:$unknown")
          Behaviors.unhandled

      }
    }.receiveSignal {
      case (ctx, PostStop) =>
        timer.cancel(SaveDataKey)
        log.info(s"${ctx.self.path} stopping....")

        val gameRecorderBuffer = gameRecordData.gameRecordBuffer
        //保存剩余gameRecorderBuffer中数据
        val buffer = gameRecorderBuffer.reverse
        buffer.headOption.foreach { e =>
          recorder.writeFrame(e.event._1.fillMiddleBuffer(middleBuffer).result(), e.event._2.map(_.fillMiddleBuffer(middleBuffer).result()))
          buffer.tail.foreach { e =>
            if (e.event._1.nonEmpty) {
              recorder.writeFrame(e.event._1.fillMiddleBuffer(middleBuffer).result())
            } else {
              recorder.writeEmptyFrame()
            }
          }
        }

        val mapInfo = essfMap.map { essf =>
          if (essf._2.leftF == -1L) {
            (essf._1, EssfMapJoinLeftInfo(essf._2.joinF, endF))
          } else essf
        }

        recorder.putMutableInfo(AppSettings.essfMapKeyName, userMapEncode(mapInfo))
        recorder.finish()
        val endTime = System.currentTimeMillis()
        val filePath = AppSettings.gameDataDirectoryPath + fileName + s"_$fileIndex"
        val recordInfo = SlickTables.rGameRecord(-1L, gameRecordData.roomId, gameRecordData.gameInformation.gameStartTime, endTime, filePath)
        val recordId = Await.result(recordDao.insertGameRecord(recordInfo), 1.minute)
        val list = ListBuffer[SlickTables.rUserRecordMap]()
        userAllMap.foreach {
          userRecord =>
            list.append(SlickTables.rUserRecordMap(userRecord._1, recordId, roomId, userRecord._2))

        }
        Await.result(recordDao.insertUserRecordList(list.toList), 2.minute)
        Behaviors.stopped
    }
  }

  private def save(
    gameRecordData: GameRecorderData,
    essfMap: mutable.HashMap[EssfMapKey, EssfMapJoinLeftInfo],
    userAllMap: mutable.HashMap[String, String],
    userMap: mutable.HashMap[String, String],
    startF: Long,
    endF: Long
  )(
    implicit stashBuffer: StashBuffer[Command],
    timer: TimerScheduler[Command],
    middleBuffer: MiddleBufferInJvm
  ): Behavior[Command] = {
    import gameRecordData._
    Behaviors.receive[Command] { (ctx, msg) =>
      msg match {
        case msg: SaveData =>
          log.info(s"${ctx.self.path} save get msg SaveData")
          val gameRecorderBuffer = gameRecordData.gameRecordBuffer
          //保存剩余gameRecorderBuffer中数据
          val buffer = gameRecorderBuffer.reverse
          buffer.headOption.foreach { e =>
            recorder.writeFrame(e.event._1.fillMiddleBuffer(middleBuffer).result(), e.event._2.map(_.fillMiddleBuffer(middleBuffer).result()))
            buffer.tail.foreach { e =>
              if (e.event._1.nonEmpty) {
                recorder.writeFrame(e.event._1.fillMiddleBuffer(middleBuffer).result())
              } else {
                recorder.writeEmptyFrame()
              }
            }
          }

          val mapInfo = essfMap.map {
            essf =>
              if (essf._2.leftF == -1L) {
                (essf._1, EssfMapJoinLeftInfo(essf._2.joinF, endF))
              } else {
                essf
              }
          }
          recorder.putMutableInfo(AppSettings.essfMapKeyName, userMapEncode(mapInfo))
          recorder.finish()

          log.info(s"${ctx.self.path} has save game data to file=${fileName}_$fileIndex")
          val endTime = System.currentTimeMillis()
          val filePath = AppSettings.gameDataDirectoryPath + fileName + s"_$fileIndex"
          val recordInfo = SlickTables.rGameRecord(-1L, gameRecordData.roomId, gameRecordData.gameInformation.gameStartTime, endTime, filePath)
          recordDao.insertGameRecord(recordInfo).onComplete {
            case Success(recordId) =>
              val list = ListBuffer[SlickTables.rUserRecordMap]()
              userAllMap.foreach {
                userRecord =>
                  list.append(SlickTables.rUserRecordMap(userRecord._1, recordId, roomId, userRecord._2))
              }
              recordDao.insertUserRecordList(list.toList).onComplete {
                case Success(_) =>
                  log.info(s"insert user record success")
                  ctx.self ! SwitchBehavior("initRecorder", initRecorder(roomId, gameRecordData.fileName, fileIndex, gameInformation, userMap))
                  if (msg.stop == 1) ctx.self ! StopRecord
                case Failure(e) =>
                  log.error(s"insert user record fail, error: $e")
                  ctx.self ! SwitchBehavior("initRecorder", initRecorder(roomId, gameRecordData.fileName, fileIndex, gameInformation, userMap))
                  if (msg.stop == 1) ctx.self ! StopRecord
              }
            case Failure(e) =>
              log.error(s"insert game record fail, error: $e")
              ctx.self ! SwitchBehavior("initRecorder", initRecorder(roomId, gameRecordData.fileName, fileIndex, gameInformation, userMap))
              if (msg.stop == 1) ctx.self ! StopRecord
          }

          switchBehavior(ctx, "busy", busy())


        case msg: SaveEmpty =>
          log.info(s"${ctx.self.path} save get msg SaveEmpty")
          val mapInfo = essfMap.map {
            essf =>
              if (essf._2.leftF == -1L) {
                (essf._1, EssfMapJoinLeftInfo(essf._2.joinF, endF))
              } else {
                essf
              }
          }
          recorder.putMutableInfo(AppSettings.essfMapKeyName, userMapEncode(mapInfo))
          recorder.finish()
          val deleteFile = new File(msg.fileName)
          if (deleteFile.isFile && deleteFile.exists()) {
            deleteFile.delete()
          } else {
            log.error(s"delete file error, file is ${msg.fileName}")
          }
          if (msg.stop == 1) ctx.self ! StopRecord
          initRecorder(roomId, gameRecordData.fileName, fileIndex, gameInformation, userMap)


        case unknown =>
          log.warn(s"unknown msg:$unknown")
          Behaviors.unhandled
      }

    }
  }

  private def initRecorder(
    roomId: Long,
    fileName: String,
    fileIndex: Int,
    gameInformation: GameInformation,
    userMap: mutable.HashMap[String, String]
  )(
    implicit stashBuffer: StashBuffer[Command],
    timer: TimerScheduler[Command],
    middleBuffer: MiddleBufferInJvm
  ): Behavior[Command] = {
    Behaviors.receive[Command] { (ctx, msg) =>
      msg match {

        case msg: GameRecord =>
          log.info(s"${ctx.self.path} init get msg gameRecord")
          val startF = msg.event._2.get match {
            case tank: ThorSnapshot =>
              tank.state.f
          }
          val startTime = System.currentTimeMillis()
          val newInitStateOpt = msg.event._2
          val newRecorder = initFileRecorder(fileName, fileIndex + 1, gameInformation, newInitStateOpt)
          val newGameInformation = GameInformation(startTime, gameInformation.thorGameConfig)
          val newGameRecorderData = GameRecorderData(roomId, fileName, fileIndex + 1, newGameInformation, newInitStateOpt, newRecorder, gameRecordBuffer = List[GameRecord]())
          val newEssfMap = mutable.HashMap.empty[EssfMapKey, EssfMapJoinLeftInfo]
          val newUserAllMap = mutable.HashMap.empty[String, String]
          userMap.foreach {
            user =>
              newEssfMap.put(EssfMapKey(user._1, user._2), EssfMapJoinLeftInfo(startF, -1L))
              newUserAllMap.put(user._1, user._2)
          }
          switchBehavior(ctx, "work", work(newGameRecorderData, newEssfMap, newUserAllMap, userMap, startF, -1L))

        case StopRecord =>
          log.info(s"${ctx.self.path} room close, stop record ")
          Behaviors.stopped

        case unknown =>
          log.warn(s"unknown msg:$unknown")
          Behaviors.unhandled
      }
    }
  }


  private def busy()(
    implicit stashBuffer: StashBuffer[Command],
    timer: TimerScheduler[Command]
  ): Behavior[Command] =
    Behaviors.receive[Command] { (ctx, msg) =>
      msg match {
        case SwitchBehavior(name, behavior, durationOpt, timeOut) =>
          switchBehavior(ctx, name, behavior, durationOpt, timeOut)

        case TimeOut(m) =>
          log.debug(s"${ctx.self.path} is time out when busy,msg=${m}")
          Behaviors.stopped

        case unknownMsg =>
          stashBuffer.stash(unknownMsg)
          Behavior.same
      }
    }

}
