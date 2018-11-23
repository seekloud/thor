package org.seekloud.thor.front.pages

import org.seekloud.thor.front.common.Page
import org.seekloud.thor.front.thorClient.GameHolder
import org.seekloud.thor.front.utils.Shortcut
import org.seekloud.thor.shared.ptcl.model.Point
import mhtml.Var
import org.scalajs.dom
import org.scalajs.dom.ext.Color
import org.scalajs.dom.html.Canvas
import mhtml._
import scala.xml.Elem

/**
  * Created by Jingyi on 2018/11/9
  */
class ThorRender(name: String, id: String = "1", accessCode: String = "1" )extends Page{

  private val canvas = <canvas id ="GameView" tabindex="1"></canvas>


  private val modal = Var(emptyHTML)

  def init() = {

    println("ThorRender init")

    val gameHolder = new GameHolder("GameView")
    gameHolder.start(name, id, accessCode)
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
