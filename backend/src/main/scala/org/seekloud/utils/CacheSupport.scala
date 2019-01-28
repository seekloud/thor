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

package org.seekloud.utils

import akka.http.scaladsl.model.headers.CacheDirectives.{`max-age`, `public`}
import akka.http.scaladsl.model.headers.`Cache-Control`
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._

/**
  * User: zhaorui
  * Date: 2016/10/24
  * Time: 13:29
  */
trait CacheSupport {
  private val cacheSeconds = 24 * 60 * 60

  val addCacheControlHeaders: Directive0 = {
    mapResponseHeaders { headers =>
      `Cache-Control`(`public`, `max-age`(cacheSeconds)) +: headers
    }
  }
}
