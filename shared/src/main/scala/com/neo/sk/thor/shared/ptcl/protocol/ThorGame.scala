package com.neo.sk.thor.shared.ptcl.protocol

import com.neo.sk.thor.shared.ptcl.model.Score
import com.neo.sk.thor.shared.ptcl.thor.{Adventurer, AdventurerState, GridState}


object ThorGame {

  sealed trait GameEvent{
    val frame:Long
  }

  trait UserEvent extends GameEvent

  trait UserActionEvent extends UserEvent{
    val userId: Long
    val timestamp: Long
  }

  //前端
  sealed trait WsMsgFrontSource
  case object CompleteMsgFrontServer extends WsMsgFrontSource
  case class FailMsgFrontServer(ex: Exception) extends WsMsgFrontSource

  sealed trait WsMsgFront extends WsMsgFrontSource

  //后台
  sealed trait WsMsgSource
  case object CompleteMsgServer extends WsMsgSource
  case class FailMsgServer(ex: Exception) extends WsMsgSource
  final case class Wrap(ws:Array[Byte],isKillMsg:Boolean = false) extends WsMsgSource

  sealed trait WsMsgServer extends WsMsgSource


  final case class UserInfo(uId: Long, name: String) extends WsMsgServer

  final case class UserEnterRoom(userId:Long, name:String, adventurer: AdventurerState, override val frame: Long = 0l) extends UserEvent with WsMsgServer

  final case class UserLeftRoom(userId:Long, name:String, override val frame: Long = 0l) extends UserEvent with WsMsgServer

  final case class BeAttacked(userId:Long, name:String, override val frame: Long = 0l) extends UserEvent with WsMsgServer

  final case class EatFood(userId:Long, foodId: Long, foodLevel: Int, override val frame: Long = 0l) extends UserEvent with WsMsgServer

  final case class MouseMove(userId:Long,d:Float, frame:Long, timestamp: Long) extends UserActionEvent with WsMsgFront with WsMsgServer

  final case class MouseClick(userId:Long, frame:Long, timestamp:Long) extends UserActionEvent with WsMsgFront with WsMsgServer

  final case class RestartGame(name:String) extends WsMsgFront

  final case class Ranks(currentRank: List[Score], historyRank: List[Score]) extends WsMsgServer

  final case class MouseMoveServer(userId: Long, frame: Long, action: MouseMove) extends WsMsgServer

  final case class MouseClickServer(userId: Long, frame: Long, action: MouseClick) extends WsMsgServer

  final case class GridSyncState(d:GridState) extends WsMsgServer


  sealed trait GameSnapshot

  final case class ThorSnapshot(
                                     state:GridState
                                   ) extends GameSnapshot

}
