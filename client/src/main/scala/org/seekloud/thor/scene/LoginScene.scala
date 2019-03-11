//  Copyright 2018 seekloud (https://github.com/seekloud)
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.seekloud.thor.scene

import java.io.ByteArrayInputStream

import javafx.scene.canvas.{Canvas, GraphicsContext}
import javafx.scene.control.Button
import javafx.scene.effect.DropShadow
import javafx.scene.image.{Image, ImageView}
import javafx.scene.input.MouseEvent
import javafx.scene.{Group, Scene}
import org.seekloud.thor.ClientBoot
import org.seekloud.thor.model.Constants
import org.seekloud.thor.scene.ModeScene.ModeSceneListener

/**
  * User: TangYaruo
  * Date: 2019/3/7
  * Time: 16:43
  */

object LoginScene {

  trait LoginSceneListener {

    def emailLogin()

    def backToPrevious()
  }

}

class LoginScene {

  import LoginScene._


  val width: Int = Constants.PreWindow.width
  val height: Int = Constants.PreWindow.height
  val group = new Group
  private val scene = new Scene(group, width, height)


  scene.getStylesheets.add(
    this.getClass.getClassLoader.getResource("css/scene.css").toExternalForm
  )

  val canvas = new Canvas(width, height)
  val canvasCtx: GraphicsContext = canvas.getGraphicsContext2D
  group.getChildren.add(canvas)

  val img = new Image("img/modeBg.jpg")
  ClientBoot.addToPlatform(canvasCtx.drawImage(img, 0, 0, width, height))

  //  val img = new ImageView("img/modeBg.jpg")
  //  img.setFitWidth(width)
  //  img.setFitHeight(height)
  //  group.getChildren.add(img)

  val emailLogin = new Button("Use Email")
  val backToPrevious = new Button("Back")

  emailLogin.setLayoutX(210)
  emailLogin.setLayoutY(400)
  emailLogin.getStyleClass.add("mode-choice")
  backToPrevious.setLayoutX(320)
  backToPrevious.setLayoutY(400)
  backToPrevious.getStyleClass.add("mode-choice")

  val shadow = new DropShadow()

  emailLogin.addEventHandler(MouseEvent.MOUSE_ENTERED, (_: MouseEvent) => {
    emailLogin.setEffect(shadow)
  })

  emailLogin.addEventHandler(MouseEvent.MOUSE_EXITED, (_: MouseEvent) => {
    emailLogin.setEffect(null)
  })

  backToPrevious.addEventHandler(MouseEvent.MOUSE_ENTERED, (_: MouseEvent) => {
    backToPrevious.setEffect(shadow)
  })

  backToPrevious.addEventHandler(MouseEvent.MOUSE_EXITED, (_: MouseEvent) => {
    backToPrevious.setEffect(null)
  })

  group.getChildren.addAll(emailLogin, backToPrevious)

  def drawScanUrl(imageStream: ByteArrayInputStream): Unit = {
    ClientBoot.addToPlatform {
      val img = new Image(imageStream)
      canvasCtx.drawImage(img, 150, 80)
    }
  }

  def getScene: Scene = this.scene

  var listener: LoginSceneListener = _

  emailLogin.setOnAction(_ => listener.emailLogin())
  backToPrevious.setOnAction(_ => listener.backToPrevious())


  def setLoginSceneListener(listener: LoginSceneListener) {
    this.listener = listener
  }


}
