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

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.adapter._
import javafx.animation.{Animation, AnimationTimer, KeyFrame, Timeline}
import javafx.scene.input.KeyCode
import javafx.scene.media.{AudioClip, Media, MediaPlayer}
import javafx.util.Duration
import org.seekloud.thor.ClientBoot
import org.seekloud.thor.actor.WsClient
import org.seekloud.thor.common.StageContext
import org.seekloud.thor.game.NetWorkInfo
import org.seekloud.thor.model.GameServerInfo
import org.seekloud.thor.protocol.ThorClientProtocol
import org.seekloud.thor.scene.GameScene
import org.seekloud.thor.shared.ptcl.config.ThorGameConfigImpl
import org.seekloud.thor.shared.ptcl.model.Constants.GameState
import org.seekloud.thor.shared.ptcl.model.{Point, Score}
import org.seekloud.thor.shared.ptcl.protocol.ThorGame
import org.seekloud.thor.shared.ptcl.protocol.ThorGame._
import org.seekloud.thor.shared.ptcl.thor.ThorSchemaClientImpl
import org.seekloud.thor.scene.PreDraw
import org.seekloud.thor.ClientBoot.{executor, scheduler}
import org.seekloud.thor.scene.DrawScene
import org.slf4j.LoggerFactory

/**
  * User: Jason
  * Date: 2019/3/7
  * Time: 20:53
  */
class GameController(
  wsClient: ActorRef[WsClient.WsCommand],
  playerInfo: WsClient.PlayerInfo,
  context: StageContext,
  playGameScreen: GameScene
) extends NetWorkInfo{

  private[this] val log = LoggerFactory.getLogger(this.getClass)

  def getPlayer: WsClient.PlayerInfo = playerInfo

  private val ws = wsClient

  def getWs: ActorRef[WsClient.WsCommand] = ws

  private val gameScene = playGameScreen

  def getGs: GameScene = gameScene

  protected var firstCome = true

  private val actionSerialNumGenerator = new AtomicInteger(0)

  private val preExecuteFrameOffset = org.seekloud.thor.shared.ptcl.model.Constants.preExecuteFrameOffset

  protected val preDrawFrame = new PreDraw
  //  private var recYourInfo: Boolean = false
  //  private var recSyncGameState: Option[ThorGame.GridSyncState] = None

  var thorSchemaOpt: Option[ThorSchemaClientImpl] = None
  private val window = Point(playGameScreen.canvasBoundary.x - 12, playGameScreen.canvasBoundary.y - 12.toFloat)
  var gameState = GameState.loadingPlay
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


  private var mouseLeft = true

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
      drawGameByTime(System.currentTimeMillis() - logicFrameTime, playGameScreen.canvasUnit, playGameScreen.canvasBounds)
    }
  }

  protected def setGameState(s: Int): Unit = {
    gameState = s
  }

  def start(): Unit = {
    if (firstCome) {
      firstCome = false
      println("start...")
//      wsClient ! WsClient.StartGame(playerInfo.roomId)
      startGameLoop()
      addUserActionListenEvent
      checkAndChangePreCanvas()
      logicFrameTime = System.currentTimeMillis()
    } else {
      println(s"restart...")
      thorSchemaOpt.foreach { r =>
        wsClient ! WsClient.DispatchMsg(ThorGame.RestartGame)
        setGameState(GameState.loadingPlay)
//        wsClient ! WsClient.StartGameLoop
      }
    }
  }
  import scala.concurrent.duration._
  def startGameLoop(): Unit = { //渲染帧
    logicFrameTime = System.currentTimeMillis()
    timeline.setCycleCount(Animation.INDEFINITE)
//    val keyFrame = new KeyFrame(Duration.millis(100), { _ =>
//      logicLoop()
//    })
    scheduler.schedule(10.millis, 150.millis) {
      logicLoop()
    }
//    timeline.getKeyFrames.add(keyFrame)
    animationTimer.start()
    timeline.play()
  }

  def drawGameByTime(offsetTime: Long, canvasUnit: Float, canvasBounds: Point): Unit = {
    thorSchemaOpt match {
      case Some(thorSchema: ThorSchemaClientImpl) =>
        if (thorSchema.adventurerMap.contains(mainId)) {
          val start = System.currentTimeMillis()
//          thorSchema.drawGame4Client(mainId, offsetTime, canvasUnit, canvasBounds)
          val drawScene: DrawScene = new DrawScene(thorSchema)
          drawScene.drawGame4Client(mainId, offsetTime, canvasUnit, canvasBounds)
          val a = System.currentTimeMillis()
          thorSchema.drawRank(currentRank, CurrentOrNot = true, byteId)
          thorSchema.drawSmallMap(mainId)

//          println(s"the span is ${b-a}")
          drawTime = drawTime :+ System.currentTimeMillis() - start
          if (drawTime.length >= drawTimeSize) {
            drawTimeLong = drawTime.sum / drawTime.size
            drawTime = Nil
          }
          if (frameTime.length >= frameTimeSize) {
            frameTimeLong = frameTime.sum / frameTime.size
            frameTime = Nil
          }
          //          println(s"${if(frameTimeSingle>10) "!!!!!!!!!!!!!" else ""} 逻辑帧时间：$frameTimeSingle")
          thorSchema.drawNetInfo(getNetworkLatency, drawTimeLong, frameTimeSingle, currentRank.length)
          if (barrageTime > 0) {

            drawScene.drawBarrage(barrage._1, barrage._2)
            barrageTime -= 1
          }
        }
        else {
          thorSchema.drawGameLoading()
        }

      case None =>

    }
  }

  var lastSendReq = 0L

  def logicLoop(): Unit ={
    var myLevel = 0
    thorSchemaOpt.foreach { thorSchema =>
      thorSchema.adventurerMap.get(mainId).foreach {
        ad => myLevel = ad.getAdventurerState.level
      }
    }
    ClientBoot.addToPlatform {
      val (boundary, unit) = playGameScreen.handleResize(myLevel)
      if (unit != 0) {
        thorSchemaOpt.foreach { r =>
          r.updateSize(boundary, unit)
        }
      }
      logicFrameTime = System.currentTimeMillis()

      gameState match {
        case GameState.firstCome =>
          thorSchemaOpt.foreach(_.drawGameLoading())
        case GameState.loadingPlay =>
//          println("loading play")
          thorSchemaOpt.foreach(_.drawGameLoading())
        case GameState.stop =>
          thorSchemaOpt.foreach {
            _.update()
          }
          frameTime :+ System.currentTimeMillis() - logicFrameTime
          logicFrameTime = System.currentTimeMillis()
          ping()
        case GameState.replayLoading =>
          thorSchemaOpt.foreach {
            _.drawGameLoading()
          }
        case GameState.play =>
          //        if (Shortcut.isPaused("bgm-2")) {
          //          Shortcut.playMusic("bgm-2")
          //        }
          thorSchemaOpt.foreach { thorSchema =>
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

          ping()

        case _ => log.info(s"state=$gameState failed")
      }
    }
  }

  def wsMessageHandle(data: ThorGame.WsMsgServer): Unit = {

    ClientBoot.addToPlatform{
      data match {
        case e: ThorGame.YourInfo =>
          println(s"start---------")
          gameMusicPlayer.play()
          mainId = e.id
          try {
            thorSchemaOpt = Some(ThorSchemaClientImpl(playGameScreen.drawFrame, playGameScreen.getCanvasContext, e.config, e.id, e.name, playGameScreen.canvasBoundary, playGameScreen.canvasUnit))
            gameConfig = Some(e.config)
            checkAndChangePreCanvas()
            e.playerIdMap.foreach { p => thorSchemaOpt.foreach { t => t.playerIdMap.put(p._1, p._2)}}
            startGameLoop()
//            wsClient ! WsClient.StartGameLoop
            gameState = GameState.play

          } catch {
            case e: Exception =>
              closeHolder
              println(e.getMessage)
              println("client is stop!!!")
          }


        case x@RestartYourInfo =>
          println(x)
          mainId = playerInfo.playerId
          gameState = GameState.play

        case e: ThorGame.BeAttacked =>
          println(s"receive attacked msg:\n $e")
          barrage = (e.killerName, e.name)
          barrageTime = 300
          println(s"be attacked by ${e.killerName}")
          if (e.playerId == mainId) {
            mainId = e.killerId //跟随凶手视角
            if (e.playerId == playerInfo.playerId) {
              gameState = GameState.stop
              endTime = System.currentTimeMillis()
              thorSchemaOpt match {
                case Some(thorSchema: ThorSchemaClientImpl) =>
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
          //          if (e.playerId == playerInfo.playerId) {
          ////            gameState = GameState.stop
          //            wsActor ! PlayGameActor.StopGameLater
          //            endTime = System.currentTimeMillis()
          //            val time = duringTime(endTime - startTime)
          //            thorSchemaOpt.foreach { thorSchema =>
          //              if (thorSchema.adventurerMap.contains(playerInfo.playerId)) {
          //                thorSchema.adventurerMap.get(playerInfo.playerId).foreach { my =>
          //                  thorSchema.killerNew = e.killerName
          //                  thorSchema.duringTime = time
          //                  killerName = e.killerName
          //                  killNum = my.killNum
          //                  energy = my.energy
          //                  level = my.level
          //                }
          //
          //              }
          //            }
          //          }
          thorSchemaOpt.foreach(_.receiveGameEvent(e))


        case e: ThorGame.Ranks =>
          currentRank = e.currentRank
        //          historyRank = e.historyRank

        case e: ThorGame.GridSyncState =>
          thorSchemaOpt.foreach(_.receiveThorSchemaState(e.d))
          justSynced = true

        case UserMap(map) =>
          println(s"userMap ---- $map")
          thorSchemaOpt.foreach { grid =>
            map.foreach(p => grid.playerIdMap.put(p._1, p._2))
            grid.needUserMap = false
          }

        case e: PingPackage =>
          receivePingPackage(e)

        case RebuildWebSocket =>
          thorSchemaOpt.foreach(_.drawReplayMsg("存在异地登录"))
          closeHolder

        case e: ThorGame.UserActionEvent =>
          thorSchemaOpt.foreach(_.receiveUserEvent(e))

        case e: ThorGame.GameEvent =>
          e match {
            case event: ThorGame.UserEnterRoom =>
              if (thorSchemaOpt.nonEmpty) {
                thorSchemaOpt.get.playerIdMap.put(event.shortId, (event.playerId, event.name))
                if (event.playerId == playerInfo.playerId) byteId = event.shortId
              } else {
//                wsClient ! WsClient.UserEnterRoom(event)
              }
            case event: UserLeftRoom =>
              if (event.shortId == byteId) println(s"${event.shortId}  ${event.playerId} ${event.name} left room...")
              thorSchemaOpt.foreach(thorSchema => thorSchema.playerIdMap.remove(event.shortId))
            case _ =>
          }
          thorSchemaOpt.foreach(_.receiveGameEvent(e))

        case x => println(s"接收到无效消息$x")

      }
    }

  }

  private def closeHolder: Unit = {
    animationTimer.stop()
    //remind 此处关闭WebSocket
    wsClient ! WsClient.Stop
  }

  def getActionSerialNum: Byte = (actionSerialNumGenerator.getAndIncrement() % 127).toByte

  var lastMouseMove = 0l //限制只能发一次mousemove
  val frequency = 50

  private def addUserActionListenEvent: Unit = {
    playGameScreen.canvas.getCanvas.requestFocus()

    /*鼠标移动操作*/
    playGameScreen.canvas.getCanvas.setOnMouseMoved { e =>
      val point = Point(e.getX.toFloat, e.getY.toFloat)
      val theta = point.getTheta(playGameScreen.canvasBounds * playGameScreen.canvasUnit / 2).toFloat
      thorSchemaOpt.foreach { thorSchema =>

        if (thorSchema.adventurerMap.contains(playerInfo.playerId)) {
          val mouseDistance = math.sqrt(math.pow(e.getX - playGameScreen.screen.getWidth / 2.0, 2) + math.pow(e.getY - playGameScreen.screen.getHeight / 2.0, 2))
          val r = gameConfig.get.getAdventurerRadiusByLevel(thorSchema.adventurerMap(playerInfo.playerId).getAdventurerState.level) * playGameScreen.canvasUnit
          val direction = thorSchema.adventurerMap(playerInfo.playerId).direction
          if (System.currentTimeMillis() > lastMouseMove + frequency && math.abs(theta - direction) > 0.3) { //角度差大于0.3才执行

            val offsetX = (e.getX - playGameScreen.screen.getWidth / 2.0).toShort
            val offsetY = (e.getY - playGameScreen.screen.getHeight / 2.0).toShort
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
    playGameScreen.canvas.getCanvas.setOnMousePressed { e =>
            println(s"left: [${e.isPrimaryButtonDown}]; right: [${e.isSecondaryButtonDown}]")
      thorSchemaOpt.foreach { thorSchema =>
        if (gameState == GameState.play && thorSchema.adventurerMap.exists(_._1 == playerInfo.playerId) && !thorSchema.dyingAdventurerMap.exists(_._1 == playerInfo.playerId)) {
          if (e.isPrimaryButtonDown) {
            attackMusic.play()
            mouseLeft = true
            val preExecuteAction = MouseClickDownLeft(byteId, thorSchema.systemFrame + preExecuteFrameOffset, getActionSerialNum)
            thorSchema.preExecuteUserEvent(preExecuteAction)
            wsClient ! WsClient.DispatchMsg(preExecuteAction)
          }
          else if (e.isSecondaryButtonDown) {
            mouseLeft = false
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
    playGameScreen.canvas.getCanvas.setOnMouseReleased { e =>
      thorSchemaOpt.foreach { thorSchema =>
        if (gameState == GameState.play && thorSchema.adventurerMap.exists(_._1 == playerInfo.playerId) && !thorSchema.dyingAdventurerMap.exists(_._1 == playerInfo.playerId)) {
          if (!mouseLeft) { //右键
            val preExecuteAction = MouseClickUpRight(byteId, thorSchema.systemFrame + preExecuteFrameOffset, getActionSerialNum)
            thorSchema.preExecuteUserEvent(preExecuteAction)
            wsClient ! WsClient.DispatchMsg(preExecuteAction)
          }
          else ()
        }
      }
    }

    /*键盘事件*/
    playGameScreen.canvas.getCanvas.setOnKeyPressed { e =>
      thorSchemaOpt.foreach { thorSchema =>
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

  def checkAndChangePreCanvas(): Unit ={
    val timer = new Timer
    def timerTask(fun: => Unit) = new TimerTask {
      override def run(): Unit = fun
    }

    (preDrawFrame.foodCanvas, preDrawFrame.adventurerCanvas, preDrawFrame.weaponCanvas, preDrawFrame.deathCanvas) match{
      case (Nil, Nil, Nil, Nil) =>
        timer.schedule(timerTask(checkAndChangePreCanvas()), 1000)
      case (foodCanvas, Nil, Nil, Nil) =>
        thorSchemaOpt.foreach(_.changePreCanvas(foodCanvas, Nil, Nil, Nil))
        thorSchemaOpt.foreach(_.changePreImage(foodCanvas, Nil, Nil, Nil))
        timer.schedule(timerTask(checkAndChangePreCanvas()), 1000)
      case (foodCanvas, adventurerCanvas, Nil, Nil) =>
        thorSchemaOpt.foreach(_.changePreCanvas(foodCanvas, adventurerCanvas, Nil, Nil))
        thorSchemaOpt.foreach(_.changePreImage(foodCanvas, adventurerCanvas, Nil, Nil))
        timer.schedule(timerTask(checkAndChangePreCanvas()), 1000)
      case (foodCanvas, adventurerCanvas, weaponCanvas, Nil) =>
        thorSchemaOpt.foreach(_.changePreCanvas(foodCanvas, adventurerCanvas, weaponCanvas, Nil))
        thorSchemaOpt.foreach(_.changePreImage(foodCanvas, adventurerCanvas, weaponCanvas, Nil))
      case (foodCanvas, adventurerCanvas, weaponCanvas, deathCanvas) =>
        thorSchemaOpt.foreach(_.changePreCanvas(foodCanvas, adventurerCanvas, weaponCanvas, deathCanvas))
        thorSchemaOpt.foreach(_.changePreImage(foodCanvas, adventurerCanvas, weaponCanvas, deathCanvas))
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
