/*
 * Copyright 2018 seekloud (https://github.com/seekloud)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.seekloud.thor.front.thorClient

import org.seekloud.thor.front.common.Routes
import org.seekloud.thor.shared.ptcl.protocol.ThorGame
//import org.seekloud.thor.front.utils.byteObject.MiddleBufferInJs
import org.seekloud.thor.shared.ptcl.protocol.ThorGame.{DecodeError, WsMsgFront, WsMsgServer}
import org.scalajs.dom
import org.scalajs.dom.raw._

import scala.scalajs.js.typedarray.ArrayBuffer

import org.seekloud.byteobject.MiddleBufferInJs

class WebSocketClient(
  connectSuccessCallback: Event => Unit,
  connectErrorCallback: Event => Unit,
  messageHandler: WsMsgServer => Unit,
  closeCallback: Event => Unit
) {

  println("WebSocketClient...")

  private var wsSetup = false

  private var replay: Boolean = false

  private var websocketStreamOpt: Option[WebSocket] = None

  def getWsState = wsSetup

  def setWsReplay(r: Boolean) = {
    replay = r
  }

  def getWebSocketUri(url: String): String = {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"
    s"$wsProtocol://${dom.document.location.host}$url"
  }

  private val sendBuffer: MiddleBufferInJs = new MiddleBufferInJs(2048)
  import org.seekloud.byteobject.ByteObject._
  import org.seekloud.byteobject.MiddleBufferInJs
  import scala.scalajs.js.typedarray.ArrayBuffer

  def sendMsg(msg: WsMsgFront) = {
    //    import org.seekloud.thor.front.utils.byteObject.ByteObject._
    websocketStreamOpt.foreach { s =>
      s.send(msg.fillMiddleBuffer(sendBuffer).result())
    }
  }


  def setup(url: String): Unit = {
    println(s"set up ${getWebSocketUri(url)} ${System.currentTimeMillis()}")

    val webSocketStream = new WebSocket(getWebSocketUri(url))
    websocketStreamOpt = Some(webSocketStream)
    webSocketStream.onopen = { event: Event =>
      wsSetup = true
      connectSuccessCallback(event)
    }
    webSocketStream.onerror = { event: Event =>
      wsSetup = false
      websocketStreamOpt = None
      connectErrorCallback(event)
    }

    webSocketStream.onmessage = { event: MessageEvent =>
      //        println(s"receive msg:${event.data.toString}")
      event.data match {
        case blobMsg: Blob =>
          val fr = new FileReader()
          fr.readAsArrayBuffer(blobMsg)
          fr.onloadend = { _: Event =>
            val buf = fr.result.asInstanceOf[ArrayBuffer]
            if(replay) messageHandler(replayEventDecode(buf))
            else messageHandler(wsByteDecode(buf))
          }
        case jsonStringMsg: String =>
          import io.circe.generic.auto._
          import io.circe.parser._
          decode[WsMsgServer](jsonStringMsg) match {
            case Right(data) =>
              messageHandler(data)
            case Left(e) =>
              println(s"ws msg decode error: $e")
          }
        case unknown => println(s"receive unknown msg:$unknown")
      }
    }

    webSocketStream.onclose = { event: Event =>
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

  import org.seekloud.byteobject.ByteObject._

  private def wsByteDecode(a: ArrayBuffer): ThorGame.WsMsgServer = {
    val middleDataInJs = new MiddleBufferInJs(a)
    bytesDecode[ThorGame.WsMsgServer](middleDataInJs) match {
      case Right(r) =>
        r
      case Left(e) =>
        println(e.message)
        ThorGame.DecodeError()
    }
  }

  private def replayEventDecode(a: ArrayBuffer): ThorGame.WsMsgServer = {
    val middleDataInJs = new MiddleBufferInJs(a)
    if (a.byteLength > 0) {
      bytesDecode[List[ThorGame.WsMsgServer]](middleDataInJs) match {
        case Right(r) =>
          ThorGame.EventData(r)
        case Left(e) =>
          println(e.message)
          replayStateDecode(a)
      }
    } else {
      ThorGame.DecodeError()
    }
  }

  private def replayStateDecode(a: ArrayBuffer): ThorGame.WsMsgServer = {
    val middleDataInJs = new MiddleBufferInJs(a)
    bytesDecode[ThorGame.GameSnapshot](middleDataInJs) match {
      case Right(r) =>
        ThorGame.GridSyncState(r.asInstanceOf[ThorGame.ThorSnapshot].state)
      case Left(e) =>
        println(e.message)
        ThorGame.DecodeError()
    }
  }


}
