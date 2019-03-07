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

import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.effect.DropShadow
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.{Group, Scene}

/**
  * User: TangYaruo
  * Date: 2019/3/7
  * Time: 16:43
  */


object ModeScene {

  trait ModeSceneListener {
    def gotoHumanScene()

    def gotoBotScene()
  }

}

class ModeScene {

  import ModeScene._

  val width = 500
  val height = 500
  val group = new Group
  private val scene = new Scene(group, width, height)

  scene.getStylesheets.add(
    this.getClass.getResource("css/modeScene.css").toExternalForm
  )


  val img = new ImageView("img/modeBg.jpg")
  img.setFitWidth(500)
  img.setFitHeight(500)
  group.getChildren.add(img)


  val humanChoice = new Button("Human")
  val botChoice = new Button("Robot")

  humanChoice.setLayoutX(205)
  humanChoice.setLayoutY(150)
  humanChoice.getStyleClass.add("mode-choice")
  botChoice.setLayoutX(105)
  botChoice.setLayoutY(280)
  botChoice.getStylesheets.add("mode-choice")

  val shadow = new DropShadow()

  humanChoice.addEventHandler(MouseEvent.MOUSE_ENTERED, (_: MouseEvent) => {
    humanChoice.setEffect(shadow)
  })

  humanChoice.addEventHandler(MouseEvent.MOUSE_EXITED, (_: MouseEvent) => {
    humanChoice.setEffect(null)
  })

  botChoice.addEventHandler(MouseEvent.MOUSE_ENTERED, (_: MouseEvent) => {
    botChoice.setEffect(shadow)
  })

  botChoice.addEventHandler(MouseEvent.MOUSE_EXITED, (_: MouseEvent) => {
    botChoice.setEffect(null)
  })

  def getScene: Scene = this.scene

  var listener: ModeSceneListener = _

  group.getChildren.addAll(humanChoice, botChoice)

  humanChoice.setOnAction(_ => listener.gotoHumanScene())
  botChoice.setOnAction(_ => listener.gotoBotScene())

  def setListener(listener: ModeSceneListener): Unit = {
    this.listener = listener
  }

}
