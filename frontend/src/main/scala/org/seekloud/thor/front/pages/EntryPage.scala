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
import org.seekloud.thor.front.thorClient._
import mhtml.Var
import org.scalajs.dom
import org.scalajs.dom.html.Input

import scala.xml.Elem

/**
  * User: XuSiRan
  * Date: 2018/11/13
  * Time: 11:13
  */
object EntryPage extends Page{
  val defaultName: String =
    if(dom.window.localStorage.getItem("entry_nickname") == null){
      ""
    }
    else
      dom.window.localStorage.getItem("entry_nickname")
  val showGame = Var(0)
  val show = showGame.map{
    case 0 =>
      <div>
        <div class="entry">
          <div class="title">
            <h1>Thor</h1>
          </div>
          <div class="text">
            <input type="text" class="form-control" id="userName" placeholder="nickname" value={s"$defaultName"}></input>
          </div>
          <div class="button">
            <button type="button" class="btn" onclick={()=>joinGame()}>join</button>
          </div>
        </div>
      </div>

    case 1 =>
      val pName = dom.document.getElementById("userName").asInstanceOf[Input].value
      new ThorRender(List(pName)).render
  }

  def joinGame(): Unit = {
    println("joinGame")
    val userName = dom.document.getElementById("userName").asInstanceOf[Input].value
    dom.window.localStorage.setItem("entry_nickname", userName)
//    dom.window.location.hash = s"home/$userName"
    showGame := 1
  }

  override def render: Elem =
    <div>
      {show}
    </div>
}
