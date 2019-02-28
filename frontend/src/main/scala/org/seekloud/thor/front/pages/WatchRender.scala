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

import mhtml.{Var, emptyHTML}

import org.seekloud.thor.front.common.Page
import org.seekloud.thor.front.thorClient.{GameHolder4Play, GameHolder4Watch}
import org.seekloud.thor.front.utils.Shortcut




import scala.xml.Elem

/**
  * @author Jingyi
  * @version 创建时间：2018/11/27
  */
class WatchRender(roomId: Long, playerId: String, accessCode: String)extends Page{


  private val canvas = <canvas id ="GameView" tabindex="1" style="cursor:url(http://pic.neoap.com/hestia/files/image/OnlyForTest/8970e0eb3ae30901488d351953d0df70.png),auto;"> </canvas>

  def init() = {

    val gameHolder = new GameHolder4Watch("GameView", roomId, playerId, accessCode)
    gameHolder.watch()
  }



  override def render: Elem ={
    println("ThorRender render")
    Shortcut.scheduleOnce(() =>init(),0)
    <div>
      {canvas}
    </div>
  }



}
