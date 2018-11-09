package com.neo.sk.thor.http

import akka.actor.{ActorSystem, Scheduler}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import com.neo.sk.thor.common.AppSettings
import akka.actor.typed.scaladsl.AskPattern._

import scala.concurrent.{ExecutionContextExecutor, Future}
import com.neo.sk.thor.Boot.{executor, scheduler, timeout}
import com.neo.sk.thor.core.UserManager


trait HttpService
  extends ResourceService
  with ServiceUtils {

  import akka.actor.typed.scaladsl.AskPattern._
  import com.neo.sk.utils.CirceSupport._
  import io.circe.generic.auto._

  implicit val system: ActorSystem

  implicit val executor: ExecutionContextExecutor

  implicit val materializer: Materializer

  implicit val timeout: Timeout

  implicit val scheduler: Scheduler


  import akka.actor.typed.scaladsl.adapter._





  lazy val routes: Route = pathPrefix(AppSettings.rootPath) {
    resourceRoutes ~
      (pathPrefix("game") & get){
        pathEndOrSingleSlash{
          getFromResource("html/admin.html")
        } ~
          path("join"){
          parameter('name){ name =>
            log.debug(s"sssssssssname=${name}")
            complete("sss")
          }
        }

      }
  }




}
