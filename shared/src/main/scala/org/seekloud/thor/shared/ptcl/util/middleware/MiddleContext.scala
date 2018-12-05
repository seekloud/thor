package org.seekloud.thor.shared.ptcl.util.middleware

/**
  * copied from tank
  * 合并框架中的ctx
  */

/**
  * 本文件为了统一JvavFx和Js，请注意以下:
  * color：设置rgb或者rgba或者16进制
  *
  **/
trait MiddleContext {

  def setGlobalAlpha(alpha: Double): Unit

  def fill(): Unit

  def setFill(color: String): Unit

  def setStrokeStyle(color: String): Unit

  def moveTo(x: Double, y: Double): Unit

  def drawImage(image: Any, offsetX: Double, offsetY: Double, size: Option[(Double, Double)] = None, imgOffsetX: Option[Double] = None, imgOffsetY: Option[Double] = None, imgSize: Option[(Double, Double)] = None): Unit

  def fillRec(x: Double, y: Double, w: Double, h: Double): Unit

  def clearRect(x: Double, y: Double, w: Double, h: Double): Unit

  def beginPath(): Unit

  def lineTo(x1: Double, y1: Double): Unit

  def stroke(): Unit

  def fillText(text: String, x: Double, y: Double, z: Double = 500): Unit

  def setTextBaseLine(s: String):Unit

  def setFont(fontFamily: String, fontSize: Double): Unit

  def setTextAlign(s: String)

  def rect(x: Double, y: Double, w: Double, h: Double)

  def strokeText(text: String, x: Double, y: Double, maxWidth: Double): Unit

  def rotate(d: Float): Unit

  def translate(x: Float, y: Float): Unit

  def save(): Unit

  def restore(): Unit
}
