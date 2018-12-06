package org.seekloud.thor.common

import java.net.URLEncoder

/**
  * @author Jingyi
  * @version 创建时间：2018/12/3
  */
object Routes {
  def getJoinGameWebSocektUri(name:String,domain:String,roomIdOpt:Option[String]):String ={
    val wsProtocol = "ws"
    s"$wsProtocol://${domain}/thor${wsJoinGameurl(URLEncoder.encode(name,"utf-8"),roomIdOpt)}"
  }

  def wsJoinGameurl(name:String, roomIdOpt:Option[String]):String = {
    s"game/join?name=$name" +
      (roomIdOpt match {
        case Some(roomId) =>
          s"&roomId=$roomId"
        case None =>
          ""
      })
  }
}
