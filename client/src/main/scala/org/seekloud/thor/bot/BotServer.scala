package org.seekloud.thor.bot

import akka.actor.typed.scaladsl.AskPattern._
import org.seekloud.thor.ClientBoot.{executor, scheduler, timeout}
import org.slf4j.LoggerFactory
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.adapter._
import io.grpc.stub.StreamObserver
import org.seekloud.thor.actor.{BotActor, GrpcStreamSender}
import io.grpc.{Server, ServerBuilder}
import org.seekloud.esheepapi.pb.api._
import org.seekloud.esheepapi.pb.service.EsheepAgentGrpc
import org.seekloud.esheepapi.pb.service.EsheepAgentGrpc.EsheepAgent
import org.seekloud.thor.actor.WsClient.CreateRoom
import org.seekloud.thor.common.{AppSettings, BotSettings}
import org.seekloud.thor.controller.BotController
import org.seekloud.thor.protocol.BotProtocol.EnterRoomRsp
import org.seekloud.thor.shared.ptcl.model.Constants.GameState
import org.seekloud.thor.ClientBoot.{executor, system}

import scala.concurrent.{ExecutionContext, Future}

/**
  * User: TangYaruo
  * Date: 2019/3/7
  * Time: 11:24
  */
object BotServer {

  private val log = LoggerFactory.getLogger(this.getClass)

  var isObservationConnect = false
  var isFrameConnect = false

  var streamSender: Option[ActorRef[GrpcStreamSender.Command]] = None

  var state: State = State.unknown

  def checkBotToken(apiToken: String): Boolean = {
    if (apiToken == BotSettings.apiToken) true else false
  }

  def build(
    port: Int,
    executionContext: ExecutionContext,
    botActor: ActorRef[BotActor.Command],
    botController: BotController
  ): Server = {
    log.info(s"thor gRPC server is building...")
    val service = new BotServer(botActor, botController)

    ServerBuilder.forPort(port).addService(
      EsheepAgentGrpc.bindService(service, executionContext)
    ).build()
  }

}

class BotServer(botActor: ActorRef[BotActor.Command], botController: BotController) extends EsheepAgent {

  import BotServer._

  override def createRoom(request: CreateRoomReq): Future[CreateRoomRsp] = {
    if (checkBotToken(request.credit.get.apiToken)) {
      BotServer.state = State.in_game
      val enterRoomRsp: Future[EnterRoomRsp] = botActor ? (BotActor.CreateRoom(Some(request.password), _))
      enterRoomRsp.map {
        rsp =>
          if (rsp.errCode == 0)
            CreateRoomRsp(rsp.roomId.toString, 0, BotServer.state, "ok")
          else
            CreateRoomRsp(rsp.roomId.toString, rsp.errCode, BotServer.state, rsp.msg)
      }
    } else {
      Future.successful(CreateRoomRsp(errCode = 10001, state = State.unknown, msg = "Auth Error"))
    }

  }

  override def joinRoom(request: JoinRoomReq): Future[SimpleRsp] = {
    if (checkBotToken(request.credit.get.apiToken)) {
      BotServer.state = State.in_game
      val enterRoomRsp: Future[EnterRoomRsp] = botActor ? (BotActor.JoinRoom(request.roomId.toLong, request.password, _))
      enterRoomRsp.map { rsp =>
        if (rsp.errCode == 0)
          SimpleRsp(0, BotServer.state, "ok")
        else
          SimpleRsp(rsp.errCode, BotServer.state, rsp.msg)
      }
    } else {
      Future.successful(SimpleRsp(errCode = 10002, state = State.unknown, msg = "Auth Error"))
    }
  }

  override def leaveRoom(request: Credit): Future[SimpleRsp] = {
    if (checkBotToken(request.apiToken)) {
      BotServer.state = State.ended
      BotServer.streamSender.foreach(_ ! GrpcStreamSender.LeaveRoom)
      Future.successful {
        botActor ! BotActor.LeaveRoom
        SimpleRsp(state = BotServer.state, msg = "ok")
      }
    } else {
      Future.successful(SimpleRsp(errCode = 10003, state = State.unknown, msg = "Auth Error"))
    }
  }

  /**
    * fire: 0-攻击，1-加速
    *
    **/
  override def actionSpace(request: Credit): Future[ActionSpaceRsp] = {
    if (checkBotToken(request.apiToken)) {
      val rsp = ActionSpaceRsp(swing = true, fire = List(0, 1), state = BotServer.state, msg = "ok")
      Future.successful(rsp)
    } else {
      Future.successful(ActionSpaceRsp(errCode = 10004, state = State.unknown, msg = "Auth Error"))
    }
  }

  override def systemInfo(request: Credit): Future[SystemInfoRsp] = {
    if (checkBotToken(request.apiToken)) {
      val rsp = SystemInfoRsp(framePeriod = BotSettings.frameDuration, state = BotServer.state, msg = "ok")
      Future.successful(rsp)
    } else {
      Future.successful(SystemInfoRsp(errCode = 10005, state = State.unknown, msg = "Auth Error"))
    }
  }

  override def currentFrame(request: Credit, responseObserver: StreamObserver[CurrentFrameRsp]): Unit = {
    if (checkBotToken(request.apiToken)) {
      BotServer.isFrameConnect = true
      if (BotServer.streamSender.isDefined) {
        BotServer.streamSender.get ! GrpcStreamSender.FrameObserver(responseObserver)
      } else {
        BotServer.streamSender = Some(system.spawn(GrpcStreamSender.create(botController), "grpcStreamSender"))
        BotServer.streamSender.get ! GrpcStreamSender.FrameObserver(responseObserver)
      }

    } else {
      responseObserver.onCompleted()
    }
  }

  override def action(request: ActionReq): Future[ActionRsp] = {
    if(request.credit.nonEmpty && checkBotToken(request.credit.get.apiToken)) {
      //TODO botController 收到动作 获取帧号函数
//      val rsp =ActionRsp(frameIndex = , state = BotServer.state, msg = "ok")
//      Future.successful(rsp)
    } else {
      Future.successful(ActionRsp(errCode = 10006, state = State.unknown, msg = "Auth error"))
    }
  }

  override def observation(request: Credit): Future[ObservationRsp] = {
    if (checkBotToken(request.apiToken)) {
      //TODO 从botController获取state
      //TODO 从ByteReceiver获取Observation

    } else {
      Future.successful(ObservationRsp(errCode = 10007, state = State.unknown, msg = "Auth error"))
    }
  }

  override def observationWithInfo(request: Credit, responseObserver: StreamObserver[ObservationWithInfoRsp]): Unit = {
    if (checkBotToken(request.apiToken)) {
      BotServer.isObservationConnect = true
      if (BotServer.streamSender.isDefined) {
        BotServer.streamSender.get ! GrpcStreamSender.ObservationObserver(responseObserver)
      } else {
        BotServer.streamSender = Some(system.spawn(GrpcStreamSender.create(botController), "grpcStreamSender"))
        BotServer.streamSender.get ! GrpcStreamSender.ObservationObserver(responseObserver)
      }
    } else {
      responseObserver.onCompleted()
    }
  }

  override def inform(request: Credit): Future[InformRsp] = {
    if (checkBotToken(request.apiToken)) {
      //TODO 从botController获得状态（gameState、score、kill）
    } else {
      Future.successful(InformRsp(errCode = 10008, state = State.unknown, msg = "Auth Error"))
    }
  }

  override def reincarnation(request: Credit): Future[SimpleRsp] = {
    if (checkBotToken(request.apiToken)) {
      log.info(s"================RESTART=================")
      //TODO 重启操作
      Future.successful(SimpleRsp(state = BotServer.state, msg = "ok"))
    } else {
      Future.successful(SimpleRsp(errCode = 10009, state = State.unknown, msg = "Auth error"))
    }
  }


}
