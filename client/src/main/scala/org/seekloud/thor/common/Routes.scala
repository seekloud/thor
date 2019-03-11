/*
 * Copyright 2018 seekloud (https://github.com/seekloud)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.seekloud.thor.common

import java.net.URLEncoder

/**
  * @author Jingyi
  * @version 创建时间：2018/12/3
  */
object Routes {

  val baseUrl = AppSettings.baseUrl
  val gameName = AppSettings.esheepGameName

  val wsProtocol = AppSettings.gameServerProtocol
  val wsDomain = AppSettings.gameServerDomain

  def wsJoinGameUrl(playerId: String, name:String, accessCode: String, roomIdOpt:Option[String]):String = {
    s"game/playGame/userJoin?playerId=$playerId&playerName=$name&accessCode=$accessCode" +
      (roomIdOpt match {
        case Some(roomId) =>
          s"&roomId=$roomId"
        case None =>
          ""
      })
  }

  def clientLinkGame(playerId: String, name: String, accessCode: String): String = {
    val playerIdEncoder =URLEncoder.encode(playerId, "UTF-8")
    val playerNameEncoder = URLEncoder.encode(name, "UTF-8")
    s"$wsProtocol://$wsDomain/" + gameName + s"/game/playGame/clientLinkGame?playerId=$playerIdEncoder&playerName=$playerNameEncoder&accessCode=$accessCode"
  }



}
