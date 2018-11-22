package org.seekloud.thor.shared.ptcl.component

import org.seekloud.thor.shared.ptcl.config.ThorGameConfig
import org.seekloud.thor.shared.ptcl.model
import org.seekloud.thor.shared.ptcl.model.{Constants, Point, Rectangle}
import org.seekloud.thor.shared.ptcl.util.QuadTree

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
  var speedLevel: Int
  var isSpeedUp: Boolean
  var killNum: Int
  var isMove: Boolean

  val maxLevel = 20
  def getMoveState() = isMove

  //判断adventurer是否吃到食物
  def checkEatFood(p: Food, eatFoodCallback: Food => Unit): Unit = {
    if (this.isIntersects(p)) {
      eatFoodCallback(p)
    }
  }

  //判断扇形区域碰撞,角度为刀的角度
  def isSectorDuang(Theta: Double, o: Adventurer)(implicit config: ThorGameConfig) = {
    this.position.distance(o.position) < (this.radius + config.getWeaponLengthByLevel(weaponLevel) + o.radius) && scala.math.abs(o.position.getTheta(this.position) - this.direction + Theta) < scala.math.Pi * (2.0 / 9.0)
  }

  //判断是否攻击到其他
  def checkAttacked(p: Adventurer, attackingStep: Int, attackedCallback: Adventurer => Unit)(implicit config: ThorGameConfig): Unit = {
//    println(s"attacking: ${p.playerId},$attackingStep")
    if (isSectorDuang(scala.math.Pi * 2.0 / 9.0 * attackingStep - 0 , p)){
      println(s"${p.playerId} is attacked")
      attackedCallback(p)
    }
  }

  def getAdventurerState: AdventurerState = {
    AdventurerState(playerId, name, level, energy, radiusLevel, position, direction, faceDirection, weaponLevel, speedLevel, isSpeedUp, killNum, isMove)
  }

  def attacking(killedLevel: Int)(implicit config: ThorGameConfig): Unit ={
    println(s"killing $killedLevel Level adventurer")
    this.energy += config.getEnergyByKillingAdventurerLevel(killedLevel)
    if (energy > config.getMaxEnergyByLevel(this.level)) {
      updateLevel
    }
  }

  def eatFood(food: Food)(implicit config: ThorGameConfig): Unit = {
    this.energy += config.getEnergyByFoodLevel(food.level)
    if (energy > config.getMaxEnergyByLevel(this.level)) {
      updateLevel
    }
  }

  def setMoveDirection(d: Float, mouseDistance: Float)(implicit config: ThorGameConfig) = {
    if (mouseDistance > config.getAdventurerRadiusByLevel(this.level) * 10)
      {
        isMove = true
        direction = d
      }
    else
      isMove = false

  }

  def setFaceDirection(target: Float)(implicit config: ThorGameConfig) = {
    if (target > faceDirection) {
      faceDirection += config.facePalstance
    } else {
      faceDirection -= config.facePalstance
    }
  }

  def updateLevel(implicit thorGameConfig: ThorGameConfig) = {
    if (level < thorGameConfig.getAdventurerLevelSize) {
      level += 1
      speedLevel += 1
      weaponLevel += 1
    }
  }

  def speedUp(implicit thorGameConfig: ThorGameConfig) = {
    if (!isSpeedUp && energy > thorGameConfig.speedUpEnergyLoose) isSpeedUp = true
  }

  def cancleSpeedUp(implicit thorGameConfig: ThorGameConfig) = {
    if (isSpeedUp) isSpeedUp = false
  }

  def move(boundary: Point, quadTree: QuadTree)(implicit thorGameConfig: ThorGameConfig): Unit = {
    if (isMove) {
      val moveDistance = if (isSpeedUp) {
        if (energy >= thorGameConfig.speedUpEnergyLoose) {
          energy -= thorGameConfig.speedUpEnergyLoose
        } else {
          cancleSpeedUp
        }
        thorGameConfig.getMoveDistanceByFrame(this.speedLevel, isSpeedUp).rotate(direction)
      } else {
        thorGameConfig.getMoveDistanceByFrame(this.speedLevel, isSpeedUp).rotate(direction)
      }

      val horizontalDistance = moveDistance.copy(y = 0)
      val verticalDistance = moveDistance.copy(x = 0)
      List(horizontalDistance, verticalDistance).foreach { d =>
        if (d.x != 0 || d.y != 0) {
          val originPosition = this.position
          this.position = this.position + d
          val movedRec = Rectangle(this.position - Point(radius, radius), this.position + Point(radius, radius))
          if (movedRec.topLeft > model.Point(0, 0) && movedRec.downRight < boundary) {
            quadTree.updateObject(this)
          }
          if (movedRec.topLeft.x <= 0 || movedRec.topLeft.y <= 0) {
            if (movedRec.topLeft.x <= 0) this.position = Point(radius, this.position.y)
            if (movedRec.topLeft.y <= 0) this.position = Point(this.position.x, radius)
            quadTree.updateObject(this)
          }
          if (movedRec.downRight.x >= boundary.x || movedRec.downRight.y >= boundary.y) {
            if (movedRec.downRight.x >= boundary.x) this.position = Point(boundary.x - radius, this.position.y)
            if (movedRec.downRight.y >= boundary.y) this.position = Point(this.position.x, boundary.y - radius)
            quadTree.updateObject(this)
          }
        }
      }
    }
  }


  def moveDistance(boundary: Point, quadTree: QuadTree)(implicit thorGameConfig: ThorGameConfig): Option[Point] = {
    if (isMove) {
      var moveDistance = if (isSpeedUp) {
        thorGameConfig.getMoveDistanceByFrame(this.speedLevel, isSpeedUp).rotate(direction)
      } else {
        thorGameConfig.getMoveDistanceByFrame(this.speedLevel, isSpeedUp).rotate(direction)
      }

      val horizontalDistance = moveDistance.copy(y = 0)
      val verticalDistance = moveDistance.copy(x = 0)
      List(horizontalDistance, verticalDistance).foreach { d =>
        if (d.x != 0 || d.y != 0) {
          val originPosition = this.position
          this.position = this.position + d
          val movedRec = Rectangle(this.position - Point(radius, radius), this.position + Point(radius, radius))
          if (movedRec.topLeft > model.Point(0, 0) && movedRec.downRight < boundary) {
            quadTree.updateObject(this)
          }
          if (movedRec.topLeft.x <= 0 || movedRec.topLeft.y <= 0) {
            if (movedRec.topLeft.x <= 0) this.position = Point(radius, this.position.y)
            moveDistance = moveDistance.copy(x = radius - originPosition.x)
            if (movedRec.topLeft.y <= 0) this.position = Point(this.position.x, radius)
            moveDistance = moveDistance.copy(y = radius - originPosition.y)
            quadTree.updateObject(this)
          }
          if (movedRec.downRight.x >= boundary.x || movedRec.downRight.y >= boundary.y) {
            if (movedRec.downRight.x >= boundary.x) this.position = Point(boundary.x - radius, this.position.y)
            moveDistance = moveDistance.copy(x = boundary.x - radius - originPosition.x)
            if (movedRec.downRight.y >= boundary.y) this.position = Point(this.position.x, boundary.y - radius)
            moveDistance = moveDistance.copy(y = boundary.y - radius - originPosition.y)
            quadTree.updateObject(this)
          }
          this.position = originPosition
        }
      }
      Some(moveDistance)
    } else None
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
  var speedLevel: Int,
  var isSpeedUp: Boolean,
  var killNum: Int,
  var isMove: Boolean
) extends Adventurer {
  def this(config: ThorGameConfig, adventurerState: AdventurerState) {
    this(config, adventurerState.playerId, adventurerState.name, adventurerState.level, adventurerState.energy, adventurerState.radiusLevel, adventurerState.position,
      adventurerState.direction, adventurerState.faceDirection, adventurerState.weaponLevel, adventurerState.speedLevel, adventurerState.isSpeedUp,
      adventurerState.killNum, adventurerState.isMove)
  }

  override var radius: Float = config.getAdventurerRadiusByLevel(radiusLevel)

//  def getPosition4Animation(boundary: Point, quadTree: QuadTree, offsetTime: Long): Point = {
//
//  }


}