package org.seekloud.thor.http

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.headers.CacheDirectives.{`max-age`, public}
import akka.http.scaladsl.model.headers.`Cache-Control`
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import org.seekloud.thor.common.AppSettings

import scala.concurrent.ExecutionContextExecutor

/**
  * User: Taoz
  * Date: 11/16/2016
  * Time: 10:37 PM
  *
  * 12/09/2016:   add response compress. by zhangtao
  * 12/09/2016:   add cache support self. by zhangtao
  *
  */
trait ResourceService {

  implicit val system: ActorSystem

  implicit val executor: ExecutionContextExecutor

  implicit val materializer: Materializer

  val log: LoggingAdapter

  private val htmlResources = {
    pathPrefix("html") {
      extractUnmatchedPath { path =>
        getFromResourceDirectory("html")
      }
    }
  }

  private val resources = {
    pathPrefix("css") {
      extractUnmatchedPath { path =>
        getFromResourceDirectory("css")
      }
    } ~
    pathPrefix("js") {
      extractUnmatchedPath { path =>
        getFromResourceDirectory("js")
      }
    } ~
    pathPrefix(s"sjsout-${AppSettings.version}") {
      extractUnmatchedPath { path =>
        getFromResourceDirectory("sjsout")
      }
    } ~
    pathPrefix("img") {
      getFromResourceDirectory("img")
    }~
      pathPrefix("music") {
        getFromResourceDirectory("music")
      }
  }

  //cache code copied from zhaorui.
  private val cacheSeconds = 24 * 60 * 60

  def resourceRoutes: Route = (pathPrefix("static") & get) {
    mapResponseHeaders { headers => `Cache-Control`(`public`, `max-age`(cacheSeconds)) +: headers } {
      encodeResponse(resources)
    } ~ mapResponseHeaders { headers => `Cache-Control`(`public`, `max-age`(0)) +: headers } {
      encodeResponse(htmlResources)
    }
  }


}
