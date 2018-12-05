package org.seekloud.thor.front.thorClient

import org.scalajs.dom
import org.seekloud.thor.front.common.Routes
import org.seekloud.thor.front.model.{PlayerInfo, ReplayInfo}
import org.seekloud.thor.front.utils.{JsFunc, Shortcut}
import org.seekloud.thor.shared.ptcl.protocol.ThorGame
import org.seekloud.thor.shared.ptcl.thor.{ThorSchemaClientImpl, ThorSchemaState}

/**
  * @author Jingyi
  * @version 创建时间：2018/11/26
  */
class GameHolder4Replay(name: String, playerInfoOpt: Option[PlayerInfo] = None) extends GameHolder(name) {

  websocketClient.setWsReplay(true)

  def startReplay(option: Option[ReplayInfo] = None) = {
    println(s"start replay...")
    if (firstCome) {
      println(s"replay ws url: ${Routes.wsReplayGameUrl(option.get)}")
      websocketClient.setup(Routes.wsReplayGameUrl(option.get))
      gameLoop()
    } else if (websocketClient.getWsState) {
      firstCome = true
      gameLoop()
    } else {
      JsFunc.alert("网络连接失败，请重新刷新")
    }
  }


  override protected def wsMessageHandler(data: ThorGame.WsMsgServer): Unit = {
    data match {
      case e: ThorGame.YourInfo =>
        println(s"start...")
        if (nextFrame != 0) {
          dom.window.cancelAnimationFrame(nextFrame)
          Shortcut.cancelSchedule(timer)
          firstCome = true
        }
        myId = e.id
        myName = e.name
        gameConfig = Some(e.config)
        startTime = System.currentTimeMillis()
        thorSchemaOpt = Some(ThorSchemaClientImpl(drawFrame, ctx, e.config, e.id, e.name, canvasBoundary, canvasUnit))

      case e: ThorGame.GridSyncState =>
        if (firstCome) {
          firstCome = false
          thorSchemaOpt.foreach(_.receiveThorSchemaState(e.d))
        } else {
          thorSchemaOpt.foreach(_.receiveThorSchemaState(e.d))
        }

      case e: ThorGame.Ranks =>
        currentRank = e.currentRank
        historyRank = e.historyRank


      case ThorGame.StartReplay =>
        println(s"start replay...")
        timer = Shortcut.schedule(gameLoop, thorSchemaOpt.get.config.frameDuration / thorSchemaOpt.get.config.playRate)
        nextFrame = dom.window.requestAnimationFrame(gameRender())

      case e: ThorGame.UserActionEvent =>
        //remind here only add preAction without rollback
        thorSchemaOpt.get.preExecuteUserEvent(e)

      case e: ThorGame.GameEvent =>
        thorSchemaOpt.foreach(_.receiveGameEvent(e))
        //remind 此处判断是否为用户进入，更新userMap
        e match {
          case t: ThorGame.UserEnterRoom =>
            if (t.playerId == thorSchemaOpt.get.myId) {
              thorSchemaOpt.foreach(_.changeAdventurerId(t.playerId))
            }

          case t: ThorGame.UserLeftRoom =>
            if (t.playerId == thorSchemaOpt.get.myId) {
              println(s"receive userLeft=$t, set stop")
            }

          case _ =>
        }

      case e: ThorGame.EventData =>
        e.list.foreach(r => wsMessageHandler(r))
        //remind 快速播放
        thorSchemaOpt.foreach(_.update())

      case e: ThorGame.PingPackage =>
        receivePingPackage(e)

      case e: ThorGame.DecodeError =>

      case e: ThorGame.InitReplayError =>
        thorSchemaOpt.foreach(_.drawReplayMsg(e.msg))

      case e: ThorGame.ReplayFinish =>
        thorSchemaOpt.foreach(_.drawReplayMsg("游戏回放完毕。。。"))
        closeHolder

      case ThorGame.RebuildWebSocket =>
        thorSchemaOpt.foreach(_.drawReplayMsg("存在异地登录。。"))
        closeHolder

    }
  }


}
