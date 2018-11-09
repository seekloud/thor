package com.neo.sk.thor.shared.ptcl.protocol

import com.neo.sk.thor.shared.ptcl.thor.{GridState}


object ThorGame {

  sealed trait GameEvent{
    val frame:Long
  }

  trait UserEvent
  trait EnvironmentEvent


  sealed trait GameSnapshot

  final case class ThorSnapshot(
                                     state:GridState
                                   ) extends GameSnapshot

}
