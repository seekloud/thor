package org.seekloud.thor.http

import akka.actor.{ActorSystem, Scheduler}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import org.seekloud.thor.common.AppSettings
import akka.actor.typed.scaladsl.AskPattern._
import java.net.URLEncoder

import scala.concurrent.{ExecutionContextExecutor, Future}
import org.seekloud.thor.Boot.{eSheepLinkClient, executor, scheduler, timeout, userManager}
import org.seekloud.thor.core.{ESheepLinkClient, UserManager}
import org.seekloud.thor.protocol.ESheepProtocol.{ErrorGetPlayerByAccessCodeRsp, GetPlayerByAccessCodeRsp}


trait HttpService
  extends ResourceService
  with ServiceUtils
  with PlatService{

  import akka.actor.typed.scaladsl.AskPattern._
  import org.seekloud.utils.CirceSupport._
  import io.circe.generic.auto._

  implicit val system: ActorSystem

  implicit val executor: ExecutionContextExecutor

  implicit val materializer: Materializer

  implicit val timeout: Timeout

  implicit val scheduler: Scheduler


  import akka.actor.typed.scaladsl.adapter._





  lazy val routes: Route = pathPrefix(AppSettings.rootPath) {
    resourceRoutes ~ platEnterRoute ~
      (pathPrefix("game") & get){
        pathEndOrSingleSlash{
          getFromResource("html/admin.html")
        } ~
          path("join"){
          parameter('name){ name =>
            val flowFuture:Future[Flow[Message,Message,Any]] = userManager ? (UserManager.GetWebSocketFlow("test", name,_,None))
            dealFutureResult(
              flowFuture.map(t => handleWebSocketMessages(t))
            )
          }
        } ~
          (path("watchGame") & get & pathEndOrSingleSlash){
            parameter(
              'roomId.as[Long],
              'playerId.as[String],
              'accessCode.as[String]
            ){
              case (roomId, playerId, accessCode) =>
                val VerifyAccessCode: Future[GetPlayerByAccessCodeRsp] = eSheepLinkClient ? (ESheepLinkClient.VerifyAccessCode(accessCode, _))
                dealFutureResult {
                  VerifyAccessCode.flatMap {
                    case GetPlayerByAccessCodeRsp(data, 0, "ok") =>
                      println("ws and access ok")
                      val flowFuture: Future[Flow[Message, Message, Any]] = userManager ? (UserManager.GetWebSocketFlow4Watch(roomId, playerId, _, data.get.playerId, data.get.nickname))
                      flowFuture.map(t => handleWebSocketMessages(t))
                    case GetPlayerByAccessCodeRsp(data, _, _) =>
                      println("ws and accessCode error")
                      Future(complete(ErrorGetPlayerByAccessCodeRsp))
                  }
                }
            }
        } ~ platGameRoutes

      }
  }

  def platEnterRoute: Route = path("playGame"){
    parameter(
      'playerId.as[String],
      'playerName.as[String],
      'accessCode.as[String],
      'roomId.as[Long].?
    ) {
      case (playerId, playerName, accessCode, roomIdOpt) =>
        redirect(s"/thor/game/#/thor/playGame/$playerId/${URLEncoder.encode(playerName, "utf-8")}" + roomIdOpt.map(s => s"/$s").getOrElse("") + s"/$accessCode",
          StatusCodes.SeeOther
        )

    }
  } ~ path("watchGame") {
    parameter(
      'roomId.as[Long],
      'accessCode.as[String],
      'playerId.as[String].?
    ) {
      case (roomId, accessCode, playerIdOpt) =>
        redirect(s"/thor/game/#/watchGame/$roomId" + playerIdOpt.map(s => s"/$s").getOrElse("") + s"/$accessCode",
          StatusCodes.SeeOther
        )

    }
  }




}
