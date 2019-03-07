package org.seekloud.thor.actor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import org.slf4j.LoggerFactory

/**
  * User: TangYaruo
  * Date: 2019/3/7
  * Time: 11:06
  */
object WsClient {

  private val log = LoggerFactory.getLogger(this.getClass)

  sealed trait WsCommand

  def create(): Behavior[WsCommand] =
    Behaviors.setup[WsCommand] { ctx =>
      working()
    }


  private def working(): Behavior[WsCommand] =
    Behaviors.receive[WsCommand] { (ctx, msg) =>
      msg match {
        case x =>
          log.warn(s"unknown msg: $x")
          Behaviors.same
      }
    }











}
