package com.neo.sk.thor.core.game

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.TimerScheduler
import com.neo.sk.thor.core.thor.AdventurerServer
import com.neo.sk.thor.core.{RoomActor, UserActor}
import com.neo.sk.thor.shared.ptcl.`object`._
import org.slf4j.Logger
import com.neo.sk.thor.shared.ptcl.config.ThorGameConfig
import com.neo.sk.thor.shared.ptcl.model._
import com.neo.sk.thor.shared.ptcl.protocol.ThorGame._
import com.neo.sk.thor.shared.ptcl.thor.ThorSchema

import scala.collection.mutable

/**
  * User: XuSiRan
  * Date: 2018/11/15
  * Time: 11:37
  */
case class ThorSchemaServerImpl (
                             override implicit val config: ThorGameConfig,
                             roomActorRef:ActorRef[RoomActor.Command],
                             timer:TimerScheduler[RoomActor.Command],
                             log:Logger,
                             dispatch:WsMsgServer => Unit,
                             dispatchTo:(String, WsMsgServer) => Unit,
                             // TODO 参数
                           )extends ThorSchema{

  import scala.language.implicitConversions

  override def debug(msg: String): Unit = log.debug(msg)

  override def info(msg: String): Unit = log.info(msg)

  private val foodIdGenerator = new AtomicInteger(100)

  private var justJoinUser:List[(String, String, ActorRef[UserActor.Command])] = Nil

  override protected implicit def adventurerState2Impl(adventurer: AdventurerState): Adventurer = {
    //TODO AdventurerState 转 Adventurer 具体实现
    new AdventurerImpl(config, adventurer)
  }

  //↓↓↓需要重写的函数↓↓↓

  override protected def adventurerEatFoodCallback(adventurer: Adventurer)(food: Food): Unit = {
    val event = EatFood(adventurer.playerId, food.fId, food.level, systemFrame)
    addGameEvent(event)
    dispatch(event)
  }

  override protected def adventurerAttackedCallback(killer: Adventurer)(adventurer: Adventurer): Unit = {
    val event = BeAttacked(adventurer.playerId, adventurer.name, killer.playerId, killer.name, systemFrame)
    addGameEvent(event)
    dispatch(event)
  }

  override def update(): Unit = super.update()

  //↓↓↓只有后台执行的函数↓↓↓

  private final def gengerateFood(level: Int = 1, position: Point, radius: Float = 2): Unit ={
    //生成食物事件，被后台定时事件调用，前端不产生此事件，食物的属性暂且全部作为参数
    val foodState = FoodState(foodIdGenerator.getAndIncrement(), level, position, radius)
    val event = GenerateFood(systemFrame, foodState)
    addGameEvent(event)
    dispatch(event)
  }

  def genFood(num: Int) = {

    def genPosition():Point = {
      Point(random.nextInt(boundary.x.toInt - 10),
        random.nextInt(boundary.y.toInt - 10))
    }

    (1 to num).foreach{
      t =>
        gengerateFood(random.nextInt(5) + 1, genPosition())
    }
  }

  def joinGame(userId:String, name:String, userActor:ActorRef[UserActor.Command]):Unit = {
    justJoinUser = (userId, name, userActor) :: justJoinUser
  }

  def leftGame(userId:String,name:String) = {
    val event = UserLeftRoom(userId,name,systemFrame)
    addGameEvent(event)
    dispatch(event)
  }

  def receiveUserAction(action: UserActionEvent):Unit = {
    val f = math.max(action.frame,systemFrame)

    val act = action match {
      case a: MouseMove => a.copy(frame = f)
      case a: MouseClickDownLeft => a.copy(frame = f)
      case a: MouseClickDownRight => a.copy(frame = f)
      case a: MouseClickUpRight => a.copy(frame = f)
    }

    addUserAction(act)
    dispatch(act)
  }

  override def handleUserEnterRoomNow() = {

    def generateAdventurer(playerId: String, name: String) = {

      def genPosition():Point = {
        Point(random.nextInt(boundary.x.toInt - 15),
          random.nextInt(boundary.y.toInt - 15))
      }

      def genAdventurer() = {
        val position = genPosition()
        var adventurer = AdventurerServer(roomActorRef, timer, config, playerId, name, position)
        var objects = quadTree.retrieveFilter(adventurer).filter(t => t.isInstanceOf[Adventurer])
//        while (adventurer.isIntersectsObject(objects)){
//          val position = genPosition()
//          adventurer = AdventurerServer(roomActorRef, timer, config, playerId, name, position)
//          objects = quadTree.retrieveFilter(adventurer).filter(t => t.isInstanceOf[Adventurer])
//        }
        adventurer
      }

      genAdventurer()
    }

    justJoinUser.foreach{
      case (playerId, name, ref) =>
        val adventurer = generateAdventurer(playerId, name)
        val event = UserEnterRoom(playerId, name, adventurer.getAdventurerState, systemFrame)
        dispatch(event)
        addGameEvent(event)
        ref ! UserActor.JoinRoomSuccess(adventurer, playerId, roomActorRef, config)
        adventurerMap.put(playerId, adventurer)
        quadTree.insert(adventurer)
    }

    justJoinUser = Nil
  }

}
