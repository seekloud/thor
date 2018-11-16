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
  radius: Float
)


trait ThorGameConfig {

  def frameDuration:Long

  def boundary: Point

  def adventurerRadius:Float

  def getEnergyByFoodLevel(foodLevel: Int): Int
  def getMaxEnergyByLevel(adventurerLevel: Int): Int
  def getWeaponLevelByLevel(adventurerLevel: Int): Int
  def getWeaponLengthByLevel(adventurerLevel: Int): Int

}


case class ThorGameConfigImpl (
  gridBoundary: GridBoundary,
  frameDuration:Long,
  adventurerParams: AdventurerParams
) extends ThorGameConfig {

  def getThorGameConfigImpl(): ThorGameConfigImpl = this

  def boundary = gridBoundary.getBoundary

  def adventurerRadius = adventurerParams.radius


  //TODO 详写
  def getEnergyByFoodLevel(foodLevel: Int) = {
    0
  }
  def getMaxEnergyByLevel(adventurerLevel: Int) = {
    0
  }
  def getWeaponLevelByLevel(adventurerLevel: Int)= {
    0
  }
  def getWeaponLengthByLevel(adventurerLevel: Int) = {
    0
  }



}

