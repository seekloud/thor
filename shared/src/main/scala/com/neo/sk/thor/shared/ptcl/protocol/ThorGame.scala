package com.neo.sk.thor.shared.ptcl.protocol

import com.neo.sk.thor.shared.ptcl.`object`.AdventurerState
import com.neo.sk.thor.shared.ptcl.model.Score
import com.neo.sk.thor.shared.ptcl.thor.ThorSchemaState


object ThorGame {

  sealed trait GameEvent {
    val frame: Long
  }

  trait UserEvent extends GameEvent

  trait UserActionEvent extends UserEvent {
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

  sealed trait WsMsgServer extends WsMsgSource


  final case class UserInfo(uId: Long) extends WsMsgServer

  final case class UserEnterRoom(userId: Long, name: String, adventurer: AdventurerState, override val frame: Long = 0l) extends UserEvent with WsMsgServer

  final case class UserLeftRoom(userId: Long, name: String, override val frame: Long = 0l) extends UserEvent with WsMsgServer

  final case class BeAttacked(userId: Long, name: String, override val frame: Long = 0l) extends UserEvent with WsMsgServer

  final case class EatFood(userId: Long, foodId: Long, foodLevel: Int, override val frame: Long = 0l) extends UserEvent with WsMsgServer

  final case class MouseMove(userId: Long, d: Float, frame: Long, timestamp: Long) extends UserActionEvent with WsMsgFront with WsMsgServer

  final case class MouseClick(userId: Long, frame: Long, timestamp: Long) extends UserActionEvent with WsMsgFront with WsMsgServer

  final case class RestartGame(name: String) extends WsMsgFront

  final case class Ranks(currentRank: List[Score], historyRank: List[Score]) extends WsMsgServer

  final case class GridSyncState(d: ThorSchemaState) extends WsMsgServer


  sealed trait GameSnapshot

  final case class ThorSnapshot(
    state: ThorSchemaState
  ) extends GameSnapshot

}
