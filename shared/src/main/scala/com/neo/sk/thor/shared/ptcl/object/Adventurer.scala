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
  radius: Float,
  position: Point,
  direction: Float,
  weaponLevel: Int,
  weaponLength: Float,
  speed: Float,
  isSpeedUp: Boolean,
  killNum: Int,
)

trait Adventurer extends CircleObjectOfGame {
  val playerId: String
  val name: String
  var level: Int
  var energy: Int
  var radiusLevel: Int
//  var position: Point
  var direction: Float
  var weaponLevel: Int
  var weaponLength: Float
  var speed: Float
  var isSpeedUp: Boolean
  var killNum: Int

  //判断adventurer是否吃到食物
  def checkEatFood(p:Food,eatFoodCallback:Food => Unit):Unit = {
    if(this.isIntersects(p)){
      eatFoodCallback(p)
    }
  }

  //判断是否被攻击
  def checkAttacked(p:Adventurer,attackedCallback: Adventurer => Unit): Unit ={
    if(this.position.distance(p.position) < (this.weaponLength + this.weaponLength + p.radius) && scala.math.abs(p.position.getTheta(this.position) - this.direction) < (scala.math.Pi / 3))
      attackedCallback(p)
  }

  def getAdventurerState(implicit config: ThorGameConfig): AdventurerState = {
    AdventurerState(playerId, name, level, energy, config.getAdventurerRadiusByLevel(radiusLevel), position, direction, weaponLevel, weaponLength, speed, isSpeedUp, killNum)
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

  def speedUp() = {
    if (!isSpeedUp) isSpeedUp = true
    speed *= SpeedLevel.speedUpRatio
    energy -= Energy.speedUpStep
  }

  def cancleSpeedUp() = {
    if (isSpeedUp) isSpeedUp = false
    speed = speed / SpeedLevel.speedUpRatio
  }


  //TODO 位置移动 击杀


}

case class AdventurerImpl(
  config: ThorGameConfig,
  playerId: String,
  name: String,
  var level: Int,
  var energy: Int,
  var radiusLevel: Int,
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

  override var radius: Float = config.getAdventurerRadiusByLevel(radiusLevel)






}