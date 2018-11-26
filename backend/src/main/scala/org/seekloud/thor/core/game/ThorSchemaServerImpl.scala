package org.seekloud.thor.core.game

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.TimerScheduler
import org.seekloud.thor.core.{ESheepLinkClient, RoomActor, UserActor}
import org.seekloud.thor.protocol.ESheepProtocol.{ESheepRecord, ESheepRecordSimple}
import org.seekloud.thor.shared.ptcl.component._
import org.slf4j.Logger
import org.seekloud.thor.shared.ptcl.config.ThorGameConfig
import org.seekloud.thor.shared.ptcl.model._
import org.seekloud.thor.shared.ptcl.protocol.ThorGame._
import org.seekloud.thor.shared.ptcl.thor.ThorSchema
import org.seekloud.thor.Boot.eSheepLinkClient

import scala.collection.mutable

/**
  * User: XuSiRan
  * Date: 2018/11/15
  * Time: 11:37
  */
case class ThorSchemaServerImpl(
  config: ThorGameConfig,
  roomActorRef: ActorRef[RoomActor.Command],
  timer: TimerScheduler[RoomActor.Command],
  log: Logger,
  dispatch: WsMsgServer => Unit,
  dispatchTo: (String, WsMsgServer) => Unit) extends ThorSchema {

  import scala.language.implicitConversions

  init()

  override def debug(msg: String): Unit = log.debug(msg)

  override def info(msg: String): Unit = log.info(msg)

  private val foodIdGenerator = new AtomicInteger(100)

  private var justJoinUser: List[(String, String, ActorRef[UserActor.Command])] = Nil

  private val RecordMap = mutable.HashMap[String, ESheepRecordSimple]() //后台战绩： playerId -> (开始时间，)

  override protected implicit def adventurerState2Impl(adventurer: AdventurerState): Adventurer = {
    new AdventurerImpl(config, adventurer)
  }

  //↓↓↓需要重写的函数↓↓↓

  override protected def adventurerEatFoodCallback(adventurer: Adventurer)(food: Food): Unit = {
    val event = EatFood(adventurer.playerId, food.fId, food.level, systemFrame)
    addGameEvent(event)
    dispatch(event)
  }

  override protected def adventurerAttackedCallback(killer: Adventurer)(adventurer: Adventurer): Unit = {
    super.adventurerAttackedCallback(killer)(adventurer)
    val event = BeAttacked(adventurer.playerId, adventurer.name, killer.playerId, killer.name, systemFrame)
    RecordMap.get(adventurer.playerId).foreach{ a =>
      a.killed += 1
      a.killing += adventurer.killNum
      a.score += adventurer.energy
    }
//    println(RecordMap)
    dispatchTo(adventurer.playerId, event)
  }

  implicit val scoreOrdering = new Ordering[Score] {
    override def compare(x: Score, y: Score): Int = {
      var r = y.e - x.e
      if (r == 0) {
        r = y.k - x.k
      }
      r
    }
  }

  private[this] def updateRanks() = {
    currentRankList = adventurerMap.values.map(a => Score(a.playerId, a.name, a.killNum, a.energy)).toList.sorted
    var historyChange = false
    currentRankList.foreach { cScore =>
      historyRankMap.get(cScore.id) match {
        case Some(oldScore) if cScore.e > oldScore.e || (cScore.e == oldScore.e && cScore.k > oldScore.k) =>
          historyRankMap += (cScore.id -> cScore)
          historyChange = true
        case None if cScore.e > historyRankThreshold =>
          historyRankMap += (cScore.id -> cScore)
          historyChange = true
        case _ => // do nothing
      }
    }

    if (historyChange) {
      historyRank = historyRankMap.values.toList.sorted.take(historyRankLength)
      historyRankThreshold = historyRank.lastOption.map(_.e).getOrElse(-1)
      historyRankMap = historyRank.map(s => s.id -> s).toMap
    }
  }

//  override def clearEventWhenUpdate(): Unit = {
//    gameEventMap -= systemFrame - 1
//    actionEventMap -= systemFrame - 1
//    systemFrame += 1
//  }

  override def update(): Unit = {
    super.update()
    updateRanks()
  }

  //↓↓↓只有后台执行的函数↓↓↓

  private final def generateFood(level: Int = 1, position: Point, radius: Float = 2): FoodState = {
    //生成食物事件，被后台定时事件调用，前端不产生此事件，食物的属性暂且全部作为参数,color作为随机数
    val foodState = FoodState(foodIdGenerator.getAndIncrement(), level, position, radius, random.nextInt(8))
    val event = GenerateFood(systemFrame, foodState)
    addGameEvent(event)
    dispatch(event)
    foodState
  }

  def genFood(num: Int): List[FoodState] = {
    var foodList: List[FoodState] = List()

    def genPosition(): Point = {
      Point(random.nextInt(boundary.x.toInt - 10),
        random.nextInt(boundary.y.toInt - 10))
    }

    (1 to num).foreach {
      t =>
        if(foodMap.size < config.getFoodMax()){
          val food = generateFood(random.nextInt(5), genPosition())
          foodList = foodList :+ food
        }
    }
    foodList
  }

  def joinGame(userId: String, name: String, userActor: ActorRef[UserActor.Command]): Unit = {
    justJoinUser = (userId, name, userActor) :: justJoinUser
  }

  override def leftGame(userId: String, name: String) = {
    val event = UserLeftRoom(userId, name, systemFrame)
    addGameEvent(event)
    RecordMap.get(userId).foreach { a =>
      val record = ESheepRecord(
        playerId = userId,
        nickname = name,
        killing = a.killing ,
        killed = a.killed ,
        score = a.score,
        startTime = a.startTime,
        endTime = System.currentTimeMillis())
      eSheepLinkClient ! ESheepLinkClient.AddRecord2ESheep(record)
    }

    //    dispatch(event)
  }

  def receiveUserAction(preExecuteUserAction: UserActionEvent): Unit = {
    val f = math.max(preExecuteUserAction.frame, systemFrame)

    val act = preExecuteUserAction match {
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

      def genPosition(): Point = {
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

    justJoinUser.foreach {
      case (playerId, name, ref) =>
        val adventurer = generateAdventurer(playerId, name)
        val event = UserEnterRoom(playerId, name, adventurer.getAdventurerState, systemFrame)
        dispatch(event)
        addGameEvent(event)
        ref ! UserActor.JoinRoomSuccess(adventurer, playerId, roomActorRef, config.getThorGameConfigImpl())
        RecordMap.put(playerId, ESheepRecordSimple(System.currentTimeMillis(), 0, 0, 0))
        adventurerMap.put(playerId, adventurer)
        quadTree.insert(adventurer)
    }

    justJoinUser = Nil
  }

  private def init(): Unit = {
    clearEventWhenUpdate()
  }

}
