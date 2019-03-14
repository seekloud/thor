/*
 * Copyright 2018 seekloud (https://github.com/seekloud)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.seekloud.thor.shared.ptcl.config

import org.seekloud.thor.shared.ptcl.model.Point

/**
  * User: TangYaruo
  * Date: 2018/11/12
  * Time: 16:13
  */

final case class GridBoundary(width: Int, height: Int) {
  def getBoundary: Point = Point(width, height)
}

final case class AdventurerMoveSpeed(speeds: List[Float]) {
  def getThorSpeedByLevel(l: Int) = Point(speeds(l - 1), 0)
}

final case class AdventurerParams(
  speeds: AdventurerMoveSpeed,
  radius: List[Float],
  maxEnergyList: List[Int],
  containEnergyList: List[Int],
  facePalstance: Float,
  speedUpRate: Float,
  speedUpEnergyLoose: Int,
  dyingAnimation: Int,
  speedUpAnimation: Int,
  levelUpAnimation: Int,
  newbornFrame: Byte
)

final case class FoodParams(
  max: Int,
  energyList: List[Int],
  radiusList: List[Float],
  scatterAnimation: Byte
)

final case class WeaponParams(
  lengthList: List[Float]
)

final case class RobotParams(
  number: Int,
  nameList: List[String],
  moveFrequency: List[Double],
  attackFrequency: List[Double],
  level: Int,
  isAttack: Boolean,
  isMove: Boolean
)


trait ThorGameConfig {

  def frameDuration: Long

  def playRate: Int

  def replayRate: Int

  def boundary: Point

  def facePalstance: Float

  def newbornFrame: Byte

  def getAdventurerRadiusByLevel(l: Byte): Float

  def getFoodMax(): Int

  def getRadiusByFoodLevel(l: Byte): Float

  def getEnergyByFoodLevel(l: Byte): Int

  def getScatterAnimation: Byte

  def getEnergyByKillingAdventurerLevel(l: Byte): Int

  def getMaxEnergyByLevel(l: Byte): Int

//  def getWeaponLevelByLevel(l: Byte): Byte

  def getWeaponLengthByLevel(l: Byte): Float

  def getThorGameConfigImpl(): ThorGameConfigImpl

  def getThorSpeedByLevel(l: Byte, isSpeedUp: Boolean = false): Point

  def getMoveDistanceByFrame(l: Byte, isSpeedUp: Boolean = false): Point = getThorSpeedByLevel(l, isSpeedUp) * frameDuration / 1000

  def getAdventurerLevelSize: Int

  def speedUpEnergyLoose: Int

  def getAdventurerDyingAnimation: Int

  def getAdventurerLevelUpAnimation: Int

  def getRobotNumber:Int

  def getRobotNames:List[String]

  def getRobotMoveFrequency:List[Double]

  def getRobotAttackFrequency:List[Double]

  def getRobotLevel:Int

  def isRobotAttack:Boolean

  def isRobotMove:Boolean

}


case class ThorGameConfigImpl(
  gridBoundary: GridBoundary,
  frameDuration: Long,
  playRate: Int,
  replayRate: Int,
  adventurerParams: AdventurerParams,
  foodParams: FoodParams,
  weaponParams: WeaponParams,
  robotParams: RobotParams
) extends ThorGameConfig {

  def getThorGameConfigImpl(): ThorGameConfigImpl = this

  def boundary: Point = gridBoundary.getBoundary

  def facePalstance: Float = adventurerParams.facePalstance

  def newbornFrame: Byte = adventurerParams.newbornFrame

  def getAdventurerRadiusByLevel(l: Byte): Float = {
    adventurerParams.radius(l - 1)
  }


  override def getFoodMax(): Int = {
    foodParams.max
  }

  override def getRadiusByFoodLevel(l: Byte): Float = {
    foodParams.radiusList(l)
  }

  override def getEnergyByFoodLevel(l: Byte): Int = {
    foodParams.energyList(l)
  }

  override def getScatterAnimation: Byte = {
    foodParams.scatterAnimation
  }

  override def getEnergyByKillingAdventurerLevel(l: Byte): Int = {
    adventurerParams.containEnergyList(l - 1)
  }

  override def getMaxEnergyByLevel(l: Byte): Int = {
    adventurerParams.maxEnergyList(l - 1)
  }

  override def getWeaponLengthByLevel(l: Byte): Float = {
    weaponParams.lengthList(l - 1)
  }

  def getThorSpeedByLevel(l: Byte, isSpeedUp: Boolean = false): Point = if (isSpeedUp) {
    adventurerParams.speeds.getThorSpeedByLevel(l) * adventurerParams.speedUpRate
  } else adventurerParams.speeds.getThorSpeedByLevel(l)

  def getAdventurerLevelSize: Int = adventurerParams.radius.size

  override def speedUpEnergyLoose: Int = adventurerParams.speedUpEnergyLoose

  def getAdventurerDyingAnimation: Int = adventurerParams.dyingAnimation

  def getAdventurerLevelUpAnimation: Int = adventurerParams.levelUpAnimation

  //robot
  def getRobotNumber:Int = robotParams.number
  def getRobotNames:List[String] = robotParams.nameList
  def getRobotMoveFrequency:List[Double] = robotParams.moveFrequency
  def getRobotAttackFrequency:List[Double] = robotParams.attackFrequency
  def getRobotLevel:Int = robotParams.level
  def isRobotAttack:Boolean = robotParams.isAttack
  def isRobotMove:Boolean = robotParams.isMove



}

