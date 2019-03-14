package org.seekloud.thor.actor

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer, TimerScheduler}
import org.seekloud.thor.ClientBoot
import org.seekloud.thor.controller.{GameController, RoomController}
import org.seekloud.thor.shared.ptcl.model.Constants.GameState
import org.seekloud.thor.shared.ptcl.protocol.ThorGame
import org.seekloud.thor.shared.ptcl.protocol.ThorGame._
import org.seekloud.thor.shared.ptcl.thor.ThorSchemaClientImpl
import org.slf4j.LoggerFactory

/**
  * User: TangYaruo
  * Date: 2019/3/7
  * Time: 11:29
  */
object GameMsgReceiver {

  private val log = LoggerFactory.getLogger(this.getClass)

  private[this] def switchBehavior(ctx: ActorContext[WsMsgServer],
    behaviorName: String,
    behavior: Behavior[WsMsgServer])
    (implicit stashBuffer: StashBuffer[WsMsgServer]) = {
    log.debug(s"${ctx.self.path} becomes $behaviorName behavior.")
    stashBuffer.unstashAll(ctx, behavior)
  }

  def create(wsClient: ActorRef[WsClient.WsCommand]): Behavior[WsMsgServer] = {
    Behaviors.setup[WsMsgServer] { ctx =>
      Behaviors.withTimers[WsMsgServer] { t =>
        implicit val stashBuffer: StashBuffer[WsMsgServer] = StashBuffer[WsMsgServer](Int.MaxValue)
        implicit val timer: TimerScheduler[WsMsgServer] = t
        switchBehavior(ctx, "waiting", waiting("", 0L, wsClient))
      }
    }
  }


  /**
    * 接收游戏开始前game server发来的消息
    *
    **/
  def waiting(
    myId: String,
    myRoomId: Long,
    wsClient: ActorRef[WsClient.WsCommand]
  )(
    implicit stashBuffer: StashBuffer[WsMsgServer],
    timer: TimerScheduler[WsMsgServer]
  ): Behavior[WsMsgServer] =
    Behaviors.receive[WsMsgServer] { (ctx, msg) =>
      msg match {
        case msg: JoinRoomFail =>
          wsClient ! WsClient.JoinRoomFail(msg.error)
          Behaviors.same

        case msg: CreateRoomRsp =>
          wsClient ! WsClient.CreateRoomRsp(msg.roomId)
          switchBehavior(ctx, "running", running(myId, myRoomId))

        case msg: YourInfo =>
          wsClient ! WsClient.JoinRoomSuccess
          switchBehavior(ctx, "running", running(myId, myRoomId))


        case x =>
          //          log.warn(s"unknown msg: $x")
          Behaviors.unhandled
      }
    }

  /**
    * 接收游戏过程中，game server发来的消息
    *
    **/

  def running(myId: String, myRoomId: Long)(
    implicit stashBuffer: StashBuffer[WsMsgServer],
    timer: TimerScheduler[WsMsgServer]
  ): Behavior[WsMsgServer] =
    Behaviors.receive[WsMsgServer] { (ctx, msg) =>
      msg match {
        //        case e: ThorGame.YourInfo =>
        //          println(s"start---------")
        //          gameController.thorSchemaOpt
        //          gameMusicPlayer.play()
        //          mainId = e.id
        //          try {
        //            gameController.thorSchemaOpt = Some(ThorSchemaClientImpl(playGameScreen.drawFrame, playGameScreen.getCanvasContext, e.config, e.id, e.name, playGameScreen.canvasBoundary, playGameScreen.canvasUnit))
        //            gameConfig = Some(e.config)
        //            checkAndChangePreCanvas()
        //            e.playerIdMap.foreach { p => thorSchemaOpt.foreach { t => t.playerIdMap.put(p._1, p._2)}}
        //            animationTimer.start()
        //            //            wsClient ! WsClient.StartGameLoop
        //            gameState = GameState.play
        //
        //          } catch {
        //            case e: Exception =>
        //              closeHolder
        //              println(e.getMessage)
        //              println("client is stop!!!")
        //          }
        //
        //
        //        case x@RestartYourInfo =>
        //          println(x)
        //          mainId = playerInfo.playerId
        //          gameState = GameState.play
        //
        //        case e: ThorGame.BeAttacked =>
        //          println(s"receive attacked msg:\n $e")
        //          barrage = (e.killerName, e.name)
        //          barrageTime = 300
        //          println(s"be attacked by ${e.killerName}")
        //          if (e.playerId == mainId) {
        //            mainId = e.killerId //跟随凶手视角
        //            if (e.playerId == playerInfo.playerId) {
        //              gameState = GameState.stop
        //              endTime = System.currentTimeMillis()
        //              thorSchemaOpt match {
        //                case Some(thorSchema: ThorSchemaClientImpl) =>
        //                  thorSchema.adventurerMap.get(playerInfo.playerId).foreach { my =>
        //                    thorSchema.killerNew = e.killerName
        //                    thorSchema.duringTime = duringTime(endTime - startTime)
        //                    killerName = e.killerName
        //                    killNum = my.killNum
        //                    energyScore = my.energyScore
        //                    level = my.level
        //                  }
        //                case None =>
        //                  println("there is nothing")
        //              }
        //            }
        //
        //          }
        //          //          if (e.playerId == playerInfo.playerId) {
        //          ////            gameState = GameState.stop
        //          //            wsActor ! PlayGameActor.StopGameLater
        //          //            endTime = System.currentTimeMillis()
        //          //            val time = duringTime(endTime - startTime)
        //          //            thorSchemaOpt.foreach { thorSchema =>
        //          //              if (thorSchema.adventurerMap.contains(playerInfo.playerId)) {
        //          //                thorSchema.adventurerMap.get(playerInfo.playerId).foreach { my =>
        //          //                  thorSchema.killerNew = e.killerName
        //          //                  thorSchema.duringTime = time
        //          //                  killerName = e.killerName
        //          //                  killNum = my.killNum
        //          //                  energy = my.energy
        //          //                  level = my.level
        //          //                }
        //          //
        //          //              }
        //          //            }
        //          //          }
        //          thorSchemaOpt.foreach(_.receiveGameEvent(e))
        //
        //
        //        case e: ThorGame.Ranks =>
        //          currentRank = e.currentRank
        //        //          historyRank = e.historyRank
        //
        //        case e: ThorGame.GridSyncState =>
        //          thorSchemaOpt.foreach(_.receiveThorSchemaState(e.d))
        //          justSynced = true
        //
        //        case UserMap(map) =>
        //          println(s"userMap ---- $map")
        //          thorSchemaOpt.foreach { grid =>
        //            map.foreach(p => grid.playerIdMap.put(p._1, p._2))
        //            grid.needUserMap = false
        //          }
        //
        //        case e: PingPackage =>
        //          receivePingPackage(e)
        //
        //        case RebuildWebSocket =>
        //          thorSchemaOpt.foreach(_.drawReplayMsg("存在异地登录"))
        //          closeHolder
        //
        //        case e: ThorGame.UserActionEvent =>
        //          thorSchemaOpt.foreach(_.receiveUserEvent(e))
        //
        //        case e: ThorGame.GameEvent =>
        //          e match {
        //            case event: ThorGame.UserEnterRoom =>
        //              if (thorSchemaOpt.nonEmpty) {
        //                thorSchemaOpt.get.playerIdMap.put(event.shortId, (event.playerId, event.name))
        //                if (event.playerId == playerInfo.playerId) byteId = event.shortId
        //              } else {
        //                //                wsClient ! WsClient.UserEnterRoom(event)
        //              }
        //            case event: UserLeftRoom =>
        //              if (event.shortId == byteId) println(s"${event.shortId}  ${event.playerId} ${event.name} left room...")
        //              thorSchemaOpt.foreach(thorSchema => thorSchema.playerIdMap.remove(event.shortId))
        //            case _ =>
        //          }
        //          thorSchemaOpt.foreach(_.receiveGameEvent(e))

        case x =>
          //          log.warn(s"unknown msg: $x")
          Behaviors.unhandled
      }
    }


}
