package org.seekloud.utils

import org.seekloud.thor.protocol.ESheepProtocol._
import org.slf4j.{Logger, LoggerFactory}
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.parser._

import scala.concurrent.Future
import org.seekloud.thor.Boot.executor
/**
  * User: XuSiRan
  * Date: 2018/11/22
  * Time: 14:16
  */
object ESheepClient extends HttpUtil {

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  val thorId = 1000000006l // TODO 写入配置文件？
  val thorKey = "Ta5DFWhVhJY4F7gRTWzsGufcaOED478v"// TODO 写入配置文件？

  def gsKey2Token(): Future[Either[Throwable, GetTokenByGsKeyRsp]] ={

    val methodName = "test_name_gsKey2Token"
    val url = "http://10.1.29.250:30374/esheep/api/gameServer/gsKey2Token" //FIXME 修改url
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
    val url = s"http://10.1.29.250:30374/esheep/api/gameServer/verifyAccessCode?token=$token"//FIXME 修改url
    val jsonStr = VerifyAccessCodeReq(thorId, accessCode).asJson.noSpaces

    postJsonRequestSend(methodName, url, Nil, jsonStr).map{
      case Right(jsonRsp) =>
        decode[GetPlayerByAccessCodeRsp](jsonRsp)
      case Left(e) =>
        log.debug(s"verifyAccessCodeError: $e")
        Left(e)
    }
  }
}
