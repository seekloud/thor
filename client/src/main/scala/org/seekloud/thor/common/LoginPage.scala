package org.seekloud.thor.common

import java.io.ByteArrayInputStream

import javafx.scene.{Group, Scene}
import javafx.stage.Stage
import org.seekloud.thor.App.{executor, scheduler, timeout, system}
import org.seekloud.thor.App.loginActor
import org.seekloud.thor.actor.LoginActor
import akka.actor.typed.scaladsl.AskPattern._
import javafx.scene.image.{Image, ImageView}
import javafx.scene.paint.Color
import javafx.scene.text.{Text, Font}
import org.seekloud.thor.protocol.ESheepProtocol._
import sun.misc.BASE64Decoder

import scala.concurrent.Future

/**
  * User: XuSiRan
  * Date: 2018/12/4
  * Time: 11:41
  */
class LoginPage(stage: Stage){

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
    val NameText = new Text(100, 50, data.data.nickname)
    val TokenText = new Text(100, 100, "Token: " + data.data.token)
    val TimeText = new Text(100, 150, "ExpireTime: " + data.data.tokenExpireTime.toString)
    val GenderText = new Text(100, 200, "Gender: " + data.data.gender.toString)
    NameText.setFont(Font.font(32))
    TokenText.setFont(Font.font(17))
    TimeText.setFont(Font.font(17))
    GenderText.setFont(Font.font(17))
    val group = new Group()
    group.getChildren.removeAll()
    group.getChildren.add(NameText)
    group.getChildren.add(TokenText)
    group.getChildren.add(TimeText)
    group.getChildren.add(GenderText)
    val scene = new Scene(group, 900, 500)
    stage.setScene(scene)
    stage.sizeToScene()
    stage.centerOnScreen()
    stage.show()
  }
}
