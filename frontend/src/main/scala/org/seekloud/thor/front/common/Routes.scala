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
