/*
 *   Copyright 2018 seekloud (https://github.com/seekloud)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.seekloud.thor.utils

import java.util.Optional

import javafx.scene.control.{Alert, ButtonType}
import javafx.scene.control.Alert.AlertType

/**
  * User: TangYaruo
  * Date: 2019/3/8
  * Time: 10:55
  */
object WarningDialog {

  def initWarningDialog(context:String): Optional[ButtonType] = {
    val alert = new Alert(AlertType.WARNING)
    alert.setTitle("警告")
    alert.setHeaderText("")
    alert.setContentText(context)
    alert.showAndWait()
  }

}
