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

import akka.actor.Scheduler
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import org.seekloud.thor.Boot.{eSheepLinkClient, executor, roomManager, userManager}
import org.seekloud.thor.core.{ESheepLinkClient, RoomManager, UserManager}
import org.seekloud.thor.protocol.ESheepProtocol._
import org.seekloud.thor.shared.ptcl.SuccessRsp
import org.seekloud.thor.shared.ptcl.protocol.CommonProtocol.{GetRoom4GARsp, VerifyPsw, VerifyPswRsp}
import org.slf4j.LoggerFactory

import scala.concurrent.Future

object RoomInfoService {
  private val log = LoggerFactory.getLogger(this.getClass)
}

trait RoomInfoService extends ServiceUtils{

  import org.seekloud.thor.http.RoomInfoService._

  import io.circe.generic.auto._

  implicit val timeout: Timeout
  implicit val scheduler: Scheduler

  private val getRoomList: Route = (path("getRoomList") & post){
    dealGetReq{
      val roomListRsp: Future[GetRoomListRsp] = roomManager ? (t => RoomManager.GetRoomList(t))
      roomListRsp.map{ rsp =>
        complete(rsp)
      }.recover{
        case e: Exception =>
          log.debug(s"getRoomList error in Service: $e")
          complete(ErrorGetRoomList)
      }
    }
  }

  private val getRoomList4GA: Route = (path("getRoomList4GA") & post) {
    dealGetReq {
      val roomListRsp: Future[GetRoom4GARsp] = roomManager ? (t => RoomManager.GetRoom4GA(t))
      roomListRsp.map{ rsp =>
        complete(rsp)
      }.recover{
        case e: Exception =>
          log.debug(s"getRoomList error in Service: $e")
          complete(ErrorGetRoomList)
      }
    }
  }

  private val verifyPwd: Route = (path("verifyPsw") & post) {
    dealPostReq[VerifyPsw] { req =>
      val verifyPwdRsp: Future[VerifyPswRsp] = roomManager ? (RoomManager.VerifyPwd(req.roomId, req.psw, _))
      verifyPwdRsp.map {
        rsp =>
          complete(rsp)
      }
    }

  }

  private val getRoomById: Route = (path("getRoomId") & post){
    dealPostReq[GetRoomIdReq]{ req =>
      val roomIdRsp: Future[GetRoomIdRsp] = roomManager ? (t => RoomManager.GetRoomByPlayer(req.playerId, t))
      roomIdRsp.map{ rsp =>
        complete(rsp)
      }.recover{
        case e: Exception =>
          log.debug(s"getRoomById error in Service: $e")
          complete(ErrorGetRoomId)
      }
    }
  }

  private val getPlayerByRoom: Route = (path("getRoomPlayerList") & post){
    dealPostReq[GetRoomPlayerListReq]{ req =>
      val playListRsp: Future[GetRoomPlayerListRsp] = roomManager ? (t => RoomManager.GetRoomPlayerList(req.roomId, t))
      playListRsp.map{ rsp =>
        complete(rsp)
      }.recover{
        case e: Exception =>
          log.debug(s"getPlayerByRoom error in Service: $e")
          complete(ErrorGetRoomPlayerList)
      }
    }
  }


  val roomInfoRoutes: Route =  getRoomList ~ getRoomById ~ getPlayerByRoom ~ getRoomList4GA ~ verifyPwd

}
