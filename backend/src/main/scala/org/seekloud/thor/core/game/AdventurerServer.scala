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

package org.seekloud.thor.core.game

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.TimerScheduler
import org.seekloud.thor.core.RoomActor
import org.seekloud.thor.shared.ptcl.component.{Adventurer, AdventurerState, Food}
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
  byteId: Byte,
  playerId : String,
  name : String,
  var position: Point = Point(0, 0),
  var level: Byte = 1,
  var energy: Int = 0,
  var energyScore: Int = 0,
  var direction: Float = 0,
  var faceDirection: Float = 0,
  var isSpeedUp: Boolean = false,
  var killNum: Int = 0,
  var stickKillNum : Int = 0,
  var isMove: Boolean = true,
  var isUpdateLevel: Boolean = false,
  var levelUpExecute: Int = 100,
  var mouseStop: Boolean = false,
  var isIntersect: Byte = 0
) extends Adventurer{

  override var radius: Float = config.getAdventurerRadiusByLevel(level)

//  def this(roomActor:ActorRef[RoomActor.Command], timer:TimerScheduler[RoomActor.Command],config: ThorGameConfig,adventurerState: AdventurerState, playerId: String, name: String){
//    this(roomActor, timer, adventurerState.byteId, config, playerId, name, adventurerState.position,adventurerState.level,adventurerState.energy, adventurerState.energyScore, adventurerState.direction,
//      adventurerState.isSpeedUp,adventurerState.killNum, adventurerState.isMove, adventurerState.isUpdateLevel, adventurerState.levelUpExecute,
//      adventurerState.mouseStop, adventurerState.isIntersect)
//  }
}
