package org.seekloud.thor.protocol

/**
  * User: TangYaruo
  * Date: 2019/3/9
  * Time: 15:22
  */
object ThorClientProtocol {

  case class ClientUserInfo(
    playerId: String,
    name: String,
    token: String,
    tokenExistTime: Option[Int] = None
  )

  case class PlayerInfo(
    playerId:String,
    nickName:String,
    roomId: Long
  )

}
