/*
 * Copyright 2018 seekloud (https://github.com/seekloud)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.seekloud.thor.core.game

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.TimerScheduler
import org.seekloud.thor.core.{ESheepLinkClient, RobotActor, RoomActor, UserActor}
import org.seekloud.thor.protocol.ESheepProtocol.{ESheepRecord, ESheepRecordSimple}
import org.seekloud.thor.shared.ptcl.component._
import org.slf4j.Logger
import org.seekloud.thor.shared.ptcl.config.ThorGameConfig
import org.seekloud.thor.shared.ptcl.model._
import org.seekloud.thor.shared.ptcl.protocol.ThorGame._
import org.seekloud.thor.shared.ptcl.thor.ThorSchema
import org.seekloud.thor.Boot.eSheepLinkClient
import org.seekloud.thor.core.UserActor.JoinRoomSuccess4Watch
import org.seekloud.thor.shared.ptcl.protocol.ThorGame

import scala.collection.mutable

/**
  * User: XuSiRan
  * Date: 2018/11/15
  * Time: 11:37
  *
  **/
case class ThorSchemaServerImpl(
  config: ThorGameConfig,
  roomActorRef: ActorRef[RoomActor.Command],
  timer: TimerScheduler[RoomActor.Command],
  log: Logger,
  dispatch: WsMsgServer => Unit,
  dispatchTo: (String, WsMsgServer, Option[mutable.HashMap[String, ActorRef[UserActor.Command]]]) => Unit) extends ThorSchema {

  import scala.language.implicitConversions

  init()

  override def debug(msg: String): Unit = log.debug(msg)

  override def info(msg: String): Unit = log.info(msg)

  private val foodIdGenerator = new AtomicInteger(100)

  private var justJoinUser: List[(String, String, Byte, ActorRef[UserActor.Command])] = Nil // userId, name, Actor
  private var justJoinBot: List[(String, String, Byte, ActorRef[RobotActor.Command])] = Nil // botId, name
  private val robotMap: mutable.HashMap[String, ActorRef[RobotActor.Command]] = mutable.HashMap.empty
  private val watchingMap: mutable.HashMap[String, mutable.HashMap[String, ActorRef[UserActor.Command]]] = mutable.HashMap.empty

  private val RecordMap = mutable.HashMap[String, ESheepRecordSimple]() //后台战绩： playerId -> (开始时间，)

  def getUserActor4WatchGameList(uId: String) = watchingMap.get(uId)

  override protected implicit def adventurerState2Impl(adventurer: AdventurerState): Adventurer = {
    val playerInfo = playerIdMap(adventurer.byteId)
    new AdventurerImpl(config, adventurer, playerInfo._1, playerInfo._2)
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
      a.score += adventurer.energyScore
    }
    addGameEvent(event)
  }

  override protected def handleAdventurerAttacked(e: BeAttacked): Unit = {
    super.handleAdventurerAttacked(e)
    val killerOpt = adventurerMap.get(e.killerId)
    if(killerOpt.nonEmpty){
      dispatch(e)
      if(e.playerId.take(5).equals("robot")){
        if(robotMap.contains(e.playerId)){
          robotMap(e.playerId) ! RobotActor.RobotDead
        }
        robotMap.remove(e.playerId)
        adventurerMap.get(e.playerId).foreach(quadTree.remove)
        adventurerMap.remove(e.playerId)
      }
    }
  }

  override protected def handleAdventurerDyingNow(): Unit = {
    dyingAdventurerMap.foreach { dying =>
      if (dying._2._2 <= 0) {
        dyingAdventurerMap.remove(dying._1)
        val foods = genBodyFood(dying._2._1)
        val event = BodyToFood(systemFrame, dying._2._1.position, foods)
        addGameEvent(event)
        dispatch(event)
      } else {
        dyingAdventurerMap.update(dying._1, (dying._2._1, dying._2._2 - 1))
      }
    }
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
    currentRankList = adventurerMap.values.map{ a =>
      Score(a.byteId, a.killNum.toShort, a.energyScore.toShort)
    }.toList.sorted
    var historyChange = false
    currentRankList.foreach { cScore =>
      historyRankMap.get(cScore.bId) match {
        case Some(oldScore) if cScore.e > oldScore.e || (cScore.e == oldScore.e && cScore.k > oldScore.k) =>
          historyRankMap += (cScore.bId -> cScore)
          historyChange = true
        case None if cScore.e > historyRankThreshold =>
          historyRankMap += (cScore.bId -> cScore)
          historyChange = true
        case _ => // do nothing
      }
    }

    if (historyChange) {
      historyRank = historyRankMap.values.toList.sorted.take(historyRankLength)
      historyRankThreshold = historyRank.lastOption.map(_.e).getOrElse(-1)
      historyRankMap = historyRank.map(s => s.bId -> s).toMap
    }
  }

  override def clearEventWhenUpdate(): Unit = {
    super.clearEventWhenUpdate()
    gameEventMap -= systemFrame - 1
    actionEventMap -= systemFrame - 1
    systemFrame += 1
  }

  override def update(): Unit = {
    super.update()
    updateRanks()
  }

  //↓↓↓只有后台执行的函数↓↓↓

  private final def generateFood(level: Byte = 1, position: Point, radius: Float = 2): FoodState = {
    //生成食物事件，被后台定时事件调用，前端不产生此事件，食物的属性暂且全部作为参数,color作为随机数
    val foodState = FoodState(foodIdGenerator.getAndIncrement(), level, position, radius, random.nextInt(8).toByte)
    val event = GenerateFood(systemFrame, foodState)
    addGameEvent(event)
    dispatch(event)
    foodState
  }

  def genFood(num: Int): List[FoodState] = {
    var foodList: List[FoodState] = List()

    def genPosition(): Point = {
      Point(random.nextInt(boundary.x.toInt - 20) + 10,
        random.nextInt(boundary.y.toInt - 20) + 10)
    }

    (1 to num).foreach {
      t =>
        if(foodMap.size < config.getFoodMax()){
          val food = generateFood(random.nextInt(5).toByte, genPosition())
          foodList = foodList :+ food
        }
    }
    foodList
  }

  def genBodyFood(adv: Adventurer): List[FoodState] = {
    var bodyFoods: List[FoodState] = List()
    val advRadius = if (adv.level != config.getAdventurerLevelSize) {
      config.getAdventurerRadiusByLevel(adv.level)
    } else {
      config.getAdventurerRadiusByLevel((adv.level - 1).toByte) * 1.5.toFloat
    }
//    val advRadius = config.getAdventurerRadiusByLevel(adv.level)
    def genPosition(): Point = {
      var position = Point(random.nextInt((2 * 1.5 * advRadius).toInt) + adv.position.x - 1.5.toFloat * advRadius,
        random.nextInt((2 * 1.5 * advRadius).toInt) + adv.position.y - 1.5.toFloat * advRadius
      )
      while (position.x < 5 || position.y < 5 || position.x > boundary.x - 5 || position.y > boundary.y - 5) {
        position = Point(random.nextInt((2 * 1.5 * advRadius).toInt) + adv.position.x - 1.5.toFloat * advRadius,
          random.nextInt((2 * 1.5 * advRadius).toInt) + adv.position.y - 1.5.toFloat * advRadius
        )
      }
      position
    }

    val maxNum = if (adv.level != config.getAdventurerLevelSize) adv.level * 2 else adv.level * 5
    (1 to maxNum).foreach {
      _ =>
        val endP = genPosition()

        val food = FoodState(foodIdGenerator.getAndIncrement(), random.nextInt(5).toByte, endP, 2, random.nextInt(8).toByte, Some(config.getScatterAnimation))
        bodyFoods = bodyFoods :+ food
    }
    bodyFoods
  }

  def joinGame(userId: String, name: String, shortId: Byte, userActor: ActorRef[UserActor.Command]): Unit = {
    justJoinUser = (userId, name, shortId, userActor) :: justJoinUser
  }

  def robotJoinGame(botId: String, name: String, shortId: Byte, ref: ActorRef[RobotActor.Command]): Unit ={
    justJoinBot = (botId, name, shortId, ref) :: justJoinBot
  }

  def handleJoinRoom4Watch(userActor4WatchGame: ActorRef[UserActor.Command], uid: String, playerId: String) = {
    adventurerMap.find(_._2.playerId == playerId) match {
      case Some((_, adventurer)) =>
        watchingMap.values.foreach(t => t.remove(uid))
        val playerObserversMap = watchingMap.getOrElse(playerId, mutable.HashMap[String, ActorRef[UserActor.Command]]())
        playerObserversMap.put(uid, userActor4WatchGame)
        watchingMap.put(playerId, playerObserversMap)
//        log.debug(s"当前的watchingMaps是${watchingMap}")
        userActor4WatchGame ! UserActor.JoinRoomSuccess4Watch(adventurer, config.getThorGameConfigImpl(), roomActorRef, GridSyncState(getThorSchemaState()), playerIdMap.toList)

      case None =>
        userActor4WatchGame ! UserActor.JoinRoomFail4Watch(s"观战的用户${playerId}不存在")
        roomActorRef ! RoomActor.LeftRoom4Watch(uid, playerId)
    }
  }

  override def leftGame(userId: String, name: String) = {
    val shortId = playerId2ByteId(userId)
//    playerIdMap.toList.sortBy(_._1).foreach(x => log.info(s"${x._1}->${x._2}"))
    shortId match {
      case Right(sId) =>
        val event = UserLeftRoom(userId, sId, name, systemFrame)
        addGameEvent(event)
      case Left(_) => // do nothing
    }

    //只有平台用户才上传战绩（平台用户的id是guest.../user...）
    if(userId.take(5).equals("guest") || userId.take(4).equals("user"))
      RecordMap.get(userId).foreach { a =>
        //若是死之后离开房间，不会执行以下foreach
        adventurerMap.get(userId).foreach{ adventurer =>
          RecordMap.get(userId).foreach{ a =>
            a.killing += adventurer.killNum
            a.score += adventurer.energyScore
          }
        }
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
  }

  def leftRoom4Watch(uId: String, playerId: String) = {
    watchingMap.get(playerId) match {
      case Some(maps) =>
        maps.remove(uId)
        watchingMap.update(playerId, maps)
      case None =>
    }
  }

  def receiveUserAction(preExecuteUserAction: UserActionEvent): Unit = {
    val f = math.max(preExecuteUserAction.frame, systemFrame)

    val act = preExecuteUserAction match {
      case a: MM => a.copy(frame = f)
      case a: MouseClickDownLeft => a.copy(frame = f)
      case a: MouseClickDownRight => a.copy(frame = f)
      case a: MouseClickUpRight => a.copy(frame = f)
    }

    addUserAction(act)
    dispatch(act)
  }

  def getCurGameSnapshot: ThorGame.ThorSnapshot = {
    ThorGame.ThorSnapshot(getThorSchemaState())
  }

  def getLastGameEvent: List[ThorGame.WsMsgServer] = {
    (gameEventMap.getOrElse(this.systemFrame - 1, Nil) ::: actionEventMap.getOrElse(this.systemFrame - 1, Nil))
      .filter(_.isInstanceOf[ThorGame.WsMsgServer]).map(_.asInstanceOf[ThorGame.WsMsgServer])
  }

  def getCurSnapshot: Option[ThorGame.GameSnapshot] = {
    Some(getCurGameSnapshot)
  }

  override def handleUserEnterRoomNow() = {

    def generateAdventurer(shortId: Byte, playerId: String, name: String) = {

      def isNextToBig(point: Point): Boolean = {
        var isNextTo = false
        val bigMap = adventurerMap.filter(_._2.level >= 10).values
        bigMap.foreach { big =>
          if (big.position.distance(point) <= 100) {
            isNextTo = true
          }
        }
        isNextTo
      }

      def genPosition(): Point = {
        var x = random.nextInt(boundary.x.toInt - 15)
        var y = random.nextInt(boundary.y.toInt - 15)
        while (x < 15) {
          x = random.nextInt(boundary.x.toInt - 15)
        }
        while (y < 15) {
          y = random.nextInt(boundary.y.toInt - 15)
        }
        while (isNextToBig(Point(x, y))) {
          x = random.nextInt(boundary.x.toInt - 15)
          y = random.nextInt(boundary.y.toInt - 15)
        }
        Point(x, y)
      }

      def genAdventurer() = {
        val position = genPosition()
        val adventurer = AdventurerServer(roomActorRef, timer, config, shortId, playerId, name, position)
        adventurer
      }

      genAdventurer()
    }

    //User和Bot加入房间的统一操作
    def normalJoin(adventurer: Adventurer, Id: String, name: String, shortId: Byte): Unit = {
      playerIdMap.put(shortId, (Id, name))
      adventurerMap.put(adventurer.playerId, adventurer)
      quadTree.insert(adventurer)
      newbornAdventurerMap.put(Id, (adventurer, config.newbornFrame))
//      val event = UserEnterRoom(Id, shortId, name, adventurer.getAdventurerState, systemFrame)
//      dispatch(event)
    }

    justJoinUser.foreach {
      case (playerId, name, shortId, ref) =>
        val adventurer = generateAdventurer(shortId, playerId, name)
        normalJoin(adventurer, playerId, name, shortId)
        ref ! UserActor.JoinRoomSuccess(adventurer, playerId, shortId, roomActorRef, config.getThorGameConfigImpl(), playerIdMap.toList)
        watchingMap.find(_._1 == playerId).foreach{ playerWatchMap =>
          playerWatchMap._2.foreach(_._2 ! JoinRoomSuccess4Watch(adventurer, config.getThorGameConfigImpl(), roomActorRef, GridSyncState(getThorSchemaState()),playerIdMap.toList))
        }
        val event = UserEnterRoom(playerId, shortId, name, adventurer.getAdventurerState, systemFrame)
        dispatch(event)
        RecordMap.put(playerId, ESheepRecordSimple(System.currentTimeMillis(), 0, 0, 0))
    }
    justJoinBot.foreach {
      case (botId, name, shortId, ref) =>
        val adventurer = generateAdventurer(shortId, botId, name)
        normalJoin(adventurer, botId, name, shortId)
        val event = UserEnterRoom(botId, shortId, name, adventurer.getAdventurerState, systemFrame)
        dispatch(event)
        robotMap.put(botId, ref)
    }

    justJoinUser = Nil
    justJoinBot = Nil
  }

  private def init(): Unit = {
    clearEventWhenUpdate()
  }

}
