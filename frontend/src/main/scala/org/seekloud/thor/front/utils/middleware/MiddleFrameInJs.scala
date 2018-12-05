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
