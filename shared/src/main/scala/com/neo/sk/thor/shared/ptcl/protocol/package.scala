package com.neo.sk.thor.shared.ptcl

import com.neo.sk.thor.shared.ptcl.protocol.WsFrontProtocol.AdventurerAction

package object protocol {
  trait Event{
    val frame:Long
  }

  final case class TankActionFrame(override val frame:Long,action:AdventurerAction) extends Event




}
