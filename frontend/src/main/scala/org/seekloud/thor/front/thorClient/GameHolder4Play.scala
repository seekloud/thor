package org.seekloud.thor.front.thorClient

import java.util.concurrent.atomic.AtomicInteger

import org.scalajs.dom
import org.scalajs.dom.Blob
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.raw.{Event, FileReader, MessageEvent}
import org.seekloud.byteobject.ByteObject.bytesDecode
import org.seekloud.byteobject.MiddleBufferInJs
import org.seekloud.thor.front.common.Routes
import org.seekloud.thor.front.utils.{JsFunc, Shortcut}
import org.seekloud.thor.shared.ptcl.model.Constants.GameState
import org.seekloud.thor.shared.ptcl.model.Point
import org.seekloud.thor.shared.ptcl.protocol.ThorGame._
import org.seekloud.thor.shared.ptcl.thor.ThorSchemaClientImpl

import scala.scalajs.js.typedarray.ArrayBuffer

/**
  * @author Jingyi
  * @version 创建时间：2018/11/26
  */
class GameHolder4Play(name: String, user: Option[UserInfo] = None) extends GameHolder(name) {

  private[this] val actionSerialNumGenerator = new AtomicInteger(0)
  private val preExecuteFrameOffset = org.seekloud.thor.shared.ptcl.model.Constants.preExecuteFrameOffset
  private val window = Point((dom.window.innerWidth - 12).toFloat, (dom.window.innerHeight - 12).toFloat)

  private var shortId = 0

  //游戏启动
  def start(name: String, id: Option[String], accessCode: Option[String], roomId: Option[Long]): Unit = {
    println(s"start $name; firstCome $firstCome")
    myName = name
    canvas.getCanvas.focus()
    if (firstCome) {
      addActionListenEvent()
      val url = if (id.isEmpty) Routes.wsJoinGameUrl(name) else Routes.wsJoinGameUrlESheep(id.get, name, accessCode.getOrElse("?"), roomId)
//      dom.window.setTimeout(()=>websocketClient.setup(url), 3000)
      websocketClient.setup(url)
      gameLoop()
    }
    else if (websocketClient.getWsState) {
      println("~~~~~~restart!!!!")
      websocketClient.sendMsg(RestartGame(name))
    } else {
      JsFunc.alert("网络连接失败，请重新刷新")
    }
  }

  def reStart() = {
    //    firstCome = true
//    start(myName, None, None, None) //重启没有验证accessCode
        websocketClient.sendMsg(RestartGame(myName))
  }

  def getActionSerialNum = actionSerialNumGenerator.getAndIncrement()

  override protected def wsMessageHandler(data: WsMsgServer) = {
    //    import org.seekloud.thor.front.utils.byteObject.ByteObject._
    data match {
      case YourInfo(config, id, yourName, sId) =>
        dom.console.log(s"get YourInfo $config $id $yourName")
        startTime = System.currentTimeMillis()
        myId = id
        mainId = id
        shortId = sId
        myName = yourName
        gameConfig = Some(config)
        thorSchemaOpt = Some(ThorSchemaClientImpl(drawFrame, ctx, config, id, yourName, canvasBoundary, canvasUnit, preDrawFrame.canvas, preDrawFrame.adventurerCanvas))
        if (timer != 0) {
          dom.window.clearInterval(timer)
          thorSchemaOpt.foreach { grid => timer = Shortcut.schedule(gameLoop, grid.config.frameDuration) }
        }
        else thorSchemaOpt.foreach { grid => timer = Shortcut.schedule(gameLoop, grid.config.frameDuration) }

        gameState = GameState.play
        Shortcut.playMusic("bgm-2")
        if(nextFrame == 0) nextFrame = dom.window.requestAnimationFrame(gameRender())
        firstCome = false


      case e: BeAttacked =>
        barrage = s"${e.killerName}  杀死了  ${e.name}"
        barrageTime = 300
        if (e.playerId == mainId) {
          mainId = e.killerId //跟随凶手视角
          if(e.playerId == myId){
            gameState = GameState.stop
            killer = e.killerName
            endTime = System.currentTimeMillis()
            thorSchemaOpt match {
              case Some(thorSchema: ThorSchemaClientImpl) =>
                thorSchema.adventurerMap.get(myId).foreach { my =>
                  thorSchema.killerNew = e.killerName
                  thorSchema.duringTime = duringTime(endTime - startTime)
                  killerName = e.killerName
                  killNum = my.killNum
                  energy = my.energy
                  level = my.level
                }
              case None =>
            }
          }
        }
        if(e.playerId == myId || e.killerId == myId){
          println(s"后台消息：玩家${e.name}(${e.playerId})被杀，凶手是${e.killerName}(${e.killerId})")
        }
        thorSchemaOpt.foreach(_.receiveGameEvent(e))


      case Ranks(current) =>
        currentRank = current
//        historyRank = history

      case GridSyncState(d) =>
        //                  dom.console.log(d.toString)
        thorSchemaOpt.foreach(_.receiveThorSchemaState(d))
        justSynced = true

      case e: PingPackage =>
        receivePingPackage(e)

      case RebuildWebSocket =>
        thorSchemaOpt.foreach(_.drawReplayMsg("存在异地登录"))
        closeHolder


      case e: UserActionEvent => thorSchemaOpt.foreach(_.receiveUserEvent(e))

      case e: GameEvent =>
        thorSchemaOpt.foreach(_.receiveGameEvent(e))

      case x => dom.window.console.log(s"接收到无效消息$x")
    }
  }

  def addActionListenEvent(): Unit = {
    canvas.getCanvas.focus()
    canvas.getCanvas.oncontextmenu = _ => false //取消右键弹出行为
    canvas.getCanvas.onmousemove = { e: dom.MouseEvent =>
      val point = Point(e.clientX.toFloat, e.clientY.toFloat)
      val theta = point.getTheta(canvasBounds * canvasUnit / 2).toFloat
      var lastMouseMove = 0l //限制一帧只能发一次mousemove
      thorSchemaOpt match {
        case Some(thorSchema: ThorSchemaClientImpl) =>
          if (thorSchema.adventurerMap.contains(myId)) {
//            val mouseDistance = math.sqrt(math.pow(e.clientX - dom.window.innerWidth / 2.0, 2) + math.pow(e.clientY - dom.window.innerHeight / 2.0, 2))
            val direction = thorSchema.adventurerMap(myId).direction
            if (thorSchema.systemFrame > lastMouseMove && math.abs(theta - direction) > 0.3) { //角度差大于0.3才执行
              val data = MM(shortId.toShort, (e.clientX - dom.window.innerWidth / 2.0).toShort, (e.clientY - dom.window.innerHeight / 2.0).toShort, thorSchema.systemFrame + preExecuteFrameOffset, getActionSerialNum)
              websocketClient.sendMsg(data)
              thorSchema.preExecuteUserEvent(data)
              lastMouseMove = thorSchema.systemFrame
            }
          }

        case None =>
      }
      e.preventDefault()
    }
    canvas.getCanvas.onmousedown = { (e: dom.MouseEvent) =>
      thorSchemaOpt match {
        case Some(thorSchema: ThorSchemaClientImpl) =>
          if (thorSchema.adventurerMap.contains(myId)) {
//            println("mouse down")
            if (e.button == 0) { //左键
              val event = MouseClickDownLeft(myId, thorSchema.systemFrame + preExecuteFrameOffset, getActionSerialNum)
              websocketClient.sendMsg(event)
              thorSchema.preExecuteUserEvent(event)
              //              thorSchema.addMyAction(event)
              Shortcut.playMusic("sound-4")
              //              e.preventDefault()
            }
            else if (e.button == 2) { //右键
              val event = MouseClickDownRight(myId, thorSchema.systemFrame + preExecuteFrameOffset, getActionSerialNum)
              websocketClient.sendMsg(event)
              thorSchema.preExecuteUserEvent(event) // actionEventMap
              //              thorSchema.addMyAction(event) // myAdventurerAction
              e.preventDefault()
            }
          }
          else if (!thorSchema.adventurerMap.contains(myId) && !thorSchema.dyingAdventurerMap.contains(myId)){
            println("on mouse down!!!!!")
            val x = e.clientX
            val y = e.clientY
            //            println(s"x = ${window.x * 0.4} y = ${window.y * 0.8} clientX = $x clientY = $y")
            //            if (x >= window.x * 0.4 && x <= window.x * 0.6 && y >= window.y * 0.85 && y <= window.y * 0.95)
            reStart()
            //            e.preventDefault()
          }
        case None =>
      }

    }
    canvas.getCanvas.onmouseup = { (e: dom.MouseEvent) =>
      thorSchemaOpt match {
        case Some(thorSchema: ThorSchemaClientImpl) =>
          if (thorSchema.adventurerMap.contains(myId))
            if (e.button == 2) { //右键
              val event = MouseClickUpRight(myId, thorSchema.systemFrame + preExecuteFrameOffset, getActionSerialNum)
              websocketClient.sendMsg(event)
              thorSchema.preExecuteUserEvent(event)
              //              thorSchema.addMyAction(event)
              e.preventDefault()
            }
        case None =>
      }

    }
    canvas.getCanvas.onkeydown = { (e: dom.KeyboardEvent) =>
      thorSchemaOpt match {
        case Some(thorSchema: ThorSchemaClientImpl) =>
          if (!thorSchema.adventurerMap.contains(myId)) {
            if (e.keyCode == KeyCode.Space) {
//              println("key space down")
              reStart()
              e.preventDefault()
            }
          }
        case None =>
      }
    }

  }
}
