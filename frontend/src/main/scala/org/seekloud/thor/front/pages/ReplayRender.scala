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

  private val canvas = <canvas id ="GameView" tabindex="1" style="cursor:url(http://pic.neoap.com/hestia/files/image/OnlyForTest/8970e0eb3ae30901488d351953d0df70.png),auto;"> </canvas>


  def init() = {
    val gameHolder = new GameHolder4Replay("GameView")
    gameHolder.startReplay(Some(replayInfo))
  }



  override def render: Elem ={
    println("ThorRender render")
    Shortcut.scheduleOnce(() =>init(),0)
    <div>
      {canvas}
    </div>
  }

}
