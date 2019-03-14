package org.seekloud.thor.actor

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import org.seekloud.esheepapi.pb.api.SimpleRsp
import org.seekloud.thor.controller.BotController
import org.seekloud.thor.protocol.BotProtocol.EnterRoomRsp
import org.slf4j.LoggerFactory

/**
  * User: TangYaruo
  * Date: 2019/3/12
  * Time: 13:42
  */
object BotActor {

  private val log = LoggerFactory.getLogger(this.getClass)

  trait Command

  final case class CreateRoom(psw:Option[String], replyTo: ActorRef[EnterRoomRsp]) extends Command

  final case class JoinRoom(roomId: Long, pwd: String, replyTo: ActorRef[EnterRoomRsp]) extends Command

  final case object LeaveRoom extends Command


  def create(wsClient: ActorRef[WsClient.WsCommand], botController: BotController): Behavior[Command] =
    Behaviors.setup[Command] { ctx =>
      working(wsClient, botController)
    }


  def working(wsClient: ActorRef[WsClient.WsCommand], botController: BotController): Behavior[Command] =
    Behaviors.receive[Command] { (ctx, msg) =>
      msg match {

        case msg: CreateRoom =>
          botController.sdkReplyTo = Some(msg.replyTo)
          wsClient ! WsClient.CreateRoom(msg.psw)
          Behaviors.same

        case msg: JoinRoom =>
          botController.sdkReplyTo = Some(msg.replyTo)
          wsClient ! WsClient.StartGame(msg.roomId, Some(msg.pwd))
          Behaviors.same

        case LeaveRoom =>
          wsClient ! WsClient.Stop
          Behaviors.same

        case x =>
          log.warn(s"unknown msg: $x")
          Behaviors.unhandled
      }
    }





}
