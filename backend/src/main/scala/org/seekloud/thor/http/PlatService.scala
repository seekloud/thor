/*
 * Copyright 2018 seekloud (https://github.com/seekloud)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
      'accessCode.as[String],
      'roomId.as[Long].?) { (id, name, accessCode, roomId) =>
      val VerifyAccessCode: Future[GetPlayerByAccessCodeRsp] = eSheepLinkClient ? (ESheepLinkClient.VerifyAccessCode(accessCode, _))
      dealFutureResult {
        VerifyAccessCode.flatMap {
          case GetPlayerByAccessCodeRsp(data, 0, "ok") =>
            println("ws and access ok")
            val flowFuture: Future[Flow[Message, Message, Any]] = userManager ? (UserManager.GetWebSocketFlow(id, name, _, roomId))
            flowFuture.map(t => handleWebSocketMessages(t))
          case GetPlayerByAccessCodeRsp(data, _, _) =>
            println("ws and accessCode error")
            Future(complete(ErrorGetPlayerByAccessCodeRsp))
        }
      }
    }
  }

  private val clientLinkGame: Route = path("clientLinkGame") {
    parameter(
      'playerId.as[String],
      'playerName.as[String],
      'accessCode.as[String]
    ) { (playerId, playerName, accessCode) =>
      log.debug(s"client-$playerId link game...")
      val verifyAccessCode: Future[GetPlayerByAccessCodeRsp] = eSheepLinkClient ? (ESheepLinkClient.VerifyAccessCode(accessCode, _))
      verifyAccessCode.map { rsp =>
        if (rsp.errCode == 0) {
          log.debug(s"client-$playerId link game success...")
          val flowFuture: Future[Flow[Message, Message, Any]] = userManager ? (UserManager.GetWebSocketFlow4GA(playerId, playerName, _))
          flowFuture.map(handleWebSocketMessages)
        } else {
          println("clientLinkGame verify accessCode error.")
          Future(complete(ErrorGetPlayerByAccessCodeRsp))
        }
      }
      complete("")
    }
  }

  val platGameRoutes: Route = pathPrefix("playGame") {
    platUserJoin ~ clientLinkGame
  }

}
