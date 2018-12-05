package org.seekloud.thor.common

import java.io.ByteArrayInputStream

import akka.actor.typed.ActorRef
import javafx.scene.{Group, Scene}
import javafx.stage.Stage
import org.seekloud.thor.App.{executor, scheduler, system, timeout}
import org.seekloud.thor.App.loginActor
import org.seekloud.thor.actor.{LoginActor, TokenActor}
import akka.actor.typed.scaladsl.AskPattern._
import javafx.collections.{FXCollections, ObservableList}
import javafx.scene.control.{Button, ListView}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.paint.Color
import javafx.scene.text.{Font, Text}
import org.seekloud.thor.protocol.ESheepProtocol._
import sun.misc.BASE64Decoder

import scala.concurrent.Future

/**
  * User: XuSiRan
  * Date: 2018/12/4
  * Time: 11:41
  * 存储各种page
  */
class ClientPage(stage: Stage){

  loginActor ! LoginActor.GetLoginImg(this)

  def imgSence(urls: LoginUrlRsp): Unit ={
    val decoder = new BASE64Decoder()
    val bytes = decoder.decodeBuffer(urls.data.scanUrl.split(",")(1))
    val out = new ByteArrayInputStream(bytes)
    val image = new Image(out)
    val imageView = new ImageView()
    imageView.setImage(image)
//    imageView.setFitHeight(300)
//    imageView.setFitWidth(300)
//    imageView.setX(100)
//    imageView.setY(60)
    val group = new Group
    group.getChildren.add(imageView)
    val scene = new Scene(group)
    stage.setScene(scene)
    stage.show()
  }

  //一个临时的Scene
  def infoSence(data: WsPlayerInfoRsp): Unit ={
    val NameText = new Text(300, 50, "连接中")
    NameText.setFont(Font.font(48))
    val group = new Group()
    group.getChildren.add(NameText)
    val scene = new Scene(group, 900, 500)
    stage.setScene(scene)
    stage.sizeToScene()
    stage.centerOnScreen()
    stage.show()
  }

  def roomScene(replyTo: ActorRef[TokenActor.Command], roomList: List[Long]) ={
    val confirmBtn = new Button("进入游戏房间")

    val observableList:ObservableList[String] = FXCollections.observableArrayList()
    val listView = new ListView[String](observableList)

    confirmBtn.setOnAction{_ => println("ok")}
    confirmBtn.setLayoutX(300)

    roomList.foreach(t => observableList.add(t.toString))
    val group = new Group()
    group.getChildren.add(listView)
    group.getChildren.add(confirmBtn)
    val scene = new Scene(group, 900, 500)
    stage.setScene(scene)
    stage.sizeToScene()
    stage.centerOnScreen()
    stage.show()
  }
}
