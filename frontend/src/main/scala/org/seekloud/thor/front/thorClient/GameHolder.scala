package org.seekloud.thor.front.thorClient

import java.util.concurrent.atomic.AtomicInteger

import org.scalajs.dom.raw.HTMLAudioElement
import org.seekloud.thor.front.common.PreDraw
import org.seekloud.thor.front.utils.middleware.MiddleFrameInJs
import org.seekloud.thor.shared.ptcl.config.ThorGameConfigImpl
import org.seekloud.thor.shared.ptcl.model.Constants
import org.seekloud.thor.shared.ptcl.model.Constants.GameState
import org.seekloud.thor.shared.ptcl.thor.ThorSchemaClientImpl

//import org.seekloud.thor.front.utils.byteObject.MiddleBufferInJs
import org.seekloud.thor.front.utils.{JsFunc, Shortcut}
import org.seekloud.thor.shared.ptcl
import org.seekloud.thor.shared.ptcl.config.ThorGameConfig
import org.seekloud.thor.shared.ptcl.model.{Boundary, Point, Score}
import org.seekloud.thor.shared.ptcl.protocol.ThorGame._
import org.seekloud.thor.shared.ptcl.protocol._
import org.seekloud.thor.shared.ptcl.thor.ThorSchemaState
import mhtml.Var
import org.scalajs.dom.Blob
import org.scalajs.dom.ext.{Color, KeyCode}
import org.scalajs.dom.html.{Canvas, Image}
import org.scalajs.dom.raw.{Event, FileReader, MessageEvent, MouseEvent}

import scala.collection.mutable
import scala.scalajs.js.typedarray.ArrayBuffer
import org.seekloud.byteobject.ByteObject.bytesDecode
import org.seekloud.byteobject.MiddleBufferInJs
import scala.xml.Elem
import org.scalajs.dom

import scala.language.implicitConversions

/**
  * Created by Jingyi on 2018/11/9
  */
abstract class GameHolder(canvasName: String) extends NetworkInfo {

  println("GameHolder ...")

  import io.circe._, io.circe.generic.auto.exportDecoder, io.circe.parser._, io.circe.syntax._

  val drawFrame = new MiddleFrameInJs

  protected var canvasWidth = dom.window.innerWidth.toFloat
  protected var canvasHeight = dom.window.innerHeight.toFloat
  protected val canvas = drawFrame.createCanvas(canvasName, canvasWidth, canvasHeight)
  protected val ctx = canvas.getCtx

  protected val preDrawFrame = new PreDraw

//  protected val bounds = Point(Boundary.w,Boundary.h)

  protected var canvasBoundary = Point(dom.window.innerWidth.toFloat, dom.window.innerHeight.toFloat)
  protected var canvasUnit = canvasWidth / Constants.canvasUnitPerLine
  protected var canvasBounds = canvasBoundary / canvasUnit

  var thorSchemaOpt : Option[ThorSchemaClientImpl] = None

  var gameState: Int = GameState.firstCome

  //  var thorSchema = thorSchemaOpt.get
  protected var myId = "test"
  protected var mainId = "test" //主视角ID（方便死亡跟随）
  protected var shortId = 0
  protected var myName = "testName"
  protected var killer = "someone"
  protected var startTime = 0l
  protected var endTime = 0l
  protected var gameConfig: Option[ThorGameConfigImpl] = None
  protected var firstCome = true
  protected var currentRank = List.empty[Score]
  protected var historyRank = List.empty[Score]

  var drawTime:List[Long] = Nil
  var drawTimeLong = 0l
  var drawTimeSize = 60
  var frameTime:List[Long] = Nil
  var frameTimeLong = 0l
  var frameTimeSize = 10
  var frameTimeSingle = 0l



  protected val websocketClient = new WebSocketClient(wsConnectSuccess, wsConnectError, wsMessageHandler, wsConnectClose)

  var justSynced = false


//  canvas.width = canvasBoundary.x.toInt
//  canvas.height = canvasBoundary.y.toInt


  protected var timer: Int = 0
  protected var nextFrame = 0
  protected var logicFrameTime = System.currentTimeMillis()

  var barrage = ""
  var barrageTime = 0

  protected var killerName = ""
  protected var killNum = 0
  protected var energy = 0
  protected var level = 0



  def gameRender(): Double => Unit = { d =>
    val curTime = System.currentTimeMillis()
    val offsetTime = curTime - logicFrameTime
    drawGameByTime(offsetTime, canvasUnit, canvasBounds)
    if(gameState == GameState.stop && thorSchemaOpt.nonEmpty) thorSchemaOpt.foreach(_.drawGameStop(killerName, killNum, energy, level))
    nextFrame = dom.window.requestAnimationFrame(gameRender())
  }

  protected def handleResize = {
    val width = dom.window.innerWidth.toFloat
    val height = dom.window.innerHeight.toFloat
    if(width != canvasWidth || height != canvasHeight){
      canvasWidth = width
      canvasHeight = height
      canvasUnit = canvasWidth / Constants.canvasUnitPerLine
      canvasBoundary = Point(canvasWidth, canvasHeight)
      canvasBounds = canvasBoundary / canvasUnit
      canvas.setWidth(canvasWidth)
      canvas.setHeight(canvasHeight)
      thorSchemaOpt.foreach(_.updateSize(canvasBoundary, canvasUnit))
    }
  }


  protected def wsConnectSuccess(e: Event) = {
    println(s"连接服务器成功")
    e
  }


  protected def wsConnectError(e: Event) = {
    JsFunc.alert("网络连接错误，请重新刷新")
    e
  }


  protected def wsConnectClose(e: Event) = {
    JsFunc.alert("网络连接失败，请重新刷新")
    e
  }

  protected def wsMessageHandler(e: WsMsgServer)


  def closeHolder={
    dom.window.cancelAnimationFrame(nextFrame)
    Shortcut.cancelSchedule(timer)
    websocketClient.closeWs
  }


  protected def sendMsg2Server(msg:ThorGame.WsMsgFront):Unit ={
      websocketClient.sendMsg(msg)
  }



  var tickCount = 0L
//  var testStartTime = System.currentTimeMillis()
//  var testEndTime = System.currentTimeMillis()
//  var startTime = System.currentTimeMillis()

  var lastSendReq = 0L
  protected def gameLoop(): Unit = {
    handleResize
    logicFrameTime = System.currentTimeMillis()
    gameState match{
      case GameState.firstCome =>
        drawGameLoading()
      case GameState.loadingPlay =>
        thorSchemaOpt.foreach{ _.drawGameLoading()}
      case GameState.stop =>
        thorSchemaOpt.foreach{ _.update()}
        frameTime :+ System.currentTimeMillis() - logicFrameTime
        logicFrameTime = System.currentTimeMillis()
        ping()
      case GameState.replayLoading =>
        thorSchemaOpt.foreach{ _.drawGameLoading()}
      case GameState.play =>
//        if (Shortcut.isPaused("bgm-2")) {
//          Shortcut.playMusic("bgm-2")
//        }
        thorSchemaOpt.foreach{ thorSchema =>
          thorSchema.update()
          if( thorSchema.needUserMap && logicFrameTime - lastSendReq > 5000){
            println("request for user map")
            websocketClient.sendMsg(UserMapReq)
            lastSendReq = System.currentTimeMillis()
          }
        }
        frameTime = frameTime :+ System.currentTimeMillis() - logicFrameTime
        frameTimeSingle = System.currentTimeMillis() - logicFrameTime
        logicFrameTime = System.currentTimeMillis()

        ping()
    }
  }



  def drawGameByTime(offsetTime: Long, canvasUnit: Float, canvasBounds: Point): Unit = {
    thorSchemaOpt match{
      case Some(thorSchema: ThorSchemaClientImpl) =>
        if(thorSchema.adventurerMap.contains(mainId)){
          val start = System.currentTimeMillis()
          thorSchema.drawGame(mainId, offsetTime, canvasUnit, canvasBounds)
          thorSchema.drawRank(currentRank,true,myId)
          drawTime = drawTime :+ System.currentTimeMillis() - start
          if(drawTime.length >= drawTimeSize){
            drawTimeLong = drawTime.sum / drawTime.size
            drawTime = Nil
          }
          if(frameTime.length >= frameTimeSize){
            frameTimeLong = frameTime.sum / frameTime.size
            frameTime = Nil
          }
//          println(s"${if(frameTimeSingle>10) "!!!!!!!!!!!!!" else ""} 逻辑帧时间：$frameTimeSingle")
          thorSchema.drawNetInfo(getNetworkLatency, drawTimeLong, frameTimeSingle)
          if (barrageTime > 0){
            thorSchema.drawBarrage(barrage)
            barrageTime -= 1
          }
        }
        else{
          drawGameLoading()
        }

      case None =>

    }

  }

  def checkAndChangePreCanvas(): Unit ={
    (preDrawFrame.foodCanvas, preDrawFrame.adventurerCanvas, preDrawFrame.weaponCanvas) match{
      case (Nil, Nil, Nil) =>
        dom.window.setTimeout(()=>checkAndChangePreCanvas(), 1000)
      case (foodCanvas, Nil, Nil) =>
        thorSchemaOpt.foreach(_.changePreCanvas(foodCanvas, Nil, Nil))
        dom.window.setTimeout(()=>checkAndChangePreCanvas(), 1000)
      case (foodCanvas, adventurerCanvas, Nil) =>
        thorSchemaOpt.foreach(_.changePreCanvas(foodCanvas, adventurerCanvas, Nil))
        dom.window.setTimeout(()=>checkAndChangePreCanvas(), 1000)
      case (foodCanvas, adventurerCanvas, weaponCanvas) =>
        thorSchemaOpt.foreach(_.changePreCanvas(foodCanvas, adventurerCanvas, weaponCanvas))
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

  def drawGameLoading(): Unit = {
    println("loading")
    ctx.setFill("#000000")
    ctx.fillRec(0, 0, canvasWidth, canvasHeight)
    ctx.setFill("rgb(250, 250, 250)")
    ctx.setTextAlign("left")
    ctx.setFont("Helvetica", 36)
    if(gameState == GameState.stop)
      ctx.fillText("您观战的玩家已离开房间", 150, 180)
    else
      ctx.fillText("请稍等，正在连接服务器", 150, 180)

  }
}
