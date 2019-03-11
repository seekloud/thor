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

import javafx.geometry.VPos
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.{Image, WritableImage}
import javafx.scene.paint.Color
import javafx.scene.effect.InnerShadow
import javafx.scene.shape.{StrokeLineCap, StrokeLineJoin}
import javafx.scene.text.{Font, FontWeight, Text, TextAlignment}
import org.seekloud.thor.shared.ptcl.util.middleware.MiddleContext
import org.seekloud.utils.middleware.MiddleCanvasInFx

/**
  * copied from tank
  */
object MiddleContextInFx {
  def apply(canvas: MiddleCanvasInFx): MiddleContextInFx = new MiddleContextInFx(canvas)

  implicit def string2TextAlignment(s: String): TextAlignment = {
    s match {
      case "center" => TextAlignment.CENTER
      case "left" => TextAlignment.LEFT
      case "right" => TextAlignment.RIGHT
      case "justify" => TextAlignment.JUSTIFY
      case _ => TextAlignment.CENTER
    }
  }

  implicit def string2TextBaseline(s: String): VPos = {
    s match {
      case "middle" => VPos.CENTER
      case "top" => VPos.TOP
      case "center" => VPos.CENTER
      case "bottom" => VPos.BOTTOM
      case _ => VPos.CENTER //设置默认值
    }
  }

  implicit def string2StrokeLineCap(s: String): StrokeLineCap = {
    s match {
      case "round" => StrokeLineCap.ROUND
      case "butt" => StrokeLineCap.BUTT
      case "square" => StrokeLineCap.SQUARE
      case _ => StrokeLineCap.ROUND //设置默认值
    }
  }

  implicit def string2StrokeLineJoin(s: String): StrokeLineJoin = {
    s match {
      case "round" => StrokeLineJoin.ROUND
      case "miter" => StrokeLineJoin.MITER
      case "revel" => StrokeLineJoin.BEVEL
      case _ => StrokeLineJoin.ROUND
    }
  }
}

class MiddleContextInFx extends MiddleContext {

  import MiddleContextInFx._

  private[this] var context: GraphicsContext = _

  def this(canvas: MiddleCanvasInFx) = {
    this()
    context = canvas.getCanvas.getGraphicsContext2D
  }

  def getContext: GraphicsContext = context

  override def setGlobalAlpha(alpha: Double): Unit = context.setGlobalAlpha(alpha)

  override def setStrokeStyle(color: String): Unit = {
    context.setStroke(Color.web(color))
  }

  override def fill: Unit = context.fill()

  override def setFill(color: String): Unit = context.setFill(Color.web(color))

  override def moveTo(x: Double, y: Double): Unit = context.moveTo(x, y)

  override def drawImage(image: Any, offsetX: Double, offsetY: Double, size: Option[(Double, Double)] = None, imgOffsetX: Option[Double] = None, imgOffsetY: Option[Double] = None, imgSize: Option[(Double, Double)] = None): Unit = {
    image match {
      case js: MiddleImageInFx =>
        if(imgOffsetX.isEmpty){
          if (size.isEmpty) {
            context.drawImage(js.getImage, offsetX, offsetY)
          } else {
            context.drawImage(js.getImage, offsetX, offsetY, size.get._1, size.get._2)
          }
        }
        else{
          context.drawImage(js.getImage, imgOffsetX.get, imgOffsetY.get, imgSize.get._1, imgSize.get._2, offsetX, offsetY, size.get._1, size.get._2)
        }
      case js: WritableImage =>
        if(imgOffsetX.isEmpty) {
          if (size.isEmpty) {
            context.drawImage(js, offsetX, offsetY)
          } else {
            context.drawImage(js, offsetX, offsetY, size.get._1, size.get._2)
          }
        }
        else{
          context.drawImage(js, imgOffsetX.get, imgOffsetY.get, imgSize.get._1, imgSize.get._2, offsetX, offsetY, size.get._1, size.get._2)
        }

      case js: Image =>
        if(imgOffsetX.isEmpty) {
          if (size.isEmpty) {
            context.drawImage(js, offsetX, offsetY)
          } else {
            context.drawImage(js, offsetX, offsetY, size.get._1, size.get._2)
          }
        }
        else{
          context.drawImage(js, imgOffsetX.get, imgOffsetY.get, imgSize.get._1, imgSize.get._2, offsetX, offsetY, size.get._1, size.get._2)
        }
      case js: MiddleCanvasInFx =>
        if(imgOffsetX.isEmpty){
          if (size.isEmpty) {
            context.drawImage(js.change2Image(), offsetX, offsetY)
          } else {

            context.drawImage(js.change2Image(), offsetX, offsetY, size.get._1, size.get._2)
          }
        }
        else{
          context.drawImage(js.change2Image(), imgOffsetX.get, imgOffsetY.get, imgSize.get._1, imgSize.get._2, offsetX, offsetY, size.get._1, size.get._2)
        }
    }
  }

  override def fillRec(x: Double, y: Double, w: Double, h: Double): Unit = context.fillRect(x, y, w, h)

  override def clearRect(x: Double, y: Double, w: Double, h: Double): Unit = context.clearRect(x, y, w, h)

  override def beginPath(): Unit = context.beginPath()

  override def closePath(): Unit = context.closePath()

  override def lineTo(x1: Double, y1: Double): Unit = context.lineTo(x1, y1)

  override def stroke(): Unit = context.stroke()

  override def fillText(text: String, x: Double, y: Double, z: Double = 500): Unit = context.fillText(text, x, y)

  override def setFont(f: String, s: Double, wid: String = "normal"): Unit = context.setFont(Font.font(f, s))

  override def setTextAlign(s: String): Unit = context.setTextAlign(s)

  override def setShadowColor(s: String): Unit = {
    val shadowColor = new InnerShadow()
    shadowColor.setColor(Color.WHITE)
    context.setEffect(shadowColor)
  }

  override def setTextBaseLine(s: String): Unit = context.setTextBaseline(s)

  override def rect(x: Double, y: Double, w: Double, h: Double): Unit = context.rect(x, y, w, h)

  override def strokeText(text: String, x: Double, y: Double, maxWidth: Double): Unit = context.strokeText(text, x, y, maxWidth)

  override def rotate(d: Float): Unit = context.rotate(math.toDegrees(d))

  override def translate(x: Float, y: Float): Unit = context.translate(x, y)

  override def save(): Unit = context.save()

  override def restore(): Unit = context.restore()

  override def arc(x: Double, y: Double, r: Double, sAngle: Double, eAngle: Double, counterclockwise: Boolean): Unit = context.arc(x, y, r, r, sAngle, eAngle)

  override def lineWidth(width: Double): Unit = context.setLineWidth(width)

  override def measureText(s: String): Double = new Text(s).getLayoutBounds.getWidth
}
