package com.neo.sk.thor.shared.ptcl.thor
import com.neo.sk.thor.shared.ptcl.`object`.{AdventurerImpl, AdventurerState}
import com.neo.sk.thor.shared.ptcl.config.ThorGameConfig
import com.neo.sk.thor.shared.ptcl.protocol.ThorGame.{GameEvent, UserActionEvent}

import scala.collection.mutable

/**
  * User: TangYaruo
  * Date: 2018/11/14
  * Time: 16:54
  */
class ThorSchemaImpl(
  override implicit val config: ThorGameConfig,
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
//      rollback4GameEvent(e)
    }
  }

  //接受服务器的用户事件
  def receiveUserEvent(e: UserActionEvent) = {

  }

  def preExecuteUserEvent(action: UserActionEvent) = {

  }

  final def addMyAction(action: UserActionEvent) = {

  }

  protected def handleThorSchemaState(thorSchemaSate: ThorSchemaState) = {
    systemFrame = thorSchemaSate.f
    quadTree.clear()
    adventurerMap.clear()
    foodMap.clear()
    thorSchemaSate.adventurer.foreach { a =>
      val adventurer = new AdventurerImpl(config, a)
      quadTree.insert(adventurer)
      adventurerMap.put(a.playerId, adventurer)
    }
    thorSchemaSate.food.foreach { f =>
      //TODO complete
//      val food = new FoodImpl(config, f)
//      quadTree.insert(food)
//      foodMap.put(f.fId, food)
    }

    waitSyncData = false
  }




}
