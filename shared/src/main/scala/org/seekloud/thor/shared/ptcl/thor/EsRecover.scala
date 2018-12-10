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

  private val gameEventHistoryMap = mutable.HashMap[Long, List[GameEvent]]()
  private val actionEventHistoryMap = mutable.HashMap[Long, List[UserActionEvent]]()
  private val gameSnapshotMap = mutable.HashMap[Long, ThorSchemaState]()

  def addEventHistory(frame: Long, gameEvents: List[GameEvent], actionEvents: List[UserActionEvent]): Unit = {
    gameEventHistoryMap.put(frame, gameEvents)
    actionEventHistoryMap.put(frame, actionEvents)
  }

  def addGameSnapshot(frame:Long,gameState:ThorSchemaState) = {
    gameSnapshotMap.put(frame,gameState)
  }

  def clearEsRecoverData():Unit = {
    gameEventHistoryMap.clear()
    actionEventHistoryMap.clear()
    gameSnapshotMap.clear()
  }

  def rollback(frame:Long) = {
    require(frame < this.systemFrame)

    gameSnapshotMap.get(frame) match {
      case Some(thorSchemaState) =>
        val startTime = System.currentTimeMillis()
        val curFrame = this.systemFrame
        handleThorSchemaState(thorSchemaState)
        //同步所有数据
        removeKillInfoByRollback(frame)
        (frame until curFrame).foreach{ f =>
          this.addGameEvents(f,gameEventHistoryMap.getOrElse(f,Nil),actionEventHistoryMap.getOrElse(f,Nil))
          this.rollbackUpdate()
        }
        val endTime = System.currentTimeMillis()
        println(s"roll back to frame=${frame},nowFrame=${curFrame} use Time:${endTime - startTime}")
      case None => println(s"there are not snapshot frame=${frame}")
    }
  }

  def rollback4GameEvent(e:GameEvent) = {
//    println(s"roll back to frame=${e.frame},nowFrame=${systemFrame} because event:${e}")
    gameEventHistoryMap.put(e.frame, e :: gameEventHistoryMap.getOrElse(e.frame, Nil))
    rollback(e.frame)
  }

  def rollback4UserActionEvent(e:UserActionEvent) = {
//    println(s"roll back to frame=${e.frame},nowFrame=${systemFrame} because event:${e}")
    actionEventHistoryMap.put(e.frame, e :: actionEventHistoryMap.getOrElse(e.frame, Nil))
    rollback(e.frame)
  }

  def removePreEventHistory(frame:Long, playerId:String, serialNum:Int):Unit = {
    actionEventHistoryMap.get(frame).foreach{ actions =>
      actionEventHistoryMap.put(frame,actions.filterNot(t => t.playerId == playerId && t.serialNum == serialNum))
    }
}

  def addUserActionHistory(e:UserActionEvent) = {
    actionEventHistoryMap.put(e.frame, e :: actionEventHistoryMap.getOrElse(e.frame, Nil))
  }



}
