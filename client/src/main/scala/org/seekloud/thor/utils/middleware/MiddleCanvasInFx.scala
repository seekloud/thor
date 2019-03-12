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

import javafx.scene.SnapshotParameters
import javafx.scene.canvas.Canvas
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import org.seekloud.thor.shared.ptcl.util.middleware._

/**
  * copied from tank
  */
object MiddleCanvasInFx {
  def apply(width: Float, height: Float): MiddleCanvasInFx = new MiddleCanvasInFx(width, height)
}

class MiddleCanvasInFx private() extends MiddleCanvas {

  private[this] var canvas: Canvas = _

  def this(width: Float, height: Float) = {
    this()
    canvas = new Canvas(width, height)
    setWidth(width)
    setHeight(height)
  }

  def getCanvas: Canvas = canvas

  override def getCtx = MiddleContextInFx(this)

  override def getWidth(): Double = canvas.getWidth

  override def getHeight(): Double = canvas.getHeight

  override def setWidth(h: Any): Unit = h match {
    case d: Float => canvas.setWidth(d)
    case _ => canvas.setWidth(h.asInstanceOf[Int].toFloat)
  }

  override def setHeight(h: Any): Unit = h match {
    case d: Float => canvas.setHeight(d)
    case _ => canvas.setHeight(h.asInstanceOf[Int].toFloat)
  }

  override def change2Image(): WritableImage = {
    val a = System.currentTimeMillis()
    val params = new SnapshotParameters
    params.setFill(Color.TRANSPARENT)
    val c = canvas.snapshot(params, null)
    val b = System.currentTimeMillis()
//    println(s"span 2 is ${b-a}")
    c
  }
}
