package org.seekloud.thor.controller

import java.util.concurrent.atomic.AtomicInteger

import javafx.animation.{Animation, AnimationTimer, KeyFrame, Timeline}
import javafx.scene.media.{AudioClip, Media, MediaPlayer}
import javafx.util.Duration
import akka.actor.typed.scaladsl.adapter._
import org.seekloud.thor.App
import org.seekloud.thor.game.NetWorkInfo
import org.seekloud.thor.App.system
import org.seekloud.thor.common.Context
import org.seekloud.thor.core.PlayGameActor
import org.seekloud.thor.core.PlayGameActor.DispatchMsg
import org.seekloud.thor.model.{GameServerInfo, PlayerInfo}
import org.seekloud.thor.shared.ptcl.model.Constants.GameState
import org.seekloud.thor.shared.ptcl.model.{Point, Score}
import org.seekloud.thor.shared.ptcl.protocol.ThorGame
import org.seekloud.thor.shared.ptcl.protocol.ThorGame.{MouseMove, PingPackage}
import org.seekloud.thor.shared.ptcl.thor.ThorSchemaClientImpl
import org.seekloud.thor.view.PlayGameView
import org.slf4j.LoggerFactory

/**
  * @author Jingyi
  * @version 创建时间：2018/12/3
  *
  *          updated by TangYaruo
  */
class PlayGameController(
  playerInfo: PlayerInfo,
  gameServerInfo: GameServerInfo,
  context: Context,
  playGameScreen: PlayGameView,
  roomInfo: Option[String] = None
) extends NetWorkInfo {

  private val log = LoggerFactory.getLogger(this.getClass)
  val playGameActor = system.spawn(PlayGameActor.create(this), "playGameActor")

  protected var firstCome = true

  private val actionSerialNumGenerator = new AtomicInteger(0)

  private val preExecuteFrameOffset = org.seekloud.thor.shared.ptcl.model.Constants.preExecuteFrameOffset

//  private var recYourInfo: Boolean = false
//  private var recSyncGameState: Option[ThorGame.GridSyncState] = None

  protected var thorSchemaOpt: Option[ThorSchemaClientImpl] = None

  private var gameState = GameState.loadingPlay
  private var logicFrameTime = System.currentTimeMillis()

  protected var currentRank = List.empty[Score]
  protected var historyRank = List.empty[Score]

  var barrage = ""
  var barrageTime = 0

  protected var startTime = 0l
  protected var endTime = 0l

  var justSynced = false




  private val animationTimer = new AnimationTimer() {
    override def handle(now: Long): Unit = {
      drawGameByTime(System.currentTimeMillis() - logicFrameTime)
    }
  }

  private def drawGameByTime(offsetTime: Long): Unit = {
    thorSchemaOpt.foreach { thorSchema =>
      if (thorSchema.adventurerMap.contains(playerInfo.playerId)) {
        thorSchema.drawGame(offsetTime, playGameScreen.canvasUnit, playGameScreen.canvasBounds)
        thorSchema.drawRank(historyRank, CurrentOrNot = false, playerInfo.playerId)
        thorSchema.drawRank(currentRank, CurrentOrNot = true, playerInfo.playerId)
        thorSchema.drawNetInfo(getNetworkLatency)
        if (barrageTime > 0) {
          thorSchema.drawBarrage(barrage, playGameScreen.canvasBoundary.x * 0.5, playGameScreen.canvasBoundary.y * 0.17)
          barrageTime -= 1
        }
      }
    }
  }

  protected def setGameState(s: Int): Unit = {
    gameState = s
  }

  def getActionSerialNum: Int = actionSerialNumGenerator.getAndIncrement()


  def start = {
    if (firstCome) {
      firstCome = false
      println("start!!!!!!!")
      playGameActor ! PlayGameActor.ConnectGame(playerInfo, gameServerInfo, roomInfo)
      addUserActionListenEvent
      logicFrameTime = System.currentTimeMillis()
    } else {
      thorSchemaOpt.foreach { r =>
        playGameActor ! DispatchMsg(ThorGame.RestartGame(r.myName))
        setGameState(GameState.loadingPlay)
        playGameActor ! PlayGameActor.StartGameLoop
      }
    }
  }


  def logicLoop() = {
    App.pushStack2AppThread {
      val (boundary, unit) = playGameScreen.handleResize
      if (unit != 0) {
        thorSchemaOpt.foreach { r =>
          r.updateSize(boundary, unit)
        }
      }
      gameState match {
        case GameState.loadingPlay =>
          thorSchemaOpt.foreach(_.drawGameLoading())
        case GameState.play =>

          /** */
          thorSchemaOpt.foreach(_.update())
          logicFrameTime = System.currentTimeMillis()
          ping()

        case GameState.stop =>
          closeHolder
          thorSchemaOpt.foreach(_.drawGameStop())
        //          timeline.play()


        case _ => log.info(s"state=$gameState failed")
      }
    }
  }

  private def closeHolder = {
    animationTimer.stop()
    //remind 此处关闭WebSocket
    playGameActor ! PlayGameActor.StopGameActor
  }


  private def addUserActionListenEvent: Unit = {
    playGameScreen.canvas.getCanvas.requestFocus()

    /*鼠标移动操作*/
    playGameScreen.canvas.getCanvas.setOnMouseMoved { e =>
      val point = Point(e.getX.toFloat, e.getY.toFloat)
      val theta = point.getTheta(playGameScreen.canvasBounds * playGameScreen.canvasUnit / 2).toFloat
      if (thorSchemaOpt.nonEmpty && gameState == GameState.play) {
        thorSchemaOpt.foreach { thorSchema =>
          if (thorSchema.adventurerMap.contains(playerInfo.playerId)) {
            val mouseDistance = math.sqrt(math.pow(e.getX - playGameScreen.screen.getWidth / 2.0, 2) + math.pow(e.getY - playGameScreen.screen.getHeight / 2.0, 2))
            val preExecuteAction = MouseMove(thorSchema.myId, theta, mouseDistance.toFloat, thorSchema.systemFrame + preExecuteFrameOffset, getActionSerialNum)
            thorSchema.preExecuteUserEvent(preExecuteAction)
            playGameActor ! DispatchMsg(preExecuteAction)
          }
        }
      }

    }

    /*鼠标点击事件*/
    playGameScreen.canvas.getCanvas.setOnMousePressed { e =>

    }

    /*鼠标释放事件*/
    playGameScreen.canvas.getCanvas.setOnMouseDragReleased { e =>

    }

    /*键盘事件*/
    playGameScreen.canvas.getCanvas.setOnKeyPressed { e =>

    }

  }

  def wsMessageHandle(data: ThorGame.WsMsgServer): Unit = {

    App.pushStack2AppThread {
      data match {
        case e: ThorGame.YourInfo =>
          println(s"start---------")
          try {
            thorSchemaOpt = Some(ThorSchemaClientImpl(playGameScreen.drawFrame, playGameScreen.getCanvasContext, e.config, e.id, e.name, playGameScreen.canvasBoundary, playGameScreen.canvasUnit))
//            recYourInfo = true
//            recSyncGameState.foreach(t => wsMessageHandle(t))
            animationTimer.start()
            playGameActor ! PlayGameActor.StartGameLoop
            gameState = GameState.play

          } catch {
            case e: Exception =>
              closeHolder
              println(e.getMessage)
              println("client is stop!!!")
          }
        case e: ThorGame.UserEnterRoom =>
          barrage = s"${playerInfo.nickName}加入了游戏"
          barrageTime = 300

        case e: ThorGame.UserLeftRoom =>
          barrage = s"${playerInfo.nickName}离开了游戏"
          barrageTime = 300
          thorSchemaOpt.foreach( thorSchema => thorSchema.leftGame(playerInfo.playerId, playerInfo.nickName))

        case e: ThorGame.BeAttacked =>
          gameState = GameState.stop
          barrage = s"${e.killerName}杀死了${e.name}"
          barrageTime = 300
          endTime = System.currentTimeMillis()
          println(s"be attacked by $e.killerName")
          val time = duringTime(endTime - startTime)
          thorSchemaOpt.foreach { thorSchema =>
            if (thorSchema.adventurerMap.contains(playerInfo.playerId)){
              thorSchema.killerNew = e.killerName
              thorSchema.duringTime = time
            }
            thorSchema.drawGameStop()
          }
          animationTimer.stop()

        case e: ThorGame.Ranks =>
          currentRank = e.currentRank
          historyRank = e.historyRank

        case e: ThorGame.GridSyncState =>
          thorSchemaOpt.foreach(_.receiveThorSchemaState(e.d))
          justSynced = true

        case e: PingPackage =>
          receivePingPackage(e)

        case e: ThorGame.UserActionEvent =>
          thorSchemaOpt.foreach(_.receiveUserEvent(e))

        case e: ThorGame.GameEvent =>
          thorSchemaOpt.foreach(_.receiveGameEvent(e))

        case x => println(s"接收到无效消息$x")

      }
    }

  }

  def duringTime(time:Long) = {
    var remain = time / 1000 % 86400
    val hour = remain / 3600
    remain = remain % 3600
    val min = remain / 60
    val sec = remain % 60
    val timeString = s"$hour : $min : $sec"
    timeString
  }


}
