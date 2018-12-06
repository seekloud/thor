package org.seekloud.thor.controller

import java.util.concurrent.atomic.AtomicInteger

import javafx.animation.{Animation, AnimationTimer, KeyFrame, Timeline}
import javafx.scene.media.{AudioClip, Media, MediaPlayer}
import javafx.util.Duration
import akka.actor.typed.scaladsl.adapter._
import org.seekloud.thor.App
import org.seekloud.thor.game.NetWorkInfo
import org.seekloud.thor.App.system
import org.seekloud.thor.core.PlayGameActor
import org.seekloud.thor.shared.ptcl.model.Constants.GameState
import org.seekloud.thor.shared.ptcl.protocol.ThorGame
import org.seekloud.thor.shared.ptcl.thor.ThorSchemaClientImpl
import org.slf4j.LoggerFactory

/**
  * @author Jingyi
  * @version 创建时间：2018/12/3
  *
  * updated by TangYaruo
  */
class PlayGameController extends NetWorkInfo{

  private val log = LoggerFactory.getLogger(this.getClass)
  val playGameActor = system.spawn(PlayGameActor.create(this), "playGameActor")

  protected var firstCome = true

  private val actionSerialNumGenerator = new AtomicInteger(0)

  private val preExecuteFrameOffset = org.seekloud.thor.shared.ptcl.model.Constants.preExecuteFrameOffset

  private var recvYourInfo: Boolean = false
  private var recvSyncGameState: Option[ThorGame.GridSyncState] = None

  protected var thorSchemaOpt: Option[ThorSchemaClientImpl] = None

  private var gameState = GameState.loadingPlay
  private var logicFrameTime = System.currentTimeMillis()











}
