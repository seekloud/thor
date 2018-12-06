package org.seekloud.thor.view

import java.io.ByteArrayInputStream

import akka.actor.typed.ActorRef
import javafx.beans.property.{ObjectProperty, SimpleObjectProperty, SimpleStringProperty, StringProperty}
import javafx.collections.{FXCollections, ObservableList}
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.control.{Button, Label, TableColumn, TableView}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.VBox
import javafx.scene.text.{Font, Text}
import javafx.scene.{Group, Scene}
import javafx.stage.Stage
import org.seekloud.thor.App.loginActor
import org.seekloud.thor.actor.{LoginActor, TokenActor}
import org.seekloud.thor.protocol.ESheepProtocol._
import sun.misc.BASE64Decoder

/**
  * User: XuSiRan
  * Date: 2018/12/4
  * Time: 11:41
  * 存储各种page
  */
class ClientPage(stage: Stage){

  // 房间列表对应的Property类
  case class RoomInfo(
    id: StringProperty,
    desc: StringProperty,
    edit: ObjectProperty[Button] = new SimpleObjectProperty[Button](new Button("进入房间"))
  ){
    def getId: String = id.get()
    def setId(ids: String): Unit = id.set(ids)
    def getDesc: String = desc.get()
    def setDesc(enters: String): Unit = desc.set(enters)
    def getEdit: Button = edit.get()
    def setEdit(buttons: Button): Unit = edit.set(buttons)
  }

  loginActor ! LoginActor.GetLoginImg(this)

  def imgSence(urls: LoginUrlRsp): Unit ={
    val decoder = new BASE64Decoder()
    val bytes = decoder.decodeBuffer(urls.data.scanUrl.split(",")(1))
    val out = new ByteArrayInputStream(bytes)
    val image = new Image(out)
    val imageView = new ImageView()
    imageView.setImage(image)
    val group = new Group
    group.getChildren.add(imageView)
    val scene = new Scene(group)
    stage.setScene(scene)
    stage.show()
  }

  //一个临时的Scene
  def infoSence(data: WsPlayerInfoRsp): Unit ={
    val NameText = new Text(120, 80, "连接中")
    NameText.setFont(Font.font(48))
    val group = new Group()
    group.getChildren.add(NameText)
    val scene = new Scene(group, 400, 200)
    stage.setScene(scene)
    stage.sizeToScene()
    stage.centerOnScreen()
    stage.show()
  }

    def roomScene(replyTo: ActorRef[TokenActor.Command], roomList: List[Long], playerInfo: ClientPlayerInfo) ={

    val group = new Group()
    val scene = new Scene(group)

    val observableList: ObservableList[RoomInfo] = FXCollections.observableArrayList()
    roomList.foreach{t =>
      val oneRoomInfo = RoomInfo(new SimpleStringProperty(t.toString), new SimpleStringProperty("一般房间"))
      oneRoomInfo.edit.get().setOnAction{e =>
        println("okok")
        //TODO 进入游戏按钮操作
      }
      observableList.add(oneRoomInfo)
    }

    val tableView = new TableView[RoomInfo]()

    val nameLabel = new Label("用户名：" + playerInfo.nickname)
    nameLabel.setFont(Font.font(17))
//    val label = new Label("房间列表")
//    label.setFont(Font.font(20))

    val roomIdCol = new TableColumn[RoomInfo, String]("房间号")
    roomIdCol.setMinWidth(80)
    roomIdCol.setCellValueFactory( new PropertyValueFactory[RoomInfo, String]("id"))
    val descCol = new TableColumn[RoomInfo, String]("房间描述")
    descCol.setMinWidth(100)
    descCol.setCellValueFactory( new PropertyValueFactory[RoomInfo, String]("desc"))
    val btnCol = new TableColumn[RoomInfo, Button]("操作")
//    btnCol.setMinWidth(80)
    btnCol.setCellValueFactory( new PropertyValueFactory[RoomInfo, Button]("edit"))

    tableView.setItems(observableList)
    tableView.getColumns.addAll(roomIdCol, descCol, btnCol)

    val vBox = new VBox()
    vBox.setSpacing(10)
    vBox.setPadding(new Insets(15,5,20,5))
    vBox.getChildren.addAll(nameLabel, tableView)

    group.getChildren.add(vBox)

    stage.setTitle("THOR")
    stage.setScene(scene)
    stage.sizeToScene()
    stage.centerOnScreen()
    stage.show()
  }
}
