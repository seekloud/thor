package com.neo.sk.thor.shared.ptcl.protocol


object WsFrontProtocol {

  sealed trait WsMsgFront

  trait AdventurerAction{
    val timestamp:Long
    val frame:Long
  }


  final case class MouseMove(d:Float,override val timestamp:Long,override val frame:Long) extends AdventurerAction with WsMsgFront

  final case class MouseClick(time:Long,override val timestamp:Long,override val frame:Long) extends AdventurerAction with WsMsgFront

  final case class RestartGame(name:String) extends WsMsgFront

}
