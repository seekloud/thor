package org.seekloud.thor.front.thorClient

import java.util.concurrent.atomic.AtomicInteger

import org.seekloud.thor.shared.ptcl.config.ThorGameConfigImpl
import org.seekloud.thor.shared.ptcl.model.Constants.GameState

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


  protected val canvas = dom.document.getElementById(canvasName).asInstanceOf[Canvas]
  protected val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
  protected var canvasWidth = dom.window.innerWidth.toFloat
  protected var canvasHeight = dom.window.innerHeight.toFloat

//  protected val bounds = Point(Boundary.w,Boundary.h)

  protected val canvasUnit = 10
  protected val canvasBoundary = Point(dom.window.innerWidth.toFloat, dom.window.innerHeight.toFloat)


  protected val canvasBounds = canvasBoundary / canvasUnit
  println(canvasBounds)

  var thorSchemaOpt : Option[ThorSchemaClientImpl] = None

  //  var thorSchema = thorSchemaOpt.get
  protected var myId = "test"
  protected var myName = "testName"
  protected var killer = "someone"
  protected var startTime = 0l
  protected var endTime = 0l
  protected var gameConfig: Option[ThorGameConfigImpl] = None
  protected var firstCome = true
  protected var currentRank = List.empty[Score]
  protected var historyRank = List.empty[Score]



  protected val websocketClient = new WebSocketClient(wsConnectSuccess, wsConnectError, wsMessageHandler, wsConnectClose)

  var justSynced = false


  canvas.width = canvasBoundary.x.toInt
  canvas.height = canvasBoundary.y.toInt


  protected var timer: Int = 0
  protected var nextFrame = 0
  protected var logicFrameTime = System.currentTimeMillis()

  var barrage = ""
  var barrageTime = 0





  def gameRender(): Double => Unit = { d =>
    val curTime = System.currentTimeMillis()
    val offsetTime = curTime - logicFrameTime
    drawGameByTime(offsetTime, canvasUnit, canvasBounds)
    nextFrame = dom.window.requestAnimationFrame(gameRender())
  }

  protected def checkScreenSize={
    val newWidth=dom.window.innerWidth.toFloat
    val newHeight=dom.window.innerHeight.toFloat
    if(newWidth!=canvasWidth||newHeight!=canvasHeight){
      println("the screen size is change")
      canvasWidth = newWidth
      canvasHeight = newHeight
//      canvasUnit = getCanvasUnit(canvasWidth)
//      canvasBounds = Point(canvasWidth, canvasHeight)/canvasUnit
      println(s"update screen=${canvasUnit},=${(canvasWidth,canvasHeight)}")
      canvas.width = canvasWidth.toInt
      canvas.height = canvasHeight.toInt
      thorSchemaOpt.foreach{r=>
        r.updateClientSize(canvasBounds, canvasUnit)
      }
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

  protected def gameLoop(): Unit = {
    logicFrameTime = System.currentTimeMillis()
    thorSchemaOpt match{
      case Some(thorSchema: ThorSchemaClientImpl) =>
        thorSchema.update()
        ping()
      case None =>
    }
  }



  def drawGameByTime(offsetTime: Long, canvasUnit: Int, canvasBounds: Point): Unit = {
    println("drawGameByTime")
    thorSchemaOpt match{
      case Some(thorSchema: ThorSchemaClientImpl) =>
        if(thorSchema.adventurerMap.contains(myId)){
          thorSchema.drawGame(offsetTime, canvasUnit, canvasBounds)
          thorSchema.drawRank(historyRank,false,myId)
          thorSchema.drawRank(currentRank,true,myId)
          thorSchema.drawNetInfo(getNetworkLatency)
          if (barrageTime > 0){
            thorSchema.drawBarrage(barrage,canvasBoundary.x*0.5,canvasBoundary.y*0.17)
            barrageTime -= 1
          }
        }

      case None =>
          drawGameLoading()
    }

  }

  def drawGameLoading(): Unit = {
    println("loading")
    ctx.fillStyle = Color.Black.toString()
    ctx.fillRect(0, 0, dom.window.innerWidth, dom.window.innerHeight)
    ctx.fillStyle = "rgb(250, 250, 250)"
    ctx.textAlign = "left"
    ctx.textBaseline = "top"
    ctx.font = "36px Helvetica"
    ctx.fillText("请稍等，正在连接服务器", 150, 180)
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
