package org.seekloud.thor.controller

import akka.actor.typed.ActorRef
import org.seekloud.thor.protocol.BotProtocol.EnterRoomRsp

/**
  * User: TangYaruo
  * Date: 2019/3/12
  * Time: 13:43
  */
class BotController {

  var sdkReplyTo: Option[ActorRef[EnterRoomRsp]] = None


}
