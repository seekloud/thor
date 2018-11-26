package org.seekloud.thor.front.thorClient

import java.util.concurrent.atomic.AtomicInteger

import org.scalajs.dom
import org.scalajs.dom.Blob
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.raw.{Event, FileReader, MessageEvent}
import org.seekloud.byteobject.ByteObject.bytesDecode
import org.seekloud.byteobject.MiddleBufferInJs
import org.seekloud.thor.front.utils.{JsFunc, Shortcut}
import org.seekloud.thor.shared.ptcl.model.Point
import org.seekloud.thor.shared.ptcl.protocol.ThorGame._

import scala.scalajs.js.typedarray.ArrayBuffer

/**
  * @author Jingyi
  * @version 创建时间：2018/11/26
  */
class GameHolder4Play(name: String, user: Option[UserInfo] = None) extends GameHolder(name) {

  private[this] val actionSerialNumGenerator = new AtomicInteger(0)
  private val preExecuteFrameOffset = org.seekloud.thor.shared.ptcl.model.Constants.preExecuteFrameOffset

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

  def getActionSerialNum = actionSerialNumGenerator.getAndIncrement()

  override protected def wsMessageHandler(e: MessageEvent) = {
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

                case e:PingPackage =>
                  receivePingPackage(e)


                case e: UserActionEvent => thorSchemaOpt.foreach(_.receiveUserEvent(e))

                case e: GameEvent => thorSchemaOpt.foreach(_.receiveGameEvent(e))

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
            val mouseDistance = math.sqrt(math.pow(e.clientX - dom.window.innerWidth / 2.0, 2) + math.pow(e.clientY - dom.window.innerHeight / 2.0, 2))
            //            println(s"mouseDistance: $mouseDistance")
            val data = MouseMove(thorSchema.myId,theta, mouseDistance.toFloat, thorSchema.systemFrame + preExecuteFrameOffset, getActionSerialNum)
            websocketClient.sendMsg(data)
            //            if(org.seekloud.thor.shared.ptcl.model.Constants.fakeRender) {
            //              thorSchema.addMyAction(data)
            //            }
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
              val event = MouseClickDownLeft(myId, thorSchema.systemFrame + preExecuteFrameOffset, getActionSerialNum)
              websocketClient.sendMsg(event)
              thorSchema.preExecuteUserEvent(event)
              //              thorSchema.addMyAction(event)
              e.preventDefault()
            }
            else if(e.button == 2){ //右键
              val event = MouseClickDownRight(myId, thorSchema.systemFrame + preExecuteFrameOffset, getActionSerialNum)
              websocketClient.sendMsg(event)
              thorSchema.preExecuteUserEvent(event) // actionEventMap
              //              thorSchema.addMyAction(event) // myAdventurerAction
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
              val event = MouseClickUpRight(myId, thorSchema.systemFrame + preExecuteFrameOffset, getActionSerialNum)
              websocketClient.sendMsg(event)
              thorSchema.preExecuteUserEvent(event)
              //              thorSchema.addMyAction(event)
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
              start(myName, "1", "1") // FIXME 重启没有验证accessCode
              websocketClient.sendMsg(RestartGame(myName))
              e.preventDefault()
            }
          }
        case None =>
      }
    }

  }
}
