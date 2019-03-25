package org.seekloud.thor.controller

import akka.actor.typed.ActorRef
import org.seekloud.thor.ClientBoot
import org.seekloud.thor.actor.WsClient
import org.seekloud.thor.common.StageContext
import org.seekloud.thor.scene.BotScene.BotSceneListener
import org.seekloud.thor.scene.{BotScene, ModeScene}
import org.seekloud.thor.utils.WarningDialog

/**
  * User: TangYaruo
  * Date: 2019/3/13
  * Time: 16:49
  */
class BotSceneController(wsClient: ActorRef[WsClient.WsCommand], modeScene: ModeScene, botScene: BotScene, context: StageContext) {


  botScene.setListener(new BotSceneListener {
    override def confirm(botId: String, botKey: String, botFrame: String): Unit = {
      if (botId.nonEmpty && botKey.nonEmpty && botFrame.nonEmpty) {
        wsClient ! WsClient.BotLogin(botId, botKey, botFrame.toInt)
      } else {
        ClientBoot.addToPlatform {
          WarningDialog.initWarningDialog("请输入完整信息！")
        }
      }
    }

    override def backToPrevious(): Unit = {
      ClientBoot.addToPlatform {
        context.switchScene(modeScene.getScene, "mode_choice")
      }
    }
  })



  def showScene(): Unit = {
    ClientBoot.addToPlatform(
      context.switchScene(botScene.getScene, title = "bot_info")
    )
  }

}
