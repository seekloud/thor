package org.seekloud.thor.controller

import org.seekloud.thor.ClientBoot
import org.seekloud.thor.common.StageContext
import org.slf4j.LoggerFactory
import org.seekloud.thor.protocol.ThorClientProtocol._
import org.seekloud.thor.scene.{LoginScene, RoomScene}
import org.seekloud.thor.utils.{ThorClient, WarningDialog}
import org.seekloud.thor.ClientBoot.executor
import akka.actor.typed.ActorRef
import javafx.scene.control.TextInputDialog
import javafx.scene.image.ImageView
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

    // TODO  for test
//    val roomList = List("1-10-0", "2-15-1")
//    ClientBoot.addToPlatform {
//      roomScene.updateRoomList(roomList)
//    }
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


  roomScene.setListener(new RoomScene.RoomSceneListener {
    override def confirmRoom(roomId: Int, hasPwd: Boolean): Unit = {
      if (hasPwd) {
        val pwd = inputPwd
        if (pwd.nonEmpty) {
          if (pwd.get.nonEmpty) {
            ThorClient.verifyPsw(roomId, pwd.get).map {
              case Right(rst) =>
                if (rst.errCode == 0) {
                  wsClient ! StartGame(roomId)
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
      val pwd = inputPwd
      if (pwd.nonEmpty) {
        if (pwd.get.nonEmpty) {
          wsClient ! WsClient.CreateRoom(Some(pwd.get))
        } else {
          wsClient ! WsClient.CreateRoom(None)
        }
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
