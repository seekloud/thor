package org.seekloud.thor.core.game

import akka.util.Helpers
import org.seekloud.thor.shared.ptcl.config._
import org.seekloud.thor.shared.ptcl.model.Point
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

  private[this] val playRate = config.getInt("thorGame.playRate")
    .requiring(t => t >= 1,"minimum game play rate is 1 ms")

  private[this] val replayRate = config.getInt("thorGame.replayRate")
    .requiring(t => t >= 1,"minimum game replay rate is 1 ms")

  private[this] val adventurerRadiusLevel = config.getDoubleList("thorGame.adventurer.radius")
    .requiring(_.size() >= 1,"minimum supported adventurer radius size is 1").asScala.map(_.toFloat).toList

  private[this] val adventurerSpeedLevel = config.getDoubleList("thorGame.adventurer.speed")
    .requiring(_.size() >= 1,"minimum supported adventurer speed size is 1").asScala.map(_.toFloat).toList

  private[this] val adventurerMaxEnergyLevel = config.getIntList("thorGame.adventurer.maxEnergy")
    .requiring(_.size() >= 1,"minimum supported adventurer max energy size is 1").asScala.map(_.toInt).toList


  private[this] val adventurerContainEnergyLevel = config.getIntList("thorGame.adventurer.containEnergy")
    .requiring(_.size() >= 1,"minimum supported adventurer max energy size is 1").asScala.map(_.toInt).toList

  private[this] val adventurerFacePalstance = config.getDouble("thorGame.adventurer.facePalstance")
    .requiring(t => t >= 1,"minimum supported adventurer face palstance size is 1").toFloat

  private[this] val adventurerSpeedUpRate = config.getDouble("thorGame.adventurer.speedUpRate")
    .requiring(t => t >= 1,"minimum supported adventurer speedUpRate size is 1").toFloat

  private[this] val adventurerSpeedUpEnergyLoose = config.getInt("thorGame.adventurer.speedUpEnergyLoose")
    .requiring(t => t > 1,"minimum supported adventurer speed up energy loss  is 1")

  private[this] val adventurerDyingAnimation = config.getInt("thorGame.adventurer.dyingAnimation")
    .requiring(t => t > 1,"minimum supported adventurer dying animation  is 1")

  private[this] val adventurerSpeedUpAnimation = config.getInt("thorGame.adventurer.speedUpAnimation")
    .requiring(t => t > 1,"minimum supported adventurer speed up animation  is 1")

  private[this] val adventurerLevelUpAnimation = config.getInt("thorGame.adventurer.levelUpAnimation")
    .requiring(t => t > 1,"minimum supported adventurer level up animation  is 1")


  private[this] val weaponLengthLevel = config.getDoubleList("thorGame.weapon.length")
    .requiring(_.size() >= 1,"minimum supported weapon length size is 1").asScala.map(_.toFloat).toList

  private[this] val foodMax = config.getInt("thorGame.food.max")
    .requiring(t => t >= 30,"minimum supported food max size is 30")

  private[this] val foodEnergyLevel = config.getIntList("thorGame.food.energy")
    .requiring(_.size() >= 1,"minimum supported food energy size is 1").asScala.map(_.toInt).toList

  private[this] val foodRadiusLevel = config.getDoubleList("thorGame.food.radius")
    .requiring(_.size() >= 1,"minimum supported food energy size is 1").asScala.map(_.toFloat).toList





//  private[this] val adventurerRadiusData = config.getDouble("thorGame.adventurer.adventurerRadius")
//    .requiring(_ > 0, "minimum supported adventurer radius is 1").toFloat

  private[this] val adventurerParams = AdventurerParams(AdventurerMoveSpeed(adventurerSpeedLevel), adventurerRadiusLevel, adventurerMaxEnergyLevel, adventurerContainEnergyLevel,
    adventurerFacePalstance, adventurerSpeedUpRate, adventurerSpeedUpEnergyLoose, adventurerDyingAnimation, adventurerSpeedUpAnimation, adventurerLevelUpAnimation)

  private[this] val foodParams = FoodParams(foodMax, foodEnergyLevel, foodRadiusLevel)

  private[this] val weaponParams = WeaponParams(weaponLengthLevel)

  private val thorGameConfig = ThorGameConfigImpl(gridBoundary, gameFameDuration, replayRate, adventurerParams, foodParams, weaponParams)

  def getThorGameConfigImpl: ThorGameConfigImpl = thorGameConfig

  def boundary:Point = thorGameConfig.boundary

  def frameDuration:Long = thorGameConfig.frameDuration

  def facePalstance: Float = thorGameConfig.facePalstance

  def getAdventurerRadiusByLevel(adventurerLevel: Int): Float = thorGameConfig.getAdventurerRadiusByLevel(adventurerLevel)

  override def getRadiusByFoodLevel(l: Int): Float = thorGameConfig.getRadiusByFoodLevel(l)
  def getEnergyByFoodLevel(l: Int) = thorGameConfig.getEnergyByFoodLevel(l)
  def getEnergyByKillingAdventurerLevel(l: Int): Int = thorGameConfig.getEnergyByKillingAdventurerLevel(l)
  def getMaxEnergyByLevel(l: Int) = thorGameConfig.getMaxEnergyByLevel(l)
  def getWeaponLevelByLevel(l: Int) = thorGameConfig.getWeaponLevelByLevel(l)
  def getWeaponLengthByLevel(l: Int) = thorGameConfig.getWeaponLengthByLevel(l)

  override def getThorSpeedByLevel(l: Int, isSpeedUp: Boolean = false): Point = thorGameConfig.getThorSpeedByLevel(l, isSpeedUp)

  def getAdventurerLevelSize: Int = thorGameConfig.getAdventurerLevelSize

  override def speedUpEnergyLoose: Int = thorGameConfig.adventurerParams.speedUpEnergyLoose

  override def getFoodMax(): Int = thorGameConfig.getFoodMax()

  def getAdventurerDyingAnimation: Int = thorGameConfig.getAdventurerDyingAnimation

  override def getAdventurerLevelUpAnimation: Int = thorGameConfig.getAdventurerLevelUpAnimation




}
