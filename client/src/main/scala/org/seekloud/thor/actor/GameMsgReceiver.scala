package org.seekloud.thor.actor

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer, TimerScheduler}
import org.seekloud.thor.ClientBoot
import org.seekloud.thor.controller.RoomController
import org.seekloud.thor.shared.ptcl.protocol.ThorGame._
import org.slf4j.LoggerFactory

/**
  * User: TangYaruo
  * Date: 2019/3/7
  * Time: 11:29
  */
object GameMsgReceiver {

  private val log = LoggerFactory.getLogger(this.getClass)

  private[this] def switchBehavior(ctx: ActorContext[WsMsgSource],
    behaviorName: String,
    behavior: Behavior[WsMsgSource])
    (implicit stashBuffer: StashBuffer[WsMsgSource]) = {
    log.debug(s"${ctx.self.path} becomes $behaviorName behavior.")
    stashBuffer.unstashAll(ctx, behavior)
  }

  def create(wsClient: ActorRef[WsClient.WsCommand]): Behavior[WsMsgSource] = {
    Behaviors.setup[WsMsgSource] { ctx =>
      Behaviors.withTimers[WsMsgSource] { t =>
        implicit val stashBuffer: StashBuffer[WsMsgSource] = StashBuffer[WsMsgSource](Int.MaxValue)
        implicit val timer: TimerScheduler[WsMsgSource] = t
        switchBehavior(ctx, "waiting", waiting(wsClient))
      }
    }
  }


  /**
    * 接收游戏开始前game server发来的消息
    *
    * */
  private def waiting(
    wsClient: ActorRef[WsClient.WsCommand]
  )(
    implicit stashBuffer: StashBuffer[WsMsgSource],
    timer: TimerScheduler[WsMsgSource]
  ): Behavior[WsMsgSource] =
    Behaviors.receive[WsMsgSource] { (ctx, msg) =>
      msg match {
        case msg: JoinRoomFail =>
          wsClient ! WsClient.JoinRoomFail(msg.error)
          Behaviors.same

        case msg: CreateRoomRsp =>
          wsClient ! WsClient.CreateRoomRsp(msg.roomId)
          switchBehavior(ctx, "running", running())


        case x =>
          log.warn(s"unknown msg: $x")
          Behaviors.unhandled
      }
    }

  /**
    * 接收游戏过程中，game server发来的消息
    *
    * */
  def running()(
    implicit stashBuffer: StashBuffer[WsMsgSource],
    timer: TimerScheduler[WsMsgSource]
  ): Behavior[WsMsgSource] =
    Behaviors.receive[WsMsgSource] { (ctx, msg) =>
      msg match {
        case x =>
          log.warn(s"unknown msg: $x")
          Behaviors.unhandled
      }
    }


}
