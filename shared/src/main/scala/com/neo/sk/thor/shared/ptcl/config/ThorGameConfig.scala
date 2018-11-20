package com.neo.sk.thor.shared.ptcl.config

import com.neo.sk.thor.shared.ptcl.model.Point

/**
  * User: TangYaruo
  * Date: 2018/11/12
  * Time: 16:13
  */

final case class GridBoundary(width:Int,height:Int){
  def getBoundary:Point = Point(width,height)
}

final case class AdventurerMoveSpeed(speeds: List[Float]) {
  def getThorSpeedByLevel(l: Int) = Point(speeds(l), 0)
}

final case class AdventurerParams(
  speeds: AdventurerMoveSpeed,
  radius: List[Float],
  maxEnergyList: List[Int],
  containEnergyList: List[Int],
  facePalstance: Float
)

final case class FoodParams(
  energyList: List[Int],
  radiusList: List[Float]
)
final case class WeaponParams(
  lengthList: List[Float]
)


trait ThorGameConfig {

  def frameDuration:Long

  def boundary: Point

  def facePalstance: Float

  def getAdventurerRadiusByLevel(l: Int): Float

  def getRadiusByFoodLevel(l: Int): Float

  def getEnergyByFoodLevel(l: Int): Int

  def getMaxEnergyByLevel(l: Int): Int

  def getWeaponLevelByLevel(l: Int): Int

  def getWeaponLengthByLevel(l: Int): Float

  def getThorGameConfigImpl(): ThorGameConfigImpl

  def getThorSpeedByLevel(l: Int, isSpeedUp: Boolean = false): Point

  def getMoveDistanceByFrame(l: Int, isSpeedUp: Boolean = false) = getThorSpeedByLevel(l, isSpeedUp) * frameDuration / 1000

  def getAdventurerLevelSize: Int

}


case class ThorGameConfigImpl (
  gridBoundary: GridBoundary,
  frameDuration:Long,
  adventurerParams: AdventurerParams,
  foodParams: FoodParams,
  weaponParams: WeaponParams
) extends ThorGameConfig {

  def getThorGameConfigImpl(): ThorGameConfigImpl = this

  def boundary = gridBoundary.getBoundary

  def facePalstance: Float = adventurerParams.facePalstance

  def getAdventurerRadiusByLevel(l: Int) = {
    adventurerParams.radius(l)
  }


  override def getRadiusByFoodLevel(l: Int): Float = {
    foodParams.radiusList(l)
  }
  override def getEnergyByFoodLevel(l: Int): Int = {
    foodParams.energyList(l)
  }
  override def getMaxEnergyByLevel(l: Int): Int = {
    adventurerParams.maxEnergyList(l)
  }
  override def getWeaponLevelByLevel(l: Int): Int = {
    l
  }
  override def getWeaponLengthByLevel(l: Int): Float = {
    weaponParams.lengthList(l)
  }

  def getThorSpeedByLevel(l: Int, isSpeedUp: Boolean = false) = if (isSpeedUp) {
    adventurerParams.speeds.getThorSpeedByLevel(l) * 1.5.toFloat
  } else adventurerParams.speeds.getThorSpeedByLevel(l)

  def getAdventurerLevelSize: Int = adventurerParams.radius.size


}

