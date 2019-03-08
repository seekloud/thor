///*
// *   Copyright 2018 seekloud (https://github.com/seekloud)
// *
// *   Licensed under the Apache License, Version 2.0 (the "License");
// *   you may not use this file except in compliance with the License.
// *   You may obtain a copy of the License at
// *
// *       http://www.apache.org/licenses/LICENSE-2.0
// *
// *   Unless required by applicable law or agreed to in writing, software
// *   distributed under the License is distributed on an "AS IS" BASIS,
// *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *   See the License for the specific language governing permissions and
// *   limitations under the License.
// */
//
//package org.seekloud.thor.scene
//
//import javafx.scene.control.Button
//import javafx.scene.effect.DropShadow
//import javafx.scene.image.ImageView
//import javafx.scene.input.MouseEvent
//import javafx.scene.{Group, Scene}
//import com.neo.sk.utils.JavaFxUtil
//import javafx.scene.canvas.GraphicsContext
//import org.seekloud.thor.ClientBoot
//import org.seekloud.thor.common.StageContext
//import org.seekloud.thor.shared.ptcl.model.{Constants, Point}
//import javafx.scene.canvas.GraphicsContext
//import javafx.scene.canvas.Canvas
//import javafx.scene.{Group, ImageCursor, Scene}
//import javafx.scene.paint.Color
//import javafx.scene.image.Image
//import javafx.scene.text.{Font, FontWeight, TextAlignment}
//import akka.actor.typed.ActorRef
//import javafx.beans.property.{ObjectProperty, SimpleObjectProperty, SimpleStringProperty, StringProperty}
//import javafx.collections.{FXCollections, ObservableList}
//import javafx.fxml.FXML
//import javafx.geometry.Insets
//import javafx.scene.control.cell.PropertyValueFactory
//import javafx.scene.control.{Button, Label, TableColumn, TableView}
//import javafx.scene.image.{Image, ImageView}
//import javafx.scene.layout.VBox
//import javafx.scene.text.{Font, Text}
//import javafx.scene.{Group, Scene}
//import javafx.stage.Stage
//import org.seekloud.thor.ClientBoot
////import org.seekloud.thor.ClientBoot.loginActor
//import org.seekloud.thor.common.StageContext
////import org.seekloud.thor.controller.PlayGameController
//import org.seekloud.thor.core.{TokenActor, LoginActor}
//import org.seekloud.thor.model.{GameServerInfo, PlayerInfo, UserInfo}
//import org.seekloud.thor.protocol.ESheepProtocol._
//import sun.misc.BASE64Decoder
//
//
//import org.seekloud.utils.middleware.{MiddleCanvasInFx, MiddleFrameInFx}
///**
//  * User: Jason
//  * Date: 2019/3/7
//  * Time: 20:55
//  */
//class RoomScene(context: StageContext) {
//  def roomScene(replyTo: ActorRef[TokenActor.Command], roomList: List[Long], playerInfo: PlayerInfo, gameServerInfo: GameServerInfo) ={
//
//    val group = new Group()
//    val scene = new Scene(group)
//
//    val observableList: ObservableList[RoomInfo] = FXCollections.observableArrayList()
//    roomList.foreach{t =>
//      val oneRoomInfo = RoomInfo(new SimpleStringProperty(t.toString), new SimpleStringProperty("一般房间"))
//      oneRoomInfo.edit.get().setOnAction{e =>
//        App.pushStack2AppThread{
//          val playGameView = new PlayGameView(context)
//          new PlayGameController(playerInfo, gameServerInfo, context, playGameView, Some(t.toString)).start
//          context.switchScene(playGameView.getScene, resize = true, fullScreen = true)
//        }
//      }
//      observableList.add(oneRoomInfo)
//    }
//
//    val tableView = new TableView[RoomInfo]()
//
//    val nameLabel = new Label("用户名：" + playerInfo.nickName)
//    nameLabel.setFont(Font.font(17))
//    //    val label = new Label("房间列表")
//    //    label.setFont(Font.font(20))
//
//    val roomIdCol = new TableColumn[RoomInfo, String]("房间号")
//    roomIdCol.setMinWidth(80)
//    roomIdCol.setCellValueFactory( new PropertyValueFactory[RoomInfo, String]("id"))
//    val descCol = new TableColumn[RoomInfo, String]("房间描述")
//    descCol.setMinWidth(100)
//    descCol.setCellValueFactory( new PropertyValueFactory[RoomInfo, String]("desc"))
//    val btnCol = new TableColumn[RoomInfo, Button]("操作")
//    //    btnCol.setMinWidth(80)
//    btnCol.setCellValueFactory( new PropertyValueFactory[RoomInfo, Button]("edit"))
//
//    tableView.setItems(observableList)
//    tableView.getColumns.addAll(roomIdCol, descCol, btnCol)
//
//    val vBox = new VBox()
//    vBox.setSpacing(10)
//    vBox.setPadding(new Insets(15,5,20,5))
//    vBox.getChildren.addAll(nameLabel, tableView)
//
//    group.getChildren.add(vBox)
//
//    context.switchScene(scene)
//  }
//}
