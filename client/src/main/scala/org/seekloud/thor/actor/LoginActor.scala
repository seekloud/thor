package org.seekloud.thor.actor

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import org.seekloud.thor.protocol.ESheepProtocol._
import org.seekloud.utils.EsheepClient
import org.seekloud.thor.App.{executor, materializer, pushStack2AppThread, system}
import org.seekloud.thor.common.LoginPage
import akka.{Done, NotUsed}
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws._

import scala.concurrent.Future
import io.circe._
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import javafx.scene.Scene
import org.slf4j.LoggerFactory


/**
  * User: XuSiRan
  * Date: 2018/12/4
  * Time: 11:55
  */
object LoginActor {

  private val log = LoggerFactory.getLogger(this.getClass)

  trait Command

  case class GetLoginImg(page: LoginPage) extends Command

  case class CreateWs(url: String, page: LoginPage) extends Command

  case object CloseWs extends Command

  case class LoginSuccess(roomList: List[Long]) extends Command

  def init: Behavior[Command] ={
    Behaviors.receive[Command]{ (ctx, msg) =>
      msg match {
        case GetLoginImg(page) =>
          EsheepClient.getLoginInfo.map{
            case Right(rsp) =>
              pushStack2AppThread(page.imgSence(rsp))
              ctx.self ! CreateWs(rsp.data.wsUrl, page)
            case Left(e) =>
              log.debug(s"get LoginUrl error: $e")
          }
          idle(page)

        case _ =>
          log.debug("初始化未完成，数据无法接受")
          Behaviors.same
      }
    }
  }

  def idle(page: LoginPage): Behavior[Command] = {
    Behaviors.receive[Command]{ (ctx, msg) =>
      msg match {
        case CreateWs(url, page) =>
          val incoming: Sink[Message, Future[Done]] =
            Sink.foreach {
              case message: TextMessage.Strict =>
                decode[Ws4AgentRsp](message.text) match {
                  case Right(rsp) =>
                    log.info("Ws4AgentRsp: " + message.text)
                    system.spawn(TokenActor.create(rsp.Ws4AgentRsp.data.token, "user" + rsp.Ws4AgentRsp.data.userId), "tokenActor")
                    pushStack2AppThread(page.infoSence(rsp.Ws4AgentRsp))
                  case Left(e) =>
                    log.info("other textMessage：" + message.text)
                }
              case _ =>
                log.debug("???none???")
            }
          val outgoing =
            Source.maybe[Message]
          val flow: Flow[Message, Message, Future[Done]] =
            Flow.fromSinkAndSourceMat(incoming, outgoing)(Keep.left)
          val (upgradeResponse, closed) =
            Http().singleWebSocketRequest(WebSocketRequest(url), flow)
          val connected = upgradeResponse.map { upgrade =>
            if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
              Done
            } else {
              throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
            }
          }
          connected.onComplete(_ => ())
          closed.foreach{_ => log.info("webSocket closed")}


//          val incoming: Sink[Message, Future[Done]] =
//            Sink.foreach[Message] {
//              case message: TextMessage.Strict =>
//                decode[Ws4AgentRsp](message.text) match {
//                  case Right(rsp) =>
////                    log.info("Ws4AgentRsp: " + message.text)
//                    pushStack2AppThread(page.infoSence(rsp.Ws4AgentRsp))
//                  case Left(e) =>
//                    log.info("other textMessage：" + message.text)
//                }
//              case _ =>
//                log.debug("???none???")
//            }
//          val outgoing = Source.maybe[Message]
//          val webSocketFlow = Http().webSocketClientFlow(WebSocketRequest(url))
//          val (upgradeResponse, closed) =
//            outgoing
//              .viaMat(webSocketFlow)(Keep.right)
//              .toMat(incoming)(Keep.both)
//              .run()
//          val connected = upgradeResponse.flatMap { upgrade =>
//            if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
//              Future.successful(Done)
//            } else {
//              throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
//            }
//          }
//          connected.onComplete(_ => ())
//          closed.foreach(_ => log.info("webSocket closed"))
          Behaviors.same

        case LoginSuccess(roomList) =>
          println("★★★")
          pushStack2AppThread(page.roomScene(roomList))
          Behaviors.same
      }
    }
  }
}
