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

//import org.seekloud.thor.model.GameServerInfo

/**
  * User: XuSiRan
  * Date: 2018/12/4
  * Time: 12:03
  */
object ESheepProtocol {

  trait ESheepReq

  final case class JoinGameReq(
    gameId: Long,
    playerId: String
  ) extends ESheepReq

  final case class RefreshTokenReq(
    playerId: String
  ) extends ESheepReq

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

//  final case class WsPlayerInfoRsp(
//    data: ClientPlayerInfo,
//    errCode: Int = 0,
//    msg: String = "ok"
//  ) extends ESheepRsp

  sealed trait WsData

//  final case class Ws4AgentRsp(Ws4AgentRsp: WsPlayerInfoRsp) extends WsData

  final case class Ws4AgentRsp(
    data: ClientPlayerInfo,
    errCode: Int,
    msg: String,
  ) extends WsData

  final case object HeartBeat extends WsData


  /*邮箱登陆*/
  final case class LoginByMailReq(
    email: String,
    password: String
  ) extends ESheepReq

  final case class ESheepUserInfoRsp(
    userName: String,
    userId: Long,
    headImg: String,
    token: String,
    gender: Int,
    errCode: Int = 0,
    msg: String = "ok"
  ) extends ESheepRsp



  case class GameServerInfo(
    ip: String,
    port: Int,
    domain: String
  )


  final case class JoinGameInfo(
    accessCode: String,
    gsPrimaryInfo: GameServerInfo,
  )

  final case class ClientJoinGameRsp(
    data: JoinGameInfo,
    errCode: Int = 0,
    msg: String = "ok"
  ) extends ESheepRsp

  final case class TokenInfo(
    token: String,
    expireTime: Int
  )

  final case class RefreshTokenRsp(
    data: TokenInfo,
    errCode: Int = 0,
    msg: String = "ok"
  ) extends ESheepRsp

  final case class RoomList(roomList: List[Long])

  final case class GetRoomListRsp(
    data: RoomList,
    errCode: Int = 0,
    msg: String = "ok"
  ) extends ESheepRsp

  /*bot*/
  case class BotKeyReq(
    botId:String,
    botKey: String
  )

  case class BotKeyRes(
    data: BotInfo,
    errCode:Int=0,
    msg:String="ok"
  )

  case class BotInfo(
    botName:String,
    token:String,
    expireTime:Long
  )

}
