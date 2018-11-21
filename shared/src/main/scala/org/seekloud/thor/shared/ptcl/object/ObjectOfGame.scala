package org.seekloud.thor.shared.ptcl.`object`

import org.seekloud.thor.shared.ptcl.model.{Point, Rectangle, Shape}

/**
  * User: TangYaruo
  * Date: 2018/11/13
  * Time: 15:15
  *
  * copied from tank
  * 游戏中的所有物体元素基类
  */
trait ObjectOfGame {

  protected var position: Point

  /**
    * 获取当前元素的位置
    *
    * @return point(x,y)
    */
  final def getPosition: Point = position

  /**
    * 获取当前元素的包围盒
    *
    * @return rectangle
    */
  def getObjectRect(): Rectangle

  /**
    * 获取当前元素的外形
    *
    * @return shape
    */
  def getObjectShape(): Shape

  /**
    * 判断元素是否和其他元素有碰撞
    *
    * @param o 其他物体
    * @return 如果碰撞，返回true；否则返回false
    */
  def isIntersects(o: ObjectOfGame): Boolean


}
