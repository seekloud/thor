package com.neo.sk.thor.front.pages

import com.neo.sk.thor.front.common.Page

import scala.xml.Elem

/**
  * User: asus
  * Date: 2018/11/13
  * Time: 11:13
  */
object JoinPage extends Page{

  override def render: Elem =
    <div>
      <div>
        <h1>Thor</h1>
      </div>
      <div>
        <input type="text" placeholder="nickname"></input>
      </div>
      <div>
        <button type="button">join</button>
      </div>
    </div>
}
