/*
 * Copyright 2018 seekloud (https://github.com/seekloud)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.seekloud.thor.protocol

import org.seekloud.thor.shared.ptcl.ErrorRsp

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

  final case class GetRecordListReq(
                                     lastRecordId: Long,
                                     count:  Int
                                   ) extends ESheepRequest

  final case class GetRecordListByTimeReq(
                                           startTime: Long,
                                           endTime: Long,
                                           lastRecordId: Long,
                                           count:  Int
                                         ) extends ESheepRequest

  final case class GetRecordListByPlayerReq(
                                             playerId: String,
                                             lastRecordId: Long,
                                             count:  Int
                                           ) extends ESheepRequest

  final case class GetRecordPlayerListReq(
                                        recordId: Long,
                                        playerId: String
                                      ) extends  ESheepRequest

  final case class GetRecordFrameReq(
                                      recordId: Long,
                                      playerId: String  //观看玩家的id
                                    )



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

  final case class Room(
    roomId: Long,
    roomName: String,
    createTime: Long
  )

  final case class RoomList(roomList: List[Room])

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

//  /** 获取录像内玩家列表 */
//  case class GetUserInRecordReq(
//    recordId: Long,
//    playerId: String
//  )
//
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
  val ErrorGetReplayPlayerList1 = ErrorRsp(300002, "error")
  val ErrorGetReplayPlayerList2 = ErrorRsp(300003, "error")

  //
//
//  /** 获取录像播放进度 */
//  case class GetRecordFrameReq(
//    recordId: Long,
//    playerId: String //观看者
//  )
//
  case class GetRecordFrameRsp(
    data: RecordFrameInfo,
    errCode: Int = 0,
    msg: String = "ok"
  ) extends CommonRsp

  case class RecordFrameInfo(frame: Int, frameNum: Long, frameDuration: Long)
  val ErrorGetRecordFrame1 = ErrorRsp(300004, "error")
  val ErrorGetRecordFrame2 = ErrorRsp(300005, "error")

  final case class ESheepRePlayInfo(
                                     recordId: Long,
                                     roomId: Long,
                                     startTime: Long,
                                     endTime: Long,
                                     userCounts: Int,
                                     userList: Seq[(String, String)]
                                   )

  final case class GetReplyRecordRsp(
                               data: List[ESheepRePlayInfo],
                               errCode: Int = 0,
                               msg: String = "ok"
                               ) extends ESheepResponse
  val ErrorGetReplyRecord = GetReplyRecordRsp(Nil, 300001, "GetReplyRecordRsp in Service")
//
//  final case class ExistTime(
//                              startFrame: Long,
//                              endFrame: Long
//                            )
//  final case class ReplyPlayerInfo(
//                                    playerId: String,
//                                    nickname: String,
//                                    existTime: List[ExistTime]
//                                  )
//  final case class ReplyPlayerList(
//                                    totalFrame: Long,
//                                    playerList: List[ReplyPlayerInfo]
//                                  )
//  final case class GetReplayPlayerListRsp(
//                                        data: Option[ReplyPlayerList],
//                                        errCode: Int = 0,
//                                        msg: String = "ok"
//                                      ) extends ESheepResponse
//
//  final case class PlayerFrameInfo(
//                                    frame: Long,
//                                    frameNum: Long //录像总帧数
//                                  )
//  final case class GetRecordFrameRsp(
//                                      data: Option[PlayerFrameInfo],
//                                      errCode: Int = 0,
//                                      msg: String = "ok"
//                                    ) extends ESheepResponse
}
