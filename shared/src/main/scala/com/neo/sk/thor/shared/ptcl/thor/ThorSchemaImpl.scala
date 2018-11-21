package com.neo.sk.thor.shared.ptcl.thor
import com.neo.sk.thor.shared.ptcl.`object`.{AdventurerImpl, AdventurerState, Food}
import com.neo.sk.thor.shared.ptcl.config.ThorGameConfig
import com.neo.sk.thor.shared.ptcl.protocol.ThorGame.{GameEvent, UserActionEvent}

import scala.collection.mutable

/**
  * User: TangYaruo
  * Date: 2018/11/14
  * Time: 16:54
  */
class ThorSchemaImpl(
  override val config: ThorGameConfig,
  myId: String,
  myName: String
) extends ThorSchema with EsRecover {

  import scala.language.implicitConversions

  protected var aId: String = myId

  def changeAdventurerId(id: String) = aId = id

  override def debug(msg: String): Unit = {}

  override def info(msg: String): Unit = println(msg)

  private val esRecoverSupport:Boolean = true

  private val uncheckedActionMap = mutable.HashMap[Int,Long]() //serinum -> frame

  private var thorSchemaStateOpt: Option[ThorSchemaState] = None

  protected var waitSyncData:Boolean = true

  private val preExecuteFrameOffset = com.neo.sk.thor.shared.ptcl.model.Constants.preExecuteFrameOffset

  override protected implicit def adventurerState2Impl(adventurer: AdventurerState) = {
    new AdventurerImpl(config, adventurer)
  }

  def receiveGameEvent(e:GameEvent) = {
    if(e.frame >= systemFrame){
      addGameEvent(e)
    }else if(esRecoverSupport){
      println(s"rollback-frame=${e.frame},curFrame=${this.systemFrame},e=${e}")
      rollback4GameEvent(e)
    }
  }

  //接受服务器的用户事件
  def receiveUserEvent(e: UserActionEvent) = {
    if (e.playerId == aId) {
      uncheckedActionMap.get(e.serialNum) match {
        case Some(preFrame) =>
          if (e.frame != preFrame) {
            println(s"preFrame=$preFrame eventFrame=${e.frame} curFrame=$systemFrame")
            if (preFrame < e.frame && esRecoverSupport) {
              if (preFrame > systemFrame) {
                removePreEvent(preFrame, e.playerId, e.serialNum)
                addUserAction(e)
              } else if (e.frame >= systemFrame) {
                removePreEventHistory(preFrame, e.playerId, e.serialNum)
                rollback(preFrame)
                addUserAction(e)
              } else {
                removePreEventHistory(preFrame, e.playerId, e.serialNum)
                addUserActionHistory(e)
                rollback(preFrame)
              }
            }
          }
        case None =>
          if (e.frame >= systemFrame) {
            addUserAction(e)
          } else if (esRecoverSupport) {
            rollback4UserActionEvent(e)
          }
      }
    } else {
      if (e.frame > systemFrame) {
        addUserAction(e)
      } else if (esRecoverSupport) {
        println(s"rollback-frame=${e.frame},curFrame=${this.systemFrame},e=$e")
        rollback4GameEvent(e)
      }
    }

  }

  def preExecuteUserEvent(action: UserActionEvent) = {
    addUserAction(action)
    uncheckedActionMap.put(action.serialNum, action.frame)
  }

  final def addMyAction(action: UserActionEvent): Unit = {
    if (action.playerId == aId) {
      myAdventurerAction.get(action.frame - preExecuteFrameOffset) match {
        case Some(actionEvents) => myAdventurerAction.put(action.frame - preExecuteFrameOffset, action :: actionEvents)
        case None => myAdventurerAction.put(action.frame - preExecuteFrameOffset, List(action))
      }
    }
  }

  protected def handleThorSchemaState(thorSchemaSate: ThorSchemaState) = {
    val curFrame = systemFrame
    val startTime = System.currentTimeMillis()
    (systemFrame until thorSchemaSate.f).foreach { _ =>
      super.update()
      if (esRecoverSupport) addGameSnapshot(systemFrame, getThorSchemaState())
    }
    val endTime = System.currentTimeMillis()
    if (curFrame < thorSchemaSate.f) {
      println(s"handleThorSchemaState update to now use time=${endTime - startTime}")
    }
    systemFrame = thorSchemaSate.f
    quadTree.clear()
    adventurerMap.clear()
    if (!thorSchemaSate.isIncrement) foodMap.clear()
    thorSchemaSate.adventurer.foreach { a =>
      val adventurer = new AdventurerImpl(config, a)
      quadTree.insert(adventurer)
      adventurerMap.put(a.playerId, adventurer)
    }
    if (!thorSchemaSate.isIncrement) {
      thorSchemaSate.food.foreach { f =>
        val food = Food(f)
        quadTree.insert(food)
        foodMap.put(f.fId, food)
      }
    } else {
      thorSchemaSate.food.foreach { f =>
        val food = Food(f)
        foodMap.put(f.fId, food)
      }
      foodMap.values.foreach {f => quadTree.insert(f)}
    }

    waitSyncData = false
  }

  def receiveThorSchemaState(thorSchemaState: ThorSchemaState): Unit = {
    if (thorSchemaState.f > systemFrame) {
      thorSchemaStateOpt = Some(thorSchemaState)
    }else if (thorSchemaState.f == systemFrame) {
      info(s"收到同步数据，立即同步，curSystemFrame=$systemFrame, sync game container state frame=${thorSchemaState.f}")
      thorSchemaStateOpt = None
      handleThorSchemaState(thorSchemaState)
    } else {
      info(s"收到同步数据，但未同步，curSystemFrame=$systemFrame, sync game container state frame=${thorSchemaState.f}")
    }

  }

  override def update(): Unit = {
    if (thorSchemaStateOpt.nonEmpty) {
      val thorSchemaState = thorSchemaStateOpt.get
      info(s"立即同步所有数据，curSystemFrame=$systemFrame, sync game container state frame=${thorSchemaState.f}")
      handleThorSchemaState(thorSchemaState)
      thorSchemaStateOpt = None
      if (esRecoverSupport) {
        clearEsRecoverData()
        addGameSnapshot(systemFrame, this.getThorSchemaState())

      }
    } else {
      super.update()
      if (esRecoverSupport) addGameSnapshot(systemFrame, getThorSchemaState())
    }
  }


  override protected def clearEventWhenUpdate(): Unit = {
    if (esRecoverSupport) {
      addEventHistory(systemFrame, gameEventMap.getOrElse(systemFrame, Nil), actionEventMap.getOrElse(systemFrame, Nil))
    }
    gameEventMap -= systemFrame
    actionEventMap -= systemFrame
    systemFrame += 1
  }

  protected def rollbackUpdate(): Unit = {
    super.update()
    if (esRecoverSupport) addGameSnapshot(systemFrame, getThorSchemaState())
  }







}
