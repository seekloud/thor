package org.seekloud.thor.core

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.{ActorContext, StashBuffer, TimerScheduler}
import org.seekloud.thor.protocol.ESheepProtocol.{ESheepRecord, ErrorGetPlayerByAccessCodeRsp, GetPlayerByAccessCodeRsp}
import org.seekloud.utils.ESheepClient
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration._
import org.seekloud.thor.Boot.executor

/**
  * User: XuSiRan
  * Date: 2018/11/22
  * Time: 13:29
  */
object ESheepLinkClient {

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  val changeTime: FiniteDuration = 30.seconds
  val initTime: FiniteDuration = 60.seconds

  trait Command

  case object UpdateToken extends Command // 更新token

  case class VerifyAccessCode(accessCode: String, replyTo: ActorRef[GetPlayerByAccessCodeRsp]) extends Command // 接受用户accessCode,反向验证

  case class AddRecord2ESheep(eSheepRecord: ESheepRecord) extends Command

  case class TimeOut(msg: String) extends Command

  case class SwitchBehavior(str: String,
                            behavior: Behavior[Command],
                            durationOpt: Option[FiniteDuration] = None,
                            timeOut: TimeOut = TimeOut("change behavior time out")) extends Command

  case object TimerUpdateTokenKey
  case object BehaviorChangeKey

  // TODO timer操作
  private[this] def switchBehavior(ctx: ActorContext[Command],
                                   behaviorName: String,
                                   behavior: Behavior[Command],
                                   durationOpt: Option[FiniteDuration] = None,
                                   timeOut: TimeOut = TimeOut("change behavior time out"))(
                                  implicit timer: TimerScheduler[Command],
                                  stashBuffer: StashBuffer[Command]) ={
    timer.cancel(BehaviorChangeKey)
    durationOpt.foreach(timer.startSingleTimer(BehaviorChangeKey,timeOut,_))
    stashBuffer.unstashAll(ctx, behavior)
  }

  def create(): Behavior[Command] ={
    Behaviors.setup[Command]{ ctx =>
      implicit val stashBuffer: StashBuffer[Command] = StashBuffer[Command](Int.MaxValue)
      Behaviors.withTimers[Command]{ implicit timer =>
        ctx.self ! UpdateToken
        init()
      }
    }
  }

  def init()(
          implicit timer: TimerScheduler[Command],
          stashBuffer: StashBuffer[Command]): Behavior[Command] ={
    Behaviors.receive[Command]{ (ctx, msg) =>
      msg match{
        case UpdateToken =>
          ESheepClient.gsKey2Token().map{
            case Right(rsp) =>
              timer.startSingleTimer(TimerUpdateTokenKey, UpdateToken, rsp.data.expireTime.seconds)
              println(s"start work token: ${rsp.data.token}")
              ctx.self ! SwitchBehavior("idle", idle(rsp.data.token))
            case Left(e) =>
              timer.startSingleTimer(TimerUpdateTokenKey, UpdateToken, 20.seconds)
              log.debug(s"init error, get token error: $e. retry after 20s.")
              ctx.self ! SwitchBehavior("init", init())
          }
          switchBehavior(ctx, "busy", busy(), Some(initTime))

        case unknown =>
          stashBuffer.stash(unknown)
          Behaviors.same
      }
    }
  }

  def busy()(
          implicit timer: TimerScheduler[Command],
          stashBuffer: StashBuffer[Command]): Behavior[Command] ={
    Behaviors.receive[Command]{ (ctx, msg)=>
      msg match{
        case SwitchBehavior(name, behavior, durationOpt, timeOut) =>
          switchBehavior(ctx, name, behavior, durationOpt, timeOut)

        case TimeOut(_) =>
          Behaviors.stopped

        case unknown =>
          stashBuffer.stash(unknown)
          Behaviors.same
      }
    }
  }

  def idle(token: String)(
          implicit timer: TimerScheduler[Command],
          stashBuffer: StashBuffer[Command]): Behavior[Command] ={
    Behaviors.receive[Command]{ (ctx, msg) =>
      msg match {
        case UpdateToken =>
          ESheepClient.gsKey2Token().map{
            case Right(rsp) =>
              timer.startSingleTimer(TimerUpdateTokenKey, UpdateToken, rsp.data.expireTime.seconds)
              ctx.self ! SwitchBehavior("idle", idle(rsp.data.token))
            case Left(e) =>
              timer.startSingleTimer(TimerUpdateTokenKey, UpdateToken, 20.seconds)
              log.debug(s"ESheepLinkClient error, get token error: $e. back to init, retry after 20s.")
              ctx.self ! SwitchBehavior("init", init())
          }
          switchBehavior(ctx, "busy", busy(), Some(initTime))

        case VerifyAccessCode(accessCode, replyTo) =>
          ESheepClient.verifyAccessCode(accessCode, token).map{
            case Right(rsp) =>
              println(s"verifyAccessCode success: $rsp")
              replyTo ! rsp
            case Left(e) =>
              println(s"verifyAccessCode error: $e")
              replyTo ! ErrorGetPlayerByAccessCodeRsp
          }
          Behaviors.same

        case AddRecord2ESheep(record) =>
          ESheepClient.addRecord2ESheep(record, token).map{
            case Right(rsp) =>
              println(s"verifyAccessCode success: $rsp")
            case Left(e) =>
              println(s"verifyAccessCode error: $e")
          }
          Behaviors.same


        case TimeOut(_) =>
          Behaviors.stopped

        case unknown =>
          stashBuffer.stash(unknown)
          Behaviors.same
      }
    }
  }
}
