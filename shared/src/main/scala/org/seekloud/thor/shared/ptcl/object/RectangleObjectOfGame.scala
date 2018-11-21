package org.seekloud.thor.shared.ptcl.`object`

import org.seekloud.thor.shared.ptcl.model
import org.seekloud.thor.shared.ptcl.model.Point

/**
  * User: TangYaruo
  * Date: 2018/11/13
  * Time: 15:24
  *
  * copied from tank
  * 矩形游戏物体元素
  */
trait RectangleObjectOfGame extends ObjectOfGame {
  protected val width: Float
  protected val height: Float
  protected val collisionOffset: Float //？

  final def getWidth = width

  final def getHeight = height

  private[this] def collisionWidth = width - collisionOffset

  private[this] def collisionHeight = height - collisionOffset


  /**
    * 获取当前元素的包围盒
    *
    * @return rectangle
    */
  override def getObjectRect(): model.Rectangle = {
    model.Rectangle(position - model.Point(width / 2, height / 2), position + model.Point(width / 2, height / 2))
  }

  /**
    * 获取当前元素的外形
    *
    * @return shape
    */
  override def getObjectShape(): model.Shape = {
    getObjectRect()
  }

  /**
    * 判断元素是否和其他元素有碰撞
    *
    * @param o 其他物体
    * @return 如果碰撞，返回true；否则返回false
    */
  override def isIntersects(o: ObjectOfGame): Boolean = {
    o match {
      case t: CircleObjectOfGame => isIntersects(t)
      case t: RectangleObjectOfGame => isIntersects(t)
    }
  }

  private def isIntersects(o: CircleObjectOfGame): Boolean = {
    val topLeft = position - model.Point(collisionWidth / 2, collisionHeight / 2)
    val downRight = position + model.Point(collisionWidth / 2, collisionHeight / 2)
    if (o.getPosition > topLeft && o.getPosition < downRight) {
      true
    } else {
      val relativeCircleCenter: Point = o.getPosition - position
      val dx = math.min(relativeCircleCenter.x, collisionWidth / 2)
      val dx1 = math.max(dx, -collisionHeight / 2)
      val dy = math.min(relativeCircleCenter.y, collisionHeight / 2)
      val dy1 = math.max(dy, -collisionHeight / 2)
      Point(dx1, dy1).distance(relativeCircleCenter) < o.radius
    }
  }

  private def isIntersects(o: RectangleObjectOfGame): Boolean = {
    getObjectRect().intersects(o.getObjectRect())
  }
}
