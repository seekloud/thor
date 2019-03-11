package org.seekloud.thor.utils

import org.seekloud.thor.ClientBoot.executor
import org.seekloud.thor.common.AppSettings
import org.seekloud.thor.protocol.ESheepProtocol.GetRoomListRsp
import org.seekloud.thor.shared.ptcl.SuccessRsp
import org.seekloud.thor.shared.ptcl.protocol.CommonProtocol.{GetRoom4GARsp, VerifyPsw, VerifyPswRsp}
import org.seekloud.thor.utils.SecureUtil.{PostEnvelope, generateSignatureParameters}
import org.seekloud.thor.utils.EsheepClient._
import org.slf4j.LoggerFactory

import scala.concurrent.Future


/**
  * User: TangYaruo
  * Date: 2019/3/10
  * Time: 13:52
  */
object ThorClient extends HttpUtil {

  import io.circe.generic.auto._
  import io.circe.parser.decode
  import io.circe.syntax._

  private val log = LoggerFactory.getLogger(this.getClass)

  private val baseUrl = AppSettings.baseUrl
  private val gameId = AppSettings.esheepAppId
  private val gameName = AppSettings.esheepGameName
  private val gsKey = AppSettings.esheepSecureKey


  def getRoomList: Future[Either[Throwable, GetRoom4GARsp]] = {
    val methodName = s"getRoomList"
    val url = baseUrl + "/" + gameName + "/" + "getRoomList4GA"
    val postEnvelope = genPostEnvelopeStr("", gameId.toString, gsKey)

    postJsonRequestSend(methodName, url, Nil, postEnvelope).map {
      case Right(jsonRsp) =>
        decode[GetRoom4GARsp](jsonRsp)
      case Left(e) =>
        log.debug(s"$methodName failed: $e")
        e.printStackTrace()
        Left(e)
    }
  }

  def verifyPsw(roomId: Long, psw: String): Future[Either[Throwable, VerifyPswRsp]] = {
    val methodName = "verifyPsw"
    val url = baseUrl + "/" + gameName + "/verifyPsw"
    val data = VerifyPsw(roomId, psw).asJson.noSpaces
    val appId = AppSettings.esheepAppId.toString
    val sn = appId + System.currentTimeMillis().toString
    val gsKey = AppSettings.esheepSecureKey

    val (timestamp, nonce, signature) = generateSignatureParameters(List(appId, sn, data), gsKey)
    val params = PostEnvelope(appId, sn, timestamp, nonce, data,signature).asJson.noSpaces

    postJsonRequestSend(methodName, url, Nil, params, needLogRsp = false).map {
      case Right(jsonRsp) =>
        decode[VerifyPswRsp](jsonRsp)
      case Left(e) =>
        log.debug(s"$methodName failed: $e")
        e.printStackTrace()
        Left(e)
    }



  }


}
