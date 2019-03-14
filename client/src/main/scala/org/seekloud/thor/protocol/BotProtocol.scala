package org.seekloud.thor.protocol

/**
  * User: TangYaruo
  * Date: 2019/3/14
  * Time: 12:35
  */
object BotProtocol {

  case class EnterRoomRsp(
    roomId: Long,
    errCode:Int =0,
    msg:String="ok"
  ) //create & join

}
