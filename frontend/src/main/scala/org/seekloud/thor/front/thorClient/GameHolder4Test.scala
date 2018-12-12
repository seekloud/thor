package org.seekloud.thor.front.thorClient

import java.util.concurrent.atomic.AtomicInteger

import org.scalajs.dom
import org.scalajs.dom.ext.KeyCode
import org.seekloud.thor.front.common.Routes
import org.seekloud.thor.front.utils.{JsFunc, Shortcut}
import org.seekloud.thor.shared.ptcl.model.Constants.GameState
import org.seekloud.thor.shared.ptcl.model.Point
import org.seekloud.thor.shared.ptcl.protocol.ThorGame._
import org.seekloud.thor.shared.ptcl.thor.ThorSchemaClientImpl

import scala.util.Random

class GameHolder4Test(name: String, user: Option[UserInfo] = None) extends GameHolder(name) {

  private[this] val actionSerialNumGenerator = new AtomicInteger(0)
  private val preExecuteFrameOffset = org.seekloud.thor.shared.ptcl.model.Constants.preExecuteFrameOffset
  private val window = Point((dom.window.innerWidth - 12).toFloat, (dom.window.innerHeight - 12).toFloat)
  private var clickTimer = 0
  private var mousemoveTimer = 0

  //游戏启动
  def start(name: String, id: Option[String], accessCode: Option[String], roomId: Option[Long]): Unit = {
    println(s"start $name")
    myName = name
    canvas.getCanvas.focus()
    if (firstCome) {
      gameState = GameState.loadingPlay
      val url = if (id.isEmpty) Routes.wsJoinGameUrl(name) else Routes.wsJoinGameUrlESheep(id.get, name, accessCode.getOrElse("?"), roomId)
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
    start(myName, None, None, None) //重启没有验证accessCode
//    websocketClient.sendMsg(RestartGame(myName))
  }

  def getActionSerialNum = actionSerialNumGenerator.getAndIncrement()

  override protected def wsMessageHandler(data: WsMsgServer) = {
    //    import org.seekloud.thor.front.utils.byteObject.ByteObject._
    data match {
      case YourInfo(config, id, name) =>
        dom.console.log(s"get YourInfo ${config} ${id} ${name}")
        startTime = System.currentTimeMillis()
        myId = id
        myName = name
        gameConfig = Some(config)
        thorSchemaOpt = Some(ThorSchemaClientImpl(drawFrame, ctx, config, id, name, canvasBoundary, canvasUnit))
        thorSchemaOpt.foreach { grid => timer = Shortcut.schedule(gameLoop, grid.config.frameDuration) }
        gameState = GameState.play
        nextFrame = dom.window.requestAnimationFrame(gameRender())
        firstCome = false
        clickTimer = Shortcut.schedule(fakeMouseClick, 3000)
        mousemoveTimer = Shortcut.schedule(fakeMouseMove, 1000)

      case e:BeAttacked =>
        println("attack!!!!!!!!!!!!!"+e)
        barrage = s"${e.killerName}杀死了${e.name}"
        barrageTime = 300
        if(e.playerId == myId){
          gameState = GameState.stop
          killer = e.killerName
          endTime = System.currentTimeMillis()
          println(s"be attacked by ${e.killerName}")
          val time = duringTime(endTime - startTime)
          thorSchemaOpt match {
            case Some(thorSchema: ThorSchemaClientImpl)=>
              if (thorSchema.adventurerMap.contains(myId)){
                thorSchema.killerNew = e.killerName
                thorSchema.duringTime = time
              }
            case None =>
          }
          dom.window.cancelAnimationFrame(nextFrame)
          Shortcut.cancelSchedule(mousemoveTimer)
          Shortcut.cancelSchedule(clickTimer)
          dom.window.setTimeout(() => reStart(), 2000)
        }
        else{
          thorSchemaOpt.foreach(_.receiveGameEvent(e))
        }



      case Ranks(current, history) =>
        currentRank = current
        historyRank = history

      case GridSyncState(d) =>
        //                  dom.console.log(d.toString)
        thorSchemaOpt.foreach(_.receiveThorSchemaState(d))
        justSynced = true

      case e: PingPackage =>
        receivePingPackage(e)


      case e: UserActionEvent => thorSchemaOpt.foreach(_.receiveUserEvent(e))

      case e: GameEvent =>
        if (e.isInstanceOf[UserEnterRoom]) {
          barrage = s"${myName}加入了游戏"
          barrageTime = 300
        }
        if (e.isInstanceOf[UserLeftRoom]) {
          barrage = s"${myName}离开了游戏"
          barrageTime = 300
        }
        thorSchemaOpt.foreach(_.receiveGameEvent(e))

      case x => dom.window.console.log(s"接收到无效消息$x")
    }
  }

  private def fakeMouseClick(): Unit = { //模拟左键
    thorSchemaOpt.foreach{ thorSchema =>
      if(thorSchema.adventurerMap.contains(myId)){
        val event = MouseClickDownLeft(myId, thorSchema.systemFrame + preExecuteFrameOffset, getActionSerialNum)
        websocketClient.sendMsg(event)
        thorSchema.preExecuteUserEvent(event)
        Shortcut.playMusic("sound-4")
      }
    }
  }

  private val random = new Random(System.currentTimeMillis())
  private def fakeMouseMove(): Unit = {
    thorSchemaOpt.foreach{ thorSchema =>
      if(thorSchema.adventurerMap.contains(myId)){
        val width = thorSchema.canvasSize.x.toInt
        val height = thorSchema.canvasSize.y.toInt
        val point = Point(random.nextInt(width), random.nextInt(height))
        val theta = point.getTheta(canvasBounds * canvasUnit / 2).toFloat
        val mouseDistance = math.sqrt(math.pow(point.x - dom.window.innerWidth / 2.0, 2) + math.pow(point.y - dom.window.innerHeight / 2.0, 2))
        //            println(s"mouseDistance: $mouseDistance")
        val direction = thorSchema.adventurerMap.get(myId).get.direction
        if(math.abs(theta - direction) > 0.1){ //角度差大于0.3才执行
          val data = MouseMove(thorSchema.myId,theta, mouseDistance.toFloat, thorSchema.systemFrame + preExecuteFrameOffset, getActionSerialNum)
          websocketClient.sendMsg(data)
          thorSchema.preExecuteUserEvent(data)
        }
      }
    }
  }

}

