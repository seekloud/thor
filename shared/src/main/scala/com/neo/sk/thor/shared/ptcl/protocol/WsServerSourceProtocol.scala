package com.neo.sk.thor.shared.ptcl.protocol


object WsServerSourceProtocol {
  trait WsMsgSource


  case object CompleteMsgServer extends WsMsgSource
  case class FailMsgServer(ex: Exception) extends WsMsgSource

}
