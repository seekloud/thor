package org.seekloud.utils.middleware

import javafx.scene.SnapshotParameters
import javafx.scene.canvas.Canvas
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

  def getCanvas = canvas

  override def getCtx = MiddleContextInFx(this)

  override def getWidth = canvas.getWidth

  override def getHeight = canvas.getHeight

  override def setWidth(h: Any) = h match {
    case d: Float => canvas.setWidth(d)
    case _ => canvas.setWidth(h.asInstanceOf[Int].toFloat)
  }

  override def setHeight(h: Any) = h match {
    case d: Float => canvas.setHeight(d)
    case _ => canvas.setHeight(h.asInstanceOf[Int].toFloat)
  }

  override def change2Image = {
    val params = new SnapshotParameters
    params.setFill(Color.TRANSPARENT)
    canvas.snapshot(params, null)
  }
}
