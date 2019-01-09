package org.seekloud.thor.shared.ptcl.component

import org.seekloud.thor.shared.ptcl.config.ThorGameConfig
import org.seekloud.thor.shared.ptcl.model
import org.seekloud.thor.shared.ptcl.model.{Constants, Point, Rectangle, Segment}
import org.seekloud.thor.shared.ptcl.util.QuadTree
/**
  * User: TangYaruo
  * Date: 2018/11/25
  * Time: 17:24
  */

case class AdventurerState(
  playerId: String,
  name: String,
  level: Byte,
  energy: Int,
//  radiusLevel: Int,
  position: Point,
  direction: Float,
  faceDirection: Float,
//  weaponLevel: Int,
//  speedLevel: Byte,
  isSpeedUp: Boolean,
  killNum: Int,
  isMove: Boolean,
  isUpdateLevel: Boolean,
  levelUpExecute: Int,
  mouseStop: Boolean,
  isIntersect: Byte
)

trait Adventurer extends CircleObjectOfGame {
  val playerId: String
  val name: String
  var level: Byte
  var energy: Int
//  var radiusLevel: Int
  var position: Point
  var direction: Float
  var faceDirection: Float
//  var weaponLevel: Int
//  var speedLevel: Byte
  var isSpeedUp: Boolean
  var killNum: Int
  var isMove: Boolean
  var isUpdateLevel: Boolean
  var levelUpExecute: Int
  var mouseStop: Boolean
  var isIntersect: Byte

  def getMoveState() = isMove

  //判断adventurer是否吃到食物
  def checkEatFood(p: Food, eatFoodCallback: Food => Unit): Unit = {
    if (this.isIntersects(p)) {
      eatFoodCallback(p)
    }
  }

  //以线段方位判断碰撞,角度为刀的相对角度
  def isSwordShapeDuang(Theta: Double, o: Adventurer)(implicit config: ThorGameConfig): Boolean ={

    //wTheta: 1帧起始刀的绝对角度
    val oTheta = {
      val myDir = this.direction
      if (myDir - Theta > math.Pi) myDir - Theta - 2 * math.Pi
      else if(myDir - Theta < -math.Pi) myDir - Theta + 2 * math.Pi
      else myDir - Theta
    }
    //tTheta: 1帧终止刀的绝对角度
    val tTheta = {
      val myDir = this.direction + math.Pi * 2.0 / 9.0
      if (myDir - Theta > math.Pi) myDir - Theta - 2 * math.Pi
      else if(myDir - Theta < -math.Pi) myDir - Theta + 2 * math.Pi
      else myDir - Theta
    }

    //获取起始刀线的斜率
    val oSwordSlope = math.tan(oTheta)
    //获取起始刀线的一点
    val oSwordPoint = Point((math.cos(oTheta + 0.5 * math.Pi) * config.getAdventurerRadiusByLevel(level)).toFloat + position.x, (math.sin(oTheta + 0.5 * math.Pi) * config.getAdventurerRadiusByLevel(level)).toFloat + position.y)
    //获取起始刀线的另一点
    val oSwordPoint2 = Point((math.cos(oTheta) * config.getWeaponLengthByLevel(level)).toFloat + oSwordPoint.x, (math.sin(oTheta) * config.getWeaponLengthByLevel(level)).toFloat + oSwordPoint.y)
    //获取起始刀线
    val oSwordSegment = new Segment(oSwordSlope, oSwordPoint)
//    val oSwordSegment = new Segment(oSwordPoint, oSwordPoint2)
    //获取终止刀线的斜率
    val tSwordSlope = math.tan(tTheta)
    //获取终止刀线的一点
    val tSwordPoint = Point((math.cos(tTheta + 0.5 * math.Pi) * config.getAdventurerRadiusByLevel(level)).toFloat + position.x, (math.sin(tTheta + 0.5 * math.Pi) * config.getAdventurerRadiusByLevel(level)).toFloat + position.y)
    //获取终止刀线的另一点
    val tSwordPoint2 = Point((math.cos(tTheta) * config.getWeaponLengthByLevel(level)).toFloat + tSwordPoint.x, (math.sin(tTheta) * config.getWeaponLengthByLevel(level)).toFloat + tSwordPoint.y)
    //获取终止刀线
    val tSwordSegment = new Segment(tSwordSlope, tSwordPoint)
//    val tSwordSegment = new Segment(tSwordPoint, tSwordPoint2)

    //判断对方是否在范围内
    val pointInShape = (math.abs(oTheta), math.abs(tTheta)) match{
      case (a, b) if a < 0.5 * math.Pi && b < 0.5 * math.Pi =>
        oSwordSegment.directionFromPoint(o.position) && !tSwordSegment.directionFromPoint(o.position)
      case (a, b) if a < 0.5 * math.Pi && b > 0.5 * math.Pi =>
        oSwordSegment.directionFromPoint(o.position) && tSwordSegment.directionFromPoint(o.position)
      case (a, b) if a > 0.5 * math.Pi && b > 0.5 * math.Pi =>
        !oSwordSegment.directionFromPoint(o.position) && tSwordSegment.directionFromPoint(o.position)
      case (a, b) if a > 0.5 * math.Pi && b < 0.5 * math.Pi =>
        !oSwordSegment.directionFromPoint(o.position) && !tSwordSegment.directionFromPoint(o.position)
      case _ => true
    }

    //判断对方是否在边界
    if(pointInShape){
      true
    }
    else{
      val distance = oSwordSegment.distanceFromPoint(o.position) < o.radius || tSwordSegment.distanceFromPoint(o.position) < o.radius
      val oSwordDirection = (o.position.x - oSwordPoint.x) * (oSwordPoint2.x - oSwordPoint.x) + (o.position.y - oSwordPoint.y) * (oSwordPoint2.y - oSwordPoint.y) > 0
      val tSwordDirection = (o.position.x - tSwordPoint.x) * (tSwordPoint2.x - tSwordPoint.x) + (o.position.y - tSwordPoint.y) * (tSwordPoint2.y - tSwordPoint.y) > 0
      distance && (oSwordDirection || tSwordDirection)
    }
  }

  //判断扇形区域碰撞,角度为刀的相对角度
  def isSectorDuang(Theta: Double, o: Adventurer)(implicit config: ThorGameConfig) = {

    //wTheta: 刀的绝对角度
    val wTheta = {
      val myDir = this.direction
      if (myDir - Theta > math.Pi) myDir - Theta - 2 * math.Pi
      else if(myDir - Theta < -math.Pi) myDir - Theta + 2 * math.Pi
      else myDir - Theta
    }
    //OTheta: 对方(人)和刀的夹角
    val oTheta = {
      val otherDir = o.position.getTheta(this.position)
      if (otherDir - wTheta > math.Pi) otherDir - wTheta - 2 * math.Pi
      else if(otherDir - wTheta < -math.Pi) otherDir - wTheta + 2 * math.Pi
      else otherDir - wTheta
    }
//    println(s"相对角$oTheta,距离${this.position.distance(o.position)},(自己，刀，对方):(${this.radius}, ${config.getWeaponLengthByLevel(level)}, ${o.radius})")
//    println(s"相对角$oTheta,距离${this.position.distance(o.position)},(自己，刀，对方):(${config.getAdventurerRadiusByLevel(level)}, ${config.getWeaponLengthByLevel(level)}, ${config.getAdventurerRadiusByLevel(o.getAdventurerState.level)})")
//    this.position.distance(o.position) < (this.radius + config.getWeaponLengthByLevel(level) + o.radius) && math.abs(oTheta) < scala.math.Pi * (1.0 / 9.0)
    this.position.distance(o.position) < (config.getAdventurerRadiusByLevel(level) + config.getWeaponLengthByLevel(level) + config.getAdventurerRadiusByLevel(o.getAdventurerState.level)) && math.abs(oTheta) < scala.math.Pi * (1.0 / 9.0)
  }

  //判断是否攻击到其他
  def checkAttacked(p: Adventurer, attackingStep: Int, attackedCallback: Adventurer => Unit, attackedMaybeCallback: (Adventurer, Int) => Unit)(implicit config: ThorGameConfig): Unit = {

    if (isSwordShapeDuang(math.Pi * 2.0 / 9.0 * attackingStep + 0.33 * math.Pi, p)){
      println(s"${p.playerId} is attacked")
      attackedCallback(p)
    }
    else {
      for(a <- 0 to 3) {
        if (isSwordShapeDuang(math.Pi * 2.0 / 9.0 * a + 0.33 * math.Pi, p)){
          attackedMaybeCallback(p, a)
        }
      }
    }
  }

  def getAdventurerState: AdventurerState = {
    AdventurerState(playerId, name, level, energy, position, direction, faceDirection, isSpeedUp, killNum, isMove, isUpdateLevel, levelUpExecute, mouseStop, isIntersect)
  }

  def attacking(killedLevel: Byte)(implicit config: ThorGameConfig): Unit ={
//    println(s"killing $killedLevel Level adventurer")
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

  def setMoveDirection(d: Float, mouseDistance: Float, isAttacking: Boolean)(implicit config: ThorGameConfig) = {
    if (mouseDistance > config.getAdventurerRadiusByLevel(this.level) * 10)
    {
      if (!isAttacking) {
        isMove = true
        direction = d
        mouseStop = false
      }

    }
    else
     {
       isMove = false
       mouseStop = true
     }

  }

  def setFaceDirection(target: Float)(implicit config: ThorGameConfig) = {
    if (target > faceDirection) {
      faceDirection += config.facePalstance
    } else {
      faceDirection -= config.facePalstance
    }
  }

  def updateLevel(implicit thorGameConfig: ThorGameConfig) = {
    if (levelUpExecute > thorGameConfig.getAdventurerLevelUpAnimation) {
      if (level < thorGameConfig.getAdventurerLevelSize) {
        levelUpExecute = thorGameConfig.getAdventurerLevelUpAnimation
        isUpdateLevel = true
        level = (level + 1).toByte
//        speedLevel += 1
//        weaponLevel += 1
      }
    } else {
      if (levelUpExecute > 0)
        levelUpExecute -= 1
      else {
        isUpdateLevel = false
        levelUpExecute = thorGameConfig.getAdventurerLevelUpAnimation + 100
      }
    }
  }

  def reduceLevel(implicit thorGameConfig: ThorGameConfig): Unit = {
    if (this.level > 1) {
      if (this.energy <= thorGameConfig.getMaxEnergyByLevel((this.level - 1).toByte)) {
        this.level = (level - 1).toByte
//        speedLevel -= 1
//        weaponLevel -= 1
      }
    }
  }

  def speedUp(implicit thorGameConfig: ThorGameConfig) = {
    if (!isSpeedUp && energy > thorGameConfig.speedUpEnergyLoose) isSpeedUp = true
  }

  def cancleSpeedUp(implicit thorGameConfig: ThorGameConfig) = {
    if (isSpeedUp) isSpeedUp = false
  }

  def move(boundary: Point, quadTree: QuadTree)(implicit thorGameConfig: ThorGameConfig): Unit = {
    if (isMove && isIntersect == 0) {
      val oldOb = this
      val moveDistance = if (isSpeedUp) {
        if (energy >= thorGameConfig.speedUpEnergyLoose) {
          energy -= thorGameConfig.speedUpEnergyLoose
          reduceLevel
        } else {
          cancleSpeedUp
        }
        thorGameConfig.getMoveDistanceByFrame(this.level, isSpeedUp).rotate(direction)
      } else {
        thorGameConfig.getMoveDistanceByFrame(this.level, isSpeedUp).rotate(direction)
      }

      val horizontalDistance = moveDistance.copy(y = 0)
      val verticalDistance = moveDistance.copy(x = 0)
      val radius = thorGameConfig.getAdventurerRadiusByLevel(this.level)
      List(horizontalDistance, verticalDistance).foreach { d =>
        if (d.x != 0 || d.y != 0) {
          val originPosition = this.position
          this.position = this.position + d
          val movedRec = Rectangle(this.position - Point(radius, radius), this.position + Point(radius, radius))
          if (movedRec.topLeft > model.Point(0, 0) && movedRec.downRight < boundary) {
            quadTree.updateObject(oldOb, this)
          }
          if (movedRec.topLeft.x <= 0 || movedRec.topLeft.y <= 0) {
            if (movedRec.topLeft.x <= 0) this.position = Point(radius, this.position.y)
            if (movedRec.topLeft.y <= 0) this.position = Point(this.position.x, radius)
            quadTree.updateObject(oldOb, this)
          }
          if (movedRec.downRight.x >= boundary.x || movedRec.downRight.y >= boundary.y) {
            if (movedRec.downRight.x >= boundary.x) this.position = Point(boundary.x - radius, this.position.y)
            if (movedRec.downRight.y >= boundary.y) this.position = Point(this.position.x, boundary.y - radius)
            quadTree.updateObject(oldOb, this)
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
  var level: Byte,
  var energy: Int,
//  var radiusLevel: Int,
  var position: Point,
  var direction: Float,
  var faceDirection: Float,
//  var weaponLevel: Int,
//  var speedLevel: Int,
  var isSpeedUp: Boolean,
  var killNum: Int,
  var isMove: Boolean,
  var isUpdateLevel: Boolean,
  var levelUpExecute: Int,
  var mouseStop: Boolean,
  var isIntersect: Byte
) extends Adventurer {
  def this(config: ThorGameConfig, adventurerState: AdventurerState) {
    this(config, adventurerState.playerId, adventurerState.name, adventurerState.level, adventurerState.energy, adventurerState.position,
      adventurerState.direction, adventurerState.faceDirection, adventurerState.isSpeedUp, adventurerState.killNum, adventurerState.isMove,
      adventurerState.isUpdateLevel, adventurerState.levelUpExecute, adventurerState.mouseStop, adventurerState.isIntersect)
  }

  override var radius: Float = config.getAdventurerRadiusByLevel(level)

}
