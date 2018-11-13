package com.neo.sk.thor.core

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer, TimerScheduler}
import com.neo.sk.thor.core.thor.GridServer
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

  case class JoinRoom(roomId: Long, userId: Long, name: String,userActor: ActorRef[UserActor.Command]) extends Command
  case class LeftRoom(roomId: Long, userId: Long, name: String, userList: List[(Long, String)]) extends Command
  case class GetKilled(userId: Long, name: String) extends Command with RoomManager.Command
  case class WsMessage(userId: Long, msg: UserActionEvent) extends Command
  case object GameLoop extends Command

  final case class SwitchBehavior(
                                   name: String,
                                   behavior: Behavior[Command],
                                   durationOpt: Option[FiniteDuration] = None,
                                   timeOut: TimeOut = TimeOut("busy time error")
                                 ) extends Command

  case class TimeOut(msg:String) extends Command

  private final case object BehaviorChangeKey
  private[this] def switchBehavior(ctx: ActorContext[Command],
                                   behaviorName: String, behavior: Behavior[Command], durationOpt: Option[FiniteDuration] = None,timeOut: TimeOut  = TimeOut("busy time error"))
                                  (implicit stashBuffer: StashBuffer[Command],
                                   timer:TimerScheduler[Command]) = {
    log.debug(s"${ctx.self.path} becomes $behaviorName behavior.")
    timer.cancel(BehaviorChangeKey)
    durationOpt.foreach(timer.startSingleTimer(BehaviorChangeKey,timeOut,_))
    stashBuffer.unstashAll(ctx,behavior)
  }

  private final case object GameLoopKey
  def create(roomId: Long): Behavior[Command] = {
    log.debug(s"RoomActor-$roomId starting...")
    Behaviors.setup[Command]{
      ctx =>
        Behaviors.withTimers[Command]{
          implicit timer =>
            val subscribersMap = mutable.HashMap[Long, ActorRef[UserActor.Command]]()
            val grid = new GridServer(ctx, log, dispatch(subscribersMap), dispatchTo(subscribersMap),Boundary.getBoundary)
            timer.startPeriodicTimer(GameLoopKey,GameLoop,Frame.millsAServerFrame.millis)
            idle(roomId, Nil, subscribersMap, grid, 0L)
        }
    }
  }

  def idle(
          roomId: Long,
          newPlayer: List[(Long, ActorRef[UserActor.Command])],
          subscribersMap: mutable.HashMap[Long, ActorRef[UserActor.Command]],
          grid: GridServer,
          tickCount: Long
          )(
            implicit timer: TimerScheduler[Command]
          ): Behavior[Command] = {
    Behaviors.receive{
      (ctx, msg) =>
        msg match {
          case JoinRoom(roomId, userId, name, userActor) =>
            //TODO grid处理用户加入
            idle(roomId, (userId, userActor) :: newPlayer, subscribersMap, grid, tickCount)

          case LeftRoom(roomId, userId, name, userList) =>
            //TODO grid处理用户离开
            subscribersMap.remove(userId)
            dispatch(subscribersMap)(UserLeftRoom(userId, name))

            if(userList.isEmpty && roomId > 1l) Behavior.stopped
            else idle(roomId, newPlayer.filter(_._1 != userId), subscribersMap, grid, tickCount)

          case GetKilled(userId, name) =>
            subscribersMap.remove(userId)
            idle(roomId, newPlayer.filter(_._1 != userId), subscribersMap, grid, tickCount)

          case WsMessage(userId, msg) =>
            //TODO grid处理用户操作
            msg match {
              case a: MouseMove =>
                dispatch(subscribersMap)(MouseMoveServer(a.userId, math.max(a.frame, grid.systemFrame), a))
              case a: MouseClick =>
                dispatch(subscribersMap)(MouseClickServer(a.userId, math.max(a.frame, grid.systemFrame), a))
              case _ => //do nothing
            }
            Behavior.same

          case GameLoop =>
            grid.update()

            val gridData = grid.getGridState()
            if (tickCount % 20 == 5) {
              dispatch(subscribersMap)(GridSyncState(gridData))
            }
            if(tickCount % 20 == 1){
//              dispatch(subscribersMap)(Ranks(grid.currentRank,grid.historyRank))
            }
            newPlayer.foreach{
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



  def dispatch(subscribers:mutable.HashMap[Long,ActorRef[UserActor.Command]])(msg: WsMsgServer) = {
    subscribers.values.foreach( _ ! UserActor.DispatchMsg(msg))
  }

  def dispatchTo(subscribers:mutable.HashMap[Long,ActorRef[UserActor.Command]])(id: Long,msg: WsMsgServer) = {
    subscribers.get(id).foreach( _ ! UserActor.DispatchMsg(msg))
  }

}
