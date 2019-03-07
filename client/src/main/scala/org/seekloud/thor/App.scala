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

package org.seekloud.thor


import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.actor.typed.scaladsl.adapter._
import javafx.animation.{Animation, AnimationTimer}
import javafx.application.Application
import javafx.scene.{Group, Scene}
import javafx.scene.canvas.Canvas
import javafx.stage.Stage

import concurrent.duration._
import javafx.application.Platform
import akka.actor.typed.ActorRef
import org.seekloud.thor.common.StageContext
import org.seekloud.thor.core.{LoginActor, TokenActor}
import org.seekloud.thor.protocol.ESheepProtocol.LoginUrlRsp
import org.seekloud.thor.view.LoginView
/**
  * @author Jingyi
  * @version 创建时间：2018/12/3T
  */
class  App extends Application{

  import App._

  override def start(primaryStage: Stage): Unit = {
    val context = new StageContext(primaryStage)
    val loginPage = new LoginView(context)
//    val loginPage = new ClientPage(primaryStage) // 处理扫码登录的操作
  }

}

object App{

  import concurrent.duration._
  import scala.language.postfixOps

  implicit val system = ActorSystem("thorSystem")
  // the executor should not be the default dispatcher.
  implicit val executor: MessageDispatcher =
    system.dispatchers.lookup("akka.actor.default-dispatcher")

  implicit val materializer = ActorMaterializer()

  implicit val scheduler = system.scheduler

  implicit val timeout:Timeout = Timeout(20 seconds) // for actor asks

  val log: LoggingAdapter = Logging(system, getClass)

  val tokenActor: ActorRef[TokenActor.Command] = system.spawn(TokenActor.create(), "tokenActor")

  val loginActor: ActorRef[LoginActor.Command] = system.spawn(LoginActor.init, "loginActor")

  def pushStack2AppThread(fun: => Unit) = {
    Platform.runLater(() => fun)
  }



}
