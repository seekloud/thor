package org.seekloud.thor.front.thorClient

import org.seekloud.thor.front.common.Routes
import org.seekloud.thor.front.utils.byteObject.MiddleBufferInJs
import org.seekloud.thor.shared.ptcl.protocol.ThorGame.WsMsgFront
import org.scalajs.dom
import org.scalajs.dom.raw._


class WebSocketClient(
                       connectSuccessCallback: Event => Unit,
                       connectErrorCallback:Event => Unit,
                       messageHandler:MessageEvent => Unit,
                       closeCallback:Event => Unit
                     ) {

  println("WebSocketClient...")

  private var wsSetup = false

  private var websocketStreamOpt : Option[WebSocket] = None

  def getWsState = wsSetup

  def getWebSocketUri(name: String, id: String, accessCode: String): String = {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"
    if(id.equals("1"))
      s"$wsProtocol://${dom.document.location.host}${Routes.wsJoinGameUrl(name)}"
    else s"$wsProtocol://${dom.document.location.host}${Routes.wsJoinGameUrlESheep(id, name, accessCode)}"
  }

  private val sendBuffer:MiddleBufferInJs = new MiddleBufferInJs(2048)

  def sendMsg(msg:WsMsgFront) = {
    import org.seekloud.thor.front.utils.byteObject.ByteObject._
    websocketStreamOpt.foreach{s =>
      s.send(msg.fillMiddleBuffer(sendBuffer).result())
    }
  }


  def setup(name:String, id:String, accessCode: String):Unit = {
    println("set up")

    val webSocketStream = new WebSocket(getWebSocketUri(name, id, accessCode))
    websocketStreamOpt = Some(webSocketStream)
    webSocketStream.onopen = { (event: Event) =>
      wsSetup = true
      connectSuccessCallback(event)
    }
    webSocketStream.onerror = { (event: Event) =>
      wsSetup = false
      websocketStreamOpt = None
      connectErrorCallback(event)
    }

    webSocketStream.onmessage = { (event: MessageEvent) =>
//        println(s"recv msg:${event.data.toString}")
      messageHandler(event)
    }

    webSocketStream.onclose = { (event: Event) =>
      wsSetup = false
      websocketStreamOpt = None
      closeCallback(event)
    }

  }


}
