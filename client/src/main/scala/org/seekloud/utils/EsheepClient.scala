package org.seekloud.utils

import org.seekloud.thor.common.AppSettings
import org.seekloud.thor.model._
import org.seekloud.thor.shared.ptcl.ErrorRsp
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import org.seekloud.thor.App.executor
import com.neo.sk.utils.HttpUtil
import org.seekloud.thor.protocol.ESheepProtocol.LoginUrlRsp
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
  private val baseUrl = s"http://flowdev.neoap.com"

  def getLoginInfo: Future[Either[Throwable, LoginUrlRsp]] ={
    val methodName = "ESheepLink: getLoginInfo"
    val url = baseUrl + "/esheep/api/gameAgent/login"

    getRequestSend(methodName, url, Nil).map{
      case Right(rsp) =>
        decode[LoginUrlRsp](rsp)
      case Left(e) =>
        log.debug(s"GetLoginInfo error: $e")
        Left(e)
    }
  }


}
