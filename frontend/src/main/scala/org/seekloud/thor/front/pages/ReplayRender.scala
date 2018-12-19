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

  private val canvas = <canvas id ="GameView" tabindex="1"></canvas>
  private val audio_1 = <audio id="bgm-2" src="/thor/static/music/bgm-2.mp3" preload="auto"></audio>
  private val audio_2 = <audio id="sound-4" src="/thor/static/music/sound-4.mp3" preload="auto"></audio>

  def init() = {
    val gameHolder = new GameHolder4Replay("GameView")
    gameHolder.startReplay(Some(replayInfo))
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
