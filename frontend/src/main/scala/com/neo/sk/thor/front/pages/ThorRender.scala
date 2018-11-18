package com.neo.sk.thor.front.pages

import com.neo.sk.thor.front.common.Page
import com.neo.sk.thor.front.thorClient.GameHolder
import com.neo.sk.thor.front.utils.Shortcut
import com.neo.sk.thor.shared.ptcl.model.Point
import mhtml.Var
import org.scalajs.dom
import org.scalajs.dom.ext.Color
import org.scalajs.dom.html.Canvas
import mhtml._
import scala.xml.Elem

/**
  * Created by Jingyi on 2018/11/9
  */
object ThorRender extends Page{

  private val canvas = <canvas id ="GameView" tabindex="1"></canvas>


  private val modal = Var(emptyHTML)

  def init() = {

    println("ThorRender init")

    val gameHolder = new GameHolder("GameView")
    gameHolder.start("test")
  }



  override def render: Elem ={
    println("ThorRender render")
    Shortcut.scheduleOnce(() =>init(),0)
    <div>
      <div >{modal}</div>
      {canvas}
    </div>
  }



}
