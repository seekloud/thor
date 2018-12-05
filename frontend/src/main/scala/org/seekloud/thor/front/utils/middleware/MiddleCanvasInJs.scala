package org.seekloud.thor.front.utils.middleware

import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.html.Canvas
import org.seekloud.thor.shared.ptcl.util.middleware.MiddleCanvas

/**
  * Created by sky
  * Date on 2018/11/16
  * Time at 下午3:18
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