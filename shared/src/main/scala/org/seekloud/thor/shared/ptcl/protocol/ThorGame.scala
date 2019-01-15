package org.seekloud.thor.shared.ptcl.protocol

import org.seekloud.thor.shared.ptcl.component.{AdventurerState, FoodState}
import org.seekloud.thor.shared.ptcl.config.{ThorGameConfig, ThorGameConfigImpl}
import org.seekloud.thor.shared.ptcl.model.Score
import org.seekloud.thor.shared.ptcl.thor.ThorSchemaState

import scala.collection.mutable


object ThorGame {

  sealed trait GameEvent {
    val frame: Int
  }

  sealed trait UserEvent extends GameEvent

  trait EnvironmentEvent extends GameEvent

  sealed trait UserActionEvent extends UserEvent {
    val playerId: Short
    val serialNum: Int
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

  final case class YourInfo(config: ThorGameConfigImpl, id: String, name: String, shortId: Short = 0, playerIdMap: List[(Short, String)] = Nil) extends WsMsgServer

  final case class UserMap(playerIdMap: List[(Short, String)] = Nil) extends WsMsgServer

  final case class UserEnterRoom(playerId: String, shortId: Short, name: String, adventurer: AdventurerState, override val frame: Int = 0) extends UserEvent with WsMsgServer

  final case class UserLeftRoom(playerId: String, shortId: Short, name: String, override val frame: Int = 0) extends UserEvent with WsMsgServer

  final case class BeAttacked(playerId: String, name: String, killerId: String, killerName: String, override val frame: Int = 0) extends UserEvent with WsMsgServer

  final case class EatFood(playerId: String, foodId: Int, foodLevel: Int, override val frame: Int = 0) extends UserEvent with WsMsgServer

  final case class MM(playerId: Short, offsetX: Short, offsetY: Short, override val frame: Int, override val serialNum: Int) extends UserActionEvent with WsMsgFront with WsMsgServer
//  final case class MouseMove(playerId: String, offsetX: Short, offsetY: Short, override val frame: Int, override val serialNum: Int) extends UserActionEvent with WsMsgFront with WsMsgServer

  final case class MouseClickDownLeft(playerId: Short, override val frame: Int, override val serialNum: Int) extends UserActionEvent with WsMsgFront with WsMsgServer

  final case class MouseClickDownRight(playerId: Short, override val frame: Int, override val serialNum: Int) extends UserActionEvent with WsMsgFront with WsMsgServer

  final case class MouseClickUpRight(playerId: Short, override val frame: Int, override val serialNum: Int) extends UserActionEvent with WsMsgFront with WsMsgServer

  final case object RebuildWebSocket extends WsMsgServer

  final case class WsMsgErrorRsp(errCode:Int, msg:String) extends WsMsgServer


  /*生成环境元素*/
  final case class GenerateFood(override val frame: Int, food: FoodState) extends EnvironmentEvent with WsMsgServer


  final case class RestartGame(name: String) extends WsMsgFront

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
