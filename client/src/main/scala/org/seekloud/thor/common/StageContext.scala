/*
 * Copyright 2018 seekloud (https://github.com/seekloud)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.seekloud.thor.common

import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.stage.Stage

/**
  * User: XuSiRan
  * Date: 2018/12/6
  * Time: 16:14
  */

object StageContext {

  trait StageListener {
    def onCloseRequest(): Unit
  }

}


class StageContext(stage: Stage) {

  import StageContext._

//  var stageListener: StageListener = _
//
//  stage.setOnCloseRequest(_ => stageListener.onCloseRequest())

  def getStage: Stage = stage

  def getStageWidth: Double = stage.getWidth

  def getStageHeight: Double = stage.getHeight

  def isFullScreen: Boolean = stage.isFullScreen

  def switchScene(scene: Scene, title: String = "THOR", resize: Boolean = false, fullScreen: Boolean = false, isSetOffX: Boolean = false): Unit = {
//    stage.centerOnScreen()
    stage.setScene(scene)
    stage.sizeToScene()
    stage.setResizable(resize)
    stage.setTitle(title)
    stage.setFullScreen(fullScreen)
    if (isSetOffX) {
      stage.setX(0)
      stage.setY(0)
    }
    stage.show()
  }

  def switchToLayer(scene: Scene, title: String = "THOR", resize: Boolean = false, fullScreen: Boolean = false): Unit = {
    //    stage.centerOnScreen()
    stage.setScene(scene)
    stage.sizeToScene()
    stage.setResizable(resize)
    stage.setTitle(title)
    stage.setFullScreen(fullScreen)
    stage.setX(350)
    stage.setY(185)
    stage.setWidth(1230)
    stage.setHeight(660)
    stage.show()
  }
//  def setStageListener(listener: StageListener): Unit = {
//    stageListener = listener
//  }

  def closeStage(): Unit = {
//    ClientBoot.sdkServer ! SdkServer.Shutdown
//    stage.close()
//    System.exit(0) //TODO
  }

}
