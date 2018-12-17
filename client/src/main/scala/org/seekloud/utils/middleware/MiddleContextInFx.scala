package org.seekloud.utils.middleware

import javafx.geometry.VPos
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import javafx.scene.shape.{StrokeLineCap, StrokeLineJoin}
import javafx.scene.text.{Font, FontWeight, TextAlignment}

import org.seekloud.thor.shared.ptcl.util.middleware.MiddleContext

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

  def getContext = context

  override def setGlobalAlpha(alpha: Double): Unit = context.setGlobalAlpha(alpha)

  override def setStrokeStyle(color: String) = {
    context.setStroke(Color.web(color))
  }

  override def fill = context.fill()

  override def setFill(color: String) = context.setFill(Color.web(color))

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
    }
  }

  override def fillRec(x: Double, y: Double, w: Double, h: Double) = context.fillRect(x, y, w, h)

  override def clearRect(x: Double, y: Double, w: Double, h: Double) = context.clearRect(x, y, w, h)

  override def beginPath() = context.beginPath()

  override def lineTo(x1: Double, y1: Double) = context.lineTo(x1, y1)

  override def stroke() = context.stroke()

  override def fillText(text: String, x: Double, y: Double, z: Double = 500) = context.fillText(text, x, y)

  override def setFont(f: String, s: Double) = context.setFont(Font.font(f, s))

  override def setTextAlign(s: String) = context.setTextAlign(s)

  override def setTextBaseLine(s: String) = context.setTextBaseline(s)

  override def rect(x: Double, y: Double, w: Double, h: Double) = context.rect(x, y, w, h)

  override def strokeText(text: String, x: Double, y: Double, maxWidth: Double) = context.strokeText(text, x, y, maxWidth)

  override def rotate(d: Float): Unit = context.rotate(math.toDegrees(d))

  override def translate(x: Float, y: Float): Unit = context.translate(x, y)

  override def save(): Unit = context.save()

  override def restore(): Unit = context.restore()

  override def arc(x: Double, y: Double, r: Double, sAngle: Double, eAngle: Double, counterclockwise: Boolean): Unit = context.arc(x, y, r, r, sAngle, eAngle)

  override def lineWidth(width: Double): Unit = context.setLineWidth(width)
}
