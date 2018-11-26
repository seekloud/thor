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



  val roomInfoRoutes: Route =  getRoomList //~ getRoomById ~ getPlayerByRoom

}
