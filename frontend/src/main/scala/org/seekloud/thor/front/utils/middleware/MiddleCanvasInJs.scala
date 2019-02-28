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

import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.html.Canvas
import org.seekloud.thor.shared.ptcl.util.middleware.MiddleCanvas

/**
  * copied from tank
  */
object MiddleCanvasInJs {
  def apply(width: Double, height: Double): MiddleCanvasInJs = new MiddleCanvasInJs(width, height)

  def apply(name: String, width: Double, height: Double): MiddleCanvasInJs = new MiddleCanvasInJs(name, width, height)
}

class MiddleCanvasInJs private() extends MiddleCanvas {
  private[this] var canvas: Canvas = _

  def this(width: Double, height: Double) = {
    this()
    canvas = dom.document.createElement("canvas").asInstanceOf[html.Canvas]
    setWidth(width)
    setHeight(height)
  }

  def this(name: String, width: Double, height: Double) = {
    this()
    canvas = dom.document.getElementById(name).asInstanceOf[Canvas]
    setWidth(width)
    setHeight(height)
  }

  def getCanvas = canvas

  override def getCtx = MiddleContextInJs(this)

  override def getWidth = canvas.width

  override def getHeight = canvas.height

  override def setWidth(h: Any) = canvas.width = h.asInstanceOf[Int]

  override def setHeight(h: Any) = canvas.height = h.asInstanceOf[Int]

  override def change2Image = canvas
}