package org.seekloud.thor.shared.ptcl.protocol

/**
  * User: TangYaruo
  * Date: 2019/3/10
  * Time: 14:14
  */
object CommonProtocol {

  trait Request

  trait Response {
    val errCode: Int
    val msg: String
  }

  final case class GeneralRoom(roomList: List[String])

  final case class GetRoom4GARsp(
    data: GeneralRoom,
    errCode: Int = 0,
    msg: String = "ok"
  ) extends Response

  final case class VerifyPsw(roomId: Long, psw: String) extends Request

  final case class VerifyPswRsp(
    errCode: Int = 0,
    msg: String = "ok"
  ) extends Response

  val RoomNotExist = VerifyPswRsp(errCode = 47001, msg = "房间已不存在!")
  val PswError = VerifyPswRsp(errCode = 47002, msg = "密码错误！")

}
