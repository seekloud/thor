package com.neo.sk.thor.shared.ptcl.config

import com.neo.sk.thor.shared.ptcl.model.Point

/**
  * User: TangYaruo
  * Date: 2018/11/12
  * Time: 16:13
  */

final case class GridBoundary(width: Int, height: Int) {
  def getBoundary: Point = Point(width, height)
}

final case class AdventurerParams(
  speedLevel: List[Float],
  radiusLevel: List[Float]
)


trait ThorGameConfig {

  def frameDuration: Long

  def boundary: Point

  def getAdventurerRadiusByLevel(l: Int): Float

  def getEnergyByFoodLevel(l: Int): Int

  def getMaxEnergyByLevel(l: Int): Int

  def getWeaponLevelByLevel(l: Int): Int

  def getWeaponLengthByLevel(l: Int): Int

}


case class ThorGameConfigImpl(
  gridBoundary: GridBoundary,
  frameDuration: Long,
  adventurerParams: AdventurerParams
) extends ThorGameConfig {

  def getThorGameConfigImpl(): ThorGameConfigImpl = this

  def boundary = gridBoundary.getBoundary

  def getAdventurerRadiusByLevel(l: Int) = {
    adventurerParams.radiusLevel(l)
  }


  //TODO 详写
  def getEnergyByFoodLevel(l: Int) = {
    0
  }

  def getMaxEnergyByLevel(l: Int) = {
    0
  }

  def getWeaponLevelByLevel(l: Int) = {
    0
  }

  def getWeaponLengthByLevel(l: Int) = {
    0
  }


}

