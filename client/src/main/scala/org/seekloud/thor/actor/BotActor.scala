package org.seekloud.thor.actor

import akka.actor.typed.{Behavior, ActorRef}
import akka.actor.typed.scaladsl.Behaviors
import org.slf4j.LoggerFactory

/**
  * User: TangYaruo
  * Date: 2019/3/12
  * Time: 13:42
  */
object BotActor {

  private val log = LoggerFactory.getLogger(this.getClass)

  sealed trait Command

  def create(wsClient: ActorRef[WsClient.WsCommand]): Behavior[Command] =
    Behaviors.setup[Command] { ctx =>
      working(wsClient)
    }


  def working(wsClient: ActorRef[WsClient.WsCommand]): Behavior[Command] =
    Behaviors.receive[Command] { (ctx, msg) =>
      msg match {
        case x =>
          log.warn(s"unknown msg: $x")
          Behaviors.unhandled
      }
    }





}
