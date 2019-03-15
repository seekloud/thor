package org.seekloud.thor.controller

import akka.actor.typed.ActorRef
import org.seekloud.thor.protocol.BotProtocol.EnterRoomRsp
import org.slf4j.LoggerFactory

/**
  * User: TangYaruo
  * Date: 2019/3/12
  * Time: 13:43
  */

object BotController {

  private val log = LoggerFactory.getLogger(this.getClass)



}



class BotController {

  import BotController._

  var sdkReplyTo: Option[ActorRef[EnterRoomRsp]] = None





}
