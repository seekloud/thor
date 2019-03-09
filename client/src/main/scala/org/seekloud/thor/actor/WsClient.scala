package org.seekloud.thor.actor

import akka.Done
import akka.actor.typed._
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{Behaviors, TimerScheduler}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{BinaryMessage, WebSocketRequest}
import akka.stream.OverflowStrategy
import akka.stream.typed.scaladsl.ActorSource
import akka.util.{ByteString, ByteStringBuilder}
import akka.http.scaladsl.model.ws._
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.typed.scaladsl.{ActorSink, ActorSource}
import org.seekloud.byteobject.ByteObject._
import org.seekloud.byteobject.MiddleBufferInJvm
import org.seekloud.byteobject.MiddleBufferInJvm
import org.seekloud.thor.common.StageContext
import org.seekloud.thor.controller.LoginController
import org.seekloud.thor.shared.ptcl.protocol.ThorGame._
import org.seekloud.thor.ClientBoot.{executor, materializer, system}
import org.seekloud.thor.protocol.ESheepProtocol
import org.seekloud.thor.protocol.ESheepProtocol.{HeartBeat, Ws4AgentRsp}
import org.slf4j.LoggerFactory

import scala.concurrent.Future

/**
  * User: TangYaruo
  * Date: 2019/3/7
  * Time: 11:06
  */
object WsClient {

  private val log = LoggerFactory.getLogger(this.getClass)

  sealed trait WsCommand

  final case class GetLoginController(loginController: LoginController) extends WsCommand

  final case class EstablishConnection2Es(wsUrl: String) extends WsCommand

  final case class GetLoginInfo(playerId: String, name: String, token: String, tokenExistTime: Int) extends WsCommand

  final case object Stop extends WsCommand

  def create(gameMsgReceiver: ActorRef[WsMsgSource], stageContext: StageContext): Behavior[WsCommand] =
    Behaviors.setup[WsCommand] { _ =>
      Behaviors.withTimers[WsCommand] { implicit timer =>
        working(gameMsgReceiver, None, stageContext)
      }
    }


  private def working(
    gameMsgReceiver: ActorRef[WsMsgSource],
    loginController: Option[LoginController],
    stageContext: StageContext
  )(
    implicit timer: TimerScheduler[WsCommand]
  ): Behavior[WsCommand] =
    Behaviors.receive[WsCommand] { (ctx, msg) =>
      msg match {
        case msg: GetLoginController =>
          working(gameMsgReceiver, Some(msg.loginController), stageContext)

        case msg: EstablishConnection2Es =>
          log.info(s"get msg: $msg")
          val webSocketFlow = Http().webSocketClientFlow(WebSocketRequest(msg.wsUrl))
          val source = getSource(ctx.self)
          val sink = getSink(ctx.self)
          val response =
            source
              .viaMat(webSocketFlow)(Keep.right)
              .toMat(sink)(Keep.left)
              .run()
          val connected = response.flatMap { upgrade =>
            if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
              Future.successful(s"EstablishConnection2Es successful.")
            } else {
              throw new RuntimeException(s"WSClient connection failed: ${upgrade.response.status}")
            }
          } //链接建立时
          connected.onComplete(i => log.info(i.toString))
          Behaviors.same

        case msg: GetLoginInfo =>
          log.info(s"get msg: $msg")
          //TODO switch & sendReq
          Behaviors.same

        case Stop =>
          log.info(s"wsClient stopped.")
          Behaviors.stopped

        case x =>
          log.warn(s"unknown msg: $x")
          Behaviors.same
      }
    }


  def getSource(wsClient: ActorRef[WsCommand]): Source[BinaryMessage.Strict, ActorRef[WsMsgFrontSource]] =
    ActorSource.actorRef[WsMsgFrontSource](
      completionMatcher = {
        case CompleteMsgFrontServer =>
          log.info("WebSocket Complete")
          wsClient ! Stop
      },
      failureMatcher = {
        case FailMsgFrontServer(ex) ⇒ ex
      },
      bufferSize = 8,
      overflowStrategy = OverflowStrategy.fail
    ).collect {
      case message: UserActionEvent =>
        //println(message)
        val sendBuffer = new MiddleBufferInJvm(409600)
        BinaryMessage.Strict(ByteString(
          message.fillMiddleBuffer(sendBuffer).result()
        ))
    }



  def getSink(self: ActorRef[WsClient.WsCommand]): Sink[Message, Future[Done]] =
    Sink.foreach[Message] {
      case TextMessage.Strict(msg) =>
        import io.circe.generic.auto._
        import io.circe.parser.decode
        log.debug(s"msg from webSocket: $msg")
        decode[ESheepProtocol.WsData](msg) match {
          case Right(rst) =>
            rst match {
              case value: Ws4AgentRsp =>
                if (value.errCode == 0) {
                  val playerId = s"user${value.data.userId}"
                  val name = value.data.nickname
                  val token = value.data.token
                  val tokenExistTime = value.data.tokenExpireTime
                  self ! GetLoginInfo(playerId, name, token, tokenExistTime)
                } else {
                  log.error(s"Ws4AgentRsp error: ${value.msg}")
                }
              case HeartBeat => // do nothing
            }

          case Left(e) =>
            log.error(s"decode MsgFromWs error: $e")

        }
      case x =>
        log.debug(s"receive unknown msg: $x")

    }


}
