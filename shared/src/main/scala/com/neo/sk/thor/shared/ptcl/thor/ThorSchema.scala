package com.neo.sk.thor.shared.ptcl.thor

import java.awt.event.KeyEvent
import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}

import com.neo.sk.thor.shared.ptcl.`object`.{Adventurer, AdventurerState, Food, FoodState}
import com.neo.sk.thor.shared.ptcl.model._
import com.neo.sk.thor.shared.ptcl.protocol.ThorGame.{GameEvent, UserActionEvent, UserEnterRoom}

import scala.collection.mutable

/**
  * Created by Jingyi on 2018/11/9
  */


case class ThorSchemaState(
  f: Long,
  adventurer: List[AdventurerState],
  food: List[FoodState],
  moveAction: List[(Int, List[Int])]
)

trait ThorSchema {


  val boundary: Point

  def debug(msg: String): Unit

  def info(msg: String): Unit

  var systemFrame: Long = 0L //系统帧数

  val adventurerMap = mutable.HashMap[Long, Adventurer]() // userId -> adventurer
  val foodMap = mutable.HashMap[Long, Food]() // foodId -> food

  protected val gameEventMap = mutable.HashMap[Long, List[GameEvent]]() //frame -> List[GameEvent] 待处理的事件 frame >= curFrame
  protected val actionEventMap = mutable.HashMap[Long, List[UserActionEvent]]() //frame -> List[UserActionEvent]


  //处理本帧加入的用户
  def handleUserJoin() = {
    gameEventMap.get(systemFrame).foreach {
      events =>
        events.filter(_.isInstanceOf[UserEnterRoom]).map(_.asInstanceOf[UserEnterRoom]).reverse.foreach {
          e =>
            adventurerMap.put(e.userId, e.adventurer.asInstanceOf[Adventurer])
        }
    }
  }

  //处理本帧离开的用户
  def handleUserLeft() = {

  }

  //处理本帧的动作信息
  def handleAction(): Unit = {

  }

  def addAction(id: Long, adventurerAction: UserActionEvent) = {

  }

  def addActionWithFrame(id: Long, adventurerAction: UserActionEvent, frame: Long) = {

  }

  def removeActionWithFrame(id: Long, adventurerAction: UserActionEvent, frame: Long) = {

  }


  def handleAdventurerMove(): Unit = {

  }

  def handleAdventurerAttacked() = {

  }

  def handleAdventurerEatFood() = {

  }

  def handleGenerateFood() = {

  }

  def update(): Unit = {
    handleAction()
    handleAdventurerMove()
    handleAdventurerAttacked()
    handleAdventurerEatFood()
    handleGenerateFood()
    systemFrame += 1
  }


  def getThorSchemaState(): ThorSchemaState = {
    ThorSchemaState(
      0L,
      Nil,
      Nil,
      Nil
    )
  }

}
