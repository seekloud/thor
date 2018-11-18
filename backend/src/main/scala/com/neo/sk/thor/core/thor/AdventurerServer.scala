package com.neo.sk.thor.core.thor

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.TimerScheduler
import com.neo.sk.thor.core.RoomActor
import com.neo.sk.thor.shared.ptcl.`object`.{Adventurer, AdventurerImpl, AdventurerState}
import com.neo.sk.thor.shared.ptcl.config.ThorGameConfig
import com.neo.sk.thor.shared.ptcl.model.Point
import com.neo.sk.thor.shared.ptcl.thor.ThorSchemaState

/**
  * @author Jingyi
  * @version 创建时间：2018/11/13
  */
case class AdventurerServer(
                        roomActor:ActorRef[RoomActor.Command],
                        timer:TimerScheduler[RoomActor.Command],
                        config:ThorGameConfig,
                        playerId : String,
                        name : String,
                        var position: Point = Point(0, 0),
                        var level: Int = 1,
                        var energy: Int = 0,
                        var radiusLevel: Int = 1,
                        var direction: Float = 0,
                        var weaponLevel: Int = 1,
                        var weaponLength: Float = 1,
                        var speed: Float = 1,
                        var isSpeedUp: Boolean = false,
                        var killNum: Int = 0
                      ) extends Adventurer{

  override var radius: Float = config.getAdventurerRadiusByLevel(radiusLevel)

  def this(roomActor:ActorRef[RoomActor.Command], timer:TimerScheduler[RoomActor.Command],config: ThorGameConfig,adventurerState: AdventurerState){
    this(roomActor, timer, config,adventurerState.playerId,adventurerState.name,adventurerState.position,adventurerState.level,adventurerState.energy,adventurerState.radiusLevel,adventurerState.direction,
      adventurerState.weaponLevel,adventurerState.weaponLength, adventurerState.speed,adventurerState.isSpeedUp,adventurerState.killNum)
  }

}