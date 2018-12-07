package org.seekloud.thor.core

import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer, TimerScheduler}
import akka.actor.typed.{ActorRef, Behavior}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage, WebSocketRequest}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Keep, Sink}
import akka.stream.typed.scaladsl.ActorSource
import akka.util.{ByteString, ByteStringBuilder}
import org.seekloud.byteobject.MiddleBufferInJvm
import org.seekloud.thor.App.{executor, materializer, system}
import org.seekloud.thor.common.Routes
import org.seekloud.thor.controller.PlayGameController
import org.seekloud.thor.model._
import org.seekloud.thor.shared.ptcl.protocol.ThorGame
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * Created by ltm on 2018/12/05
  */

object PlayGameActor {
  private val log = LoggerFactory.getLogger(this.getClass)
  private final val InitTime = Some(5.minutes)

  sealed trait Command

  final case class ConnectGame(playerInfo:PlayerInfo,gameInfo:GameServerInfo,roomInfo:Option[String]) extends Command

  final case object ConnectTimerKey

  private final case object BehaviorChangeKey

  final case class SwitchBehavior(
                                 name: String,
                                 behavior: Behavior[Command],
                                 durationOpt: Option[FiniteDuration] = None,
                                 timeOut: TimeOut = TimeOut("busy time error")
                                 ) extends Command

  case class TimeOut(msg:String) extends Command

  case class DispatchMsg(msg:ThorGame.WsMsgFront) extends Command

  case object StopGameActor extends Command

  case object StartGameLoop extends Command

  case object StopGameLoop extends Command

  case object GameLoopKey

  case object GameLoopTimeOut extends Command

  private[this] def switchBehavior(ctx: ActorContext[Command], behaviorName: String,
                                   behavior:Behavior[Command], durationOpt: Option[FiniteDuration] = None,
                                   timeOut:TimeOut = TimeOut("busy time error"))
                                  (implicit stashBuffer: StashBuffer[Command],
                                   timer:TimerScheduler[Command]) ={
    println(s"${ctx.self.path} becomes $behaviorName behavior.")
    timer.cancel(BehaviorChangeKey)
    durationOpt.foreach(timer.startSingleTimer(BehaviorChangeKey, timeOut, _))
    stashBuffer.unstashAll(ctx, behavior)
  }

  /**
    *进入游戏连接参数
    */
  def create(control:PlayGameController) = {
    Behaviors.setup[Command] {ctx =>
      implicit val stashBuffer = StashBuffer[Command](Int.MaxValue)
      Behaviors.withTimers[Command] { implicit  timer =>
        init(control)
      }
    }
  }

  def init(control: PlayGameController)(
          implicit stashBuffer: StashBuffer[Command], timer: TimerScheduler[Command]):Behavior[Command] ={
    Behaviors.receive[Command]{ (ctx,msg) =>
      msg match{
        case msg:ConnectGame =>
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
              ctx.self ! SwitchBehavior("play", play(stream,control))
              Future.successful(s"${ctx.self.path} connect success.")
            } else {
              throw new RuntimeException(s"${ctx.self.path} connection failed: ${upgrade.response.status}")
            }
          } //链接建立时
          connected.onComplete { i => println(i.toString) }
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
           control: PlayGameController)(implicit stashBuffer: StashBuffer[Command],
                                          timer: TimerScheduler[Command]) = {
    Behaviors.receive[Command] { (ctx, msg) =>
      msg match {
        case msg: DispatchMsg =>
          frontActor ! msg.msg
          Behaviors.same

        case StartGameLoop =>
          timer.startPeriodicTimer(GameLoopKey,GameLoopTimeOut,100.millis)
          Behaviors.same

        case StopGameLoop =>
          timer.cancel(GameLoopKey)
          Behaviors.same

        case GameLoopTimeOut =>
          // todo playGameCroller.logicLoop

        case StopGameActor =>
          Behaviors.stopped

        case x =>
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
          println(s"${ctx.self.path} is time out when busy,msg=${m}")
          Behaviors.stopped

        case unknowMsg =>
          stashBuffer.stash(unknowMsg)
          Behavior.same
      }
    }

  def getWebSocketUri(info:ConnectGame): String = {
    val host = "10.1.29.250:30376"
    Routes.getJoinGameWebSocektUri(info.playerInfo.nickName,info.gameInfo.domain,info.roomInfo)
  }

  import org.seekloud.byteobject.ByteObject._

  def getSink(control: PlayGameController) ={
    import scala.language.implicitConversions

    implicit def parseJsonString2WsMsgFront(s: String):ThorGame.WsMsgServer ={
      import io.circe.generic.auto._
      import io.circe.parser._
      try {
        val wsMsg = decode[ThorGame.WsMsgServer](s).right.get
        wsMsg
      } catch {
        case e:Exception =>
          println(s"parse front msg failed when json parse,s=${s}")
          ThorGame.DecodeError()
      }
    }

    Sink.foreach[Message] {
      case TextMessage.Strict(m) =>
        control.wsMessageHandler(m)

      case BinaryMessage.Strict(m) =>
        val buffer = new MiddleBufferInJvm(m.asByteBuffer)
        bytesDecode[ThorGame.WsMsgServer](buffer) match {
          case Right(req) =>
            control.wsMessageHandler(req)
          case Left(e) =>
            println(s"decode binaryMessage failed,error:${e.message}")
            control.wsMessageHandler(ThorGame.DecodeError())
        }

      case msg: BinaryMessage.Streamed =>
        println(s"ssssssssssss${msg}")
        val f = msg.dataStream.runFold(new ByteStringBuilder().result()) {
          case (s, str) => s.++(str)
        }
        f.map { m =>
          val buffer = new MiddleBufferInJvm(m.asByteBuffer)
          bytesDecode[ThorGame.WsMsgServer](buffer) match {
            case Right(req) =>
              control.wsMessageHandler(req)
            case Left(e) =>
              println(s"decode binaryMessage failed,error:${e.message}")
              control.wsMessageHandler(ThorGame.DecodeError())
          }
        }

      case _ =>
    }
  }

  def getSource = ActorSource.actorRef[ThorGame.WsMsgFrontSource](
    completionMatcher = {
      case ThorGame.CompleteMsgFrontServer =>
    },failureMatcher = {
      case ThorGame.FailMsgFrontServer(ex) =>ex
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
