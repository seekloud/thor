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
      case e: YourInfo =>
        dom.console.log(s"$e")
        startTime = System.currentTimeMillis()
        myId = e.id
        myName = e.name
        gameConfig = Some(e.config)
        thorSchemaOpt = Some(ThorSchemaClientImpl(drawFrame, ctx, e.config, e.id, name, canvasBoundary, canvasUnit))
        if (timer != 0) {
          dom.window.clearInterval(timer)
          thorSchemaOpt.foreach { grid => timer = Shortcut.schedule(gameLoop, grid.config.frameDuration) }
        } else {
          thorSchemaOpt.foreach { grid => timer = Shortcut.schedule(gameLoop, grid.config.frameDuration) }
        }
        gameState = GameState.play
        nextFrame = dom.window.requestAnimationFrame(gameRender())
        firstCome = false



      //      case e: UserLeftRoom =>
      //        if (e.playerId == myId) {
      //          Shortcut.cancelSchedule(timer)
      //          thorSchemaOpt.foreach(_.drawReplayMsg(s"玩家已经离开了房间，请重新选择观战对象"))
      //        }

      case e: GridSyncState =>
        //        println(s"still sync.but thorSchema is: $thorSchemaOpt")
        thorSchemaOpt.foreach(_.receiveThorSchemaState(e.d))
        justSynced = true

      case e: PingPackage =>
        receivePingPackage(e)

      case e: Ranks =>

        /**
          * 游戏排行榜
          **/
        currentRank = e.currentRank
        historyRank = e.historyRank

      case e: BeAttacked =>
        barrage = s"${e.killerName}杀死了${e.name}"
        barrageTime = 300
        if (e.playerId == myId) {
          gameState = GameState.stop
          killer = e.killerName
          endTime = System.currentTimeMillis()
          val time = duringTime(endTime - startTime)
          thorSchemaOpt match {
            case Some(thorSchema: ThorSchemaClientImpl) =>
              thorSchema.adventurerMap.get(myId).foreach { my =>
                thorSchema.killerNew = e.killerName
                thorSchema.duringTime = time
                killerName = e.killerName
                killNum = my.killNum
                energy = my.energy
                level = my.level
              }
            case None =>
          }
          dom.window.cancelAnimationFrame(nextFrame)
        }
        thorSchemaOpt.foreach(_.receiveGameEvent(e))

      case e: GameEvent =>
        e match {
          case event: UserLeftRoom =>
            if (event.playerId == myId) {
              //              Shortcut.cancelSchedule(timer)
              thorSchemaOpt.foreach(_.drawReplayMsg(s"玩家已经死亡，请重新选择观战对象"))
            }
          case _ =>
        }
        thorSchemaOpt.foreach(_.receiveGameEvent(e))

      case e: UserActionEvent =>
        thorSchemaOpt.foreach(_.receiveUserEvent(e))

      case RebuildWebSocket =>
        thorSchemaOpt.foreach(_.drawReplayMsg("存在异地登录。。"))
        closeHolder


      case _ =>
    }

  }
}
