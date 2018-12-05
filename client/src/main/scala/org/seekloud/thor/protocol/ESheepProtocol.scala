package org.seekloud.thor.protocol

/**
  * User: XuSiRan
  * Date: 2018/12/4
  * Time: 12:03
  */
object ESheepProtocol {

  trait ESheepRsp {
    val errCode: Int
    val msg: String
  }

  final case class URLInfo(
    wsUrl: String,
    scanUrl: String
  )

  final case class LoginUrlRsp(
    data: URLInfo,
    errCode: Int = 0,
    msg: String = "ok"
  ) extends ESheepRsp

  final case class ClientPlayerInfo(
    userId: Long, //Long,
    nickname: String, //平台用户（玩家）昵称，可在平台修改
    token: String,
    tokenExpireTime: Int,
    headImg: String,
    gender: Int,
  )

  final case class WsPlayerInfoRsp(
    data: ClientPlayerInfo,
    errCode: Int = 0,
    msg: String = "ok"
  ) extends ESheepRsp

  final case class Ws4AgentRsp(Ws4AgentRsp: WsPlayerInfoRsp)

}
