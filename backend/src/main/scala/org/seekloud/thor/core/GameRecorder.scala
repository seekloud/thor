package org.seekloud.thor.core

import akka.actor.typed.{Behavior, PostStop}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.{ActorContext, StashBuffer, TimerScheduler}
import org.seekloud.byteobject.MiddleBufferInJvm
import org.seekloud.essf.io.FrameOutputStream
import org.seekloud.thor.common.AppSettings
import org.seekloud.thor.protocol.ReplayProtocol.{EssfMapJoinLeftInfo, EssfMapKey}
import org.seekloud.thor.shared.ptcl.protocol.ThorGame
import org.seekloud.thor.shared.ptcl.protocol.ThorGame.{GameInformation, ThorSnapshot}
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.concurrent.duration._


/**
  * User: TangYaruo
  * Date: 2018/11/25
  * Time: 16:51
  */
object GameRecorder {

  private final val log = LoggerFactory.getLogger(this.getClass)


  import org.seekloud.byteobject.ByteObject._
  import org.seekloud.utils.ESSFSupport.initFileRecorder

  sealed trait Command

  final case class GameRecord(event: (List[ThorGame.WsMsgServer], Option[ThorGame.GameSnapshot])) extends Command

  final case class SaveDate(stop: Int) extends Command

  final case class SaveEmpty(stop: Int, fileName: String) extends Command

  final case object Save extends Command

  final case object RoomClose extends Command

  final case object StopRecord extends Command

  private final case object BehaviorChangeKey

  private final case object SaveDateKey

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
    //log.debug(s"${ctx.self.path} becomes $behaviorName behavior.")
    timer.cancel(BehaviorChangeKey)
    durationOpt.foreach(timer.startSingleTimer(BehaviorChangeKey, timeOut, _))
    stashBuffer.unstashAll(ctx, behavior)
  }

  def create(fileName: String, gameInformation: GameInformation, initStateOpt: Option[ThorGame.GameSnapshot] = None, roomId: Long): Behavior[Command] = {
    Behaviors.setup { ctx =>
      log.info(s"${ctx.self.path} is starting..")
      implicit val stashBuffer = StashBuffer[Command](Int.MaxValue)
      implicit val middleBuffer = new MiddleBufferInJvm(10 * 4096)
      Behaviors.withTimers[Command] { implicit timer =>
        val fileRecorder = initFileRecorder(fileName, 0, gameInformation, initStateOpt)
        val gameRecordBuffer: List[GameRecord] = List[GameRecord]()
        val data = GameRecorderData(roomId, fileName, 0, gameInformation, initStateOpt, fileRecorder, gameRecordBuffer)
        timer.startSingleTimer(SaveDateKey, Save, saveTime)
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
          //TODO
        case unknown =>
          log.warn(s"unknown msg:$unknown")
          Behaviors.unhandled

      }
    }.receiveSignal {
      case (ctx, PostStop) =>
        timer.cancel(SaveDateKey)
        log.info(s"${ctx.self.path} stopping....")
        //TODO
        Behaviors.stopped
    }
  }

  private def save(
    gameRecordData: GameRecorderData,
    essfMap: mutable.HashMap[EssfMapKey,EssfMapJoinLeftInfo],
    userAllMap: mutable.HashMap[String,(Int,String)],
    userMap: mutable.HashMap[String,(Int,String)],
    startF: Long,
    endF: Long
  )(
    implicit stashBuffer:StashBuffer[Command],
    timer:TimerScheduler[Command],
    middleBuffer: MiddleBufferInJvm
  ): Behavior[Command] = {
    import gameRecordData._
    Behaviors.receive[Command] { (ctx, msg) =>
      msg match {
          //TODO
        case unknown =>
          log.warn(s"unknown msg:$unknown")
          Behaviors.unhandled
      }

    }
  }

  private def initRecorder(
    roomId: Long,
    fileName: String,
    fileIndex:Int,
    gameInformation: GameInformation,
    userMap: mutable.HashMap[String,(Int,String)]
  )(
    implicit stashBuffer:StashBuffer[Command],
    timer:TimerScheduler[Command],
    middleBuffer: MiddleBufferInJvm
  ): Behavior[Command] = {
    Behaviors.receive[Command] { (ctx, msg) =>
      msg match {
          //TODO
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
