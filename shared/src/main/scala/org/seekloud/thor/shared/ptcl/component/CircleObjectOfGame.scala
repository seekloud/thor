package org.seekloud.thor.shared.ptcl.component

import org.seekloud.thor.shared.ptcl.model

/**
  * User: TangYaruo
  * Date: 2018/11/13
  * Time: 15:22
  *
  * copied from tank
  * 圆形的游戏物体元素
  */
trait CircleObjectOfGame extends ObjectOfGame{



  var radius:Float //半径


  final def getRadius = radius

  /**
    * 获取当前元素的包围盒
    * @return  rectangle
    */
  override def getObjectRect(): model.Rectangle = {
    model.Rectangle(position - model.Point(radius, radius),position + model.Point(radius, radius))
  }

  /**
    * 获取当前元素的外形
    * @return  shape
    */
  override def getObjectShape(): model.Shape = {
    model.Circle(position,radius)
  }

  /**
    * 判断元素是否和其他元素有碰撞
    * @param o 其他物体
    * @return  如果碰撞，返回true；否则返回false
    */
  override def isIntersects(o: ObjectOfGame): Boolean = {
    o match {
      case t:CircleObjectOfGame => isIntersects(t)
      case t:RectangleObjectOfGame => isIntersects(t)
    }
  }


  private def isIntersects(o: CircleObjectOfGame): Boolean = {
    this.position.distance(o.position) < o.radius + this.radius
  }

  private def isIntersects(o: RectangleObjectOfGame): Boolean = {
    o.isIntersects(this)
  }

}
