package org.seekloud.thor.controller

import java.io.ByteArrayInputStream

import akka.actor.typed.ActorRef
import javafx.geometry.Insets
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.control._
import javafx.scene.layout.GridPane
import org.seekloud.thor.ClientBoot
import org.seekloud.thor.actor.WsClient
import org.seekloud.thor.common.StageContext
import org.seekloud.thor.scene.{LoginScene, ModeScene, RoomScene}
import org.seekloud.thor.utils.{EsheepClient, WarningDialog}
import org.seekloud.thor.ClientBoot.executor
import org.seekloud.thor.protocol.ThorClientProtocol.ClientUserInfo
import org.slf4j.LoggerFactory

/**
  * User: TangYaruo
  * Date: 2019/3/7
  * Time: 11:37
  */
class LoginController(
  wsClient: ActorRef[WsClient.WsCommand],
  modeScene: ModeScene,
  loginScene: LoginScene,
  stageContext: StageContext) {

  private[this] val log = LoggerFactory.getLogger(this.getClass)

  private val ws = wsClient

  def getWs: ActorRef[WsClient.WsCommand] = ws


  loginScene.setLoginSceneListener(new LoginScene.LoginSceneListener {
    override def emailLogin(): Unit = {
      ClientBoot.addToPlatform {
        val userInfo = initLoginDialog()
        if (userInfo.nonEmpty) {
          EsheepClient.loginByMail(userInfo.get._1, userInfo.get._2).map {
            case Right(rst) =>
              if (rst.errCode == 0) {
                val playerId = s"user${rst.userId}"
                switchToRoomScene(ClientUserInfo(playerId, rst.userName, rst.token), wsClient)
              } else {
                ClientBoot.addToPlatform(
                  WarningDialog.initWarningDialog(s"${rst.msg}")
                )
              }
            case Left(e) =>
              log.error(s"login by mail error: $e")
              ClientBoot.addToPlatform(
                WarningDialog.initWarningDialog("login error!")
              )
          }
        }
      }
    }

    override def backToPrevious(): Unit = {
      ClientBoot.addToPlatform {
        stageContext.switchScene(modeScene.getScene, "mode_choice")
      }
    }
  })

  def showScene(): Unit = {
    ClientBoot.addToPlatform {
      stageContext.switchScene(loginScene.getScene, "LoginScene")
    }
  }

  def switchToRoomScene(userInfo: ClientUserInfo, wsClient: ActorRef[WsClient.WsCommand]): Unit = {
    ClientBoot.addToPlatform {
      val roomScene = new RoomScene(userInfo)
      val roomController = new RoomController(userInfo, wsClient, loginScene, roomScene, stageContext)
      roomController.showScene()
      wsClient ! WsClient.GetRoomController(roomController)
    }
  }

  def init(): Unit = {
    ClientBoot.addToPlatform {
      EsheepClient.getLoginInfo.map {
        case Right(rst) =>
          val wsUrl = rst.data.wsUrl
          val scanUrl = rst.data.scanUrl.replaceFirst("data:image/png;base64,", "")
          loginScene.drawScanUrl(imageFromBase64(scanUrl))
          wsClient ! WsClient.EstablishConnection2Es(wsUrl)

        case Left(e) =>
          log.error(s"get login info error: $e")
      }
    }
  }

  def imageFromBase64(base64Str:String): ByteArrayInputStream  = {
    import java.util.Base64
    val decoder = Base64.getDecoder
    val bytes:Array[Byte]= decoder.decode(base64Str)
    bytes.indices.foreach{ i =>
      if(bytes(i) < 0) bytes(i)=(bytes(i)+256).toByte
    }
    val  b = new ByteArrayInputStream(bytes)
    b
  }

  def initLoginDialog(): Option[(String, String)] = {
    val dialog = new Dialog[(String, String)]()
    dialog.setTitle("email_login")
    val nameField = new TextField()
    val pwdField = new PasswordField()
    val confirmButton = new ButtonType("confirm", ButtonData.OK_DONE)
    val grid = new GridPane
    grid.setHgap(10)
    grid.setVgap(10)
    grid.setPadding(new Insets(10, 10, 15, 10))
    grid.add(new Label("username:"), 0, 0)
    grid.add(nameField, 1, 0)
    grid.add(new Label("password:"), 0, 1)
    grid.add(pwdField, 1, 1)
    dialog.getDialogPane.getButtonTypes.addAll(confirmButton, ButtonType.CANCEL)
    dialog.getDialogPane.setContent(grid)
    dialog.setResultConverter(dialogButton =>
      if (dialogButton == confirmButton)
        (nameField.getText(), pwdField.getText())
      else
        null
    )
    var loginInfo: Option[(String, String)] = None
    val rst = dialog.showAndWait()
    rst.ifPresent { a =>
      if (a._1 != null && a._2 != null && a._1 != "" && a._2 != "")
        loginInfo = Some((a._1, a._2))
      else
        None
    }
    loginInfo
  }


}
