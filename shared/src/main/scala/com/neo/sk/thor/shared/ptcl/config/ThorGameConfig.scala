package com.neo.sk.thor.shared.ptcl.config

import com.neo.sk.thor.shared.ptcl.model.Point

/**
  * User: TangYaruo
  * Date: 2018/11/12
  * Time: 16:13
  */
trait ThorGameConfig {

  def frameDuration:Long

  def boundary: Point

  def thorRadius:Float

  def getEnergyByFoodLevel(foodLevel: Int): Int
  def getMaxEnergyByLevel(adventurerLevel: Int): Int
  def getWeaponLevelByLevel(adventurerLevel: Int): Int
  def getWeaponLengthByLevel(adventurerLevel: Int): Int

}

