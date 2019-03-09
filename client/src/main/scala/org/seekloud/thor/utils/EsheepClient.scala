/*
 *   Copyright 2018 seekloud (https://github.com/seekloud)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.seekloud.thor.utils

import org.seekloud.thor.common.AppSettings._
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import org.seekloud.thor.ClientBoot.executor
import org.seekloud.thor.protocol.ESheepProtocol._

/**
  * @author Jingyi
  * @version 创建时间：2018/12/3
  */
object EsheepClient extends HttpUtil {
  import io.circe.generic.auto._
  import io.circe.parser.decode
  import io.circe.syntax._

  private val log = LoggerFactory.getLogger(this.getClass)

  //  private val baseUrl = s"${AppSettings.esheepProtocol}://${AppSettings.esheepHost}:${AppSettings.esheepPort}"
  private val gsKey = "?"
  private val gameName = "thor"
  private val gameId = 1000000006l
  private val baseUrl = s"http://flowdev.neoap.com"

  def genPostEnvelopeStr(data: String, appId: String, secureKey: String): String =
    SecureUtil.genPostEnvelope(appId,
      System.nanoTime().toString,
      data,
      secureKey
    ).asJson.noSpaces

  def getLoginInfo: Future[Either[Throwable, LoginUrlRsp]] = {
    val methodName = "GET"
    val url = esheepProtocol + "://" + esheepDomain + "/esheep/api/gameAgent/login"

    getRequestSend(methodName, url, Nil).map {
      case Right(rsp) =>
        decode[LoginUrlRsp](rsp)
      case Left(e) =>
        log.debug(s"GetLoginInfo error: $e")
        e.printStackTrace()
        Left(e)
    }
  }

  def loginByMail(email:String, pwd:String): Future[Either[Throwable, ESheepUserInfoRsp]] = {
    val methodName = "POST"
    val url = esheepProtocol + "://" + esheepDomain + "/esheep/rambler/login"

    val data = LoginByMailReq(email, pwd).asJson.noSpaces

    postJsonRequestSend(methodName, url, Nil, data).map {
      case Right(jsonStr) =>
        decode[ESheepUserInfoRsp](jsonStr)
      case Left(e) =>
        log.debug(s"loginByMail error: $e")
        e.printStackTrace()
        Left(e)
    }
  }

  def linkGame(token: String, playerId: String): Future[Either[Throwable, ClientJoinGameRsp]] = {
    val methodName = "ESheepLink: linkGame(getAccessCode)"
    val url = baseUrl + s"/esheep/api/gameAgent/joinGame?token=$token"

    postJsonRequestSend(methodName, url, Nil, JoinGameReq(gameId, playerId).asJson.noSpaces).map {
      case Right(rsp) =>
        decode[ClientJoinGameRsp](rsp)
      case Left(e) =>
        log.debug(s"linkGame error: $e")
        e.printStackTrace()
        Left(e)
    }
  }

  def refreshToken(token: String, playerId: String) = {
    val methodName = "ESheepLink: refreshToken"
    val url = baseUrl + s"/esheep/api/gameAgent/gaRefreshToken?token=$token"

    postJsonRequestSend(methodName, url, Nil, RefreshTokenReq(playerId).asJson.noSpaces).map {
      case Right(rsp) =>
        decode[RefreshTokenRsp](rsp)
      case Left(e) =>
        log.debug(s"linkGame error: $e")
        e.printStackTrace()
        Left(e)
    }
  }

  def getRoomList(ip: String, port: Long, domain: String) = {
    val methodName = s"getRoomList-$gameName"
    val url = baseUrl + "/" + gameName + "/" + "getRoomList"
    //    val url = baseUrl + "/" + gameName + "/" + "getRoomList"
    val postEnvelope = genPostEnvelopeStr("", gameId.toString, gsKey)

    postJsonRequestSend(methodName, url, Nil, postEnvelope).map {
      case Right(jsonRsp) =>
        decode[GetRoomListRsp](jsonRsp)
      case Left(e) =>
        log.debug(s"$methodName failed: $e")
        e.printStackTrace()
        Left(e)
    }

  }

}
