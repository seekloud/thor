package com.neo.sk.thor.core

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer, TimerScheduler}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Flow
import akka.stream.typed.scaladsl.{ActorSink, ActorSource}
import com.neo.sk.thor.shared.ptcl.protocol.ThorGame._
import org.seekloud.byteobject.MiddleBufferInJvm
import org.slf4j.LoggerFactory
import com.neo.sk.thor.Boot.roomManager
import com.neo.sk.thor.core.game.{ThorGameConfigServerImpl, ThorSchemaServerImpl}
import com.neo.sk.thor.core.thor.AdventurerServer
import com.neo.sk.thor.shared.ptcl.config.ThorGameConfigImpl
import org.seekloud.byteobject.ByteObject._

import scala.concurrent.duration._

/**
  * @author Jingyi
  * @version 创建时间：2018/11/9
  */
object UserActor {

  private val log = LoggerFactory.getLogger(this.getClass)
  private final val InitTime = Some(5.minutes)

  sealed trait Command

  case class WsMessage(msg: Option[WsMsgFront]) extends Command
  case object CompleteMsgFront extends Command
  case class FailMsgFront(ex: Throwable) extends Command
  case class DispatchMsg(msg: WsMsgSource) extends Command

  case class StartGame(roomId: Option[Long]) extends Command
  case class JoinRoom(playerId: String, name: String, userActor:ActorRef[UserActor.Command], roomIdOpt:Option[Long] = None) extends Command with RoomManager.Command
  case class LeftRoom[U](actorRef:ActorRef[U]) extends Command
  case class JoinRoomSuccess(adventurer: AdventurerServer, playerId: String, roomActor: ActorRef[RoomActor.Command], config: ThorGameConfigImpl) extends Command

  case class UserFrontActor(actor:ActorRef[WsMsgSource]) extends Command

  private final case object BehaviorChangeKey
  case class TimeOut(msg:String) extends Command

  private[this] def switchBehavior(ctx: ActorContext[Command],
                                   behaviorName: String, behavior: Behavior[Command], durationOpt: Option[FiniteDuration] = None,timeOut: TimeOut  = TimeOut("busy time error"))
                                  (implicit stashBuffer: StashBuffer[Command],
                                   timer:TimerScheduler[Command]) = {
    log.debug(s"${ctx.self.path} becomes $behaviorName behavior.")
    timer.cancel(BehaviorChangeKey)
    durationOpt.foreach(timer.startSingleTimer(BehaviorChangeKey,timeOut,_))
    stashBuffer.unstashAll(ctx,behavior)
  }

  private def sink(actor: ActorRef[Command]) = ActorSink.actorRef[Command](
    ref = actor,
    onCompleteMessage = CompleteMsgFront,
    onFailureMessage = FailMsgFront.apply
  )

  def flow(actor:ActorRef[UserActor.Command]):Flow[WsMessage, WsMsgSource, Any] = {
    val in = Flow[WsMessage].to(sink(actor))
    val out =
      ActorSource.actorRef[WsMsgSource](
        completionMatcher = {
          case CompleteMsgServer =>
        },
        failureMatcher = {
          case FailMsgServer(e)  => e
        },
        bufferSize = 128,
        overflowStrategy = OverflowStrategy.dropHead
      ).mapMaterializedValue(outActor => actor ! UserFrontActor(outActor))
    Flow.fromSinkAndSource(in, out)
  }

  def create(playerId: String, userInfo:UserInfo):Behavior[Command] = {
    Behaviors.setup[Command]{ctx =>
      log.debug(s"${ctx.self.path} is starting...")
      implicit val stashBuffer = StashBuffer[Command](Int.MaxValue)
      Behaviors.withTimers[Command] { implicit timer =>
        implicit val sendBuffer = new MiddleBufferInJvm(8192)
        switchBehavior(ctx,"init",init(playerId, userInfo),InitTime,TimeOut("init"))
      }
    }
  }

  private def init(playerId: String, userInfo: UserInfo)(
    implicit stashBuffer:StashBuffer[Command],
    sendBuffer:MiddleBufferInJvm,
    timer:TimerScheduler[Command]
  ): Behavior[Command] = {
    Behaviors.receive[Command] {
      (ctx, msg) =>
        msg match {
          case UserFrontActor(frontActor) =>
            ctx.watchWith(frontActor,LeftRoom(frontActor))
            switchBehavior(ctx,"idle",idle(playerId, userInfo,System.currentTimeMillis(), frontActor))

          case LeftRoom(actor) =>
            ctx.unwatch(actor)
            Behaviors.stopped

          case TimeOut(m) =>
            log.debug(s"${ctx.self.path} is time out when busy,msg=${m}")
            Behaviors.stopped

          case unknowMsg =>
            stashBuffer.stash(unknowMsg)
            Behavior.same
        }
    }
  }

  private def idle(playerId: String, userInfo: UserInfo,startTime:Long, frontActor:ActorRef[WsMsgSource])(
    implicit stashBuffer:StashBuffer[Command],
    timer:TimerScheduler[Command],
    sendBuffer:MiddleBufferInJvm
  ): Behavior[Command] = {
    Behaviors.receive[Command]{
      (ctx, msg) =>
        msg match {
          case StartGame(roomIdOpt) =>
            roomManager ! JoinRoom(playerId, userInfo.name, ctx.self, roomIdOpt)
            Behaviors.same

          case JoinRoomSuccess(adventurer, playerId, roomActor, config) =>
            val ws = YourInfo(config, playerId, userInfo.name).asInstanceOf[WsMsgServer].fillMiddleBuffer(sendBuffer).result()
            frontActor ! Wrap(ws)
            switchBehavior(ctx,"play",play(playerId, userInfo, adventurer, startTime, frontActor, roomActor))
            Behaviors.same


          case LeftRoom(actor) =>
            ctx.unwatch(actor)
            switchBehavior(ctx,"init",init(playerId, userInfo),InitTime,TimeOut("init"))

          case unknowMsg =>
            Behavior.same
        }
    }
  }

  private def play(
                    playerId: String,
                    userInfo: UserInfo,
                    adventurer: AdventurerServer,
                    startTime: Long,
                    frontActor: ActorRef[WsMsgSource],
                    roomActor: ActorRef[RoomActor.Command])(
                    implicit stashBuffer:StashBuffer[Command],
                    timer:TimerScheduler[Command],
                    sendBuffer:MiddleBufferInJvm
                  ): Behavior[Command] = {
    Behaviors.receive[Command]{
      (ctx, msg) =>
        msg match {
          case WsMessage(m) =>
            m match {
              case Some(event: UserActionEvent) =>
                roomActor ! RoomActor.WsMessage(playerId, event)
              case _ =>
            }
            Behaviors.same

          case DispatchMsg(m) =>
            if(m.asInstanceOf[Wrap].isKillMsg) { //玩家死亡
              frontActor ! m
              roomManager ! RoomManager.LeftRoom(playerId, userInfo.name)
              Behaviors.stopped
            }else{
              frontActor ! m
              Behaviors.same
            }

          case LeftRoom(actor) =>
            ctx.unwatch(actor)
            roomManager ! RoomManager.LeftRoom(playerId, userInfo.name)
            Behaviors.stopped

          case unknownMsg =>
            Behavior.same
        }
    }
  }


}
