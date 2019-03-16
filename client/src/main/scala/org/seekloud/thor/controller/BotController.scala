/*
 *   Copyright 2018 seekloud (https://github.com/seekloud)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.seekloud.thor.controller

import java.util.{Timer, TimerTask}
import java.util.concurrent.atomic.AtomicInteger

import scala.concurrent.duration._
import akka.actor.typed.ActorRef
import javafx.animation.{Animation, AnimationTimer, KeyFrame, Timeline}
import javafx.scene.input.{KeyCode, MouseButton}
import javafx.scene.media.{AudioClip, Media, MediaPlayer}
import javafx.util.Duration
import org.seekloud.thor.ClientBoot
import org.seekloud.thor.actor.WsClient
import org.seekloud.thor.common.StageContext
import org.seekloud.thor.game.{NetWorkInfo, ThorSchemaBotImpl}
import org.seekloud.thor.scene._
import org.seekloud.thor.shared.ptcl.config.ThorGameConfigImpl
import org.seekloud.thor.shared.ptcl.model.Constants.GameState
import org.seekloud.thor.shared.ptcl.model.{Point, Score}
import org.seekloud.thor.shared.ptcl.protocol.ThorGame
import org.seekloud.thor.shared.ptcl.protocol.ThorGame._
import org.seekloud.thor.shared.ptcl.thor.ThorSchemaClientImpl
import org.seekloud.thor.ClientBoot.{executor, scheduler}
import org.slf4j.LoggerFactory
import akka.actor.typed.ActorRef
import org.seekloud.esheepapi.pb.actions.Swing
import org.seekloud.thor.protocol.BotProtocol.EnterRoomRsp

/**
  * User: TangYaruo
  * Date: 2019/3/12
  * Time: 13:43
  */
class BotController(
  wsClient: ActorRef[WsClient.WsCommand],
  playerInfo: WsClient.PlayerInfo,
  context: StageContext,
  layerScreen: LayerScene
)  {

  private[this] val log = LoggerFactory.getLogger(this.getClass)

  def getPlayer: WsClient.PlayerInfo = playerInfo

  def getWs: ActorRef[WsClient.WsCommand] = wsClient

  def getLs: LayerScene = layerScreen

  private var drawLayerScene: Option[DrawLayerScene] = None

  protected var firstCome = true

  protected var exitFullScreen = false

  private val actionSerialNumGenerator = new AtomicInteger(0)

  private val preExecuteFrameOffset = org.seekloud.thor.shared.ptcl.model.Constants.preExecuteFrameOffset

  protected val preDrawFrame = new PreDraw
  //  private var recYourInfo: Boolean = false
  //  private var recSyncGameState: Option[ThorGame.GridSyncState] = None

  var thorSchemaOpt: Option[ThorSchemaClientImpl] = None

  var sdkReplyTo: Option[ActorRef[EnterRoomRsp]] = None

  var myActions: Map[Int,List[UserActionEvent]] = Map.empty

  var thorOpt: Option[ThorSchemaBotImpl] = None
  //  private val window = Point(playGameScreen.canvasBoundary.x - 12, playGameScreen.canvasBoundary.y - 12.toFloat)
  var gameState: Int = GameState.loadingPlay
  private var logicFrameTime = System.currentTimeMillis()

  protected var currentRank = List.empty[Score]
  protected var historyRank = List.empty[Score]

  var barrage: (String, String) = ("", "")
  var barrageTime = 0

  protected var startTime = 0l
  protected var endTime = 0l

  protected var gameConfig: Option[ThorGameConfigImpl] = None


  var justSynced = false

  protected var killerName = ""
  protected var killNum = 0
  protected var energy = 0
  protected var level = 0
  protected var energyScore = 0
  private var stageWidth = context.getStageWidth.toInt
  private var stageHeight = context.getStageHeight.toInt


  //  private var mouseLeft = true
  //  private var mouseRight = false

  /*BGM*/
  private val gameMusic = new Media(getClass.getResource("/music/bgm-2.mp3").toString)
  private val gameMusicPlayer = new MediaPlayer(gameMusic)
  gameMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE)
  private val attackMusic = new AudioClip(getClass.getResource("/music/sound-4.mp3").toString)
  private var needBgm = true
  private val timeline = new Timeline()

  /*视角跟随id*/
  protected var mainId = "test"
  var byteId: Byte = 0

  /*网络信息*/
  var drawTime: List[Long] = Nil
  var drawTimeLong = 0l
  var drawTimeSize = 60
  var frameTime: List[Long] = Nil
  var frameTimeLong = 0l
  var frameTimeSize = 10
  var frameTimeSingle = 0l

  private val animationTimer = new AnimationTimer() {
    override def handle(now: Long): Unit = {
      val a = System.currentTimeMillis()
      drawGameByTime(System.currentTimeMillis() - logicFrameTime, layerScreen.canvasUnit, layerScreen.canvasUnit4Huge, layerScreen.canvasBounds)
      val b = System.currentTimeMillis()
      if (b-a>10)
        println(s"draw all time span: ${b-a}")
      if (gameState == GameState.stop && thorSchemaOpt.nonEmpty) thorSchemaOpt.foreach(_.drawGameStop(killerName, killNum, energyScore, level))
    }
  }



  protected def setGameState(s: Int): Unit = {
    gameState = s
  }

  def start(): Unit = {
    if (firstCome) {
      firstCome = false
      println("start bot controller ...")
      //      wsClient ! WsClient.StartGame(playerInfo.roomId)
      startGameLoop()
      addUserActionListenEvent()
      checkAndChangePreCanvas()
      //      logicFrameTime = System.currentTimeMillis()
    } else {
      println(s"restart...")
      thorOpt.foreach { r =>
        wsClient ! WsClient.DispatchMsg(ThorGame.RestartGame)
        setGameState(GameState.loadingPlay)
        //        wsClient ! WsClient.StartGameLoop
      }
    }
  }

  def startGameLoop(): Unit = { //渲染帧
    logicFrameTime = System.currentTimeMillis()
    timeline.setCycleCount(Animation.INDEFINITE)
    val keyFrame = new KeyFrame(Duration.millis(100), { _ =>
      logicLoop()
    })
    //    scheduler.schedule(0.millis, 100.millis) {
    //      logicLoop()
    //    }
    timeline.getKeyFrames.add(keyFrame)
    animationTimer.start()
    timeline.play()
  }

  def drawGameByTime(offsetTime: Long, canvasUnit: Float, canvasUnit4Huge: Float, canvasBounds: Point): Unit = {
    thorOpt match {
      case Some(thorSchema: ThorSchemaBotImpl) =>
        if (thorSchema.adventurerMap.contains(mainId)) {
          if (drawLayerScene.isDefined) {
            val start = System.currentTimeMillis()
            val a = System.currentTimeMillis()
            myActions ++= thorSchema.getMyActionMap(byteId).toMap
            drawLayerScene.get.drawGame4Bot(mainId, offsetTime, canvasUnit, canvasUnit4Huge, canvasBounds, thorSchema.getMousePoint, myActions)

            drawTime = drawTime :+ System.currentTimeMillis() - start
            if (drawTime.length >= drawTimeSize) {
              drawTimeLong = drawTime.sum / drawTime.size
              drawTime = Nil
            }
            if (frameTime.length >= frameTimeSize) {
              frameTimeLong = frameTime.sum / frameTime.size
              frameTime = Nil
            }
          }
        }
        else {

        }

      case None =>

    }
  }

  var lastSendReq = 0L

  def logicLoop(): Unit ={
    var myLevel = 0
//    thorSchemaOpt.foreach { thorSchema =>
//      thorSchema.adventurerMap.get(mainId).foreach {
//        ad => myLevel = ad.getAdventurerState.level
//      }
//    }
    thorOpt.foreach { thorSchema =>
      thorSchema.adventurerMap.get(mainId).foreach {
        ad => myLevel = ad.getAdventurerState.level
      }
    }
    ClientBoot.addToPlatform {
//      val (boundary, unit) = layerScreen.handleResize(myLevel, context)
//      if (unit != 0) {
////        thorSchemaOpt.foreach { r =>
////          r.updateSize(boundary, unit)
////        }
//        thorOpt.foreach { r =>
//          r.updateSize(boundary, unit)
//        }
//      }
      logicFrameTime = System.currentTimeMillis()

      gameState match {
        case GameState.firstCome =>
          thorSchemaOpt.foreach(_.drawGameLoading())
        case GameState.loadingPlay =>
          thorSchemaOpt.foreach(_.drawGameLoading())
        case GameState.stop =>
//          thorSchemaOpt.foreach {
//            _.update()
//          }
          thorOpt.foreach {
            _.update()
          }
          frameTime :+ System.currentTimeMillis() - logicFrameTime
          logicFrameTime = System.currentTimeMillis()
        case GameState.replayLoading =>
          thorSchemaOpt.foreach {
            _.drawGameLoading()
          }
        case GameState.play =>
          //        if (Shortcut.isPaused("bgm-2")) {
          //          Shortcut.playMusic("bgm-2")
          //        }
//          thorSchemaOpt.foreach { thorSchema =>
//            thorSchema.update()
//            if (thorSchema.needUserMap && logicFrameTime - lastSendReq > 5000) {
//              println("request for user map")
//              wsClient ! WsClient.DispatchMsg(UserMapReq)
//              lastSendReq = System.currentTimeMillis()
//            }
//          }

          thorOpt.foreach { thorSchema =>
            thorSchema.update()
            if (thorSchema.needUserMap && logicFrameTime - lastSendReq > 5000) {
              println("request for user map")
              wsClient ! WsClient.DispatchMsg(UserMapReq)
              lastSendReq = System.currentTimeMillis()
            }
          }

          frameTime = frameTime :+ System.currentTimeMillis() - logicFrameTime
          frameTimeSingle = System.currentTimeMillis() - logicFrameTime
          logicFrameTime = System.currentTimeMillis()
        case _ => log.info(s"state=$gameState failed")
      }
    }
  }

  def wsMessageHandle(data: ThorGame.WsMsgServer): Unit = {

    ClientBoot.addToPlatform {
      data match {
        case e: ThorGame.YourInfo =>
          println(s"start---------")
          gameMusicPlayer.play()
          mainId = e.id
          try {
//            thorSchemaOpt = Some(ThorSchemaClientImpl(layerScreen.drawFrame, layerScreen.getCanvasContext, e.config, e.id, e.name, layerScreen.canvasBoundary, layerScreen.canvasUnit))
            println("get Bot impl")
            thorOpt = Some(ThorSchemaBotImpl(layerScreen.drawFrame, layerScreen.getCtxMap, e.config, e.id, e.name, layerScreen.canvasBoundary, layerScreen.canvasUnit))
//            drawScene = Some(new DrawScene(thorSchemaOpt.get))
            drawLayerScene = Some(new DrawLayerScene(thorOpt.get))
            gameConfig = Some(e.config)
            checkAndChangePreCanvas()
//            e.playerIdMap.foreach { p => thorSchemaOpt.foreach { t => t.playerIdMap.put(p._1, p._2) } }
            e.playerIdMap.foreach { p => thorOpt.foreach { t => t.playerIdMap.put(p._1, p._2) } }
            //            startGameLoop()
            //            wsClient ! WsClient.StartGameLoop
            gameState = GameState.play

          } catch {
            case e: Exception =>
              closeHolder()
              println(e.getMessage)
              println("client is stop!!!")
          }


        case x@RestartYourInfo =>
          println(x)
          mainId = playerInfo.playerId
          gameState = GameState.play

        case e: ThorGame.BeAttacked =>
//          println(s"receive attacked msg:\n $e")
          barrage = (e.killerName, e.name)
          barrageTime = 300
//          println(s"be attacked by ${e.killerName}")
          if (e.playerId == mainId) {
            mainId = e.killerId //跟随凶手视角
            if (e.playerId == playerInfo.playerId) {
              gameState = GameState.stop
              endTime = System.currentTimeMillis()
//              thorSchemaOpt match {
//                case Some(thorSchema: ThorSchemaClientImpl) =>
//                  thorSchema.adventurerMap.get(playerInfo.playerId).foreach { my =>
//                    thorSchema.killerNew = e.killerName
//                    thorSchema.duringTime = duringTime(endTime - startTime)
//                    killerName = e.killerName
//                    killNum = my.killNum
//                    energyScore = my.energyScore
//                    level = my.level
//                  }
//                case None =>
//                  println("there is nothing")
//              }
              thorOpt match {
                case Some(thorSchema: ThorSchemaBotImpl) =>
                  thorSchema.adventurerMap.get(playerInfo.playerId).foreach { my =>
                    thorSchema.killerNew = e.killerName
                    thorSchema.duringTime = duringTime(endTime - startTime)
                    killerName = e.killerName
                    killNum = my.killNum
                    energyScore = my.energyScore
                    level = my.level
                  }
                case None =>
                  println("there is nothing")
              }
            }

          }
//          thorSchemaOpt.foreach(_.receiveGameEvent(e))
          thorOpt.foreach(_.receiveGameEvent(e))


        case e: ThorGame.Ranks =>
          currentRank = e.currentRank
        //          historyRank = e.historyRank

        case e: ThorGame.GridSyncState =>
//          thorSchemaOpt.foreach(_.receiveThorSchemaState(e.d))
          thorOpt.foreach(_.receiveThorSchemaState(e.d))
          justSynced = true

        case UserMap(map) =>
          println(s"userMap ---- $map")
//          thorSchemaOpt.foreach { grid =>
//            map.foreach(p => grid.playerIdMap.put(p._1, p._2))
//            grid.needUserMap = false
//          }
          thorOpt.foreach { grid =>
            map.foreach(p => grid.playerIdMap.put(p._1, p._2))
            grid.needUserMap = false
          }



        case RebuildWebSocket =>
          thorSchemaOpt.foreach(_.drawReplayMsg("存在异地登录"))
          closeHolder()

        case e: ThorGame.UserActionEvent =>
          thorSchemaOpt.foreach(_.receiveUserEvent(e))
          thorOpt.foreach(_.receiveUserEvent(e))

        case e: ThorGame.GameEvent =>
          e match {
            case event: ThorGame.UserEnterRoom =>
              if (thorOpt.nonEmpty) {
//                thorSchemaOpt.get.playerIdMap.put(event.shortId, (event.playerId, event.name))
                thorOpt.get.playerIdMap.put(event.shortId, (event.playerId, event.name))
                if (event.playerId == playerInfo.playerId) byteId = event.shortId
              } else {
                //                wsClient ! WsClient.UserEnterRoom(event)
                scheduler.scheduleOnce(100.millis) {
//                  thorSchemaOpt.get.playerIdMap.put(event.shortId, (event.playerId, event.name))
                  thorOpt.get.playerIdMap.put(event.shortId, (event.playerId, event.name))
                  if (event.playerId == playerInfo.playerId) byteId = event.shortId
                }

              }
            case event: UserLeftRoom =>
              if (event.shortId == byteId) println(s"${event.shortId}  ${event.playerId} ${event.name} left room...")
//              thorSchemaOpt.foreach(thorSchema => thorSchema.playerIdMap.remove(event.shortId))
              thorOpt.foreach(thorSchema => thorSchema.playerIdMap.remove(event.shortId))
            case _ =>
          }
//          thorSchemaOpt.foreach(_.receiveGameEvent(e))
          thorOpt.foreach(_.receiveGameEvent(e))

        case x =>
//          println(s"接收到无效消息$x")

      }
    }

  }

  private def closeHolder(): Unit = {
    animationTimer.stop()
    //remind 此处关闭WebSocket
    wsClient ! WsClient.Stop
  }

  def getActionSerialNum: Byte = (actionSerialNumGenerator.getAndIncrement() % 127).toByte


  def receiveBotAction(action: Either[Int, Swing]): Unit = {

    action match {
      case Right(swing) =>


      case Left(fire) =>
    }
  }




  var lastMouseMove = 0l //限制只能发一次mousemove
  val frequency = 50

  private def addUserActionListenEvent(): Unit = {
    layerScreen.allPlayerCanvas.getCanvas.requestFocus()

    /*鼠标移动操作*/
    layerScreen.allPlayerCanvas.getCanvas.setOnMouseMoved { e =>
      val point = Point(e.getX.toFloat, e.getY.toFloat)
      val theta = point.getTheta(layerScreen.canvasBounds4Huge * layerScreen.canvasUnit4Huge / 2).toFloat
      drawLayerScene.foreach{ d =>
        d.drawMouse.drawMouse(point / 2)
      }
      thorOpt.foreach { thorSchema =>
        if (thorSchema.adventurerMap.contains(playerInfo.playerId)) {
          val mouseDistance = math.sqrt(math.pow(e.getX - layerScreen.canvasBoundary4Huge.x / 2.0, 2) + math.pow(e.getY - layerScreen.canvasBoundary4Huge.y / 2.0, 2))
          val r = gameConfig.get.getAdventurerRadiusByLevel(thorSchema.adventurerMap(playerInfo.playerId).getAdventurerState.level) * layerScreen.canvasUnit4Huge
          val direction = thorSchema.adventurerMap(playerInfo.playerId).direction
          if (System.currentTimeMillis() > lastMouseMove + frequency && math.abs(theta - direction) > 0.3) { //角度差大于0.3才执行

            val offsetX = (e.getX - layerScreen.canvasBoundary4Huge.x / 2.0).toShort
            val offsetY = (e.getY - layerScreen.canvasBoundary4Huge.y / 2.0).toShort
            val preExecuteAction = MM(byteId, if (mouseDistance > r) offsetX else (10000 + offsetX).toShort, offsetY, thorSchema.systemFrame + preExecuteFrameOffset, getActionSerialNum)
            //              println(s"moved: $mouseDistance r:$r data:$data  canvasUnit:$canvasUnit")
            thorSchema.preExecuteUserEvent(preExecuteAction)
            wsClient ! WsClient.DispatchMsg(preExecuteAction)
            lastMouseMove = System.currentTimeMillis()
          }
        }
      }

    }

    /*鼠标点击事件*/


    layerScreen.allPlayerCanvas.getCanvas.setOnMousePressed { e =>
      //         println(s"left: [${e.isPrimaryButtonDown}]; right: [${e.isSecondaryButtonDown}]")
      thorOpt.foreach { thorSchema =>
        if (gameState == GameState.play && thorSchema.adventurerMap.exists(_._1 == playerInfo.playerId) && !thorSchema.dyingAdventurerMap.exists(_._1 == playerInfo.playerId)) {
          if (e.isPrimaryButtonDown) {
            attackMusic.play()
            //            mouseLeft = true
            val preExecuteAction = MouseClickDownLeft(byteId, thorSchema.systemFrame + preExecuteFrameOffset, getActionSerialNum)
            thorSchema.preExecuteUserEvent(preExecuteAction)
            wsClient ! WsClient.DispatchMsg(preExecuteAction)
          }
          else if (e.isSecondaryButtonDown) {
            //            mouseRight = true
            val preExecuteAction = MouseClickDownRight(byteId, thorSchema.systemFrame + preExecuteFrameOffset, getActionSerialNum)
            thorSchema.preExecuteUserEvent(preExecuteAction)
            //            println(preExecuteAction)
            wsClient ! WsClient.DispatchMsg(preExecuteAction)
          }
          else ()
        }
        else {
          start()
        }
      }
    }

    /*鼠标释放事件*/
    layerScreen.allPlayerCanvas.getCanvas.setOnMouseReleased { e =>
      thorOpt.foreach { thorSchema =>
        if (gameState == GameState.play && thorSchema.adventurerMap.exists(_._1 == playerInfo.playerId) && !thorSchema.dyingAdventurerMap.exists(_._1 == playerInfo.playerId)) {
          if (e.getButton == MouseButton.SECONDARY) { //右键
            val preExecuteAction = MouseClickUpRight(byteId, thorSchema.systemFrame + preExecuteFrameOffset, getActionSerialNum)
            thorSchema.preExecuteUserEvent(preExecuteAction)
            wsClient ! WsClient.DispatchMsg(preExecuteAction)
          }
        }
      }
    }

    /*键盘事件*/
    layerScreen.allPlayerCanvas.getCanvas.setOnKeyPressed { e =>
      thorOpt.foreach { thorSchema =>
        if (!thorSchema.adventurerMap.exists(_._1 == playerInfo.playerId)) {
          if (e.getCode == KeyCode.SPACE) {
            print("space")
            start()
          } else if (e.getCode == KeyCode.M) {
            if (needBgm) {
              gameMusicPlayer.pause()
              needBgm = false
            } else {
              gameMusicPlayer.play()
              needBgm = true
            }
          }
        }
      }
    }

  }

  def checkAndChangePreCanvas(): Unit = {
    val timer = new Timer

    def timerTask(fun: => Unit) = new TimerTask {
      override def run(): Unit = fun
    }

    (preDrawFrame.foodImg, preDrawFrame.adventurerImg, preDrawFrame.weaponImg, preDrawFrame.deathImg) match {
      case (Nil, Nil, Nil, Nil) =>
        timer.schedule(timerTask(checkAndChangePreCanvas()), 1000)
      case (foodCanvas, Nil, Nil, Nil) =>
        thorOpt.foreach(_.changePreImage(foodCanvas, Nil, Nil, Nil))
        timer.schedule(timerTask(checkAndChangePreCanvas()), 1000)
      case (foodCanvas, adventurerCanvas, Nil, Nil) =>
        thorOpt.foreach(_.changePreImage(foodCanvas, adventurerCanvas, Nil, Nil))
        timer.schedule(timerTask(checkAndChangePreCanvas()), 1000)
      case (foodCanvas, adventurerCanvas, weaponCanvas, Nil) =>
        thorOpt.foreach(_.changePreImage(foodCanvas, adventurerCanvas, weaponCanvas, Nil))
        timer.schedule(timerTask(checkAndChangePreCanvas()), 1000)
      case (foodCanvas, adventurerCanvas, weaponCanvas, deathCanvas) =>
        thorOpt.foreach(_.changePreImage(foodCanvas, adventurerCanvas, weaponCanvas, deathCanvas))
        timer.schedule(timerTask(checkAndChangePreCanvas()), 1000)
    }
  }

  def duringTime(time: Long): String = {
    var remain = time / 1000 % 86400
    val hour = remain / 3600
    remain = remain % 3600
    val min = remain / 60
    val sec = remain % 60
    val timeString = s"$hour : $min : $sec"
    timeString
  }


}

