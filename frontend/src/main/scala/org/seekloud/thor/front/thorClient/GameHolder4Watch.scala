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

import org.scalajs.dom
import org.seekloud.thor.front.common.Routes
import org.seekloud.thor.front.utils.Shortcut
import org.seekloud.thor.shared.ptcl.protocol.ThorGame._
import org.scalajs.dom.raw.{Event, FileReader, MessageEvent, MouseEvent}
import org.seekloud.thor.shared.ptcl.model.Constants.GameState
import org.seekloud.thor.shared.ptcl.thor.ThorSchemaClientImpl

/**
  * @author Jingyi
  * @version 创建时间：2018/11/26
  */
class GameHolder4Watch(name: String, roomId: Long, playerId: String, accessCode: String) extends GameHolder(name) {

  //  override protected def gameLoop(): Unit = {
  //    handleResize
  //    thorSchemaOpt.foreach(_.update())
  //    logicFrameTime = System.currentTimeMillis()
  //  }

  def watch() = {
    gameState = GameState.loadingPlay
    canvas.getCanvas.focus()
    websocketClient.setup(Routes.wsWatchGameUrl(roomId, playerId, accessCode))
  }

  override protected def wsMessageHandler(data: WsMsgServer): Unit = {
    //    println(data.getClass)
    data match {
      case YourInfo(config, id, yourName, sId, pMap) =>
        dom.console.log(s"get YourInfo $id $yourName $pMap")
        startTime = System.currentTimeMillis()
        myId = id
        mainId = id
        shortId = sId
        myName = yourName
        gameConfig = Some(config)
        thorSchemaOpt = Some(ThorSchemaClientImpl(drawFrame, ctx, config, id, yourName, canvasBoundary, canvasUnit, preDrawFrame.foodCanvas, preDrawFrame.adventurerCanvas))
        checkAndChangePreCanvas()
        if (timer != 0) {
          dom.window.clearInterval(timer)
          thorSchemaOpt.foreach { grid =>
            timer = Shortcut.schedule(gameLoop, grid.config.frameDuration)
            pMap.foreach(p => grid.playerIdMap.put(p._1, p._2))
          }
        } else {
          thorSchemaOpt.foreach { grid =>
            timer = Shortcut.schedule(gameLoop, grid.config.frameDuration)
            pMap.foreach(p => grid.playerIdMap.put(p._1, p._2))
          }
        }
        gameState = GameState.play
        if(nextFrame == 0) nextFrame = dom.window.requestAnimationFrame(gameRender())
        firstCome = false

      case e: BeAttacked =>
        barrage = (e.killerName, e.name)
        barrageTime = 300
        if (e.playerId == mainId) {
          mainId = e.killerId
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
                  energyScore = my.energyScore
                  level = my.level
                }
              case None =>
            }
          }

//          dom.window.cancelAnimationFrame(nextFrame)
        }
        thorSchemaOpt.foreach(_.receiveGameEvent(e))

      case e: Ranks =>

        /**
          * 游戏排行榜
          **/
        currentRank = e.currentRank
//        historyRank = e.historyRank

      case UserMap(map) =>
        println(s"userMap ---- $map")
        thorSchemaOpt.foreach{grid =>
          map.foreach(p => grid.playerIdMap.put(p._1, p._2))
          grid.needUserMap = false
        }

      case e: GridSyncState =>
        //        println(s"still sync.but thorSchema is: $thorSchemaOpt")
        thorSchemaOpt.foreach(_.receiveThorSchemaState(e.d))
        justSynced = true

      case e: PingPackage =>
        receivePingPackage(e)

      case RebuildWebSocket =>
        thorSchemaOpt.foreach(_.drawReplayMsg("存在异地登录。。"))
        closeHolder

      case e: UserActionEvent =>
//        e match {
//          case _: MouseClickDownLeft => Shortcut.playMusic("sound-4")
//          case _ =>
//        }
        thorSchemaOpt.foreach(_.receiveUserEvent(e))

      case e: GameEvent =>
        e match {
          case event: UserLeftRoom =>
            if (event.playerId == myId) {
              //FIXME  Shortcut.cancelSchedule(timer)
              thorSchemaOpt.foreach(_.drawReplayMsg(s"玩家已经死亡，请重新选择观战对象"))
            }
            thorSchemaOpt.foreach(thorSchema => thorSchema.playerIdMap.remove(event.shortId))
          case _ =>
        }
        thorSchemaOpt.foreach(_.receiveGameEvent(e))


      case x =>dom.window.console.log(s"接收到无效消息:$x")
    }

  }
}
