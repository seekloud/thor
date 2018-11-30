package org.seekloud.thor.front.pages

import org.seekloud.thor.front.common.{Page, PageSwitcher}
import mhtml.{Cancelable, Rx, Var, mount}
import org.scalajs.dom

import scala.xml.Elem


object MainPage extends PageSwitcher {



  private val currentPage: Rx[Elem] = currentHashVar.map {
//    case "thor" :: "playGame" :: playerId :: playerName :: accessCode :: Nil=> new ThorRender(playerName, playerId, accessCode).render
    case "playGame" :: playerInfoList => new ThorRender(playerInfoList).render
    case "play" :: playerInfoList => new ThorRender(playerInfoList).render
    case "watchGame" :: roomId :: playerId :: accessCode :: Nil => new WatchRender(roomId.toLong, playerId, accessCode).render
    case "watchRecord" :: recordId :: playerId :: frame :: accessCode :: Nil => new ReplayRender(recordId.toLong, playerId, frame.toInt, accessCode).render
    case "entry" :: Nil => EntryPage.render
    case _ => println("error in switch"); EntryPage.render
  }


  def show(): Cancelable = {
    switchPageByHash()
    val page =
      <div>
        {currentPage}
      </div>
    mount(dom.document.body, page)
  }

}
