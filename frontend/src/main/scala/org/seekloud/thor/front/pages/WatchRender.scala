package org.seekloud.thor.front.pages

import mhtml.{Var, emptyHTML}
import org.seekloud.thor.front.common.Page
import org.seekloud.thor.front.thorClient.{GameHolder4Play, GameHolder4Watch}
import org.seekloud.thor.front.utils.Shortcut
import org.seekloud.thor.shared.ptcl.protocol.ThorGame.ThorGameInfo

import scala.xml.Elem

/**
  * @author Jingyi
  * @version 创建时间：2018/11/27
  */
class WatchRender(roomId: Long, playerId: String, accessCode: String)extends Page{


  private val canvas = <canvas id ="GameView" tabindex="1"></canvas>

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
