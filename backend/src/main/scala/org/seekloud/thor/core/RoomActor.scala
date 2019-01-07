package org.seekloud.thor.core

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer, TimerScheduler}
import org.seekloud.thor.common.AppSettings
import org.seekloud.thor.core.game.ThorSchemaServerImpl
import org.seekloud.thor.shared.ptcl._
import org.seekloud.thor.shared.ptcl.model._
import org.seekloud.thor.shared.ptcl.protocol.ThorGame._
import org.seekloud.byteobject.MiddleBufferInJvm
import org.slf4j.LoggerFactory

import concurrent.duration._
import scala.collection.mutable
import scala.concurrent.duration.FiniteDuration
import scala.language.implicitConversions
import org.seekloud.byteobject.ByteObject._
import org.seekloud.thor.shared.ptcl.protocol.ThorGame

/**
  * @author Jingyi
  * @version 创建时间：2018/11/9
  */
object RoomActor {

  private val log = LoggerFactory.getLogger(this.getClass)

  sealed trait Command

  final case class ChildDead[U](name: String, childRef: ActorRef[U]) extends Command

  case class JoinRoom(roomId: Long, playerId: String, name: String, userActor: ActorRef[UserActor.Command]) extends Command

  case class JoinRoom4Watch(playerId: String,roomId: Long, watchedPlayerId: String, userActor4Watch: ActorRef[UserActor.Command]) extends Command with  RoomManager.Command

  case class LeftRoom(playerId: String, name: String, userList: List[(String, String)]) extends Command

  case class BeDead(playerId: String, name: String, userList: List[(String, String)]) extends Command

  case class CreateRobot(botId: String, name: String) extends Command

  case class ReliveRobot(botId: String, name: String, botActor: ActorRef[RobotActor.Command]) extends Command

  case class LeftRoom4Watch(playerId:String, watchedPlayerId:String) extends Command with RoomManager.Command

  //  case class GetKilled(playerId: String, name: String) extends Command with RoomManager.Command
  case class WsMessage(playerId: String, msg: UserActionEvent) extends Command

  case object GameLoop extends Command


  case class TimeOut(msg: String) extends Command

  private final case object GameLoopKey

  def create(roomId: Long): Behavior[Command] = {
    log.debug(s"RoomActor-$roomId starting...")
    Behaviors.setup[Command] {
      ctx =>
        Behaviors.withTimers[Command] {
          implicit timer =>
            val subscribersMap = mutable.HashMap[String, ActorRef[UserActor.Command]]()
            val watchingMap = mutable.HashMap[String, ActorRef[UserActor.Command]]()
            //为新房间创建thorSchema
            implicit val sendBuffer: MiddleBufferInJvm = new MiddleBufferInJvm(81920)
            val thorSchema = ThorSchemaServerImpl(AppSettings.thorGameConfig, ctx.self, timer, log, dispatch(subscribersMap, watchingMap), dispatchTo(subscribersMap, watchingMap))

            ctx.self ! CreateRobot("robot1", "万事通")
            ctx.self ! CreateRobot("robot2", "害羞鬼")
            ctx.self ! CreateRobot("robot3", "瞌睡虫")
            ctx.self ! CreateRobot("robot4", "开心果")
            ctx.self ! CreateRobot("robot5", "迷糊蛋")
            ctx.self ! CreateRobot("robot6", "搞事精")
            ctx.self ! CreateRobot("robot7", "爱生气")

            if (AppSettings.gameRecordIsWork) {
              getGameRecorder(ctx, thorSchema, roomId, thorSchema.systemFrame)
            }
            timer.startPeriodicTimer(GameLoopKey, GameLoop, AppSettings.thorGameConfig.frameDuration.millis)
            idle(roomId, Nil, subscribersMap, watchingMap, thorSchema, 0L)
        }
    }
  }

  def idle(
    roomId: Long,
    newPlayer: List[(String, ActorRef[UserActor.Command])],
    subscribersMap: mutable.HashMap[String, ActorRef[UserActor.Command]],
    watchingMap: mutable.HashMap[String, ActorRef[UserActor.Command]],
    thorSchema: ThorSchemaServerImpl,
    tickCount: Long
  )(
    implicit timer: TimerScheduler[Command],
    sendBuffer: MiddleBufferInJvm
  ): Behavior[Command] = {
    Behaviors.receive {
      (ctx, msg) =>
        msg match {
          case CreateRobot(botId, name) =>
            val robot = ctx.spawn(RobotActor.init(ctx.self, thorSchema, botId, name), botId)
            thorSchema.robotJoinGame(botId, name, robot)
            Behaviors.same

          case ReliveRobot(botId, name, botActor) =>
            thorSchema.robotJoinGame(botId, name, botActor)
            Behaviors.same

          case JoinRoom(roomId, userId, name, userActor) =>
            log.debug(s"user $userId join room $roomId")
            thorSchema.joinGame(userId, name, userActor)
            idle(roomId, (userId, userActor) :: newPlayer, subscribersMap, watchingMap, thorSchema, tickCount)

          case JoinRoom4Watch(uid, _, playerId, userActor4Watch) =>
            log.debug(s"${ctx.self.path} recv a msg=${msg}")
            watchingMap.put(uid,userActor4Watch)
            thorSchema.handleJoinRoom4Watch(userActor4Watch,uid,playerId)
            Behaviors.same

          case LeftRoom(userId, name, userList) =>
            log.debug(s"roomactor - ${userId} left room")
            thorSchema.leftGame(userId, name)
            subscribersMap.remove(userId)
            dispatch(subscribersMap, watchingMap)(UserLeftRoom(userId, name))

            if (userList.isEmpty && roomId > 1l) Behavior.stopped //有多个房间且该房间空了，停掉这个actor
            else idle(roomId, newPlayer.filter(_._1 != userId), subscribersMap, watchingMap, thorSchema, tickCount)

          case BeDead(userId, name, userList) =>
            log.debug(s"roomactor - ${userId} die")
            thorSchema.leftGame(userId, name)
//            subscribersMap.remove(userId)
//            dispatch(subscribersMap, watchingMap)(UserLeftRoom(userId, name))

            if (userList.isEmpty && roomId > 1l) Behavior.stopped //有多个房间且该房间空了，停掉这个actor
            else idle(roomId, newPlayer.filter(_._1 != userId), subscribersMap, watchingMap, thorSchema, tickCount)

          case LeftRoom4Watch(uid,playerId) =>
            thorSchema.leftRoom4Watch(uid,playerId)
            watchingMap.remove(uid)
            Behaviors.same

          case WsMessage(userId, msg) =>
            thorSchema.receiveUserAction(msg)
            Behavior.same

          case GameLoop =>
            val startTime = System.currentTimeMillis()
            val snapShotOpt = thorSchema.getCurSnapshot

            //thorSchema定时更新
            thorSchema.update()

            val gameEvents = thorSchema.getLastGameEvent
            if (AppSettings.gameRecordIsWork) {
              if (tickCount % 20 == 1) {
                //排行榜
                val rankEvent = Ranks(thorSchema.currentRankList,thorSchema.historyRank)
                getGameRecorder(ctx, thorSchema, roomId, thorSchema.systemFrame) ! GameRecorder.GameRecord(rankEvent :: gameEvents, snapShotOpt)
              } else {
                getGameRecorder(ctx, thorSchema, roomId, thorSchema.systemFrame) ! GameRecorder.GameRecord(gameEvents, snapShotOpt)
              }
            }

            if (tickCount % 40 == 5) {
              //生成食物+同步全量adventurer数据+新生成的食物
              val newFood = thorSchema.genFood(25)

              val data = if(tickCount % 120 == 5) thorSchema.getThorSchemaState()
              else thorSchema.getThorSchemaState().copy(food = newFood, isIncrement = true)

              dispatch(subscribersMap, watchingMap)(GridSyncState(data))
            }
            if (tickCount % 20 == 1) {
              //排行榜
              dispatch(subscribersMap, watchingMap)(Ranks(thorSchema.currentRankList,thorSchema.historyRank))
            }
            newPlayer.foreach {
              //为新用户分发全量数据
              player =>
                val thorSchemaData = thorSchema.getThorSchemaState()
                val actor = thorSchema.getUserActor4WatchGameList(player._1)
                subscribersMap.put(player._1, player._2)
                dispatchTo(subscribersMap, watchingMap)(player._1, GridSyncState(thorSchemaData), actor)
            }
            idle(roomId, Nil, subscribersMap, watchingMap, thorSchema, tickCount + 1)

          case ChildDead(_, childRef) =>
            ctx.unwatch(childRef)
            Behaviors.same

          case _ =>
            log.warn(s"${ctx.self.path} recv a unknow msg=${msg}")
            Behaviors.same
        }
    }
  }


  //向所有用户发数据
  def dispatch(subscribers: mutable.HashMap[String, ActorRef[UserActor.Command]], observers: mutable.HashMap[String, ActorRef[UserActor.Command]])(msg: WsMsgServer)(implicit sendBuffer: MiddleBufferInJvm) = {
//    log.debug(s"watching map: $observers")
    val isKillMsg = msg.isInstanceOf[BeAttacked]
    val deadId = if(isKillMsg) msg.asInstanceOf[BeAttacked].playerId else ""
    subscribers.values.foreach(_ ! UserActor.DispatchMsg(Wrap(msg.asInstanceOf[WsMsgServer].fillMiddleBuffer(sendBuffer).result(), isKillMsg, deadId)))
    observers.values.foreach(_ ! UserActor.DispatchMsg(Wrap(msg.asInstanceOf[WsMsgServer].fillMiddleBuffer(sendBuffer).result(), isKillMsg, deadId)))
  }

  //向特定用户发数据
  def dispatchTo(subscribers: mutable.HashMap[String, ActorRef[UserActor.Command]], observers: mutable.HashMap[String, ActorRef[UserActor.Command]])(id: String, msg: WsMsgServer,observersByUserId:Option[mutable.HashMap[String,ActorRef[UserActor.Command]]])(implicit sendBuffer: MiddleBufferInJvm) = {

    val isKillMsg = msg.isInstanceOf[BeAttacked]
    val deadId = if(isKillMsg) msg.asInstanceOf[BeAttacked].playerId else ""
    subscribers.get(id).foreach(_ ! UserActor.DispatchMsg(Wrap(msg.asInstanceOf[WsMsgServer].fillMiddleBuffer(sendBuffer).result(), isKillMsg, deadId)))
    observersByUserId match{
      case Some(ls) => ls.keys.foreach(playerId => observers.get(playerId).foreach(t => t ! UserActor.DispatchMsg(Wrap(msg.asInstanceOf[WsMsgServer].fillMiddleBuffer(sendBuffer).result(),isKillMsg, deadId))))
      case None =>
    }
  }

  private def getGameRecorder(ctx: ActorContext[Command], thorSchema: ThorSchemaServerImpl, roomId: Long, frame: Long): ActorRef[GameRecorder.Command] = {
    val childName = s"gameRecorder-$roomId"
    ctx.child(childName).getOrElse {
      val curTime = System.currentTimeMillis()
      val fileName = s"thorGame_$curTime"
      val gameInformation = ThorGame.GameInformation(curTime, AppSettings.thorGameConfig.getThorGameConfigImpl())
      val initStateOpt = Some(thorSchema.getCurGameSnapshot)
      val actor = ctx.spawn(GameRecorder.create(fileName, gameInformation, initStateOpt, roomId), childName)
      ctx.watchWith(actor, ChildDead(childName, actor))
      actor
    }.upcast[GameRecorder.Command]
  }

}
