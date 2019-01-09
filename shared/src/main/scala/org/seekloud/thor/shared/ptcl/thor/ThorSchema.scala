package org.seekloud.thor.shared.ptcl.thor

import java.awt.event.KeyEvent
import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}

import org.seekloud.thor.shared.ptcl.component.{Adventurer, AdventurerState, Food, FoodState}
import org.seekloud.thor.shared.ptcl.model._
import org.seekloud.thor.shared.ptcl.config.ThorGameConfig
import org.seekloud.thor.shared.ptcl.protocol.ThorGame._
import org.seekloud.thor.shared.ptcl.util.QuadTree

import scala.collection.mutable

/**
  * User: TangYaruo
  * Date: 2018/11/12
  * Time: 16:30
  */
case class ThorSchemaState(
  f: Int,
  adventurer: List[AdventurerState],
  food: List[FoodState],
  isIncrement: Boolean = false
)

trait ThorSchema extends KillInformation {

  import scala.language.implicitConversions

  def debug(msg: String): Unit

  def info(msg: String): Unit

  implicit val config: ThorGameConfig

  val boundary: Point = config.boundary

  var playerId = ""

  var systemFrame: Int = 0 //系统帧数

  /*元素*/
  var adventurerMap = mutable.HashMap[String, Adventurer]() // playerId -> adventurer
  val tmpAdventurerMap = mutable.HashMap[String, Adventurer]() // playerId -> adventurer
  var foodMap = mutable.HashMap[Int, Food]() // foodId -> food
  val tmpFoodMap = mutable.HashMap[Int, Food]() // foodId -> food

  /*事件*/
  protected val gameEventMap = mutable.HashMap[Int, List[GameEvent]]() //frame -> List[GameEvent] 待处理的事件 frame >= curFrame
  protected val actionEventMap = mutable.HashMap[Int, List[UserActionEvent]]() //frame -> List[UserActionEvent]
  protected val myAdventurerAction = mutable.HashMap[Int, List[UserActionEvent]]()

  protected val attackingAdventureMap = mutable.HashMap[String, Int]() // id -> 程度
  protected var MaybeAttackingAdventureList = List[(String, String, Int)]() // id, killerId, 所在象限
  //playerId -> 攻击执行程度
  val dyingAdventurerMap = mutable.HashMap[String, (Adventurer, Int)]() //playerId -> (adventurer, 死亡执行程度)

  /*排行榜*/
  var currentRankList = List.empty[Score]
  var historyRankMap = Map.empty[String, Score]
  var historyRank = historyRankMap.values.toList.sortBy(_.e).reverse
  var historyRankThreshold = if (historyRank.isEmpty) -1 else historyRank.map(_.e).min
  val historyRankLength = 5

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
  def handleUserEnterRoomNow(): Unit = {
    gameEventMap.get(systemFrame).foreach {
      events =>
        handleUserEnterRoomEvent(events.filter(_.isInstanceOf[UserEnterRoom]).map(_.asInstanceOf[UserEnterRoom]).reverse)
    }
  }

  //处理本帧离开的用户
  protected final def handleUserLeftRoom(e: UserLeftRoom): Unit = {
//    println(s"[[[${e.playerId} left room]]]")
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
  protected final def handleUserActionEvent(actions: List[UserActionEvent]) = {
    /**
      * 用户行为事件
      **/
    actions.sortBy(_.serialNum).foreach { action =>
      adventurerMap.get(action.playerId) match {
        case Some(adventurer) =>
          action match {
            case a: MouseMove =>
              adventurer.setMoveDirection(a.direction, a.mouseDistance, attackingAdventureMap.contains(a.playerId))
              adventurer.setFaceDirection(a.direction)
            case a: MouseClickDownLeft =>
              attackingAdventureMap.get(a.playerId) match {
                case Some(_) => ()
                case None =>
                  attackingAdventureMap.put(a.playerId, 3) //动画持续帧数 现在是3
                  adventurerMap.filter(_._1 == a.playerId).values.foreach {
                    adventurer =>
                      adventurer.isMove = false
                  }
              }
            case a: MouseClickDownRight => adventurer.speedUp(config)
            case a: MouseClickUpRight => adventurer.cancleSpeedUp(config)
          }
        case None =>
          info(s"adventurer [${action.playerId}] action $action is invalid, because the adventurer doesn't exist.")

      }
    }
  }

  protected def adventurerAttackedCallback(killer: Adventurer)(adventurer: Adventurer): Unit = {
    //重写，后台dispatch
//    val event = BeAttacked(adventurer.playerId, adventurer.name, killer.playerId, killer.name, systemFrame)
//    addGameEvent(event)
  }


  final protected def handleUserActionEventNow(): Unit = {
    actionEventMap.get(systemFrame).foreach { actionEvents =>
      handleUserActionEvent(actionEvents.reverse)
    }
  }

  protected final def handleMyAction(actions: List[UserActionEvent]) = { //TODO 处理出现错误的帧


  }

  final protected def handleMyActionNow() = {
    handleMyAction(myAdventurerAction.getOrElse(systemFrame, Nil).reverse)
    myAdventurerAction.remove(systemFrame - 10)
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

  protected def addGameEvents(frame: Int, events: List[GameEvent], actionEvents: List[UserActionEvent]): Unit = {
    gameEventMap.put(frame, events)
    actionEventMap.put(frame, actionEvents)
  }

  def removePreEvent(frame: Int, playerId: String, serialNum: Int): Unit = {
    actionEventMap.get(frame).foreach { actions =>
      actionEventMap.put(frame, actions.filterNot(t => t.playerId == playerId && t.serialNum == serialNum))
    }
  }


  //  def handleAdventurerMove(): Unit = {
  //
  //
  //
  //  }

  //杀死高速从刀下穿过的人
  protected final def adventurerMaybeAttackedCallback(killer: Adventurer)(adventurer: Adventurer, page: Int): Unit ={
    MaybeAttackingAdventureList.foreach{ adventurerLi =>
      if(adventurerLi._1 == adventurer.playerId && adventurerLi._2 == killer.playerId && adventurerLi._3 < page){
        MaybeAttackingAdventureList = MaybeAttackingAdventureList.filterNot(_._1 == adventurer.playerId)
        adventurerAttackedCallback(killer)(adventurer)
      }
      else{
        MaybeAttackingAdventureList = (adventurer.playerId, killer.playerId, page) :: MaybeAttackingAdventureList
      }
    }
  }

  protected final def handleAdventurerAttackingNow(): Unit = {
    attackingAdventureMap.foreach { attacking =>
      adventurerMap.filter(_._1 == attacking._1).values.foreach { adventurer =>
        val adventurerMaybeAttacked = adventurerMap.filter(a => a._1 != adventurer.playerId && a._2.position.distance(adventurer.position) < adventurer.radius + config.getWeaponLengthByLevel(adventurer.level) + a._2.radius).values
//        println(s"潜在攻击列表${adventurerMaybeAttacked.map(_.name)}")
        adventurerMaybeAttacked.foreach(p => adventurer.checkAttacked(p, attacking._2, adventurerAttackedCallback(killer = adventurer), adventurerMaybeAttackedCallback(killer = adventurer))(config))
      }

      if (attacking._2 <= 0) {
        adventurerMap.filter(_._1 == attacking._1).values.foreach {
          adventurer =>
            if (!adventurer.mouseStop) {
              adventurer.isMove = true
            }
        }
        attackingAdventureMap.remove(attacking._1)
        MaybeAttackingAdventureList = MaybeAttackingAdventureList.filterNot(_._2 == attacking._1)
      }
      else attackingAdventureMap.update(attacking._1, attacking._2 - 1)
    }
  }

  final protected def handleAdventurerDyingNow(): Unit = {
    dyingAdventurerMap.foreach { dying =>
      if (dying._2._2 <= 0) {
        println(s"remove adventurer: ${dying._1}")
        dyingAdventurerMap.remove(dying._1)
//        adventurerMap.remove(dying._1)
      } else {
//        println(s"${dying._1} dying...")
        dyingAdventurerMap.update(dying._1, (dying._2._1, dying._2._2 - 1))
      }
    }
  }

  protected def handleAdventurerAttacked(e: BeAttacked): Unit = {
    val killerOpt = adventurerMap.get(e.killerId)
    if(killerOpt.nonEmpty){
      adventurerMap.get(e.playerId).foreach { adventurer =>
        //      println(s"handle ${e.playerId} attacked")
        killerOpt.foreach(_.killNum += 1)
        quadTree.remove(adventurer)
        adventurerMap.remove(adventurer.playerId)
        dyingAdventurerMap.put(adventurer.playerId, (adventurer, config.getAdventurerDyingAnimation))
        addKillInfo(e.killerName, adventurer.name)
      }
    }
  }


  protected final def handleAdventurerAttacked(es: List[BeAttacked]): Unit = {
    es.sortBy{ event =>
      adventurerMap.find(_._1 == event.playerId) match{
        case None => 100
        case Some(a) => a._2.level
      }
    } foreach handleAdventurerAttacked
  }

  final protected def handleAdventurerAttackedNow(): Unit = {
    gameEventMap.get(systemFrame).foreach { events =>
      handleAdventurerAttacked(events.filter(_.isInstanceOf[BeAttacked]).map(_.asInstanceOf[BeAttacked]).reverse)
    }
  }

  protected def handleAdventurerEatFood(e: EatFood): Unit = {
    foodMap.get(e.foodId).foreach { food =>
      quadTree.remove(food)
      adventurerMap.get(e.playerId).foreach(_.eatFood(food))
      foodMap.remove(e.foodId)
    }
  }

  protected def handleAdventurerEatFood(es: List[EatFood]): Unit = {
    es foreach handleAdventurerEatFood
  }

  final protected def handleAdventurerEatFoodNow(): Unit = {
    //判断是否吃到食物，吃到事件添加到当前systemFrame的gameEventMap之中
    adventurerMap.values.foreach { adventurer =>
      val adventurerMaybeEatFood = quadTree.retrieveFilter(adventurer).filter(_.isInstanceOf[Food]).map(_.asInstanceOf[Food])
      adventurerMaybeEatFood.foreach(adventurer.checkEatFood(_, adventurerEatFoodCallback(adventurer)))
    }
    //以上判断判断可以放在adventurer移动中以提前1帧处理
    gameEventMap.get(systemFrame).foreach { events =>
      handleAdventurerEatFood(events.filter(_.isInstanceOf[EatFood]).map(_.asInstanceOf[EatFood]).reverse)
    }
  }

  //后台单独重写
  protected def adventurerEatFoodCallback(adventurer: Adventurer)(food: Food): Unit = {
//    val event = EatFood(adventurer.playerId, food.fId, food.level, systemFrame)
//    addGameEvent(event)
  }

  protected def handleGenerateFood(e: GenerateFood): Unit = {
    val food = Food(e.food)
    foodMap.put(food.fId, food)
    quadTree.insert(food)
  }

  final protected def handleGenerateFood(es: List[GenerateFood]): Unit = {
    es foreach handleGenerateFood
  }

  final protected def handleGenerateFoodNow(): Unit = {
    gameEventMap.get(systemFrame).foreach { events =>
      handleGenerateFood(events.filter(_.isInstanceOf[GenerateFood]).map(_.asInstanceOf[GenerateFood]).reverse)
    }
  }

  protected def clearEventWhenUpdate(): Unit = {}

  def getThorSchemaState(): ThorSchemaState = {
    ThorSchemaState(
      systemFrame,
      adventurerMap.values.map(_.getAdventurerState).toList,
      foodMap.values.map(_.getFoodState).toList,
    )
  }


  protected def adventurerMove(): Unit = {
    adventurerMap.values.foreach { adventurer =>
      val adRadius = config.getAdventurerRadiusByLevel(adventurer.level)
      val intersectNum = adventurerMap.filterNot(_._1 == adventurer.playerId).values.foldLeft(0) {
        (sum, otherAd) =>
          val oAdRadius = config.getAdventurerRadiusByLevel(otherAd.level)
          val distance = adventurer.position.distance(otherAd.position)
          if (distance <= adRadius + oAdRadius) {
            val relativeTheta = otherAd.position.getTheta(adventurer.position)
            val theta = (math.Pi / 2) - math.acos(oAdRadius / (oAdRadius + adRadius))
            val thetaB = relativeTheta - theta
            val thetaC = relativeTheta + theta

//            println(s"主体[${adventurer.name}]角度[${adventurer.direction}]], 相对角度[$relativeTheta], 偏移[$theta]")

            if (math.max(thetaB, thetaC) <= 0 || math.min(theta, thetaC) >= 0) {
              if (adventurer.direction >= math.min(thetaB, thetaC) && adventurer.direction <= math.max(thetaB, thetaC)) {
                adventurer.isIntersect = 1
              } else {
                adventurer.isIntersect = 0
              }
            } else {
              if ((adventurer.direction >= - math.Pi && adventurer.direction <= math.min(thetaB, thetaC)) || (adventurer.direction >= math.max(theta, thetaC) && adventurer.direction <= math.Pi)) {
                adventurer.isIntersect = 1
              } else {
                adventurer.isIntersect = 0
              }
            }
            sum + 1
          } else sum
      }
      if (intersectNum == 0) {
        adventurer.isIntersect = 0
      }
      adventurer.move(boundary, quadTree)
      if (adventurer.isUpdateLevel) adventurer.updateLevel
    }
  }

  def leftGame(userId: String, name: String) = {
    val event = UserLeftRoom(userId, name, systemFrame)
    addGameEvent(event)
    //    dispatch(event)
  }

  def update(): Unit = {
    adventurerMove()
    handleUserLeftRoomNow()
    handleUserActionEventNow()

    if (org.seekloud.thor.shared.ptcl.model.Constants.fakeRender) {
      handleMyActionNow()
    }

    //    handleAdventurerMove()
    handleAdventurerAttackingNow()
    handleAdventurerAttackedNow()
    handleAdventurerDyingNow()
    handleAdventurerEatFoodNow()
    handleGenerateFoodNow()
    handleUserEnterRoomNow()


    quadTree.refresh(quadTree)
    updateKillInformation()
    clearEventWhenUpdate()
  }


}