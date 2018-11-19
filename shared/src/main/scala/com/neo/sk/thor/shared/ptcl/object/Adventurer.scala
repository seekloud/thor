package com.neo.sk.thor.shared.ptcl.`object`

import com.neo.sk.thor.shared.ptcl.config.ThorGameConfig
import com.neo.sk.thor.shared.ptcl.model
import com.neo.sk.thor.shared.ptcl.model.{Constants, Point, Rectangle}
import com.neo.sk.thor.shared.ptcl.model.Constants.{AdventurerLevel, Energy, SpeedLevel}
import com.neo.sk.thor.shared.ptcl.util.QuadTree

/**
  * Created by Jingyi on 2018/11/9
  */
case class AdventurerState(
  playerId: String,
  name: String,
  level: Int,
  energy: Int,
  radiusLevel: Int,
  position: Point,
  direction: Float,
  faceDirection: Float,
  weaponLevel: Int,
  weaponLength: Float,
  speedLevel: Int,
  isSpeedUp: Boolean,
  killNum: Int,
  isMove: Boolean
)

trait Adventurer extends CircleObjectOfGame {
  val playerId: String
  val name: String
  var level: Int
  var energy: Int
  var radiusLevel: Int
  //  var position: Point
  var direction: Float
  var faceDirection: Float
  var weaponLevel: Int
  var weaponLength: Float
  var speedLevel: Int
  var isSpeedUp: Boolean
  var killNum: Int
  var isMove: Boolean

  def getMoveState() = isMove

  //判断adventurer是否吃到食物
  def checkEatFood(p: Food, eatFoodCallback: Food => Unit): Unit = {
    if (this.isIntersects(p)) {
      eatFoodCallback(p)
    }
  }

  //判断是否被攻击
  def checkAttacked(p: Adventurer, attackedCallback: Adventurer => Unit): Unit = {
    if (this.position.distance(p.position) < (this.weaponLength + this.weaponLength + p.radius) && scala.math.abs(p.position.getTheta(this.position) - this.direction) < (scala.math.Pi / 3))
      attackedCallback(p)
  }

  def getAdventurerState: AdventurerState = {
    AdventurerState(playerId, name, level, energy, radiusLevel, position, direction, faceDirection, weaponLevel, weaponLength, speedLevel, isSpeedUp, killNum, isMove)
  }

  def eatFood(food: Food)(implicit config: ThorGameConfig): Unit = {
    this.energy += config.getEnergyByFoodLevel(food.level)
    if (energy > config.getMaxEnergyByLevel(this.level)) {
      this.level += 1
      this.weaponLength = config.getWeaponLengthByLevel(this.level)
      this.weaponLevel = config.getWeaponLevelByLevel(this.level)
    }
  }

  def setMoveDirection(d: Float) = {
    direction = d
  }

  def setFaceDirection(target: Float)(implicit config: ThorGameConfig) = {
    if (target > faceDirection) {
      faceDirection += config.facePalstance
    } else (target < faceDirection) {
      faceDirection -= config.facePalstance
    }
  }

  def updateLevel(implicit thorGameConfig: ThorGameConfig) = {
    if (level != AdventurerLevel.levelSix) {
      level += 1
      speedLevel += 1
    }
  }

  def speedUp(implicit thorGameConfig: ThorGameConfig) = {
    if (!isSpeedUp) isSpeedUp = true
    energy -= 5
  }

  def cancleSpeedUp(implicit thorGameConfig: ThorGameConfig) = {
    if (isSpeedUp) isSpeedUp = false
  }

  def move(boundary: Point, quadTree: QuadTree)(implicit thorGameConfig: ThorGameConfig): Unit = {
    if (isMove) {
      val moveDistance = thorGameConfig.getMoveDistanceByFrame(this.speedLevel).rotate(direction)

      val horizontalDistance = moveDistance.copy(y = 0)
      val verticalDistance = moveDistance.copy(x = 0)
      List(horizontalDistance, verticalDistance).foreach { d =>
        if (d.x != 0 || d.y != 0) {
          val originPosition = this.position
          this.position = this.position + d
          val movedRec = Rectangle(this.position - Point(radius, radius), this.position + Point(radius, radius))
          if (movedRec.topLeft > model.Point(0, 0) && movedRec.downRight < boundary) {
            quadTree.updateObject(this)
          } else {
            this.position = originPosition
          }
        }
      }

    }
  }


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
  var faceDirection: Float,
  var weaponLevel: Int,
  var weaponLength: Float,
  var speedLevel: Int,
  var isSpeedUp: Boolean,
  var killNum: Int,
  var isMove: Boolean
) extends Adventurer {
  def this(config: ThorGameConfig, adventurerState: AdventurerState) {
    this(config, adventurerState.playerId, adventurerState.name, adventurerState.level, adventurerState.energy, adventurerState.radiusLevel, adventurerState.position,
      adventurerState.direction, adventurerState.faceDirection, adventurerState.weaponLevel, adventurerState.weaponLength, adventurerState.speedLevel, adventurerState.isSpeedUp,
      adventurerState.killNum, adventurerState.isMove)
  }

  override var radius: Float = config.getAdventurerRadiusByLevel(radiusLevel)


}