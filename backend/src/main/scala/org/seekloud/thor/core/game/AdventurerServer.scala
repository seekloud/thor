package org.seekloud.thor.core.game

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.TimerScheduler
import org.seekloud.thor.core.RoomActor
import org.seekloud.thor.shared.ptcl.component.{Adventurer, AdventurerState}
import org.seekloud.thor.shared.ptcl.config.ThorGameConfig
import org.seekloud.thor.shared.ptcl.model.Point

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
                        var level: Byte = 1,
                        var energy: Int = 0,
//                        var radiusLevel: Int = 1,
                        var direction: Float = 0,
                        var faceDirection: Float = 0,
//                        var weaponLevel: Int = 1,
//                        var speedLevel: Int = 1,
                        var isSpeedUp: Boolean = false,
                        var killNum: Int = 0,
                        var isMove: Boolean = true,
                        var isUpdateLevel: Boolean = false,
                        var levelUpExecute: Int = 100,
                        var mouseStop: Boolean = false
                      ) extends Adventurer{

  override var radius: Float = config.getAdventurerRadiusByLevel(level)

  def this(roomActor:ActorRef[RoomActor.Command], timer:TimerScheduler[RoomActor.Command],config: ThorGameConfig,adventurerState: AdventurerState){
    this(roomActor, timer, config,adventurerState.playerId,adventurerState.name,adventurerState.position,adventurerState.level,adventurerState.energy, adventurerState.direction,
      adventurerState.faceDirection, adventurerState.isSpeedUp,adventurerState.killNum, adventurerState.isMove, adventurerState.isUpdateLevel, adventurerState.levelUpExecute,
      adventurerState.mouseStop)
  }

}
