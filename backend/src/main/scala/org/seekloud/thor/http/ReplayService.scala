package org.seekloud.thor.http


import akka.actor.Scheduler
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import org.seekloud.thor.Boot.{executor, gameRecordGetter, userManager}
import org.seekloud.thor.core.GameRecordGetter
import org.seekloud.thor.protocol.ESheepProtocol._
import org.seekloud.thor.protocol.ReplayProtocol._
import org.seekloud.thor.shared.ptcl.CommonRsp
import org.slf4j.LoggerFactory

import scala.concurrent.Future

/**
  * User: XuSiRan
  * Date: 2018/11/28
  * Time: 14:53
  */

object ReplayService {
  private val log = LoggerFactory.getLogger(this.getClass)
}

trait ReplayService extends ServiceUtils{

  import org.seekloud.thor.http.ReplayService._

  import io.circe.generic.auto._

  implicit val timeout: Timeout
  implicit val scheduler: Scheduler

  private val getRecordList: Route = (path("getRecordList") & post){
    dealPostReq[GetRecordListReq]{ data =>
      val recordListRsp: Future[GetReplyRecordRsp] = gameRecordGetter ? (t => GameRecordGetter.GetRecordList(data.lastRecordId, data.count, t))
      recordListRsp.map{ rsp =>
        complete(rsp)
      }.recover{
        case e: Exception =>
          log.debug(s"20181129 error $e")
          complete(ErrorGetReplyRecord)
      }
    }
  }

  private val getRecordListByTime: Route = (path("getRecordListByTime") & post){
    dealPostReq[GetRecordListByTimeReq]{ data =>
      val recordListRsp: Future[GetReplyRecordRsp] = gameRecordGetter ? (t => GameRecordGetter.GetRecordListByTime(data.startTime, data.endTime, data.lastRecordId, data.count, t))
      recordListRsp.map{ rsp =>
        complete(rsp)
      }.recover{
        case e: Exception =>
          log.debug(s"20181129 error $e")
          complete(ErrorGetReplyRecord)
      }
    }
  }
  private val getRecordListByPlayer: Route = (path("getRecordListByPlayer") & post){
    dealPostReq[GetRecordListByPlayerReq]{ data =>
      val recordListRsp: Future[GetReplyRecordRsp] = gameRecordGetter ? (t => GameRecordGetter.GetRecordListByPlayer(data.playerId, data.lastRecordId, data.count, t))
      recordListRsp.map{ rsp =>
        complete(rsp)
      }.recover{
        case e: Exception =>
          log.debug(s"20181129 error $e")
          complete(ErrorGetReplyRecord)
      }
    }
  }
  private val getRecordPlayerList: Route = (path("getRecordPlayerList") & post){
    dealPostReq[GetRecordPlayerListReq]{ data =>
      val recordListRsp: Future[CommonRsp] = userManager ? (t => GetUserInRecordMsg(data.recordId, data.playerId, t))
      recordListRsp.map{
        case rsp:GetUserInRecordRsp =>
          complete(rsp)
        case error =>
          log.debug(s"error in getRecordPlayerList : $error")
          complete(ErrorGetReplayPlayerList2)
      }.recover{
        case e: Exception =>
          log.debug(s"20181129 error $e")
          complete(ErrorGetReplayPlayerList1)
      }
    }
  }
  private val getRecordFrame: Route = (path("getRecordFrame") & post){
    dealPostReq[GetRecordFrameReq]{ data =>
      val recordListRsp: Future[CommonRsp] = userManager ? (t => GetRecordFrameMsg(data.recordId, data.playerId, t))
      recordListRsp.map{
        case rsp:GetRecordFrameRsp =>
          complete(rsp)
        case error =>
          log.debug(s"error in getRecordFrame : $error")
          complete(ErrorGetRecordFrame2)
      }.recover{
        case e: Exception =>
          log.debug(s"20181129 error $e")
          complete(ErrorGetRecordFrame1)
      }
    }
  }



  val replayRoutes: Route =  getRecordList ~ getRecordListByTime ~ getRecordListByPlayer ~ getRecordPlayerList ~ getRecordFrame





}

