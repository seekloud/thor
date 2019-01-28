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

package org.seekloud.thor.front.pages

import org.seekloud.thor.front.common.{Page, PageSwitcher}
import mhtml.{Cancelable, Rx, Var, mount}
import org.scalajs.dom
import org.seekloud.thor.front.Main
import org.seekloud.thor.front.model.ReplayInfo

import scala.xml.Elem


object MainPage extends PageSwitcher {



  private val currentPage: Rx[Elem] = currentHashVar.map {
//    case "thor" :: "playGame" :: playerId :: playerName :: accessCode :: Nil=> new ThorRender(playerName, playerId, accessCode).render
    case "playGame" :: playerInfoList => new ThorRender(playerInfoList).render
    case "play" :: playerInfoList => new ThorRender(playerInfoList).render
    case "watchGame" :: roomId :: playerId :: accessCode :: Nil => new WatchRender(roomId.toLong, playerId, accessCode).render
    case "watchRecord" :: recordId :: playerId :: frame :: accessCode :: Nil => new ReplayRender(ReplayInfo(recordId.toLong, playerId, frame.toInt, accessCode)).render
    case "entry" :: Nil => EntryPage.render
    case "test" :: Nil => TestRender.render
    case _ => println("error in switch"); EntryPage.render
  }

  private val versionDiv = Main.version.map{ version => <div style="color:white;position:fixed;left:20px;bottom:20px;font-family:consolas;font-weight:bold">V{version}</div>}


  def show(): Cancelable = {
    switchPageByHash()
    val page =
      <div>
        {currentPage}
        {versionDiv}
      </div>
    mount(dom.document.body, page)
  }

}
