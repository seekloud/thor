package com.neo.sk.thor.front.thor

import java.util.concurrent.atomic.AtomicInteger

import com.neo.sk.thor.front.utils.byteObject.MiddleBufferInJs
import com.neo.sk.thor.front.utils.{JsFunc, Shortcut}
import com.neo.sk.thor.shared.ptcl
import com.neo.sk.thor.shared.ptcl.model.{Boundary, Point}
import com.neo.sk.thor.shared.ptcl.protocol.ThorGame._
import com.neo.sk.thor.shared.ptcl.protocol._
import com.neo.sk.thor.shared.ptcl.thor.GridState
import mhtml.Var
import org.scalajs.dom
import org.scalajs.dom.Blob
import org.scalajs.dom.ext.{Color, KeyCode}
import org.scalajs.dom.html.{Canvas, Image}
import org.scalajs.dom.raw.{Event, FileReader, MessageEvent, MouseEvent}

import scala.collection.mutable
import scala.scalajs.js.typedarray.ArrayBuffer
import scala.xml.Elem
import org.scalajs.dom

/**
  * Created by Jingyi on 2018/11/9
  */
class GameHolder(canvasName:String) {

  import io.circe._, io.circe.generic.auto.exportDecoder, io.circe.parser._, io.circe.syntax._


  private[this] val canvas = dom.document.getElementById(canvasName).asInstanceOf[Canvas]
  private[this] val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

  private[this] val bounds = Point(ptcl.model.Boundary.w,ptcl.model.Boundary.h)

  private[this] val canvasUnit = 10
  private[this] val canvasBoundary = ptcl.model.Point(dom.window.innerWidth.toFloat,dom.window.innerHeight.toFloat)

  private[this] val canvasBounds = canvasBoundary / canvasUnit

  private[this] var myId = -1L
  private[this] var myName = ""
  private[this] var firstCome = true

  private[this] val websocketClient = new WebSocketClient(wsConnectSuccess,wsConnectError,wsMessageHandler,wsConnectClose)


  canvas.width = canvasBoundary.x.toInt
  canvas.height = canvasBoundary.y.toInt


  private var timer:Int = 0


  private var nextFrame = 0
  private var logicFrameTime = System.currentTimeMillis()

  private[this] final val maxRollBackFrames = 5
  private[this] val gameEventMap = new mutable.HashMap[Long,List[WsMsgServer]]()
  private[this] val gameSnapshotMap = new mutable.HashMap[Long,GridState]()
  private[this] val historyAction = new mutable.HashMap[Long,(Long, Long, UserActionEvent)]()


  def addGameEvent(f:Long,event:WsMsgServer) = {

  }


  def addActionWithFrame(id: Int, adventurerAction: UserActionEvent, frame: Long) = {
  }

  def addActionWithFrameFromServer(id: Int, adventurerAction: UserActionEvent, frame: Long) = {

  }


  //从第frame开始回滚到现在
  def rollback(frame:Long) = {

  }


  def gameRender():Double => Unit = {d =>
    val curTime = System.currentTimeMillis()
    val offsetTime = curTime - logicFrameTime
    drawGameByTime(offsetTime)

    nextFrame = dom.window.requestAnimationFrame(gameRender())

  }





  private def wsConnectSuccess(e:Event) = {
    println(s"连接服务器成功")
    e
  }


  private def wsConnectError(e:Event) = {
    JsFunc.alert("网络连接失败，请重新刷新")
    e
  }


  private def wsConnectClose(e:Event) = {
    JsFunc.alert("网络连接失败，请重新刷新")
    e
  }


  private def wsMessageHandler(e:MessageEvent) = {
    import com.neo.sk.thor.front.utils.byteObject.ByteObject._
    e.data match {
      case blobMsg:Blob =>
        val fr = new FileReader()
        fr.readAsArrayBuffer(blobMsg)
        fr.onloadend = { _: Event =>
          val buf = fr.result.asInstanceOf[ArrayBuffer]
          val middleDataInJs = new MiddleBufferInJs(buf)
          bytesDecode[WsMsgServer](middleDataInJs) match {
            case Right(data) =>
              data match {


                case UserEnterRoom(userId,name,adventurer,frame) =>



                case Ranks(currentRank,historyRank) =>


                case GridSyncState(d) =>


                case  _ => println(s"接收到无效消息")
              }
            case Left(error) =>
              println(s"decode msg failed,error:${error.message}")
          }
        }
      case unknow =>
        println(s"recv unknow msg:${unknow}")
    }
    e
  }



  def addActionListenEvent():Unit = {
    canvas.focus()
    canvas.onmousemove = { (e:dom.MouseEvent) =>
      val point = Point(e.clientX.toFloat,e.clientY.toFloat)
      val theta = point.getTheta(canvasBoundary / 2).toFloat

      e.preventDefault()
    }
    canvas.onclick = {(e:MouseEvent) =>

      e.preventDefault()
    }

  }

  //游戏启动
  def start(name:String):Unit = {
    myName = name
    canvas.focus()
    if(firstCome){
      firstCome = false
      addActionListenEvent()
      websocketClient.setup(name)
      gameLoop()

      timer = Shortcut.schedule(gameLoop,ptcl.model.Frame.millsAServerFrame)
    } else if(websocketClient.getWsState){
      websocketClient.sendMsg(RestartGame(name))

      timer = Shortcut.schedule(gameLoop,ptcl.model.Frame.millsAServerFrame)
    }else{
      JsFunc.alert("网络连接失败，请重新刷新")
    }
  }


  def gameLoop():Unit = {

  }


  def drawGameLoading():Unit = {
    ctx.fillStyle = Color.Black.toString()
    ctx.fillRect(0, 0, canvasBounds.x * canvasUnit, canvasBounds.y * canvasUnit)
    ctx.fillStyle = "rgb(250, 250, 250)"
    ctx.textAlign = "left"
    ctx.textBaseline = "top"
    ctx.font = "36px Helvetica"
    ctx.fillText("请稍等，正在连接服务器", 150, 180)
  }

  def drawGame(curFrame:Int,maxClientFrame:Int): Unit ={
//
    // rintln("111111111111111111111")


  }

  def drawGameByTime(offsetTime:Long): Unit ={

  }

  def drawGameStop():Unit = {
    ctx.fillStyle = Color.Black.toString()
    ctx.fillRect(0, 0, canvasBounds.x * canvasUnit, canvasBounds.y * canvasUnit)
    ctx.fillStyle = "rgb(250, 250, 250)"
    ctx.textAlign = "left"
    ctx.textBaseline = "top"
    ctx.font = "36px Helvetica"
    ctx.fillText(s"您已经死亡,被玩家=${}所杀", 150, 180)
    println()
  }


}
