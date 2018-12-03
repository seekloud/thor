package org.seekloud.thor.shared.ptcl.util.middleware

/**
  * Created by sky
  * Date on 2018/11/17
  * Time at 上午11:22
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
