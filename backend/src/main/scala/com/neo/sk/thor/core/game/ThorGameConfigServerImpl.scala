package com.neo.sk.thor.core.game

import akka.util.Helpers
import com.neo.sk.thor.shared.ptcl.config._
import com.neo.sk.thor.shared.ptcl.model.Point
import com.typesafe.config.Config

/**
  * User: TangYaruo
  * Date: 2018/11/15
  * Time: 16:38
  */
case class ThorGameConfigServerImpl(config: Config) extends ThorGameConfig {
  import collection.JavaConverters._
  import Helpers.Requiring

  private[this] val gridBoundaryWidth = config.getInt("thorGame.gridBoundary.width")
    .requiring(_ > 100,"minimum supported grid boundary width is 100")
  private[this] val gridBoundaryHeight = config.getInt("thorGame.gridBoundary.height")
    .requiring(_ > 50,"minimum supported grid boundary height is 50")
  private[this] val gridBoundary = GridBoundary(gridBoundaryWidth,gridBoundaryHeight)

  private[this] val gameFameDuration = config.getLong("thorGame.frameDuration")
    .requiring(t => t >= 1l,"minimum game frame duration is 1 ms")

  private[this] val adventurerRadiusLevel = config.getDoubleList("thorGame.adventurer.radius")
    .requiring(_.size() >= 1,"minimum supported adventurer radius size is 1").asScala.map(_.toFloat).toList

  private[this] val adventurerSpeedLevel = config.getDoubleList("thorGame.adventurer.speed")
    .requiring(_.size() >= 1,"minimum supported adventurer speed size is 1").asScala.map(_.toFloat).toList

  private[this] val adventurerMaxEnergyLevel = config.getIntList("thorGame.adventurer.maxEnergy")
    .requiring(_.size() >= 1,"minimum supported adventurer max energy size is 1").asScala.map(_.toInt).toList


  private[this] val adventurerContainEnergyLevel = config.getIntList("thorGame.adventurer.containEnergy")
    .requiring(_.size() >= 1,"minimum supported adventurer max energy size is 1").asScala.map(_.toInt).toList

  private[this] val weaponLengthLevel = config.getDoubleList("thorGame.weapon.length")
    .requiring(_.size() >= 1,"minimum supported weapon length size is 1").asScala.map(_.toFloat).toList

  private[this] val foodEnergyLevel = config.getIntList("thorGame.food.energy")
    .requiring(_.size() >= 1,"minimum supported food energy size is 1").asScala.map(_.toInt).toList

  private[this] val foodRadiusLevel = config.getDoubleList("thorGame.food.radius")
    .requiring(_.size() >= 1,"minimum supported food energy size is 1").asScala.map(_.toFloat).toList



//  private[this] val adventurerRadiusData = config.getDouble("thorGame.adventurer.adventurerRadius")
//    .requiring(_ > 0, "minimum supported adventurer radius is 1").toFloat

  private[this] val adventurerParams = AdventurerParams(AdventurerMoveSpeed(adventurerSpeedLevel), adventurerRadiusLevel, adventurerMaxEnergyLevel, adventurerContainEnergyLevel)

  private[this] val foodParams = FoodParams(foodEnergyLevel, foodRadiusLevel)

  private[this] val weaponParams = WeaponParams(weaponLengthLevel)

  private val thorGameConfig = ThorGameConfigImpl(gridBoundary, gameFameDuration, adventurerParams, foodParams, weaponParams)

  def getThorGameConfigImpl: ThorGameConfigImpl = thorGameConfig

  def boundary:Point = thorGameConfig.boundary

  def frameDuration:Long = thorGameConfig.frameDuration

  def getAdventurerRadiusByLevel(adventurerLevel: Int): Float = thorGameConfig.getAdventurerRadiusByLevel(adventurerLevel)

  override def getRadiusByFoodLevel(l: Int): Float = thorGameConfig.getRadiusByFoodLevel(l)
  def getEnergyByFoodLevel(l: Int) = thorGameConfig.getEnergyByFoodLevel(l)
  def getMaxEnergyByLevel(l: Int) = thorGameConfig.getMaxEnergyByLevel(l)
  def getWeaponLevelByLevel(l: Int) = thorGameConfig.getWeaponLevelByLevel(l)
  def getWeaponLengthByLevel(l: Int) = thorGameConfig.getWeaponLengthByLevel(l)

  override def getThorSpeedByLevel(l: Int): Point = thorGameConfig.getThorSpeedByLevel(l)




}
