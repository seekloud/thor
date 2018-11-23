package org.seekloud.thor.front.thorClient

import java.util.concurrent.atomic.AtomicInteger

import org.seekloud.thor.shared.ptcl.config.ThorGameConfigImpl

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
class GameHolder(canvasName: String) {

  println("GameHolder ...")

  import io.circe._, io.circe.generic.auto.exportDecoder, io.circe.parser._, io.circe.syntax._


  private[this] val canvas = dom.document.getElementById(canvasName).asInstanceOf[Canvas]
  private[this] val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

//  private[this] val bounds = Point(Boundary.w,Boundary.h)

  private[this] val canvasUnit = 10
  private[this] val canvasBoundary = Point(dom.window.innerWidth.toFloat, dom.window.innerHeight.toFloat)

  private[this] val canvasBounds = canvasBoundary / canvasUnit
  println(canvasBounds)

  var thorSchemaOpt : Option[ThorSchemaClientImpl] = None
//  var thorSchema = thorSchemaOpt.get
  private[this] var myId = "test"
  private[this] var myName = "testName"
  private[this] var killer = "someone"
  private[this] var gameConfig: Option[ThorGameConfigImpl] = None
  private[this] var firstCome = true
  private[this] var currentRank = List.empty[Score]
  private[this] var historyRank = List.empty[Score]

  private[this] val actionSerialNumGenerator = new AtomicInteger(0)
  private[this] val websocketClient = new WebSocketClient(wsConnectSuccess, wsConnectError, wsMessageHandler, wsConnectClose)

  var justSynced = false

  canvas.width = canvasBoundary.x.toInt
  canvas.height = canvasBoundary.y.toInt


  private var timer: Int = 0
  private var nextFrame = 0
  private var logicFrameTime = System.currentTimeMillis()

  var barrage = ""
  var barrageTime = 0


  def getActionSerialNum = actionSerialNumGenerator.getAndIncrement()



  def gameRender(): Double => Unit = { d =>
    val curTime = System.currentTimeMillis()
    val offsetTime = curTime - logicFrameTime
    drawGameByTime(offsetTime, canvasUnit, canvasBounds)
    nextFrame = dom.window.requestAnimationFrame(gameRender())

  }


  private def wsConnectSuccess(e: Event) = {
    println(s"连接服务器成功")
    e
  }


  private def wsConnectError(e: Event) = {
    JsFunc.alert("网络连接错误，请重新刷新")
    e
  }


  private def wsConnectClose(e: Event) = {
    JsFunc.alert("网络连接失败，请重新刷新")
    e
  }


  private def wsMessageHandler(e: MessageEvent) = {
//    import org.seekloud.thor.front.utils.byteObject.ByteObject._
    e.data match {
      case blobMsg: Blob =>
        val fr = new FileReader()
        fr.readAsArrayBuffer(blobMsg)
        fr.onloadend = { _: Event =>
          val buf = fr.result.asInstanceOf[ArrayBuffer]
          val middleDataInJs = new MiddleBufferInJs(buf)
          bytesDecode[WsMsgServer](middleDataInJs) match {
            case Right(data) =>
//              dom.console.log(data.toString)
              data match {
                case YourInfo(config, id, name) =>
                  dom.console.log(s"get YourInfo ${config} ${id} ${name}")
                  myId = id
                  myName = name
                  gameConfig = Some(config)
                  thorSchemaOpt = Some(ThorSchemaClientImpl(ctx,config,id,name))
                  thorSchemaOpt.foreach{grid => timer = Shortcut.schedule(gameLoop,grid.config.frameDuration)}
                  nextFrame = dom.window.requestAnimationFrame(gameRender())
                  firstCome = false
                case UserEnterRoom(userId, name, _, _) =>
                  barrage = s"${name}加入了游戏"
                  barrageTime = 300

                case UserLeftRoom(userId, name, _) =>
                  barrage = s"${name}离开了游戏"
                  barrageTime = 300
                  println(s"user left $name")
                  thorSchemaOpt.foreach{ grid => grid.leftGame(userId, name)}

                case BeAttacked(userId, name, killerId, killerName, _) =>
                  barrage = s"${killerName}杀死了${name}"
                  barrageTime = 300
                  killer = killerName
                  println(s"be attacked by $killerName")
                  dom.window.cancelAnimationFrame(nextFrame)
                  dom.window.clearInterval(timer)
                  thorSchemaOpt.foreach{grid =>
                    grid.adventurerMap.remove(userId)
                    grid.drawGameStop(killerName)
                  }

                case Ranks(current, history) =>
                  currentRank = current
                  historyRank = history

                case GridSyncState(d) =>
//                  dom.console.log(d.toString)
                  thorSchemaOpt.foreach(_.receiveThorSchemaState(d))
                  justSynced = true


                case e: UserActionEvent => thorSchemaOpt.foreach(_.receiveUserEvent(e))

                case e: GameEvent => thorSchemaOpt.foreach(_.receiveGameEvent(e))

                case x: GenerateFood =>

                case x: EatFood =>
//                  thorSchemaOpt.foreach(_.)

                case  x => dom.window.console.log(s"接收到无效消息$x")
              }
            case Left(error) =>
              println(s"decode msg failed,error:${error.toString}")
          }
        }
      case unknow =>
        println(s"recv unknow msg:${unknow}")
    }
    e
  }


  def addActionListenEvent(): Unit = {
    canvas.focus()
    canvas.oncontextmenu = _=> false //取消右键弹出行为
    canvas.onmousemove = { (e: dom.MouseEvent) =>
      val point = Point(e.clientX.toFloat, e.clientY.toFloat)
      val theta = point.getTheta(canvasBounds * canvasUnit / 2).toFloat
      thorSchemaOpt match{
        case Some(thorSchema: ThorSchemaClientImpl) =>
          if(thorSchema.adventurerMap.contains(myId)){
            val data = MouseMove(thorSchema.myId,theta,thorSchema.systemFrame,getActionSerialNum)
            websocketClient.sendMsg(data)
            thorSchema.addMyAction(data)
            thorSchema.preExecuteUserEvent(data)
          }

        case None =>
      }

      e.preventDefault()
    }
    canvas.onmousedown = {(e: dom.MouseEvent) =>
      thorSchemaOpt match{
        case Some(thorSchema: ThorSchemaClientImpl) =>
          if(thorSchema.adventurerMap.contains(myId))
            if(e.button == 0){ //左键
              val event = MouseClickDownLeft(myId, thorSchema.systemFrame, getActionSerialNum)
              websocketClient.sendMsg(event)
              thorSchema.preExecuteUserEvent(event)
              thorSchema.addMyAction(event)
              e.preventDefault()
            }
            else if(e.button == 2){ //右键
              val event = MouseClickDownRight(myId, thorSchema.systemFrame, getActionSerialNum)
              websocketClient.sendMsg(event)
              thorSchema.preExecuteUserEvent(event) // actionEventMap
              thorSchema.addMyAction(event) // myAdventurerAction
              e.preventDefault()
            }
            else ()
        case None =>
      }

    }
    canvas.onmouseup = {(e: dom.MouseEvent) =>
      thorSchemaOpt match{
        case Some(thorSchema: ThorSchemaClientImpl) =>
          if(thorSchema.adventurerMap.contains(myId))
            if(e.button == 2){ //右键
              val event = MouseClickUpRight(myId, thorSchema.systemFrame, getActionSerialNum)
              websocketClient.sendMsg(event)
              thorSchema.preExecuteUserEvent(event)
              thorSchema.addMyAction(event)
              e.preventDefault()
            }
            else ()
        case None =>
      }

    }
     canvas.onkeydown = {(e : dom.KeyboardEvent) =>
       thorSchemaOpt match{
         case Some(thorSchema: ThorSchemaClientImpl) =>
           if (!thorSchema.adventurerMap.contains(myId)){
             if (e.keyCode == KeyCode.Space){
               println("restart!!!!")
               firstCome = true
               start(myName, null, null) // FIXME 重启没有验证accessCode
               websocketClient.sendMsg(RestartGame(myName))
               e.preventDefault()
             }
           }
         case None =>
       }
     }
//    canvas.onclick = { (e: MouseEvent) =>
//      val currentTime = System.currentTimeMillis()
//      val data = MouseClick(myId,thorSchema.systemFrame,getActionSerialNum)
//      websocketClient.sendMsg(data)
//      thorSchema.addMyAction(MouseClick(myId,thorSchema.systemFrame,getActionSerialNum))
//      e.preventDefault()
//    }

  }

  //游戏启动
  def start(name: String, id: String, accessCode: String): Unit = {
    println(s"start $name")
    myName = name
    canvas.focus()
    if (firstCome) {
      drawGameLoading()
      addActionListenEvent()
      websocketClient.setup(name, id, accessCode)
      gameLoop()
    }
    else if(websocketClient.getWsState){
      websocketClient.sendMsg(RestartGame(name))
    }else{
      JsFunc.alert("网络连接失败，请重新刷新")
    }
  }

  var tickCount = 0L
  var testStartTime = System.currentTimeMillis()
  var testEndTime = System.currentTimeMillis()
  var startTime = System.currentTimeMillis()

  def gameLoop(): Unit = {
    logicFrameTime = System.currentTimeMillis()
    thorSchemaOpt match{
      case Some(thorSchema: ThorSchemaClientImpl) => thorSchema.update()
      case None =>
    }
  }



  def drawGameByTime(offsetTime: Long, canvasUnit: Int, canvasBounds: Point): Unit = {
    thorSchemaOpt match{
      case Some(thorSchema: ThorSchemaClientImpl) =>
        if(thorSchema.adventurerMap.contains(myId)){
          thorSchema.drawGame(offsetTime, canvasUnit, canvasBounds)
          thorSchema.drawRank(historyRank,false,myId)
          thorSchema.drawRank(currentRank,true,myId)
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
}
