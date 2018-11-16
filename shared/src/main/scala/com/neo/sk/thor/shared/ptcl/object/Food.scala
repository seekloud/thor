package com.neo.sk.thor.shared.ptcl.`object`

import com.neo.sk.thor.shared.ptcl.model.Point

import com.neo.sk.thor.shared.ptcl.model

/**
  * Created by Jingyi on 2018/11/9
  */
case class FoodState(fId: Long, level:Int, position: Point, radius: Float)

trait Food extends CircleObjectOfGame {

  val fId: Long

  var level: Int

  override protected var position: Point

  override var radius: Float

  def getFoodState: FoodState = {
    FoodState(fId, level, position, radius)
  }

}

object Food {
  def apply(foodState: FoodState): Food =
    AddNormalFood(foodState.fId, foodState.level, foodState.position, foodState.radius)
}

case class AddNormalFood(
                        fId: Long,
                        var level: Int,
                        var position: model.Point,
                        var radius: Float
                        ) extends Food