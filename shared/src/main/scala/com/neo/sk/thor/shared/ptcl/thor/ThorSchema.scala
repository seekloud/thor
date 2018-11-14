package com.neo.sk.thor.shared.ptcl.thor

import java.awt.event.KeyEvent
import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}

import com.neo.sk.thor.shared.ptcl.`object`.{Adventurer, AdventurerState, Food, FoodState}
import com.neo.sk.thor.shared.ptcl.model._
import com.neo.sk.thor.shared.ptcl.config.ThorGameConfig
import com.neo.sk.thor.shared.ptcl.protocol.ThorGame._
import com.neo.sk.thor.shared.ptcl.util.QuadTree

import scala.collection.mutable

/**
  * User: TangYaruo
  * Date: 2018/11/12
  * Time: 16:30
  */
case class ThorSchemaState(
  f: Long,
  adventurer: List[AdventurerState],
  food: List[FoodState],
  moveAction: List[(Int, List[Int])]
)

trait ThorSchema {

  import scala.language.implicitConversions

  def debug(msg: String): Unit

  def info(msg: String): Unit

//  implicit val config: ThorGameConfig

  val boundary: Point

  var playerId = ""

  var systemFrame: Long = 0L //系统帧数

  val adventurerMap = mutable.HashMap[String, Adventurer]() // playerId -> adventurer
  val foodMap = mutable.HashMap[Long, Food]() // foodId -> food

  protected val gameEventMap = mutable.HashMap[Long, List[GameEvent]]() //frame -> List[GameEvent] 待处理的事件 frame >= curFrame
  protected val actionEventMap = mutable.HashMap[Long, List[UserActionEvent]]() //frame -> List[UserActionEvent]

  protected val quadTree: QuadTree = new QuadTree(Rectangle(Point(0, 0), boundary))

  final protected def getPlayer(id: String): Unit = {
    playerId = id
  }

  final protected def handleUserEnterRoomEvent(l: List[UserEnterRoom]): Unit = {
    l foreach handleUserEnterRoomEvent
  }

  protected implicit def adventurerState2Impl(adventurer: AdventurerState): Adventurer

  final protected def handleUserEnterRoomEvent(e: UserEnterRoom): Unit = {
    println(s"user [${e.playerId}] enter room")
    val adventurer: Adventurer = e.adventurer
    adventurerMap.put(e.adventurer.playerId, adventurer)
    quadTree.insert(adventurer)
  }

  //处理本帧加入的用户
  protected def handleUserEnterRoomNow(): Unit = {
    gameEventMap.get(systemFrame).foreach {
      events =>
        handleUserEnterRoomEvent(events.filter(_.isInstanceOf[UserEnterRoom]).map(_.asInstanceOf[UserEnterRoom]).reverse)
      //        events.filter(_.isInstanceOf[UserEnterRoom]).map(_.asInstanceOf[UserEnterRoom]).reverse.foreach {
      //          e =>
      //            adventurerMap.put(e.playerId, e.adventurer.asInstanceOf[Adventurer])
      //        }
    }
  }

  //处理本帧离开的用户
  final protected def handleUserLeftRoom(e: UserLeftRoom): Unit = {
    adventurerMap.get(e.playerId).foreach(quadTree.remove)
    adventurerMap.remove(e.playerId)
  }

  final protected def handleUserLeftRoom(l: List[UserLeftRoom]): Unit = {
    l foreach handleUserLeftRoom
  }

  final protected def handleUserLeftRoomNow(): Unit = {
    gameEventMap.get(systemFrame).foreach { events =>
      handleUserLeftRoom(events.filter(_.isInstanceOf[UserLeftRoom]).map(_.asInstanceOf[UserLeftRoom]).reverse)
    }
  }


  //处理本帧的动作信息
  def handleUserActionEvent(actions: List[UserActionEvent]) = {
    /**
      * 用户行为事件
      **/
    actions.sortBy(_.serialNum).foreach { action =>
      adventurerMap.get(action.playerId) match {
        case Some(adventurer) =>
          action match {
            case a: MouseMove =>
            //TODO
            case a: MouseClick =>
            //TODO
          }
        case None =>
          info(s"adventurer [${action.playerId}] action $action is invalid, because the adventurer doesn't exist.")

      }
    }

  }


  final protected def handleUserActionEventNow(): Unit = {
    actionEventMap.get(systemFrame).foreach { actionEvents =>
      handleUserActionEvent(actionEvents.reverse)
    }
  }


  protected final def addUserAction(action: UserActionEvent): Unit = {
    actionEventMap.get(action.frame) match {
      case Some(actionEvents) => actionEventMap.put(action.frame, action :: actionEvents)
      case None => actionEventMap.put(action.frame, List(action))
    }
  }

  protected final def addGameEvent(event: GameEvent): Unit = {
    gameEventMap.get(event.frame) match {
      case Some(events) => gameEventMap.put(event.frame, event :: events)
      case None => gameEventMap.put(event.frame, List(event))
    }
  }

  protected def addGameEvents(frame: Long, events: List[GameEvent], actionEvents: List[UserActionEvent]): Unit = {
    gameEventMap.put(frame, events)
    actionEventMap.put(frame, actionEvents)
  }

  def removePreEvent(frame: Long, playerId: String, serialNum: Int): Unit = {
    actionEventMap.get(frame).foreach { actions =>
      actionEventMap.put(frame, actions.filterNot(t => t.playerId == playerId && t.serialNum == serialNum))
    }
  }


//  def handleAdventurerMove(): Unit = {
//
//
//
//  }

  protected final def handleAdventurerAttacked(e: BeAttacked): Unit = {
    val killerOpt = adventurerMap.get(e.killerId)
    adventurerMap.get(e.playerId).foreach { adventurer =>
      killerOpt.foreach(_.killNum += 1)
      quadTree.remove(adventurer)
      adventurerMap.remove(adventurer.playerId)
      //TODO 击杀信息
    }
  }


  protected final def handleAdventurerAttacked(es: List[BeAttacked]): Unit = {
    es foreach handleAdventurerAttacked
  }

  final protected def handleAdventurerAttackedNow(): Unit = {
    gameEventMap.get(systemFrame).foreach { events =>
      handleAdventurerAttacked(events.filter(_.isInstanceOf[BeAttacked]).map(_.asInstanceOf[BeAttacked]).reverse)
    }
  }

  def handleAdventurerEatFood(e: EatFood): Unit = {
    //吃到食物后移除食物，人物提升能量，判断升级？
    foodMap.get(e.foodId).foreach { food =>
      quadTree.remove(food)
      adventurerMap.get(e.playerId).foreach{ adventurer =>
        adventurer.energy += food.foodLevel//根据食物获取能量，数值未定义,未来可能调用adventurer方法
        //TODO 升级操作
      }
      foodMap.remove(e.foodId)
    }
  }

  protected def handleAdventurerEatFood(es: List[EatFood]): Unit = {
    es foreach handleAdventurerEatFood
  }

  final protected def handleAdventurerEatFoodNow(): Unit = {
    //判断是否吃到食物，吃到事件添加到当前systemFrame的gameEventMap之中
    adventurerMap.values.foreach{ adventurer =>
      val adventurerMaybeEatFood = quadTree.retrieveFilter(adventurer).filter(_.isInstanceOf[Food]).map(_.asInstanceOf[Food])
      adventurerMaybeEatFood.foreach(adventurer.checkEatFood(_,adventurerEatFoodCallback(adventurer)))
    }
    //以上判断判断可以放在adventurer移动中以提前1帧处理
    gameEventMap.get(systemFrame).foreach { events =>
      handleAdventurerEatFood(events.filter(_.isInstanceOf[EatFood]).map(_.asInstanceOf[EatFood]).reverse)
    }
  }

  protected def adventurerEatFoodCallback(adventurer: Adventurer)(food: Food):Unit = {
    //在这里添加gameEventMap;区别于tank后台重写添加，客户端不执行操作.
    val event = EatFood(adventurer.playerId, food.foodId, food.foodLevel, systemFrame)
    addGameEvent(event)
    //TODO dispatch ?
  }

  def handleGenerateFood() = {
    //地图生成食物
    gameEventMap.get(systemFrame).foreach{events =>
      events.filter(_.isInstanceOf[GenerateFood]).map(_.asInstanceOf[GenerateFood]).foreach{ event =>
        val food = Food(event.foodState)
        foodMap.put(food.foodId, food)
        quadTree.insert(food)
      }
    }
  }

  def generateFood(id: Long, level: Int = 1, position: Point, radius: Float = 2) ={
    //生成食物事件，被后台定时事件调用，食物的属性暂且全部作为参数
    val foodState = FoodState(id, level, position, radius)
    val event = GenerateFood(systemFrame, foodState)
    addGameEvent(event)
    //TODO dispatch ?
  }

  protected def clearEventWhenUpdate(): Unit = {
    gameEventMap -= systemFrame
    actionEventMap -= systemFrame
    systemFrame += 1
  }

  //TODO
  def getThorSchemaState(): ThorSchemaState = {
    ThorSchemaState(
      0L,
      Nil,
      Nil,
      Nil
    )
  }

  def update(): Unit = {
    handleUserLeftRoomNow()
    handleUserActionEventNow()

//    handleAdventurerMove()
    handleAdventurerAttackedNow()
    handleAdventurerEatFoodNow()
    handleGenerateFood()
    handleUserEnterRoomNow()


    quadTree.refresh(quadTree)
    clearEventWhenUpdate()
  }


}
