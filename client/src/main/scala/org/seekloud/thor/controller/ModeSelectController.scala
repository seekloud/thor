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

package org.seekloud.thor.controller

import akka.actor.typed.ActorRef
import org.seekloud.thor.ClientBoot
import org.seekloud.thor.actor.WsClient
import org.seekloud.thor.common.StageContext
import org.seekloud.thor.scene.{LoginScene, ModeScene}
import org.seekloud.thor.scene.ModeScene.ModeSceneListener

/**
  * User: TangYaruo
  * Date: 2019/3/7
  * Time: 19:30
  */
class ModeSelectController(wsClient: ActorRef[WsClient.WsCommand], modeScene: ModeScene, context: StageContext) {


  modeScene.setListener(new ModeSceneListener {
    override def gotoHumanScene(): Unit = {
      ClientBoot.addToPlatform{
        val loginScene = new LoginScene
        val loginController = new LoginController(wsClient, modeScene, loginScene, context)
        wsClient ! WsClient.GetLoginController(loginController)
        loginController.init()
        loginController.showScene()
      }
    }

    override def gotoBotScene(): Unit = {
      ClientBoot.addToPlatform(
        //TODO BOT
      )
    }
  })


  def showScene(): Unit = {
    ClientBoot.addToPlatform(
      context.switchScene(modeScene.getScene, title = "mode_choice")
    )
  }



}
