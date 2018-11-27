package org.seekloud.thor.front.common

/**
  * User: Taoz
  * Date: 2/24/2017
  * Time: 10:59 AM
  */
object Routes {


  val base = "/thor"

  def wsJoinGameUrl(name:String) = base + s"/game/join?name=$name"

  def wsJoinGameUrlESheep(id: String, name: String, accessCode: String, roomId: Option[Long]): String =
    base + s"/game/playGame/userJoin?playerId=$id&playerName=$name&accessCode=$accessCode" + roomId.getOrElse("")

  def wsWatchGameUrl(roomId: Long, id: String, accessCode: String) = base + s"/game/watchGame?roomId=$roomId&playERiD=$id&accessCode=$accessCode"









}
