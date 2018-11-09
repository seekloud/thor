package com.neo.sk.thor.shared.ptcl.protocol

import com.neo.sk.thor.shared.ptcl.model.{Point, Score}
import com.neo.sk.thor.shared.ptcl.thor._

/**
  * Created by Jingyi on 2018/11/9
  */
object WsProtocol {


  sealed trait WsMsgServer extends WsServerSourceProtocol.WsMsgSource


  case class UserInfo(uId:Long) extends WsMsgServer

  case class UserEnterRoom(userId:Long,name:String,adventurer: AdventurerState) extends WsMsgServer

  case class UserLeftRoom(userId:Long,name:String) extends WsMsgServer

  case class BeKilled(userId:Long,name:String) extends WsMsgServer

  case class MouseMove(userId:Long,frame:Long,actM:WsFrontProtocol.MouseMove) extends WsMsgServer

  case class Ranks(currentRank: List[Score], historyRank: List[Score]) extends WsMsgServer

  case class GridSyncState(d:GridState) extends WsMsgServer

}
