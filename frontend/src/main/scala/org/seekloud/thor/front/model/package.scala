package org.seekloud.thor.front

/**
  * User: TangYaruo
  * Date: 2018/11/29
  * Time: 21:48
  */
package object model {
  case class PlayerInfo(userId:String, userName:String, accessCode:String, roomIdOpt:Option[Long])

  /**发送观看消息链接信息*/
  case class ReplayInfo(recordId:Long,playerId:String,frame:Int,accessCode:String)
}
