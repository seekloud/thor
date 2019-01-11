package org.seekloud.utils

import org.seekloud.thor.protocol.ESheepProtocol._
import org.slf4j.{Logger, LoggerFactory}
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.parser._

import scala.concurrent.Future
import org.seekloud.thor.Boot.executor
import org.seekloud.thor.common.AppSettings
/**
  * User: XuSiRan
  * Date: 2018/11/22
  * Time: 14:16
  */
object ESheepClient extends HttpUtil {

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  private val baseUrl = AppSettings.esheepProtocol + "://" + AppSettings.esheepHost + ":" + AppSettings.esheepPort + "/" + AppSettings.esheepUrl
  private val thorId = AppSettings.esheepAppId
  private val thorKey = AppSettings.esheepSecureKey

  def gsKey2Token(): Future[Either[Throwable, GetTokenByGsKeyRsp]] ={

    val methodName = "test_name_gsKey2Token"
    val url = baseUrl + "/api/gameServer/gsKey2Token"
    val jsonStr = GsKey2TokenReq(thorId, thorKey).asJson.noSpaces

    postJsonRequestSend(methodName, url, Nil, jsonStr).map{
      case Right(jsonRsp) =>
        decode[GetTokenByGsKeyRsp](jsonRsp)
      case Left(e) =>
        log.debug(s"getTokenError: $e")
        Left(e)
    }
  }

  def verifyAccessCode(accessCode: String, token: String): Future[Either[Throwable, GetPlayerByAccessCodeRsp]] ={
    val methodName = "test_name_verifyAccessCode"
    val url = baseUrl + s"/api/gameServer/verifyAccessCode?token=$token"
    val jsonStr = VerifyAccessCodeReq(thorId, accessCode).asJson.noSpaces

    postJsonRequestSend(methodName, url, Nil, jsonStr).map{
      case Right(jsonRsp) =>
        decode[GetPlayerByAccessCodeRsp](jsonRsp)
      case Left(e) =>
        log.debug(s"verifyAccessCodeError: $e")
        Left(e)
    }
  }

  def addRecord2ESheep(record: ESheepRecord, token: String): Future[Either[Throwable, EsheepCommonRsp]] ={
    val methodName = "test_name_addRecord2ESheep"
    val url = baseUrl + s"/api/gameServer/addPlayerRecord?token=$token"
    val jsonStr = AddESheepRecordRsp(record).asJson.noSpaces

    postJsonRequestSend(methodName, url, Nil, jsonStr).map{
      case Right(jsonRsp) =>
        decode[EsheepCommonRsp](jsonRsp)
      case Left(e) =>
        log.debug(s"addRecord2ESheepError: $e")
        Left(e)
    }
  }
}
