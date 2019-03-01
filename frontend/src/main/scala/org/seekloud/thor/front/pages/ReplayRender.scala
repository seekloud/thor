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

import org.seekloud.thor.front.common.Page
import org.seekloud.thor.front.model.{PlayerInfo, ReplayInfo}
import org.seekloud.thor.front.thorClient.GameHolder4Replay
import org.seekloud.thor.front.utils.Shortcut

import scala.xml.Elem

/**
  * User: TangYaruo
  * Date: 2018/11/29
  * Time: 17:27
  */
class ReplayRender(replayInfo: ReplayInfo)extends Page{

  private val canvas = <canvas id ="GameView" tabindex="1"> </canvas>


  def init() = {
    val gameHolder = new GameHolder4Replay("GameView")
    gameHolder.startReplay(Some(replayInfo))
  }



  override def render: Elem ={
    println("ThorRender render")
    Shortcut.scheduleOnce(() =>init(),0)
    <div>
      {canvas}
    </div>
  }

}
