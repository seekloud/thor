package org.seekloud.thor.core

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer, TimerScheduler}
import org.seekloud.utils.EsheepClient
import org.slf4j.{Logger, LoggerFactory}
import org.seekloud.thor.App.{executor, loginActor, pushStack2AppThread}
import org.seekloud.thor.model.{GameServerInfo, PlayerInfo, UserInfo}
import org.seekloud.thor.protocol.ESheepProtocol.ClientPlayerInfo

import scala.concurrent.duration._

/**
  * User: XuSiRan
  * Date: 2018/12/5
  * Time: 12:04
  * 初始化用户信息
  * 维护客户端用户的Token
  * 执行需要Token的方法的入口
  */
object TokenActor {

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  val changeTime: FiniteDuration = 30.seconds
  val initTime: FiniteDuration = 60.seconds

  trait Command

  case class SwitchBehavior(
    str: String,
    behavior: Behavior[Command],
    durationOpt: Option[FiniteDuration] = None,
    timeOut: TimeOut = TimeOut("change behavior time out")
  ) extends Command

  case object TimerUpdateTokenKey
  case object BehaviorChangeKey

  case class TimeOut(msg: String) extends Command

  case class StartInit(playerInfo: ClientPlayerInfo) extends Command

  case object InitError extends Command

  case object UpdateToken extends Command

  case class GetAccessCode(replyTo: ActorRef[Any]) extends Command // TODO Any为获取accessCode的actor地址

  private[this] def switchBehavior(
    ctx: ActorContext[Command],
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

  def busy()(
    implicit timer: TimerScheduler[Command],
    stashBuffer: StashBuffer[Command]): Behavior[Command] ={
    Behaviors.receive[Command]{ (ctx, msg)=>
      msg match{
        case SwitchBehavior(name, behavior, durationOpt, timeOut) =>
          switchBehavior(ctx, name, behavior, durationOpt, timeOut)

        case TimeOut(_) =>
          Behaviors.stopped

        case InitError =>
          Behaviors.stopped

        case unknown =>
          stashBuffer.stash(unknown)
          Behaviors.same
      }
    }
  }

  def create(): Behavior[Command]  ={
    Behaviors.setup[Command]{ ctx =>
      implicit val stashBuffer: StashBuffer[Command] = StashBuffer[Command](Int.MaxValue)
      Behaviors.withTimers[Command]{ implicit timer =>
        Behaviors.receiveMessage[Command] {
          case StartInit(playerInfo) =>
            switchBehavior(ctx, "init", init(playerInfo))
          case _ =>
            log.debug("not init")
            Behaviors.same
        }
      }
    }
  }

  def init(playerInfo: ClientPlayerInfo)(
    implicit timer: TimerScheduler[Command],
    stashBuffer: StashBuffer[Command]): Behavior[Command] ={
    Behaviors.setup{ ctx =>
      EsheepClient.linkGame(playerInfo.token, "user" + playerInfo.userId).map{
        case Right(rsp) =>
          log.info(s"init GameInfo success: | $rsp")
          EsheepClient.getRoomList(rsp.data.gsPrimaryInfo.ip, rsp.data.gsPrimaryInfo.port, rsp.data.gsPrimaryInfo.domain).map{
            case Right(info) =>
              val playerInfoInModel = PlayerInfo(UserInfo(playerInfo.userId, playerInfo.nickname, playerInfo.token, playerInfo.tokenExpireTime),"user" + playerInfo.userId ,playerInfo.nickname ,rsp.data.accessCode )
              val gameServerInfo = GameServerInfo(rsp.data.gsPrimaryInfo.ip, rsp.data.gsPrimaryInfo.port, rsp.data.gsPrimaryInfo.domain)

              loginActor ! LoginActor.LoginSuccess(ctx.self, info.data.roomList, playerInfoInModel, gameServerInfo)
              timer.startSingleTimer(TimerUpdateTokenKey, UpdateToken, 500 .seconds)
            case Left(error) =>
              log.debug(s"getRoomList error: $error")
          }
          ctx.self ! SwitchBehavior("idle", idle(playerInfo, "user" + playerInfo.userId, playerInfo.token, rsp.data.accessCode, rsp.data.gsPrimaryInfo.ip, rsp.data.gsPrimaryInfo.port, rsp.data.gsPrimaryInfo.domain))
        case Left(e) =>
          log.info(s"init GameInfo fail: $e")
          ctx.self ! InitError
      }
      switchBehavior(ctx, "busy", busy())
    }
  }

  def idle(
    playerInfo: ClientPlayerInfo,//Info里面的token没有设置更新，所以token用下面的
    playerId: String,
    token: String,
    accessCode: String,
    ip: String,
    port: Long,
    domain: String
  )(
    implicit timer: TimerScheduler[Command],
    stashBuffer: StashBuffer[Command]): Behavior[Command] ={
    Behaviors.receive[Command]{ (ctx, msg) =>
      msg match {
        case UpdateToken =>
          EsheepClient.refreshToken(token, playerId).map{
            case Right(rsp) =>
              timer.startSingleTimer(TimerUpdateTokenKey, UpdateToken, (rsp.data.expireTime - 200) .seconds)
              ctx.self ! SwitchBehavior("idle", idle(playerInfo, playerId, rsp.data.token, accessCode, ip, port, domain))
            case Left(e) =>
              log.debug(s"UpdateToken error!!! $e")
          }
          switchBehavior(ctx, "busy", busy())

        case GetAccessCode(replyTo) =>
          replyTo ! "" // TODO 发送AccessCode
          Behaviors.same
      }
    }
  }
}
