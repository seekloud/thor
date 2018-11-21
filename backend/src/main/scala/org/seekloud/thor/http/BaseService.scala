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
