package org.seekloud.thor.http

import java.net.URLEncoder

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive, RequestContext, Route}
import org.seekloud.thor.common.{AppSettings}
import org.seekloud.thor.protocol.CommonErrorCode._
import org.seekloud.thor.shared.ptcl.ErrorRsp
import org.seekloud.utils.CirceSupport
import org.seekloud.utils.SecureUtil._
import org.seekloud.thor.http.SessionBase.SessionCombine

import org.seekloud.thor.protocol.CommonErrorCode
import io.circe.{Decoder, Error}
import io.circe.parser.decode
import org.slf4j.LoggerFactory
import org.seekloud.thor.Boot.executor
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * User: Taoz
  * Date: 11/18/2016
  * Time: 7:57 PM
  */

object ServiceUtils{
  private val log = LoggerFactory.getLogger(this.getClass)
  private def getSecureKey(appId: String) = AppSettings.appSecureMap.get(appId)
  private val authCheck = AppSettings.authCheck
}

trait ServiceUtils extends CirceSupport with SessionBase{

  import ServiceUtils._
  import io.circe.generic.auto._



  def htmlResponse(html: String): HttpResponse = {
    HttpResponse(entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, html))
  }

  def jsonResponse(json: String): HttpResponse = {
    HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, json))
  }

  def dealFutureResult(future: => Future[server.Route]): Route = {
    onComplete(future) {
      case Success(rst) => rst
      case Failure(e) =>
        e.printStackTrace()
        log.error("internal error: {}", e.getMessage)
        complete(ErrorRsp(1000, "internal error."))
    }
  }

  def loggingAction: Directive[Tuple1[RequestContext]] = extractRequestContext.map { ctx =>
    log.info(s"Access uri: ${ctx.request.uri} from ip ${ctx.request.uri.authority.host.address}.")
    ctx
  }

  def ensureAuth(
                  appClientId: String,
                  timestamp: String,
                  nonce: String,
                  sn: String,
                  data: List[String],
                  signature: String
                )(f: => Future[server.Route]): server.Route = {
    val p = getSecureKey(appClientId) match {
      case Some(secureKey) =>
        val paramList = List(appClientId.toString, timestamp, nonce, sn) ::: data
        if (timestamp.toLong + 120000 < System.currentTimeMillis()) {
          Future.successful(complete(requestTimeOut))
        } else if (checkSignature(paramList, signature, secureKey)) {
          f
        } else {
          Future.successful(complete(signatureError))
        }
      case None =>
        Future.successful(complete(appIdInvalid))
    }
    dealFutureResult(p)
  }

  def ensurePostEnvelope(e: PostEnvelope)(f: => Future[server.Route]) = {
    ensureAuth(e.appId, e.timestamp, e.nonce, e.sn, List(e.data), e.signature)(f)
  }

  private def getSecureKey(appId: String) = AppSettings.appSecureMap.get(appId)

  def dealPostReq[A](f: A => Future[server.Route])(implicit decoder: Decoder[A]): server.Route = {
    entity(as[Either[Error, PostEnvelope]]) {
      case Right(envelope) =>
        if(authCheck) {
          ensurePostEnvelope(envelope) {
            decode[A](envelope.data) match {
              case Right(req) =>
                f(req)

              case Left(e) =>
                log.error(s"json parse detail type,data=${envelope.data} error: $e")
                Future.successful(complete(parseJsonError))
            }
          }
        } else {
          dealFutureResult {
            decode[A](envelope.data) match {
              case Right(req) =>
                f(req)

              case Left(e) =>
                log.error(s"json parse detail type,data=${envelope.data} error: $e")
                Future.successful(complete(parseJsonError))
            }
          }
        }

      case Left(e) =>
        log.error(s"json parse PostEnvelope error: $e")
        complete(parseJsonError)
    }
  }

  def dealGetReq(f: => Future[server.Route]): server.Route = {
    entity(as[Either[Error, PostEnvelope]]) {
      case Right(envelope) =>
        if (authCheck) {
          ensurePostEnvelope(envelope) {
            f
          }
        } else {
          dealFutureResult {
            log.debug("authCheck is false")
            f
//            log.error(s"json parse detail type error")
//            Future.successful(complete(parseJsonError))
          }
        }

      case Left(e) =>
        log.error(s"json parse PostEnvelope error: $e")
        complete(parseJsonError)
    }
  }

  def wxRedirectUrl:String = {
    val domainUrl = AppSettings.baseUrl
    val callbackUrl = URLEncoder.encode(s"$domainUrl/${AppSettings.rootPath}/wxaccount/wxCallback", "UTF-8")
    val redirectUrl = s"https://open.weixin.qq.com/connect/oauth2/authorize?" +
      s"appid=${AppSettings.MpAuthorConfig.mpAppId}" +
      s"&redirect_uri=$callbackUrl" +
      "&response_type=code" +
      "&scope=snsapi_userinfo" +
      s"&component_appid=${AppSettings.MpAuthorConfig.componentAppId}#wechat_redirect"
    redirectUrl
  }


  def authUser(f: SessionCombine => server.Route) = loggingAction { ctx =>
    optionalUserSession {
      case Some(usersession) =>
        f(usersession)
      case None =>
        println("authuser  wxredirect+++++ 1000202")
        complete(CommonErrorCode.noSessionError(wxRedirectUrl))
    }
  }







}
