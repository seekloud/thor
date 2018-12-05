package org.seekloud.thor.shared.ptcl.util.middleware

/**
  * copied from tank
  * 合并框架中的canvas
  * 本文件中定义画图中使用的函数并在实例中重写
  */
trait MiddleCanvas {

  def getCtx: MiddleContext

  def getWidth(): Double

  def getHeight(): Double

  def setWidth(h: Any): Unit //设置宽

  def setHeight(h: Any): Unit //设置高

  def change2Image(): Any //转换快照
}
