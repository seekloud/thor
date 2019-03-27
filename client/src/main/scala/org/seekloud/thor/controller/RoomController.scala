package org.seekloud.thor.controller

import org.seekloud.thor.ClientBoot
import org.seekloud.thor.common.StageContext
import org.slf4j.LoggerFactory
import org.seekloud.thor.protocol.ThorClientProtocol._
import org.seekloud.thor.scene.{LoginScene, RoomScene}
import org.seekloud.thor.utils.{ThorClient, WarningDialog}
import org.seekloud.thor.ClientBoot.executor
import akka.actor.typed.ActorRef
import javafx.geometry.Insets
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.control._
import javafx.scene.image.ImageView
import javafx.scene.layout.GridPane
import org.seekloud.thor.actor.WsClient
import org.seekloud.thor.actor.WsClient.StartGame

/**
  * User: TangYaruo
  * Date: 2019/3/9
  * Time: 15:18
  */
class RoomController(userInfo: ClientUserInfo, wsClient: ActorRef[WsClient.WsCommand], loginScene: LoginScene, roomScene: RoomScene, stateContext: StageContext) {

  private val log = LoggerFactory.getLogger(this.getClass)
  var finalRoomId: Long = -1L

  updateRoomList()

  private def updateRoomList(): Unit = {

    ThorClient.getRoomList.map {
      case Right(rst) =>
        if (rst.errCode == 0) {
          ClientBoot.addToPlatform {
            roomScene.updateRoomList(rst.data.roomList)
          }
        } else {
          ClientBoot.addToPlatform(
            WarningDialog.initWarningDialog(s"${rst.msg}")
          )
        }
      case Left(e) =>
        log.error(s"get room list error: $e")
        ClientBoot.addToPlatform(
          WarningDialog.initWarningDialog("get room list error!")
        )

    }
  }

  def callBackWarning(msg: String): Unit = {
    ClientBoot.addToPlatform(
      WarningDialog.initWarningDialog(s"$msg")
    )
  }


  def inputPwd: Option[String] = {
    val dialog = new TextInputDialog()
    dialog.setTitle("房间密码")
    dialog.setHeaderText("")
    dialog.setGraphic(new ImageView())
    dialog.setContentText("请输入密码:")
    val rst = dialog.showAndWait()
    var pwd: Option[String] = None
    rst.ifPresent(a => pwd = Some(a))
    pwd
  }


  def createRoomDialog(): Option[(String, String)] = {
    val dialog = new Dialog[(String, String)]()
    dialog.setTitle("Create Room")
    val nameField = new TextField()
    val pwdField = new PasswordField()
    val confirmButton = new ButtonType("confirm", ButtonData.OK_DONE)
    val grid = new GridPane
    grid.setHgap(10)
    grid.setVgap(10)
    grid.setPadding(new Insets(10, 10, 15, 10))
    grid.add(new Label("room_name:"), 0, 0)
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
      if (a._1 != null && a._2 != null && a._1 != "")
        loginInfo = Some((a._1, a._2))
      else
        None
    }
    loginInfo
  }


  roomScene.setListener(listener = new RoomScene.RoomSceneListener {
    override def confirmRoom(roomId: Int, hasPwd: Boolean): Unit = {
      if (hasPwd) {
        val pwd = inputPwd
        if (pwd.nonEmpty) {
          if (pwd.get.nonEmpty) {
            ThorClient.verifyPsw(roomId, pwd.get).map {
              case Right(rst) =>
                if (rst.errCode == 0) {
                  wsClient ! StartGame(roomId, pwd)
                } else {
                  ClientBoot.addToPlatform {
                    WarningDialog.initWarningDialog(s"${rst.msg}")
                  }
                }
              case Left(e) =>
                log.error(s"verifyPsw error: $e")
            }
          } else {
            ClientBoot.addToPlatform {
              WarningDialog.initWarningDialog("输入密码不能为空！")
            }
          }
        }
      } else {
        wsClient ! WsClient.StartGame(roomId)
      }
    }

    override def gotoCreateRoomScene(): Unit = {
      val roomInfo = createRoomDialog()
      if (roomInfo.nonEmpty) {
        if (roomInfo.get._2.nonEmpty) {
          wsClient ! WsClient.CreateRoom(Some(roomInfo.get._2), roomInfo.get._1)
        } else {
          wsClient ! WsClient.CreateRoom(None, roomInfo.get._1)
        }
      } else {
        WarningDialog.initWarningDialog("请输入完整信息！")
      }

    }

    override def refreshRoomList(): Unit = {
      updateRoomList()
    }

    override def backToPrevious(): Unit = {
      ClientBoot.addToPlatform {
        stateContext.switchScene(loginScene.getScene, "LoginScene")
      }
    }
  })


  def showScene(): Unit = {
    ClientBoot.addToPlatform {
      stateContext.switchScene(roomScene.getScene, "RoomScene")
    }
  }


}
