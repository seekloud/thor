package org.seekloud.thor.front.thorClient

import org.seekloud.thor.front.common.Routes
import org.seekloud.thor.front.model.{PlayerInfo, ReplayInfo}
import org.seekloud.thor.front.utils.JsFunc
import org.seekloud.thor.shared.ptcl.protocol.ThorGame

/**
  * @author Jingyi
  * @version 创建时间：2018/11/26
  */
class GameHolder4Replay(name: String, playerInfoOpt: Option[PlayerInfo] = None) extends GameHolder (name) {

  websocketClient.setWsReplay(true)

  def startReplay(option: Option[ReplayInfo]=None)={
    if(firstCome){
      websocketClient.setup(Routes.getReplaySocketUri(option.get))
      gameLoop()
    }else if(websocketClient.getWsState){
      firstCome = true
      gameLoop()
    }else{
      JsFunc.alert("网络连接失败，请重新刷新")
    }
  }


  override protected def wsMessageHandler(e: ThorGame.WsMsgServer): Unit = ???



}
