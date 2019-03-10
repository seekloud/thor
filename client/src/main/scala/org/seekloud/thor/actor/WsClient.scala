package org.seekloud.thor.actor

import java.net.URLEncoder

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer, TimerScheduler}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage, WebSocketRequest}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Keep, Sink}
import akka.stream.typed.scaladsl.ActorSource
import akka.util.{ByteString, ByteStringBuilder}
import org.seekloud.byteobject.ByteObject.bytesDecode
import org.seekloud.thor.common.Routes
import org.seekloud.byteobject.MiddleBufferInJvm
import org.seekloud.thor.controller.GameController
import org.seekloud.thor.shared.ptcl.model.Constants.GameState
import org.seekloud.thor.model.{GameServerInfo, PlayerInfo, WsSendMsg}
import org.seekloud.thor.ClientBoot.{executor, materializer, scheduler, system}
import org.seekloud.thor.shared.ptcl.protocol.ThorGame
import org.seekloud.thor.shared.ptcl.protocol.ThorGame.PingPackage
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

/**
  * User: TangYaruo
  * Date: 2019/3/7
  * Time: 11:06
  */
object WsClient {

  private val log = LoggerFactory.getLogger(this.getClass)

  private final val InitTime = Some(5.minutes)

  sealed trait WsCommand

  final case class ConnectGame(playerInfo: PlayerInfo, gameInfo: GameServerInfo, roomInfo: Option[String]) extends WsCommand

  final case object ConnectTimerKey

  private final case object BehaviorChangeKey

  final case class SwitchBehavior(
    name: String,
    behavior: Behavior[WsCommand],
    durationOpt: Option[FiniteDuration] = None,
    timeOut: TimeOut = TimeOut("busy time error")
  ) extends WsCommand

  case class TimeOut(msg: String) extends WsCommand

  case class DispatchMsg(msg: ThorGame.WsMsgFront) extends WsCommand

  case object StopGameActor extends WsCommand

  case object StartGameLoop extends WsCommand

  case object StopGameLoop extends WsCommand

  case object GameLoopKey

  case object GameLoopTimeOut extends WsCommand

  case object StopGameLater extends WsCommand

  private case object TimerKey4StopGame

  final case object StopGame extends WsCommand

  final case class UserEnterRoom(event: ThorGame.UserEnterRoom) extends WsCommand

  final case class SetId(event: ThorGame.UserEnterRoom) extends WsCommand

  final case object TimerKey4SetId

  case class JoinByRoomId(playerInfo: PlayerInfo, domain: String, roomId: Int) extends WsCommand//roomId: Int, img: Int



  private[this] def switchBehavior(ctx: ActorContext[WsCommand], behaviorName: String,
    behavior: Behavior[WsCommand], durationOpt: Option[FiniteDuration] = None,
    timeOut: TimeOut = TimeOut("busy time error"))
    (implicit stashBuffer: StashBuffer[WsCommand],
      timer: TimerScheduler[WsCommand]) = {
    println(s"${ctx.self.path} becomes $behaviorName behavior.")
    timer.cancel(BehaviorChangeKey)
    durationOpt.foreach(timer.startSingleTimer(BehaviorChangeKey, timeOut, _))
    stashBuffer.unstashAll(ctx, behavior)
  }

  /**
    * 进入游戏连接参数
    */
  def create(control: GameController): Behavior[WsCommand] = {
    Behaviors.setup[WsCommand] { ctx =>
      implicit val stashBuffer = StashBuffer[WsCommand](Int.MaxValue)
      Behaviors.withTimers[WsCommand] { implicit timer =>
        init(control)
      }
    }
  }

//  def create(): Behavior[WsCommand] =
//    Behaviors.setup[WsCommand] { ctx =>
//      working()
//    }
//
//
//  private def working(): Behavior[WsCommand] =
//    Behaviors.receive[WsCommand] { (ctx, msg) =>
//      msg match {
//        case x =>
//          log.warn(s"unknown msg: $x")
//          Behaviors.same
//      }
//    }

  def init(control: GameController)(
    implicit stashBuffer: StashBuffer[WsCommand], timer: TimerScheduler[WsCommand]): Behavior[WsCommand] = {
    Behaviors.receive[WsCommand] { (ctx, msg) =>
      msg match {
        case msg: ConnectGame =>
          val url = getWebSocketUri(msg)
          println(s"url--$url")
          val webSocketFlow = Http().webSocketClientFlow(WebSocketRequest(url))
          val source = getSource
          val sink = getSink(control)
          val ((stream, response), closed) =
            source
              .viaMat(webSocketFlow)(Keep.both)
              .toMat(sink)(Keep.both)
              .run()
          val connected = response.flatMap { upgrade =>

            if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
              ctx.self ! SwitchBehavior("play", play(stream, control))
              Future.successful(s"${ctx.self.path} connect success.")
            } else {
              throw new RuntimeException(s"${ctx.self.path} connection failed: ${upgrade.response.status}")
            }
          } //链接建立时
          connected.onComplete { i => println(i.toString)}
          closed.onComplete { i =>
            println(s"${ctx.self.path} connect closed! try again 1 minutes later")
            //remind 此处存在失败重试
            ctx.self ! SwitchBehavior("init", init(control), InitTime)
            timer.startSingleTimer(ConnectTimerKey, msg, 1.minutes)
          } //链接断开时
          switchBehavior(ctx, "busy", busy(), InitTime)

        case x =>
          println(s"get unKnow msg $x")
          Behaviors.unhandled
      }
    }
  }

  def play(frontActor: ActorRef[ThorGame.WsMsgFront],
    control: GameController)(implicit stashBuffer: StashBuffer[WsCommand],
    timer: TimerScheduler[WsCommand]): Behavior[WsCommand] = {
    Behaviors.receive[WsCommand] { (ctx, msg) =>
      msg match {
        case msg: DispatchMsg =>
          frontActor ! msg.msg
          Behaviors.same

        case StartGameLoop =>
          timer.startPeriodicTimer(GameLoopKey, GameLoopTimeOut, 100.millis)
          Behaviors.same

        case StopGameLoop =>
          timer.cancel(GameLoopKey)
          Behaviors.same

        case GameLoopTimeOut =>
          control.logicLoop()
          Behaviors.same

        case StopGameActor =>
          Behaviors.stopped

        case StopGameLater =>
          timer.startSingleTimer(TimerKey4StopGame, StopGame, 400.millis)
          Behaviors.same

        case StopGame =>
          control.gameState = GameState.stop
          Behaviors.same

        case msg: UserEnterRoom =>
          timer.startSingleTimer(TimerKey4SetId, SetId(msg.event), 200.millis)
          Behaviors.same

        case msg: SetId =>
          //          control.thorSchemaOpt.get.playerIdMap.put(msg.event.shortId, (msg.event.playerId, msg.event.name))
          if (msg.event.playerId == control.getPlayer.playerId) control.byteId = msg.event.shortId
          control.thorSchemaOpt.foreach { thorSchema =>
            thorSchema.playerIdMap.put(msg.event.shortId, (msg.event.playerId, msg.event.name))
            thorSchema.receiveGameEvent(msg.event)
          }
          Behaviors.same

        case x =>
          log.info(s"unknown msg : $x")
          Behaviors.unhandled
      }
    }
  }

  private def busy()(
    implicit stashBuffer: StashBuffer[WsCommand],
    timer: TimerScheduler[WsCommand]
  ): Behavior[WsCommand] =
    Behaviors.receive[WsCommand] { (ctx, msg) =>
      msg match {
        case SwitchBehavior(name, behavior, durationOpt, timeOut) =>
          switchBehavior(ctx, name, behavior, durationOpt, timeOut)

        case TimeOut(m) =>
          println(s"${ctx.self.path} is time out when busy,msg=${m}")
          Behaviors.stopped

        case unknowMsg =>
          stashBuffer.stash(unknowMsg)
          Behavior.same
      }
    }

  def getWebSocketUri(info: ConnectGame): String = {
    val host = "10.1.29.250:30376"
    Routes.getJoinGameWebSocketUri(info.playerInfo.playerId, info.playerInfo.nickName, info.playerInfo.accessCode, info.gameInfo.domain, info.roomInfo)
  }

  import org.seekloud.byteobject.ByteObject._

  private[this] def getSink(control: GameController) = {
    import scala.language.implicitConversions

    implicit def parseJsonString2WsMsgFront(s: String): ThorGame.WsMsgServer = {
      import io.circe.generic.auto._
      import io.circe.parser._
      try {
        val wsMsg = decode[ThorGame.WsMsgServer](s).right.get
        wsMsg
      } catch {
        case e: Exception =>
          println(s"parse front msg failed when json parse,s=${s}")
          ThorGame.DecodeError()
      }
    }

    Sink.foreach[Message] {
      case TextMessage.Strict(m) =>
        log.debug(s"msg from webSocket: $m")
        control.wsMessageHandle(m)

      case BinaryMessage.Strict(m) =>
        val buffer = new MiddleBufferInJvm(m.asByteBuffer)
        bytesDecode[ThorGame.WsMsgServer](buffer) match {
          case Right(req) =>
            control.wsMessageHandle(req)
          case Left(e) =>
            println(s"decode binaryMessage failed,error:${e.message}")
            control.wsMessageHandle(ThorGame.DecodeError())
        }

      case msg: BinaryMessage.Streamed =>
        //        println(s"ssssssssssss${msg}")
        val f = msg.dataStream.runFold(new ByteStringBuilder().result()) {
          case (s, str) => s.++(str)
        }
        f.map { m =>
          val buffer = new MiddleBufferInJvm(m.asByteBuffer)
          bytesDecode[ThorGame.WsMsgServer](buffer) match {
            case Right(req) =>
              control.wsMessageHandle(req)
            case Left(e) =>
              println(s"decode binaryMessage failed,error:${e.message}")
              control.wsMessageHandle(ThorGame.DecodeError())
          }
        }

      case unknown@_ =>
        log.debug(s"i receiver an unknown message:$unknown")
    }
  }

  def getSource = ActorSource.actorRef[ThorGame.WsMsgFrontSource](
    completionMatcher = {
      case ThorGame.CompleteMsgFrontServer =>
    }, failureMatcher = {
      case ThorGame.FailMsgFrontServer(ex) => ex
    },
    bufferSize = 128,
    overflowStrategy = OverflowStrategy.fail
  ).collect {
    case message: ThorGame.WsMsgFront =>
      val sendBuffer = new MiddleBufferInJvm(409600)
      BinaryMessage.Strict(ByteString(
        message.fillMiddleBuffer(sendBuffer).result
      ))
  }



}
