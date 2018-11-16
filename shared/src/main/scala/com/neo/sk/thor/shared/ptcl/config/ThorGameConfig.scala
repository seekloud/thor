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

final case class AdventurerParams(
  radius: Float,
  maxEnergyList: List[Int]
)

final case class FoodParams(
  energyList: List[Int],
  radiusList: List[Float]
)
final case class WeaponParams(
  lengthList: List[Int]
)


trait ThorGameConfig {

  def frameDuration:Long

  def boundary: Point

  def adventurerRadius:Float

  def getRadiusByFoodLevel(foodLevel: Int): Float
  def getEnergyByFoodLevel(foodLevel: Int): Int
  def getMaxEnergyByLevel(adventurerLevel: Int): Int
  def getWeaponLevelByLevel(adventurerLevel: Int): Int
  def getWeaponLengthByLevel(adventurerLevel: Int): Int

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

  def adventurerRadius = adventurerParams.radius


  override def getRadiusByFoodLevel(foodLevel: Int): Float = {
    foodParams.radiusList(foodLevel)
  }
  override def getEnergyByFoodLevel(foodLevel: Int): Int = {
    foodParams.energyList(foodLevel)
  }
  override def getMaxEnergyByLevel(adventurerLevel: Int): Int = {
    adventurerParams.maxEnergyList(adventurerLevel)
  }
  override def getWeaponLevelByLevel(adventurerLevel: Int): Int = {
    adventurerLevel
  }
  override def getWeaponLengthByLevel(adventurerLevel: Int): Int = {
    weaponParams.lengthList(adventurerLevel)
  }



}

