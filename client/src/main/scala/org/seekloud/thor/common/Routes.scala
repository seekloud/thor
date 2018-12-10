package org.seekloud.thor.common

import java.net.URLEncoder

/**
  * @author Jingyi
  * @version 创建时间：2018/12/3
  */
object Routes {
  def getJoinGameWebSocketUri(playerId: String, name:String, accessCode: String, domain:String,roomIdOpt:Option[String]):String ={
    val wsProtocol = "ws"
    s"$wsProtocol://${domain}/thor/${wsJoinGameUrl(playerId, URLEncoder.encode(name,"utf-8"),accessCode,  roomIdOpt)}"
  }

  def wsJoinGameUrl(playerId: String, name:String, accessCode: String, roomIdOpt:Option[String]):String = {
    s"game/playGame/userJoin?playerId=$playerId&playerName=$name&accessCode=$accessCode" +
      (roomIdOpt match {
        case Some(roomId) =>
          s"&roomId=$roomId"
        case None =>
          ""
      })
  }
}
