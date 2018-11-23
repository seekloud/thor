package org.seekloud.thor.http

import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import akka.stream.scaladsl.Flow
import akka.actor.{ActorSystem, Scheduler}
import org.seekloud.thor.core.{ESheepLinkClient, UserManager}
import org.seekloud.thor.Boot.{eSheepLinkClient, userManager}
import akka.actor.typed.scaladsl.AskPattern._
import org.seekloud.thor.protocol.ESheepProtocol._
import org.seekloud.thor.Boot.executor
import org.slf4j.LoggerFactory

import scala.concurrent.Future

/**
  * User: XuSiRan
  * Date: 2018/11/22
  * Time: 16:35
  */

object PlatService {
  private val log = LoggerFactory.getLogger(this.getClass)
}

trait PlatService extends ServiceUtils{

  import org.seekloud.utils.CirceSupport._
  import io.circe.generic.auto._
  import io.circe._

  implicit val timeout: Timeout
  implicit val scheduler: Scheduler

  import PlatService._

  private val platUserJoin : Route = path("userJoin"){
    parameter(
      'playerId.as[String],
      'playerName.as[String],
      'accessCode.as[String]) { (id, name, accessCode) =>
      val VerifyAccessCode: Future[GetPlayerByAccessCodeRsp] = eSheepLinkClient ? (ESheepLinkClient.VerifyAccessCode(accessCode, _))
      dealFutureResult {
        VerifyAccessCode.flatMap {
          case GetPlayerByAccessCodeRsp(data, 0, "ok") =>
            println("ws and access ok")
            val flowFuture: Future[Flow[Message, Message, Any]] = userManager ? (UserManager.GetWebSocketFlow(id, name, _, None))
            flowFuture.map(t => handleWebSocketMessages(t))
          case GetPlayerByAccessCodeRsp(data, _, _) =>
            println("ws and accessCode error")
            Future(complete(ErrorGetPlayerByAccessCodeRsp))
        }
      }
    }
  }

  val platGameRoutes: Route = pathPrefix("playGame") {
    platUserJoin
  }

}
