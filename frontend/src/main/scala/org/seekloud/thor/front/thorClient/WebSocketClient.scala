package org.seekloud.thor.front.thorClient

import org.seekloud.thor.front.common.Routes
import org.seekloud.thor.front.utils.byteObject.MiddleBufferInJs
import org.seekloud.thor.shared.ptcl.protocol.ThorGame.{DecodeError, WsMsgFront, WsMsgServer}
import org.scalajs.dom
import org.scalajs.dom.raw._

import scala.scalajs.js.typedarray.ArrayBuffer

//import org.seekloud.byteobject.MiddleBufferInJs

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

  def getWebSocketUri(url:String): String = {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"
      s"$wsProtocol://${dom.document.location.host}$url"
  }

  private val sendBuffer:MiddleBufferInJs = new MiddleBufferInJs(2048)

  def sendMsg(msg:WsMsgFront) = {
    import org.seekloud.thor.front.utils.byteObject.ByteObject._
    websocketStreamOpt.foreach{s =>
      s.send(msg.fillMiddleBuffer(sendBuffer).result())
    }
  }

  import org.seekloud.byteobject.ByteObject._
  import org.seekloud.byteobject.MiddleBufferInJs
  import scala.scalajs.js.typedarray.ArrayBuffer
  private def wsByteDecode(a:ArrayBuffer):WsMsgServer={
    val middleDataInJs = new MiddleBufferInJs(a)
    bytesDecode[WsMsgServer](middleDataInJs) match {
      case Right(r) =>
        r
      case Left(e) =>
        println(e.message)
        DecodeError()
    }
  }


  def setup(url: String):Unit = {
    println("set up")

    val webSocketStream = new WebSocket(getWebSocketUri(url))
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
      event.data match {
        case blobMsg:Blob =>
          val fr = new FileReader()
          fr.readAsArrayBuffer(blobMsg)
          fr.onloadend = { _: Event =>
            val buf = fr.result.asInstanceOf[ArrayBuffer]
            messageHandler(wsByteDecode(buf))
          }
        case jsonStringMsg:String =>
          import io.circe.generic.auto._
          import io.circe.parser._
          val data = decode[WsMsgServer](jsonStringMsg).right.get
          messageHandler(data)
        case unknow =>  println(s"recv unknow msg:${unknow}")
      }    }

    webSocketStream.onclose = { (event: Event) =>
      wsSetup = false
      websocketStreamOpt = None
      closeCallback(event)
    }

  }

  def closeWs = {
    wsSetup = false
    websocketStreamOpt.foreach(_.close())
    websocketStreamOpt = None
  }

}
