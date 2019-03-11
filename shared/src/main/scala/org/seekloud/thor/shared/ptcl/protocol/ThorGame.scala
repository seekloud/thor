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

package org.seekloud.thor.shared.ptcl.protocol

import org.seekloud.thor.shared.ptcl.component.{AdventurerState, FoodState}
import org.seekloud.thor.shared.ptcl.config.{ThorGameConfig, ThorGameConfigImpl}
import org.seekloud.thor.shared.ptcl.model.{Point, Score}
import org.seekloud.thor.shared.ptcl.thor.ThorSchemaState

import scala.collection.mutable


object ThorGame {

  sealed trait GameEvent {
    val frame: Int
  }

  sealed trait UserEvent extends GameEvent

  trait EnvironmentEvent extends GameEvent

  sealed trait UserActionEvent extends UserEvent {
    val playerId: Byte
    val serialNum: Byte
  }

  final case class WsData(ws:Array[Byte]) extends WsMsgSource


  //前端
  sealed trait WsMsgFrontSource

  case object CompleteMsgFrontServer extends WsMsgFrontSource

  case class FailMsgFrontServer(ex: Exception) extends WsMsgFrontSource

  sealed trait WsMsgFront extends WsMsgFrontSource

  //后台
  sealed trait WsMsgSource

  case object CompleteMsgServer extends WsMsgSource

  case class FailMsgServer(ex: Exception) extends WsMsgSource

  final case class Wrap(ws: Array[Byte], isKillMsg: Boolean = false, deadId: String = "") extends WsMsgSource

  final case class PingPackage(sendTime:Long) extends WsMsgServer with WsMsgFront

  sealed trait WsMsgServer extends WsMsgSource


  final case class UserInfo(playerId: String, name: String) extends WsMsgServer

  final case class YourInfo(config: ThorGameConfigImpl, id: String, name: String, shortId: Byte = 0, playerIdMap: List[(Byte, (String, String))] = Nil) extends WsMsgServer

  final case object RestartYourInfo extends WsMsgServer

  final case class UserMap(playerIdMap: List[(Byte, (String, String))] = Nil) extends WsMsgServer

  final case object UserMapReq extends WsMsgFront

  final case class UserEnterRoom(playerId: String, shortId: Byte, name: String, adventurer: AdventurerState, override val frame: Int = 0) extends UserEvent with WsMsgServer

  final case class UserLeftRoom(playerId: String, shortId: Byte, name: String, override val frame: Int = 0) extends UserEvent with WsMsgServer

  final case class BeAttacked(playerId: String, name: String, killerId: String, killerName: String, override val frame: Int = 0) extends UserEvent with WsMsgServer

  final case class EatFood(playerId: String, foodId: Int, foodLevel: Int, override val frame: Int = 0) extends UserEvent with WsMsgServer

  final case class MM(playerId: Byte, offsetX: Short, offsetY: Short, override val frame: Int, override val serialNum: Byte) extends UserActionEvent with WsMsgFront with WsMsgServer
//  final case class MouseMove(playerId: String, offsetX: Short, offsetY: Short, override val frame: Int, override val serialNum: Int) extends UserActionEvent with WsMsgFront with WsMsgServer

  final case class MouseClickDownLeft(playerId: Byte, override val frame: Int, override val serialNum: Byte) extends UserActionEvent with WsMsgFront with WsMsgServer

  final case class MouseClickDownRight(playerId: Byte, override val frame: Int, override val serialNum: Byte) extends UserActionEvent with WsMsgFront with WsMsgServer

  final case class MouseClickUpRight(playerId: Byte, override val frame: Int, override val serialNum: Byte) extends UserActionEvent with WsMsgFront with WsMsgServer

  final case object RebuildWebSocket extends WsMsgServer

  final case class WsMsgErrorRsp(errCode:Int, msg:String) extends WsMsgServer


  /*Game Agent*/
  sealed trait GaUserAction extends WsMsgFront

  final case class GAStartGame(roomId: Long) extends GaUserAction

  final case class GACreateRoom(pswOpt: Option[String] = None) extends GaUserAction


  final case class JoinRoomFail(error: String) extends WsMsgServer

  final case class CreateRoomRsp(roomId: Long) extends WsMsgServer

//  final case class CreateRoomFail(error: String) extends WsMsgServer




  /*生成环境元素*/
  final case class GenerateFood(override val frame: Int, food: FoodState) extends EnvironmentEvent with WsMsgServer

  final case class BodyToFood(override val frame: Int,  startP: Point, foods: List[FoodState]) extends EnvironmentEvent with WsMsgServer

  final case object RestartGame extends WsMsgFront

  final case class Ranks(currentRank: List[Score]) extends WsMsgServer

  final case class GridSyncState(d: ThorSchemaState) extends WsMsgServer


  sealed trait GameSnapshot

  final case class ThorSnapshot(
    state: ThorSchemaState
  ) extends GameSnapshot

  final case class GameInformation(
    gameStartTime: Long,
    thorGameConfig: ThorGameConfigImpl
  )

  /* replay-frame-msg*/
  final case class ReplayFrameData(ws:Array[Byte]) extends WsMsgSource
  final case class InitReplayError(msg:String) extends WsMsgServer
  final case class ReplayFinish() extends WsMsgServer
  final case object StartReplay extends WsMsgServer

  /*replay in front*/
//  final case class ReplayInfo(playerId: String, name: String, f: Long, config: ThorGameConfigImpl) extends WsMsgServer

  final case class EventData(list: List[WsMsgServer]) extends WsMsgServer

  final case class DecodeError() extends WsMsgServer

  final case class TextMsg(m: String) extends WsMsgServer




  //解析url
  final case class ThorGameInfo(
                             name: String,
                             pId: Option[String] = None,
                             rId: Option[Long] = None,
                             userAccessCode: Option[String] = None,
                             frame: Option[Long] = None,
                             recId: Option[Long] = None
                           )

}
