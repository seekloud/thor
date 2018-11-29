package org.seekloud.thor.front.thorClient

import org.scalajs.dom
import org.seekloud.thor.front.common.Routes
import org.seekloud.thor.front.utils.Shortcut
import org.seekloud.thor.shared.ptcl.protocol.ThorGame._
import org.scalajs.dom.raw.{Event, FileReader, MessageEvent, MouseEvent}

/**
  * @author Jingyi
  * @version 创建时间：2018/11/26
  */
class GameHolder4Watch(name:String, roomId:Long, playerId: String, accessCode:String) extends GameHolder(name){

  override protected def gameLoop(): Unit = {
    thorSchemaOpt.foreach(_.update())
    logicFrameTime = System.currentTimeMillis()
  }

  def watch() = {
    websocketClient.setup(Routes.wsWatchGameUrl(roomId, playerId, accessCode))
  }

  override protected def wsMessageHandler(data:WsMsgServer):Unit = {
    //    println(data.getClass)
    dom.console.log(data.toString)
    data match {
      case e:YourInfo =>
        startTime = System.currentTimeMillis()
        thorSchemaOpt = Some(ThorSchemaClientImpl(ctx, e.config, e.id, name,canvasBounds,canvasUnit))
        Shortcut.cancelSchedule(timer)
        timer = Shortcut.schedule(gameLoop, e.config.frameDuration / e.config.frameDuration)


      case e: UserLeftRoom =>
        Shortcut.cancelSchedule(timer)
//        thorSchemaOpt.foreach(_.drawDeadImg(s"玩家已经离开了房间，请重新选择观战对象"))

      case e: GridSyncState =>
        thorSchemaOpt.foreach(_.receiveThorSchemaState(e.d))
        dom.window.cancelAnimationFrame(nextFrame)
        nextFrame = dom.window.requestAnimationFrame(gameRender())

      case e:Ranks =>
        /**
          * 游戏排行榜
          * */
        thorSchemaOpt.foreach{ t =>
          t.currentRankList = e.currentRank
          t.historyRank = e.historyRank
        }

      case e:UserActionEvent =>
        thorSchemaOpt.foreach(_.receiveUserEvent(e))

      case e:GameEvent =>
        thorSchemaOpt.foreach(_.receiveGameEvent(e))


      case e: BeAttacked =>
        killer = e.killerName
        endTime = System.currentTimeMillis()
        val time = duringTime(endTime - startTime)
        var killNum = 0
        var score = 0
        var level = 1
        thorSchemaOpt match {
          case Some(thorSchema: ThorSchemaClientImpl)=>
            if (thorSchema.adventurerMap.contains(myId)){
              killNum = thorSchema.adventurerMap(myId).killNum
              score = thorSchema.adventurerMap(myId).energy
              level = thorSchema.adventurerMap(myId).level
            }
          case None =>
        }
        thorSchemaOpt.foreach(_.drawGameStop(e.name,killNum,score,level,killer,time))
        dom.window.cancelAnimationFrame(nextFrame)

      case RebuildWebSocket=>
//        thorSchemaOpt.foreach(_.drawReplayMsg("存在异地登录。。"))
        closeHolder


      case _ =>
    }

  }
}
