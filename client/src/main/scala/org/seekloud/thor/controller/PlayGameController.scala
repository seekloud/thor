package org.seekloud.thor.controller

import org.seekloud.thor.App.{executor, materializer, scheduler, system, timeout, tokenActor}
import org.seekloud.thor.App
import org.seekloud.thor.model.PlayerInfo
import org.seekloud.thor.protocol.ESheepProtocol.GameServerInfo
import org.seekloud.thor.shared.ptcl.protocol.ThorGame
import org.seekloud.thor.shared.ptcl.thor.ThorSchemaClientImpl
import org.seekloud.thor.common.Context
import org.seekloud.thor.view.PlayGameView
import org.seekloud.utils.middleware.MiddleFrameInFx
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.scaladsl.AskPattern._
import org.slf4j.LoggerFactory
import javafx.util.Duration
import org.seekloud.thor.core.PlayGameActor
import org.seekloud.thor.game.NetworkInfo

import scala.collection.mutable
import scala.concurrent.Future
/**
  * @author Jingyi
  * @version 创建时间：2018/12/3
  */


class PlayGameController(
                          playerinfo: PlayerInfo,
                          gameServerInfo: GameServerInfo,
                          context: Context,
                          playGameView: PlayGameView,
                          roomInfo: Option[String] = None
                        )extends NetworkInfo {

  private val log = LoggerFactory.getLogger(this.getClass)
  val playGameActor = system.spawn(PlayGameActor.create(this),"PlayGameActor")

  protected var thorSchemaOpt : Option[ThorSchemaClientImpl] = None

  def wsMessageHandler(data:ThorGame.WsMsgServer):Unit ={
    App.pushStack2AppThread{
      data match {
        case e:ThorGame.YourInfo =>
          println("start----------")
          try {
            thorSchemaOpt = Some(ThorSchemaClientImpl(playGameView.drawFrame, playGameView.canvas,e.config,e.id,e.name,playGameView.canvasBoundary,playGameView.canvasUnit))

          }
      }
    }
  }
}
