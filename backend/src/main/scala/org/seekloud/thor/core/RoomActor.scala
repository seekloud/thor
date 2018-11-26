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
            implicit val sendBuffer = new MiddleBufferInJvm(81920)
            val thorSchema = ThorSchemaServerImpl(AppSettings.thorGameConfig, ctx.self, timer, log, dispatch(subscribersMap), dispatchTo(subscribersMap))
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
          case JoinRoom(roomId, userId, name, userActor) =>
            println(s"user $userId join room $roomId")
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
            dispatch(subscribersMap)(UserLeftRoom(userId, name))

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
            //            println("game loop")
            //thorSchema定时更新
            thorSchema.update()

            val thorSchemaData = thorSchema.getThorSchemaState()
            if (tickCount % 40 == 5) {
              //生成食物+同步全量adventurer数据+新生成的食物
              val newFood = thorSchema.genFood(25)

              val data = if(tickCount % 120 == 5) thorSchema.getThorSchemaState()
              else thorSchema.getThorSchemaState().copy(food = newFood, isIncrement = true)

              dispatch(subscribersMap)(GridSyncState(data))
            }
            if (tickCount % 20 == 1) {
              //排行榜
              dispatch(subscribersMap)(Ranks(thorSchema.currentRankList,thorSchema.historyRank))
            }
            newPlayer.foreach {
              //为新用户分发全量数据
              player =>
                subscribersMap.put(player._1, player._2)
                dispatchTo(subscribersMap)(player._1, GridSyncState(thorSchemaData))
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
  def dispatch(subscribers: mutable.HashMap[String, ActorRef[UserActor.Command]])(msg: WsMsgServer)(implicit sendBuffer: MiddleBufferInJvm) = {
    //    println(subscribers)
    //    subscribers.values.foreach( _ ! UserActor.DispatchMsg(msg))
    val isKillMsg = msg.isInstanceOf[BeAttacked]
    subscribers.values.foreach(_ ! UserActor.DispatchMsg(Wrap(msg.asInstanceOf[WsMsgServer].fillMiddleBuffer(sendBuffer).result(), isKillMsg)))
  }

  //向特定用户发数据
  def dispatchTo(subscribers: mutable.HashMap[String, ActorRef[UserActor.Command]])(id: String, msg: WsMsgServer)(implicit sendBuffer: MiddleBufferInJvm) = {

    val isKillMsg = msg.isInstanceOf[BeAttacked]
    subscribers.get(id).foreach(_ ! UserActor.DispatchMsg(Wrap(msg.asInstanceOf[WsMsgServer].fillMiddleBuffer(sendBuffer).result(), isKillMsg)))
  }

}
