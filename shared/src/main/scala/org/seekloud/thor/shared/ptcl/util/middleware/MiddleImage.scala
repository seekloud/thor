package org.seekloud.thor.shared.ptcl.util.middleware

/**
  * copied from tank
  * 合并框架中的image
  */
trait MiddleImage {
  def isComplete: Boolean

  def width: Double
  def height: Double
}
