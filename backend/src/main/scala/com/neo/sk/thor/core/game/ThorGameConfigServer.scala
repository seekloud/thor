package com.neo.sk.thor.core.game

import com.neo.sk.thor.shared.ptcl.config.ThorGameConfig
import com.neo.sk.thor.shared.ptcl.model
import com.neo.sk.thor.shared.ptcl.model.Point

/**
  * @author Jingyi
  * @version 创建时间：2018/11/16
  */
case class ThorGameConfigServer() extends ThorGameConfig{
  override def boundary: model.Point = Point(500, 400)

  override def frameDuration: Long = 150

  override def thorRadius: Float = 5

  override def getEnergyByFoodLevel(foodLevel: Int): Int = {
    foodLevel * 3
  }

  override def getMaxEnergyByLevel(adventurerLevel: Int): Int = {
    adventurerLevel * 20
  }

  override def getWeaponLengthByLevel(adventurerLevel: Int): Int = {
    (adventurerLevel * 1.2).toInt
  }

  override def getWeaponLevelByLevel(adventurerLevel: Int): Int = {
    adventurerLevel
  }

}
