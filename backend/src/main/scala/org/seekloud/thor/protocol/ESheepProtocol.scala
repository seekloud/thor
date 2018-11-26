package org.seekloud.thor.protocol

/**
  * User: XuSiRan
  * Date: 2018/11/22
  * Time: 14:28
  */
object ESheepProtocol {

  trait ESheepRequest

  final case class ESheepRecordSimple(
                                       startTime: Long,
                                       var killing: Int ,
                                       var killed: Int ,
                                       var score: Int
                                     )

  final case class ESheepRecord(
                                 playerId: String,
                                 gameId: Long = 1000000006l,
                                 nickname: String,
                                 killing: Int ,
                                 killed: Int ,
                                 score: Int,
                                 gameExtent: String = "",
                                 startTime: Long,
                                 endTime: Long
                               )

  final case class AddESheepRecordRsp(
                                       playerRecord: ESheepRecord
                                     )

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

  final case class CommonRsp(
                             errCode: Int,
                             msg: String
                             ) extends ESheepResponse

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
                                        data: Option[PlayerData],
                                        errCode: Int,
                                        msg: String
                                        ) extends ESheepResponse
  val ErrorGetPlayerByAccessCodeRsp = GetPlayerByAccessCodeRsp(None, 200001, "accessCode error")

  final case class RoomList(roomList: List[Long])

  final case class GetRoomListRsp(
                                 data: Option[RoomList],
                                 errCode: Int = 0,
                                 msg: String = "ok"
                                 ) extends ESheepResponse
  val ErrorGetRoomList = GetRoomListRsp(None, 200002, "getRoomList error in Service")
}
