package org.seekloud.thor.shared.ptcl.util.middleware

/**
  * Created by sky
  * Date on 2018/11/16
  * Time at 下午4:48
  * 合并框架中的image
  */
trait MiddleImage {
  def isComplete: Boolean

  def width: Double
  def height: Double
}
