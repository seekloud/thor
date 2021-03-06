/*
 * Copyright 2018 seekloud (https://github.com/seekloud)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
      websocketClient.sendMsg(RestartGame)
    } else {
      JsFunc.alert("网络连接失败，请重新刷新")
    }
  }

  def reStart() = {
//    firstCome = true
    start(myName, None, None, None) //重启没有验证accessCode
//    websocketClient.sendMsg(RestartGame(myName))
  }

  def getActionSerialNum:Byte = (actionSerialNumGenerator.getAndIncrement() % 127).toByte

  override protected def wsMessageHandler(data: WsMsgServer) = {
    //    import org.seekloud.thor.front.utils.byteObject.ByteObject._
    data match {
      case YourInfo(config, id, name, sId, pMap) =>
        dom.console.log(s"get YourInfo ${config} ${id} ${name}")
        startTime = System.currentTimeMillis()
        myId = id
        mainId = id
        shortId = sId
        myName = name
        gameConfig = Some(config)
        thorSchemaOpt = Some(ThorSchemaClientImpl(drawFrame, ctx, config, id, name, canvasBoundary, canvasUnit, preDrawFrame.foodCanvas, preDrawFrame.adventurerCanvas))
        thorSchemaOpt.foreach { grid =>
          timer = Shortcut.schedule(gameLoop, grid.config.frameDuration)
          pMap.foreach(p => grid.playerIdMap.put(p._1, p._2))
          grid.playerIdMap.put(sId, (id, name))
        }
        gameState = GameState.play
        nextFrame = dom.window.requestAnimationFrame(gameRender())
        firstCome = false
        clickTimer = Shortcut.schedule(fakeMouseClick, 3000)
        mousemoveTimer = Shortcut.schedule(fakeMouseMove, 2500)

      case e:BeAttacked =>
        println("attack!!!!!!!!!!!!!"+e)
        barrage = (e.killerName, e.name)
        barrageTime = 300
        if(e.playerId == myId){
          gameState = GameState.stop
          killer = e.killerName
          endTime = System.currentTimeMillis()
          println(s"be attacked by ${e.killerName}")
          val time = duringTime(endTime - startTime)
          thorSchemaOpt match {
            case Some(thorSchema: ThorSchemaClientImpl)=>
              thorSchema.adventurerMap.get(myId).foreach{ my =>
                thorSchema.killerNew = e.killerName
                thorSchema.duringTime = time
                killerName = e.killerName
                killNum = my.killNum
                energyScore = my.energyScore
                level = my.level
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

      case UserMap(map) =>
        thorSchemaOpt.foreach(grid => map.foreach(p => grid.playerIdMap.put(p._1, p._2)))

      case Ranks(current) =>
        currentRank = current
//        historyRank = history

      case GridSyncState(d) =>
        //                  dom.console.log(d.toString)
        thorSchemaOpt.foreach(_.receiveThorSchemaState(d))
        justSynced = true

      case e: PingPackage =>
        receivePingPackage(e)


      case e: UserActionEvent =>
        thorSchemaOpt.foreach(_.receiveUserEvent(e))

      case e: GameEvent =>
        if (e.isInstanceOf[UserEnterRoom]) {
          barrage = (myName,"join")
          barrageTime = 300
        }
        if (e.isInstanceOf[UserLeftRoom]) {
          barrage = (myName,"left")
          barrageTime = 300
        }
        thorSchemaOpt.foreach(_.receiveGameEvent(e))

      case x => dom.window.console.log(s"接收到无效消息$x")
    }
  }

  private def fakeMouseClick(): Unit = { //模拟左键
    thorSchemaOpt.foreach{ thorSchema =>
      if(thorSchema.adventurerMap.contains(myId)){
        val event = MouseClickDownLeft(shortId.toByte, thorSchema.systemFrame + preExecuteFrameOffset, getActionSerialNum)
        websocketClient.sendMsg(event)
        thorSchema.preExecuteUserEvent(event)
//        Shortcut.playMusic("sound-4")
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
        val direction = thorSchema.adventurerMap(myId).direction
        if(math.abs(theta - direction) > 0.1){ //角度差大于0.3才执行
          Shortcut.scheduleOnce(()=>fakeMouseMoveLittle((point.x - dom.window.innerWidth / 2.0).toShort, (point.y - dom.window.innerHeight / 2.0).toShort, theta), 20)
        }
      }
    }
  }
  private def fakeMouseMoveLittle(offSetX: Short, offSetY: Short, targetTheta: Float): Unit ={
    def fakeMove(thorSchema: ThorSchemaClientImpl, thetaList: List[Float], num: Int): Unit ={
      val data = MM(shortId.toByte, offSetX, offSetY, thorSchema.systemFrame + preExecuteFrameOffset, getActionSerialNum)
      websocketClient.sendMsg(data)
      thorSchema.preExecuteUserEvent(data)
      if(num < thetaList.length - 1)
        Shortcut.scheduleOnce(()=> fakeMove(thorSchema, thetaList, num + 1 ) , 200)
    }
    thorSchemaOpt.foreach{ thorSchema =>
      val direction = thorSchema.adventurerMap(myId).direction
      if(math.abs(targetTheta - direction) > 0.2){

        val increment = (1 to (math.abs(targetTheta - direction) / 0.4).toInt).map(_ => if(targetTheta - direction > 0) 0.4f else -0.4f)
        val thetaList = increment.scanLeft(direction)(_ + _)
        println(thetaList)
        Shortcut.scheduleOnce(()=> fakeMove(thorSchema, thetaList.toList, 0 ) , 50)
      }
    }
  }

}

