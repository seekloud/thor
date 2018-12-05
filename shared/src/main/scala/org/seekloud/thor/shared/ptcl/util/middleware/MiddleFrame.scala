package org.seekloud.thor.shared.ptcl.util.middleware

/**
  * copied from tank
  * 合并两个框架
  */
trait MiddleFrame {
  /**
    * @param width canvas宽
    * @param height canvas长
    * */
  def createCanvas(width: Double, height: Double): MiddleCanvas

  /**
    * @param url 图片路径（初始版本中HTML和Fx为了统一放置在对应路径中）
    * */
  def createImage(url: String): MiddleImage
}
