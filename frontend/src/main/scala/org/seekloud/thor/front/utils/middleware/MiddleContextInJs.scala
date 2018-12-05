package org.seekloud.thor.front.utils.middleware

import org.scalajs.dom
import org.scalajs.dom.html.Canvas
import org.seekloud.thor.shared.ptcl.util.middleware.MiddleContext

/**
  * copied from tank
  */
object MiddleContextInJs {
  def apply(canvas: MiddleCanvasInJs): MiddleContextInJs = new MiddleContextInJs(canvas)
}

class MiddleContextInJs extends MiddleContext {
  private[this] var context: dom.CanvasRenderingContext2D = _

  def this(canvas: MiddleCanvasInJs) = {
    this()
    context = canvas.getCanvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
  }

  def getContext = context

  override def setGlobalAlpha(alpha: Double): Unit = context.globalAlpha = alpha

  override def setStrokeStyle(color: String): Unit = context.strokeStyle = color

  override def fill = context.fill()

  override def setFill(color: String) = context.fillStyle = color

  override def moveTo(x: Double, y: Double): Unit = context.moveTo(x, y)

  override def drawImage(image: Any, offsetX: Double, offsetY: Double, size: Option[(Double, Double)] = None, imgOffsetX: Option[Double] = None, imgOffsetY: Option[Double] = None, imgSize: Option[(Double, Double)] = None): Unit = {
    image match {
      case js: MiddleImageInJs =>
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
      case js: Canvas =>
        if(imgOffsetX.isEmpty){
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

  override def fillText(text: String, x: Double, y: Double, z: Double = 500) = context.fillText(text, x, y, z)

  override def setFont(fontFamily: String, fontSize: Double) = context.font = s"${fontSize}px $fontFamily"

  override def setTextAlign(s: String) = context.textAlign = s

  override def setTextBaseLine(s: String) = context.textBaseline = s

  override def rect(x: Double, y: Double, w: Double, h: Double) = context.rect(x, y, w, h)

  override def strokeText(text: String, x: Double, y: Double, maxWidth: Double) = context.strokeText(text, x, y, maxWidth)

  override def translate(x: Float, y: Float): Unit = context.translate(x, y)

  override def rotate(d: Float): Unit = context.rotate(d)

  override def save(): Unit = context.save()

  override def restore(): Unit = context.restore()
}
