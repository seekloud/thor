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

package org.seekloud.thor.utils.middleware

import javafx.scene.image.Image

import org.seekloud.thor.shared.ptcl.util.middleware._

/**
  * Created by sky
  * Date on 2018/11/16
  * Time at 下午4:51
  */
object MiddleImageInFx {
  def apply(url: String): MiddleImageInFx = new MiddleImageInFx(url)
}

class MiddleImageInFx extends MiddleImage {
  private[this] var image: Image = _

  def this(url: String) = {
    this()
    image = new Image(url)
  }

  def getImage: Image = image

  override def isComplete: Boolean = image.isBackgroundLoading

  override def height: Double = image.getHeight

  override def width: Double = image.getWidth
}
