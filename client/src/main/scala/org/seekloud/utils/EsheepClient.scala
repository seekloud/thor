package org.seekloud.utils

import org.seekloud.thor.model._
import org.seekloud.thor.shared.ptcl.ErrorRsp
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import org.seekloud.thor.App.executor
import com.neo.sk.utils.{HttpUtil, SecureUtil}
import org.seekloud.thor.protocol.ESheepProtocol._
import io.circe._
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
/**
  * @author Jingyi
  * @version 创建时间：2018/12/3
  */
object EsheepClient extends HttpUtil {
  import io.circe._
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

  def getLoginInfo: Future[Either[Throwable, LoginUrlRsp]] ={
    val methodName = "ESheepLink: getLoginInfo"
    val url = baseUrl + "/esheep/api/gameAgent/login"

    getRequestSend(methodName, url, Nil).map{
      case Right(rsp) =>
        decode[LoginUrlRsp](rsp)
      case Left(e) =>
        log.debug(s"GetLoginInfo error: $e")
        e.printStackTrace()
        Left(e)
    }
  }

  def linkGame(token: String, playerId: String): Future[Either[Throwable, ClientJoinGameRsp]] ={
    val methodName = "ESheepLink: linkGame(getAccessCode)"
    val url = baseUrl + s"/esheep/api/gameAgent/joinGame?token=$token"

    postJsonRequestSend(methodName, url, Nil, JoinGameReq(gameId, playerId).asJson.noSpaces).map{
      case Right(rsp) =>
        decode[ClientJoinGameRsp](rsp)
      case Left(e) =>
        log.debug(s"linkGame error: $e")
        e.printStackTrace()
        Left(e)
    }
  }

  def refreshToken(token: String, playerId: String) ={
    val methodName = "ESheepLink: refreshToken"
    val url = baseUrl + s"/esheep/api/gameAgent/gaRefreshToken?token=$token"

    postJsonRequestSend(methodName, url, Nil, RefreshTokenReq(playerId).asJson.noSpaces).map{
      case Right(rsp) =>
        decode[RefreshTokenRsp](rsp)
      case Left(e) =>
        log.debug(s"linkGame error: $e")
        e.printStackTrace()
        Left(e)
    }
  }

  def getRoomList(ip: String, port: Int, domain: String) = {
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
