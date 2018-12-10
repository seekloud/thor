package org.seekloud.thor.front.pages

import org.seekloud.thor.front.common.Page
import org.seekloud.thor.front.thorClient.{GameHolder, GameHolder4Play, GameHolder4Test}
import org.seekloud.thor.front.utils.Shortcut
import org.seekloud.thor.shared.ptcl.model.Point
import org.seekloud.thor.shared.ptcl.protocol.ThorGame.ThorGameInfo
import mhtml.Var
import org.scalajs.dom
import org.scalajs.dom.ext.Color
import org.scalajs.dom.html.Canvas
import mhtml._

import scala.xml.Elem

/**
  * Created by Jingyi on 2018/11/9
  */
object TestRender extends Page{

  private val gameInfo = ThorGameInfo(name = "AUTO_TEST")

  private val canvas = <canvas id ="GameView" tabindex="1"></canvas>

  def init() = {

    println("ThorRender init")

    val gameHolder = new GameHolder4Test("GameView")
    gameHolder.start(gameInfo.name, gameInfo.pId, gameInfo.userAccessCode, gameInfo.rId)
  }



  override def render: Elem ={
    println("ThorRender render")
    Shortcut.scheduleOnce(() =>init(),0)
    <div>{canvas}</div>
  }
}
