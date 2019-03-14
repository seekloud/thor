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
import org.seekloud.thor.common.BotSettings
import org.seekloud.thor.controller.BotController
import org.seekloud.thor.protocol.BotProtocol.EnterRoomRsp
import org.seekloud.thor.shared.ptcl.model.Constants.GameState

import scala.concurrent.{ExecutionContext, Future}

/**
  * User: TangYaruo
  * Date: 2019/3/7
  * Time: 11:24
  */
object BotServer {

  private val log = LoggerFactory.getLogger(this.getClass)

  val streamSender: Option[ActorRef[GrpcStreamSender.Command]] = None

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
    * */
  override def actionSpace(request: Credit): Future[ActionSpaceRsp] = {
    if (checkBotToken(request.apiToken)) {
      val rsp = ActionSpaceRsp(swing = true, fire = List(0, 1), state = BotServer.state, msg = "ok" )
      Future.successful(rsp)
    } else {
      Future.successful(ActionSpaceRsp(errCode = 10004, state = State.unknown, msg = "Auth Error"))
    }
  }

  override def systemInfo(request: Credit): Future[SystemInfoRsp] = ???

  override def currentFrame(request: Credit, responseObserver: StreamObserver[CurrentFrameRsp]): Unit = ???

  override def action(request: ActionReq): Future[ActionRsp] = ???

  override def observation(request: Credit): Future[ObservationRsp] = ???

  override def observationWithInfo(request: Credit, responseObserver: StreamObserver[ObservationWithInfoRsp]): Unit = ???

  override def inform(request: Credit): Future[InformRsp] = ???

  override def reincarnation(request: Credit): Future[SimpleRsp] = ???


}
