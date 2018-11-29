package org.seekloud.thor.protocol

/**
  * User: XuSiRan
  * Date: 2018/11/22
  * Time: 14:28
  */
object ESheepProtocol {
  import org.seekloud.thor.shared.ptcl.CommonRsp

  trait ESheepRequest

  final case class ESheepRecordSimple(
    startTime: Long,
    var killing: Int,
    var killed: Int,
    var score: Int
  )

  final case class ESheepRecord(
    playerId: String,
    gameId: Long = 1000000006l,
    nickname: String,
    killing: Int,
    killed: Int,
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

  trait RequestFromESheep

  final case class GetRoomIdReq(playerId: String) extends RequestFromESheep

  final case class GetRoomPlayerListReq(roomId: Long) extends RequestFromESheep

  trait ESheepResponse {
    val errCode: Int
    val msg: String
  }

  final case class EsheepCommonRsp(
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

  final case class RoomId(roomId: Long)

  final case class GetRoomIdRsp(
    data: Option[RoomId],
    errCode: Int = 0,
    msg: String = "ok"
  ) extends ESheepResponse

  //  val NoRoomContainUser = GetRoomIdRsp(None, 200003, "there isn't a room which has the user")
  val ErrorGetRoomId = GetRoomIdRsp(None, 200004, "getRoomId error in Service")

  final case class PlayerList(
    playerList: List[PlayerData]
  )

  final case class GetRoomPlayerListRsp(
    data: Option[PlayerList],
    errCode: Int = 0,
    msg: String = "ok"
  ) extends ESheepResponse

  val ErrorGetRoomPlayerList = GetRoomPlayerListRsp(None, 200006, "getRoomId error in Service")

  /** 获取录像内玩家列表 */
  case class GetUserInRecordReq(
    recordId: Long,
    playerId: String
  )

  case class GetUserInRecordRsp(
    data: RecordPlayerList,
    errCode: Int = 0,
    msg: String = "ok"
  ) extends CommonRsp

  case class PlayerInRecordInfo(
    playerId: String,
    nickname: String,
    existTime: List[ExistTimeInfo]
  )

  case class ExistTimeInfo(
    startFrame: Long,
    endFrame: Long
  )

  case class RecordPlayerList(
    totalFrame: Int,
    playerList: List[PlayerInRecordInfo])


  /** 获取录像播放进度 */
  case class GetRecordFrameReq(
    recordId: Long,
    playerId: String //观看者
  )

  case class GetRecordFrameRsp(
    data: RecordFrameInfo,
    errCode: Int = 0,
    msg: String = "ok"
  ) extends CommonRsp

  case class RecordFrameInfo(frame: Int, frameNum: Long)

}
