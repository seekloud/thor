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


  private val canvas = <canvas id ="GameView" tabindex="1"></canvas>
  private val audio_1 = <audio id="bgm-2" src="/thor/static/music/bgm-2.mp3" preload="auto"></audio>
  private val audio_2 = <audio id="sound-4" src="/thor/static/music/sound-4.mp3" preload="auto"></audio>
  def init() = {

    val gameHolder = new GameHolder4Watch("GameView", roomId, playerId, accessCode)
    gameHolder.watch()
  }



  override def render: Elem ={
    println("ThorRender render")
    Shortcut.scheduleOnce(() =>init(),0)
    <div>
      {canvas}
      {audio_1}
      {audio_2}
    </div>
  }



}
