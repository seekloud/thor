package com.neo.sk.thor.core.game

import akka.util.Helpers
import com.neo.sk.thor.shared.ptcl.config.{AdventurerParams, GridBoundary, ThorGameConfig, ThorGameConfigImpl}
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

  private[this] val adventurerRadiusData = config.getDouble("thorGame.adventurer.adventurerRadius")
    .requiring(_ > 0, "minimum supported adventurer radius is 1").toFloat

  private[this] val adventurerParams = AdventurerParams(adventurerRadiusData)

  private val thorGameConfig = ThorGameConfigImpl(gridBoundary, gameFameDuration, adventurerParams)

  def getThorGameConfig: ThorGameConfigImpl = thorGameConfig

  def boundary:Point = thorGameConfig.boundary

  def frameDuration:Long = thorGameConfig.frameDuration

  def adventurerRadius:Float = thorGameConfig.adventurerRadius

  def getEnergyByFoodLevel(foodLevel: Int) = thorGameConfig.getEnergyByFoodLevel(foodLevel)
  def getMaxEnergyByLevel(adventurerLevel: Int) = thorGameConfig.getMaxEnergyByLevel(adventurerLevel)
  def getWeaponLevelByLevel(adventurerLevel: Int) = thorGameConfig.getWeaponLevelByLevel(adventurerLevel)
  def getWeaponLengthByLevel(adventurerLevel: Int) = thorGameConfig.getWeaponLengthByLevel(adventurerLevel)




}
