package org.seekloud.thor.front.pages

import org.seekloud.thor.front.common.Page
import org.seekloud.thor.front.thorClient.{GameHolder, GameHolder4Play}
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
class ThorRender(gameInfoList: List[String])extends Page{

  private val gameInfo = gameInfoList match{
    case playerName :: Nil => ThorGameInfo(name = playerName)
    case playerId :: playerName :: accessCode :: Nil => ThorGameInfo(name = playerName, pId = Some(playerId), userAccessCode = Some(accessCode))
    case playerId :: playerName :: roomId :: accessCode :: Nil =>
      ThorGameInfo(name = playerName, pId = Some(playerId), rId = Some(roomId.toLong), userAccessCode = Some(accessCode))
    case _ => ThorGameInfo("default")
  }

  private val canvas = <canvas id ="GameView" tabindex="1" style="cursor: url(/thor/static/img/cursor.png),auto;"></canvas>


  private val modal = Var(emptyHTML)

  def init() = {

    println("ThorRender init")

    val gameHolder = new GameHolder4Play("GameView")
    gameHolder.start(gameInfo.name, gameInfo.pId, gameInfo.userAccessCode, gameInfo.rId)
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
