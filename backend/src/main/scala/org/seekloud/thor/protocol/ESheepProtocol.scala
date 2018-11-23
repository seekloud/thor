package org.seekloud.thor.protocol

/**
  * User: XuSiRan
  * Date: 2018/11/22
  * Time: 14:28
  */
object ESheepProtocol {

  trait ESheepRequest

  final case class GsKey2TokenReq(
                              gameId: Long,
                              gsKey: String
                              ) extends ESheepRequest

  final case class VerifyAccessCodeReq(
                                      gameId: Long,
                                      accessCode: String
                                      ) extends ESheepRequest

  trait ESheepResponse{
    val errCode: Int
    val msg: String
  }

  final case class GsToken(
                          token: String,
                          expireTime: Long
                          )

  final case class GetTokenByGsKeyRsp(
                                  data: GsToken,
                                  errCode: Int,
                                  msg: String
                                  ) extends ESheepResponse

  final case class PlayerData(
                             playerId: String,
                             nickname: String
                             )

  final case class GetPlayerByAccessCodeRsp(
                                        data: PlayerData,
                                        errCode: Int,
                                        msg: String
                                        ) extends ESheepResponse
  val ErrorGetPlayerByAccessCodeRsp = GetPlayerByAccessCodeRsp(PlayerData("",""), 200001, "accessCode error")
}
