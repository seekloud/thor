package org.seekloud.utils.middleware

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

  def getImage = image

  override def isComplete: Boolean = true

  override def height: Double = image.getHeight

  override def width: Double = image.getWidth
}
