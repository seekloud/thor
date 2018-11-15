package com.neo.sk.thor.front.pages

import com.neo.sk.thor.front.common.Page
import org.scalajs.dom
import org.scalajs.dom.html.Input

import scala.xml.Elem

/**
  * User: XySiRan
  * Date: 2018/11/13
  * Time: 11:13
  */
object EntryPage extends Page{

  def joinGame(): Unit = {
    val userName = dom.document.getElementById("userName").asInstanceOf[Input].value
    dom.window.location.hash = s"home/$userName"
  }

  override def render: Elem =
    <div>
      <div class="entry">
        <div class="title">
          <h1>Thor</h1>
        </div>
        <div class="text">
          <input type="text" class="form-control" id="userName" placeholder="nickname"></input>
        </div>
        <div class="button">
          <button type="button" class="btn" onclick={()=>joinGame()}>join</button>
        </div>
      </div>
    </div>
}
