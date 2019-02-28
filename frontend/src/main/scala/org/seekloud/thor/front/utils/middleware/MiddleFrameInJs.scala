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

package org.seekloud.thor.front.utils.middleware

import org.seekloud.thor.shared.ptcl.util.middleware.{MiddleCanvas, MiddleFrame, MiddleImage}

/**
  * copied from tank
  */
class MiddleFrameInJs extends MiddleFrame {
  override def createCanvas(width: Double, height: Double): MiddleCanvas = MiddleCanvasInJs(width, height)

  def createCanvas(name: String, width: Double, height: Double) = MiddleCanvasInJs(name, width, height)

  override def createImage(url: String): MiddleImage = MiddleImageInJs(url)
}
