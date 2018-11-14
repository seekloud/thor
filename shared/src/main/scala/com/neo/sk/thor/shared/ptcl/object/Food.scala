package com.neo.sk.thor.shared.ptcl.`object`

import com.neo.sk.thor.shared.ptcl.model

/**
  * Created by Jingyi on 2018/11/9
  */
case class FoodState(id: Int, level:Byte, position: model.Point, radius: Float)

trait Food extends CircleObjectOfGame {

  val foodId: Int
  val foodLevel: Byte
  override var position: model.Point
  override val radius: Float

  /*具体实现方法方法继承，可以重写
  * isIntersects(o: ObjectOfGame)
  * getRadius
  * getPosition()*/
}

object Food {
  def apply(foodState: FoodState): Food =
    AddNormalFood(foodState.id, foodState.level, foodState.position, foodState.radius)
}

case class AddNormalFood(
                        foodId: Int,
                        foodLevel: Byte,
                        var position: model.Point,
                        radius: Float
                        ) extends Food