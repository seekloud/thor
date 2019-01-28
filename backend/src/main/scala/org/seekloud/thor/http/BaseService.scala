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

package org.seekloud.thor.http

import akka.actor.{ActorSystem, Scheduler}
import akka.actor.typed.ActorRef
import akka.stream.Materializer
import akka.util.Timeout
import org.seekloud.utils.CirceSupport

import scala.concurrent.ExecutionContextExecutor

/**
  * User: TangYaruo
  * Date: 2018/11/12
  * Time: 11:22
  */
trait BaseService extends CirceSupport with ServiceUtils {

  implicit val system: ActorSystem

  implicit val executor: ExecutionContextExecutor

  implicit val materializer: Materializer

  implicit val timeout: Timeout

  implicit val scheduler: Scheduler



}
