package org.seekloud.thor.front.common

import org.scalajs.dom
import org.seekloud.thor.front.model.ReplayInfo

/**
  * User: Taoz
  * Date: 2/24/2017
  * Time: 10:59 AM
  */
object Routes {


  val base = "/thor"

  val getPsw = base + "/getTestPsw"
  val getVersion = base + "/getVersion"

  def wsJoinGameUrl(name:String) = base + s"/game/join?name=$name"

  def wsJoinGameUrlESheep(id: String, name: String, accessCode: String, roomId: Option[Long]): String = {
    if(roomId.isEmpty)
      base + s"/game/playGame/userJoin?playerId=$id&playerName=$name&accessCode=$accessCode"
    else
      base + s"/game/playGame/userJoin?playerId=$id&playerName=$name&accessCode=$accessCode&roomId=${roomId.get}"
  }

  def wsWatchGameUrl(roomId: Long, id: String, accessCode: String) = base + s"/game/watchGame?roomId=$roomId&playerId=$id&accessCode=$accessCode"

  def wsReplayGameUrl(info:ReplayInfo) = base + s"/game/replay?recordId=${info.recordId}&playerId=${info.playerId}&frame=${info.frame}&accessCode=${info.accessCode}"


//  def getReplaySocketUri(info:ReplayInfo): String = {
//    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"
//    s"$wsProtocol://${dom.document.location.host}${Routes.wsReplayGameUrl(info)}"
//  }









}
