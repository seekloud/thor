package com.neo.sk.thor.shared.ptcl.`object`

import com.neo.sk.thor.shared.ptcl.config.ThorGameConfig
import com.neo.sk.thor.shared.ptcl.model.Point
import com.neo.sk.thor.shared.ptcl.model.Constants
import com.neo.sk.thor.shared.ptcl.model.Constants.{AdventurerLevel, Energy, SpeedLevel}

/**
  * Created by Jingyi on 2018/11/9
  */
case class AdventurerState(
  playerId: String,
  name: String,
  level: Int,
  energy: Int,
//  radius: Int,
  position: Point,
  direction: Float,
  weaponLevel: Int,
  weaponLength: Int,
  speed: Float,
  isSpeedUp: Boolean,
  killNum: Int,
)

trait Adventurer extends CircleObjectOfGame {
  val playerId: String
  val name: String
  var level: Int
  var energy: Int
//  var radius: Int
  var position: Point
  var direction: Float
  var weaponLevel: Int
  var weaponLength: Int
  var speed: Float
  var isSpeedUp: Boolean
  var killNum: Int

  //判断adventurer是否吃到食物
  def checkEatFood(p:Food,eatFoodCallback:Food => Unit):Unit = {
    if(this.isIntersects(p)){
      eatFoodCallback(p)
    }
  }

  def getAdventurerState: AdventurerState = {
    AdventurerState(playerId, name, level, energy, position, direction, weaponLevel, weaponLength, speed, isSpeedUp, killNum)
  }

  def eatFood(food: Food)(implicit config: ThorGameConfig): Unit = {
    this.energy += config.getEnergyByFoodLevel(food.level)
    if(energy > config.getMaxEnergyByLevel(this.level)){
      this.level += 1
      this.weaponLength = config.getWeaponLengthByLevel(this.level)
      this.weaponLevel = config.getWeaponLevelByLevel(this.level)
    }
  }

  def setAdventurerDirection(d: Float) = {
    direction = d
  }

  def updateLevel() = {
    if (level != AdventurerLevel.levelFive) {
      level += AdventurerLevel.step
      speed -= SpeedLevel.step
    }
  }

  def  speedUp() = {
    if (!isSpeedUp) isSpeedUp = true
    speed *= SpeedLevel.speedUpRatio
    energy -= Energy.speedUpStep
  }

  def cancleSpeedUp() = {
    if (isSpeedUp) isSpeedUp = false
    speed = speed / SpeedLevel.speedUpRatio
  }





}

case class AdventurerImpl(
  config: ThorGameConfig,
  playerId: String,
  name: String,
  var level: Int,
  var energy: Int,
//  radius: Int,
  var position: Point,
  var direction: Float,
  var weaponLevel: Int,
  var weaponLength: Int,
  var speed: Float,
  var isSpeedUp: Boolean,
  var killNum: Int
) extends Adventurer {
  def this(config: ThorGameConfig, adventurerState: AdventurerState) {
    this(config, adventurerState.playerId, adventurerState.name, adventurerState.level, adventurerState.energy, adventurerState.position,
      adventurerState.direction, adventurerState.weaponLevel, adventurerState.weaponLength, adventurerState.speed, adventurerState.isSpeedUp,
      adventurerState.killNum)
  }

  override val radius: Float = config.thorRadius






}