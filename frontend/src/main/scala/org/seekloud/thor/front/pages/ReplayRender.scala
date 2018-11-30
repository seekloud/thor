package org.seekloud.thor.front.pages

import org.seekloud.thor.front.common.Page
import org.seekloud.thor.front.model.ReplayInfo
import org.seekloud.thor.front.thorClient.GameHolder4Replay
import org.seekloud.thor.front.utils.Shortcut

import scala.xml.Elem

/**
  * User: TangYaruo
  * Date: 2018/11/29
  * Time: 17:27
  */
class ReplayRender(recordId: Long, playerId: String, frame: Int, accessCode: String)extends Page{

  private val canvas = <canvas id ="GameView" tabindex="1"></canvas>

  def init() = {
//    TODO
//    val gameHolder = new GameHolder4Replay("GameReplay", Some(ReplayInfo(recordId, playerId, frame, accessCode)))
//    gameHolder.startReplay()
  }



  override def render: Elem ={
    println("ThorRender render")
    Shortcut.scheduleOnce(() =>init(),0)
    <div>
      {canvas}
    </div>
  }

}
