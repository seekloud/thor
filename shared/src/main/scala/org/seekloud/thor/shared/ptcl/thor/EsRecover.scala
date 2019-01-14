package org.seekloud.thor.shared.ptcl.thor

import org.seekloud.thor.shared.ptcl.protocol.ThorGame.{GameEvent, UserActionEvent}

import scala.collection.mutable

/**
  * User: TangYaruo
  * Date: 2018/11/12
  * Time: 16:04
  */
trait EsRecover {
  this: ThorSchemaImpl =>

  private val gameEventHistoryMap = mutable.HashMap[Int, List[GameEvent]]()
  private val actionEventHistoryMap = mutable.HashMap[Int, List[UserActionEvent]]()
  val gameSnapshotMap = mutable.HashMap[Int, ThorSchemaState]()
  var rollBackFrame: List[Int] = Nil

  def addEventHistory(frame: Int, gameEvents: List[GameEvent], actionEvents: List[UserActionEvent]): Unit = {
    gameEventHistoryMap.put(frame, gameEvents)
    actionEventHistoryMap.put(frame, actionEvents)
  }

  def addGameSnapshot(frame: Int, gameState: ThorSchemaState) = {
    gameSnapshotMap.put(frame, gameState)
  }

  def clearEsRecoverData(): Unit = {
    gameEventHistoryMap.clear()
    actionEventHistoryMap.clear()
    gameSnapshotMap.clear()
  }

  def rollback(frame: Int) = {
    require(frame < this.systemFrame)
    removeRollBackFrame(frame)

    gameSnapshotMap.get(frame) match {
      case Some(thorSchemaState) =>
        val startTime = System.currentTimeMillis()
        val curFrame = this.systemFrame
        handleThorSchemaState(thorSchemaState)
        //同步所有数据
        removeKillInfoByRollback(frame)
//        removeRollBackFrame(frame)
        (frame until curFrame).foreach { f =>
//          this.systemFrame = f
          this.addGameEvents(f, gameEventHistoryMap.getOrElse(f, Nil), actionEventHistoryMap.getOrElse(f, Nil))
          this.rollbackUpdate()
        }
        val endTime = System.currentTimeMillis()
        println(s"roll back to frame=$frame, nowFrame=$curFrame use Time:${endTime - startTime}")
      case None =>
        println(s"there are not snapshot frame=$frame")
//        removeRollBackFrame(frame)
        this.rollbackUpdate()
    }
  }

  def rollback4GameEvent(e: GameEvent) = {
    //    println(s"roll back to frame=${e.frame},nowFrame=${systemFrame} because event:${e}")
    gameEventHistoryMap.put(e.frame, e :: gameEventHistoryMap.getOrElse(e.frame, Nil))
//    rollback(e.frame)
    addRollBackFrame(e.frame)
  }

  def rollback4UserActionEvent(e: UserActionEvent) = {
    //    println(s"roll back to frame=${e.frame},nowFrame=${systemFrame} because event:${e}")
    actionEventHistoryMap.put(e.frame, e :: actionEventHistoryMap.getOrElse(e.frame, Nil))
//    rollback(e.frame)
    addRollBackFrame(e.frame)
  }

  def addRollBackFrame(frame: Int) = {
    if (!rollBackFrame.contains(frame)) rollBackFrame = frame :: rollBackFrame
  }

  def removeRollBackFrame(frame: Int) = {
    if (rollBackFrame.contains(frame)) rollBackFrame = rollBackFrame.filterNot(_ == frame)
  }

  def removePreEventHistory(frame: Int, playerId: String, serialNum: Int): Unit = {
    actionEventHistoryMap.get(frame).foreach { actions =>
      actionEventHistoryMap.put(frame, actions.filterNot(t => t.playerId == playerId && t.serialNum == serialNum))
    }
  }

  def addUserActionHistory(e: UserActionEvent) = {
    actionEventHistoryMap.put(e.frame, e :: actionEventHistoryMap.getOrElse(e.frame, Nil))
  }


}
