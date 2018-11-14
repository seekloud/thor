package com.neo.sk.thor.shared.ptcl.`object`

import com.neo.sk.thor.shared.ptcl.config.ThorGameConfig
import com.neo.sk.thor.shared.ptcl.model.Point

/**
  * Created by Jingyi on 2018/11/9
  */
case class AdventurerState(
  playerId: String,
  level: Int,
  energy: Int,
//  radius: Int,
  position: Point,
  direction: Float,
  weaponLevel: Int,
  weaponLength: Int,
  speed: Float,
  killNum: Int
)

trait Adventurer extends CircleObjectOfGame {
  val playerId: String
  var level: Int
  var energy: Int
//  var radius: Int
  var position: Point
  var direction: Float
  var weaponLevel: Int
  var weaponLength: Int
  var speed: Float
  var killNum: Int


}

case class AdventurerImpl(
  config: ThorGameConfig,
  playerId: String,
  protected var level: Int,
  protected var energy: Int,
//  radius: Int,
  protected var position: Point,
  protected var direction: Float,
  protected var weaponLevel: Int,
  protected var weaponLength: Int,
  protected var speed: Float,
  protected var killNum: Int
) extends Adventurer {
  def this(config: ThorGameConfig, adventurerState: AdventurerState) {
    this(config, adventurerState.playerId, adventurerState.level, adventurerState.energy, adventurerState.position,
      adventurerState.direction, adventurerState.weaponLevel, adventurerState.weaponLength, adventurerState.speed, adventurerState.killNum)
  }

  override val radius: Float = config.thorRadius







}