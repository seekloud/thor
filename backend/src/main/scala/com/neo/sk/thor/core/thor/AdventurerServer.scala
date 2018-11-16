package com.neo.sk.thor.core.thor

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.TimerScheduler
import com.neo.sk.thor.core.RoomActor
import com.neo.sk.thor.shared.ptcl.`object`.Adventurer
import com.neo.sk.thor.shared.ptcl.config.ThorGameConfig
import com.neo.sk.thor.shared.ptcl.model.Point

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
                        position: Point = Point(0, 0),
                        level: Int = 1,
                        energy: Int = 0,
                        radius: Int = 1,
                        direction: Float = 0,
                        weaponLevel: Int = 1,
                        weaponLength: Int = 1,
                        speed: Float = 1,
                        isSpeedUp: Boolean = false,
                        killNum: Int = 0
                      ) extends Adventurer{

}
