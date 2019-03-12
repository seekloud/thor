package org.seekloud.thor.bot

import org.slf4j.LoggerFactory
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.adapter._
import io.grpc.stub.StreamObserver
import org.seekloud.thor.actor.{BotActor, GrpcStreamSender}
import io.grpc.{Server, ServerBuilder}
import org.seekloud.esheepapi.pb.api._
import org.seekloud.esheepapi.pb.service.EsheepAgentGrpc
import org.seekloud.esheepapi.pb.service.EsheepAgentGrpc.EsheepAgent
import org.seekloud.thor.controller.BotController

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

  override def createRoom(request: CreateRoomReq): Future[CreateRoomRsp] = ???

  override def joinRoom(request: JoinRoomReq): Future[SimpleRsp] = ???

  override def leaveRoom(request: Credit): Future[SimpleRsp] = ???

  override def actionSpace(request: Credit): Future[ActionSpaceRsp] = ???

  override def systemInfo(request: Credit): Future[SystemInfoRsp] = ???

  override def currentFrame(request: Credit, responseObserver: StreamObserver[CurrentFrameRsp]): Unit = ???

  override def action(request: ActionReq): Future[ActionRsp] = ???

  override def observation(request: Credit): Future[ObservationRsp] = ???

  override def observationWithInfo(request: Credit, responseObserver: StreamObserver[ObservationWithInfoRsp]): Unit = ???

  override def inform(request: Credit): Future[InformRsp] = ???

  override def reincarnation(request: Credit): Future[SimpleRsp] = ???


}
