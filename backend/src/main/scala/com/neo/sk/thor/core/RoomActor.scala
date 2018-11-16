package com.neo.sk.thor.core

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer, TimerScheduler}
import com.neo.sk.thor.common.AppSettings
import com.neo.sk.thor.core.game.ThorSchemaServerImpl
import com.neo.sk.thor.shared.ptcl._
import com.neo.sk.thor.shared.ptcl.model._
import com.neo.sk.thor.shared.ptcl.protocol.ThorGame._
import org.slf4j.LoggerFactory

import concurrent.duration._
import scala.collection.mutable
import scala.concurrent.duration.FiniteDuration

/**
  * @author Jingyi
  * @version 创建时间：2018/11/9
  */
object RoomActor {

  private val log = LoggerFactory.getLogger(this.getClass)

  sealed trait Command

  final case class ChildDead[U](name:String,childRef:ActorRef[U]) extends Command

  case class JoinRoom(roomId: Long, playerId: String, name: String,userActor: ActorRef[UserActor.Command]) extends Command
  case class LeftRoom(playerId: String, name: String, userList: List[(String, String)]) extends Command
//  case class GetKilled(playerId: String, name: String) extends Command with RoomManager.Command
  case class WsMessage(playerId: String, msg: UserActionEvent) extends Command
  case object GameLoop extends Command


  case class TimeOut(msg:String) extends Command

  private final case object GameLoopKey
  def create(roomId: Long): Behavior[Command] = {
    log.debug(s"RoomActor-$roomId starting...")
    Behaviors.setup[Command]{
      ctx =>
        Behaviors.withTimers[Command]{
          implicit timer =>
            val subscribersMap = mutable.HashMap[String, ActorRef[UserActor.Command]]()
            //为新房间创建grid
            val grid = ThorSchemaServerImpl(AppSettings.thorGameConfig, ctx.self, timer, log, dispatch(subscribersMap), dispatchTo(subscribersMap))
            timer.startPeriodicTimer(GameLoopKey,GameLoop,Frame.millsAServerFrame.millis)
            idle(roomId, Nil, subscribersMap, grid, 0L)
        }
    }
  }

  def idle(
          roomId: Long,
          newPlayer: List[(String, ActorRef[UserActor.Command])],
          subscribersMap: mutable.HashMap[String, ActorRef[UserActor.Command]],
          grid: ThorSchemaServerImpl,
          tickCount: Long
          )(
            implicit timer: TimerScheduler[Command]
          ): Behavior[Command] = {
    Behaviors.receive{
      (ctx, msg) =>
        msg match {
          case JoinRoom(roomId, userId, name, userActor) =>
            grid.joinGame(userId, name, userActor)
            idle(roomId, (userId, userActor) :: newPlayer, subscribersMap, grid, tickCount)

          case LeftRoom(userId, name, userList) =>
            grid.leftGame(userId, name)
            subscribersMap.remove(userId)
            dispatch(subscribersMap)(UserLeftRoom(userId, name))

            if(userList.isEmpty && roomId > 1l) Behavior.stopped //有多个房间且该房间空了，停掉这个actor
            else idle(roomId, newPlayer.filter(_._1 != userId), subscribersMap, grid, tickCount)

          case WsMessage(userId, msg) =>
            grid.receiveUserAction(msg)
            msg match {
              case a: MouseMove =>
                dispatch(subscribersMap)(MouseMove(a.playerId, a.direction, math.max(a.frame, grid.systemFrame), a.serialNum))
              case a: MouseClick =>
                dispatch(subscribersMap)(MouseClick(a.playerId, math.max(a.frame, grid.systemFrame), a.serialNum))
              case _ => //do nothing
            }
            Behavior.same

          case GameLoop =>
            //grid定时更新
            grid.update()

            val gridData = grid.getThorSchemaState()
            if (tickCount % 20 == 5) {
              //生成食物+同步全量数据
              grid.genFood(20)
              dispatch(subscribersMap)(GridSyncState(grid.getThorSchemaState()))
            }
            if(tickCount % 20 == 1){
              //排行榜
//              dispatch(subscribersMap)(Ranks(grid.currentRank,grid.historyRank))
            }
            newPlayer.foreach{
              //为新用户分发全量数据
              player => dispatchTo(subscribersMap)(player._1, GridSyncState(gridData))
            }
            idle(roomId, Nil, subscribersMap, grid, tickCount+1)

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
  def dispatch(subscribers:mutable.HashMap[String,ActorRef[UserActor.Command]])(msg: WsMsgServer) = {
    subscribers.values.foreach( _ ! UserActor.DispatchMsg(msg))
  }

  //向特定用户发数据
  def dispatchTo(subscribers:mutable.HashMap[String,ActorRef[UserActor.Command]])(id: String,msg: WsMsgServer) = {
    subscribers.get(id).foreach( _ ! UserActor.DispatchMsg(msg))
  }

}
