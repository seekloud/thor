package org.seekloud.thor.bot

import io.grpc.stub.StreamObserver
import io.grpc.{ManagedChannel, ManagedChannelBuilder}
import org.seekloud.esheepapi.pb.api._
import org.seekloud.esheepapi.pb.service.EsheepAgentGrpc
import org.seekloud.esheepapi.pb.service.EsheepAgentGrpc.EsheepAgentStub
import org.seekloud.thor.model.Constants.FireAction

import scala.concurrent.Future

/**
  * User: TangYaruo
  * Date: 2019/3/7
  * Time: 11:24
  */
class BotClient(
  host: String,
  port: Int,
  playerId: String,
  apiToken: String
) {

  private[this] val channel: ManagedChannel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build()

  private val esheepStub: EsheepAgentStub = EsheepAgentGrpc.stub(channel)

  val credit = Credit(apiToken = apiToken)

  def createRoom(): Future[CreateRoomRsp] = esheepStub.createRoom(CreateRoomReq(Some(credit), "password"))

  def joinRoom(roomId: String, password: String): Future[SimpleRsp] = esheepStub.joinRoom(JoinRoomReq(Some(credit), password, roomId))

  def leaveRoom(): Future[SimpleRsp] = esheepStub.leaveRoom(credit)

  def actionSpace(): Future[ActionSpaceRsp] = esheepStub.actionSpace(credit)

  def systemInfo(): Future[SystemInfoRsp] = esheepStub.systemInfo(credit)



  val frameStream: StreamObserver[CurrentFrameRsp] = new StreamObserver[CurrentFrameRsp] {
    override def onNext(value: CurrentFrameRsp): Unit = {}
    override def onCompleted(): Unit = {}
    override def onError(t: Throwable): Unit = {}
  }

  def currentFrame(): Unit = esheepStub.currentFrame(credit, frameStream)


  val actionReq = ActionReq(fire = FireAction.attack, credit = Some(credit))

  def action(): Future[ActionRsp] = esheepStub.action(actionReq)


  def observation(): Future[ObservationRsp] = esheepStub.observation(credit) //可能废弃？


  def obsStream: StreamObserver[ObservationWithInfoRsp] = new StreamObserver[ObservationWithInfoRsp] {
    override def onNext(value: ObservationWithInfoRsp): Unit = {}
    override def onCompleted(): Unit = {}
    override def onError(t: Throwable): Unit = {}
  }

  def observationWithInfo(): Unit = esheepStub.observationWithInfo(credit, obsStream)


  def inform: Future[InformRsp] = esheepStub.inform(credit)

  def reincarnation: Future[SimpleRsp] = esheepStub.reincarnation(credit)


}


object BotClient {

  def main(args: Array[String]): Unit = {

    val host = "127.0.0.1"
    val port = 5321
    val playerId = "test"
    val apiToken = "test"

    val client = new BotClient(host, port, playerId, apiToken)

    val rsp1 = client.createRoom()

    val rsp2 = client.observation()

    println("--------  begin sleep   ----------------")
    Thread.sleep(10000)
    println("--------  end sleep   ----------------")

    println(rsp1)
    println("------------------------")
    println(rsp2)
    println("client DONE.")


  }

}
