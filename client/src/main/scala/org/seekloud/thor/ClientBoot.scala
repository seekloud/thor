/*
 *   Copyright 2018 seekloud (https://github.com/seekloud)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.seekloud.thor

import akka.actor.typed.ActorRef
import akka.actor.{ActorSystem, Scheduler}
import akka.actor.typed.scaladsl.adapter._
import akka.dispatch.MessageDispatcher
import akka.stream.ActorMaterializer
import akka.util.Timeout
import javafx.application.Platform
import javafx.stage.Stage
import org.seekloud.thor.actor.WsClient.BotLogin
import org.seekloud.thor.actor.{SdkServerHandler, WsClient}
import org.seekloud.thor.common.{AppSettings, StageContext}
import org.seekloud.thor.controller.ModeSelectController
import org.seekloud.thor.scene.ModeScene
import org.seekloud.thor.shared.ptcl.protocol.ThorGame
import org.slf4j.LoggerFactory

import concurrent.duration._
import scala.language.postfixOps

/**
  * User: TangYaruo
  * Date: 2019/3/7
  * Time: 19:00
  */

object ClientBoot {

  import org.seekloud.thor.common.AppSettings._

  implicit val system: ActorSystem = ActorSystem("thor", config)
  implicit val executor: MessageDispatcher = system.dispatchers.lookup("akka.actor.my-blocking-dispatcher")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val scheduler: Scheduler = system.scheduler
  implicit val timeout: Timeout = Timeout(20 seconds)

  //  lazy val gameMsgReceiver: ActorRef[ThorGame.WsMsgSource] = system.spawn(GameMsgReceiver.create(), "gameMsgReceiver")

  val sdkServerHandler: ActorRef[SdkServerHandler.Command] = system.spawn(SdkServerHandler.create(), "sdkServerHandler")

  def addToPlatform(fun: => Unit): Unit = {
    Platform.runLater(() => fun)
  }
}

class ClientBoot extends javafx.application.Application {

  import ClientBoot._
  import org.seekloud.thor.common.BotSettings

  private[this] val log = LoggerFactory.getLogger(this.getClass)

  override def start(primaryStage: Stage): Unit = {

    val context = new StageContext(primaryStage)

    val wsClient = system.spawn(WsClient.create(context), "WsClient")

    if (BotSettings.render){
      val modeScene = new ModeScene()
      val modeSelectController = new ModeSelectController(wsClient, modeScene, context)
      modeSelectController.showScene()
    }
    else wsClient ! BotLogin(BotSettings.botId, BotSettings.botKey, AppSettings.frameRate)



  }


}
