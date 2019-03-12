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

package org.seekloud.thor.core

import java.util.concurrent.atomic.AtomicLong

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, TimerScheduler}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.{ActorAttributes, Supervision}
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import org.seekloud.thor.core.UserActor.{ChangeUserInfo, ChangeWatchedPlayerId}
import org.seekloud.thor.protocol.ReplayProtocol._
import org.seekloud.thor.shared.ptcl.protocol.ThorGame
import org.seekloud.thor.shared.ptcl.protocol.ThorGame._
import org.seekloud.utils.BwClient._
import org.slf4j.LoggerFactory

import scala.collection.mutable

/**
  * @author Jingyi
  * @version 创建时间：2018/11/9
  */
object UserManager {

  private val log = LoggerFactory.getLogger(this.getClass)

  trait Command

  final case class ChildDead[U](name: String, childRef: ActorRef[U]) extends Command

  final case class GetWebSocketFlow(id: String, name: String, replyTo: ActorRef[Flow[Message, Message, Any]], roomId: Option[Long] = None) extends Command

  final case class GetWebSocketFlow4GA(playerId: String, name: String, replyTo: ActorRef[Flow[Message, Message, Any]]) extends Command

  final case class GetWebSocketFlow4Watch(roomId: Long, watchedPlayerId: String, replyTo: ActorRef[Flow[Message, Message, Any]], watchingId: String, name: String) extends Command

  final case class GetWebSocketFlow4Replay(recordId: Long, frame: Long, watchedPlayerId: String, replyTo: ActorRef[Flow[Message, Message, Any]], watchingId: String, name: String) extends Command

  def create(): Behavior[Command] = {
    log.debug(s"UserManager start...")
    Behaviors.setup[Command] {
      ctx =>
        Behaviors.withTimers[Command] {
          implicit timer =>
            val uidGenerator = new AtomicLong(1L)
            idle(uidGenerator)
        }
    }
  }

  private def idle(uidGenerator: AtomicLong)
    (
      implicit timer: TimerScheduler[Command]
    ): Behavior[Command] = {
    Behaviors.receive[Command] {
      (ctx, msg) =>
        msg match {
          case GetWebSocketFlow(id, name, replyTo, roomIdOpt) =>

            //              val playerInfo = UserInfo(uidGenerator.getAndIncrement().toString, name)
            val playerInfo = UserInfo(if (id.equals("test")) uidGenerator.getAndIncrement().toString else id, name)

            getUserActorOpt(ctx, id) match {
              case Some(actor) =>
                actor ! UserActor.ChangeBehaviorToInit
              case None =>
            }
            val userActor = getUserActor(ctx, playerInfo.playerId, playerInfo)
            replyTo ! getWebSocketFlow(userActor)
            userActor ! UserActor.StartGame(roomIdOpt)
            Behaviors.same

          case msg: GetWebSocketFlow4GA =>
            log.debug(s"get msg: GetWebSocketFlow4GA-${msg.playerId}")
            getUserActorOpt(ctx, msg.playerId).foreach(_ ! UserActor.ChangeBehaviorToInit)
            val userActor = getUserActor(ctx, msg.playerId, UserInfo(msg.playerId, msg.name))
            msg.replyTo ! getWebSocketFlow(userActor)
            Behaviors.same

          case GetWebSocketFlow4Watch(roomId, watchedPlayerId, replyTo, watchingId, name) =>
//            log.debug(s"$watchingId GetWebSocketFlow4Watch")
            val playerInfo = UserInfo(watchingId, name)
            getUserActorOpt(ctx, watchingId) match {
              case Some(actor) =>
                actor ! UserActor.ChangeBehaviorToInit
              case None =>
            }
            val userActor = getUserActor(ctx, watchingId, playerInfo)
            replyTo ! getWebSocketFlow(userActor)
            userActor ! ChangeUserInfo(playerInfo)
            //发送用户观战命令
            userActor ! UserActor.StartWatching(roomId, watchedPlayerId)
            Behaviors.same

          case GetWebSocketFlow4Replay(recordId, frame, watchedPlayerId, replyTo, watchingId, name) =>
//            log.debug(s"$watchingId GetWebSocketFlow4Replay")
            val playerInfo = UserInfo(watchingId, name)
            getUserActorOpt(ctx, watchingId) match {
              case Some(actor) =>
                actor ! UserActor.ChangeBehaviorToInit
              case None =>
            }
            val userActor = getUserActor(ctx, watchingId, playerInfo)
            replyTo ! getWebSocketFlow(userActor)
            userActor ! ChangeUserInfo(playerInfo)
            //发送用户看录像命令
            userActor ! UserActor.StartReplay(recordId, watchedPlayerId, frame.toInt)
            Behaviors.same

          case msg: ChangeWatchedPlayerId =>
            getUserActor(ctx, msg.playerInfo.playerId, msg.playerInfo) ! msg
            Behaviors.same

          case msg: GetUserInRecordMsg =>
            getUserActor(ctx, msg.watchId, UserInfo(msg.watchId, msg.watchId)) ! msg
            Behaviors.same

          case msg: GetRecordFrameMsg =>
            getUserActor(ctx, msg.watchId, UserInfo(msg.watchId, msg.watchId)) ! msg
            Behaviors.same

          case ChildDead(child, childRef) =>
            log.debug(s"userManager 不再监管user:$child,$childRef")
            ctx.unwatch(childRef)
            Behaviors.same

          case unknown =>
            log.error(s"${ctx.self.path} recv an unknown msg when idle:$unknown")
            Behaviors.same
        }
    }
  }

  /*----------------------------带宽统计----------------------------*/

  var timer = System.currentTimeMillis()
  val period = 30 * 1000

  private def getWebSocketFlow(userActor: ActorRef[UserActor.Command]): Flow[Message, Message, Any] = {
    import scala.language.implicitConversions
    import org.seekloud.byteobject.ByteObject._
    import org.seekloud.byteobject.MiddleBufferInJvm

    implicit def parseJsonString2WsMsgFront(s: String): Option[WsMsgFront] = {
      import io.circe.generic.auto._
      import io.circe.parser._

      try {
        val wsMsg = decode[WsMsgFront](s).right.get
        Some(wsMsg)
      } catch {
        case e: Exception =>
          log.warn(s"parse front msg failed when json parse,s=$s")
          None
      }
    }

    Flow[Message]
      .collect {
        case TextMessage.Strict(m) =>
          UserActor.WsMessage(m)

        case BinaryMessage.Strict(m) =>
          val buffer = new MiddleBufferInJvm(m.asByteBuffer)
          bytesDecode[WsMsgFront](buffer) match {
            case Right(req) =>
              val sendBuffer = new MiddleBufferInJvm(409600)
              val msg = req.fillMiddleBuffer(sendBuffer).result()
              req match {
                case _: MM =>
                  uploadStatistics.update("MM", uploadStatistics("MM") + msg.length.toDouble / 1024)
                  uploadStatistics.update("MM_num", uploadStatistics("MM_num") + 1)
                case _: MouseClickDownLeft =>
                  uploadStatistics.update("MouseClickDownLeft", uploadStatistics("MouseClickDownLeft") + msg.length.toDouble / 1024)
                  uploadStatistics.update("MouseClickDownLeft_num", uploadStatistics("MouseClickDownLeft_num") + 1)
                case _: MouseClickUpRight =>
                  uploadStatistics.update("MouseClickUpRight", uploadStatistics("MouseClickUpRight") + msg.length.toDouble / 1024)
                  uploadStatistics.update("MouseClickUpRight_num", uploadStatistics("MouseClickUpRight_num") + 1)
                case _: MouseClickDownRight =>
                  uploadStatistics.update("MouseClickDownRight", uploadStatistics("MouseClickDownRight") + msg.length.toDouble / 1024)
                  uploadStatistics.update("MouseClickDownRight_num", uploadStatistics("MouseClickDownRight_num") + 1)

                case _: PingPackage =>
                  uploadStatistics.update("PingPackage", uploadStatistics("PingPackage") + msg.length.toDouble / 1024)
                  uploadStatistics.update("PingPackage_num", uploadStatistics("PingPackage_num") + 1)

                case RestartGame =>
                  uploadStatistics.update("RestartGame", uploadStatistics("RestartGame") + msg.length.toDouble / 1024)
                  uploadStatistics.update("RestartGame_num", uploadStatistics("RestartGame_num") + 1)

                case _ =>
                  uploadStatistics.update("others", uploadStatistics("others") + msg.length.toDouble / 1024)

              }
              UserActor.WsMessage(Some(req))
            case Left(e) =>
              log.error(s"decode binaryMessage failed,error:${e.message}")
              UserActor.WsMessage(None)
          }
      }.via(UserActor.flow(userActor))
      .map {
        case wrap: Wrap =>
          val s = ByteString(wrap.ws)
          val buffer = new MiddleBufferInJvm(s.asByteBuffer)
          bytesDecode[WsMsgServer](buffer) match {
            case Right(req) =>
              val sendBuffer = new MiddleBufferInJvm(409600)
              val msg = req.fillMiddleBuffer(sendBuffer).result()
              req match {
                case _: GridSyncState =>
                  downloadStatistics.update("GridSyncState", downloadStatistics("GridSyncState") + msg.length.toDouble / 1024)
                  downloadStatistics.update("GridSyncState_num", downloadStatistics("GridSyncState_num") + 1)
                case _: YourInfo =>
                  downloadStatistics.update("YourInfo", downloadStatistics("YourInfo") + msg.length.toDouble / 1024)
                  downloadStatistics.update("YourInfo_num", downloadStatistics("YourInfo_num") + 1)
                case _: UserEnterRoom =>
                  downloadStatistics.update("UserEnterRoom", downloadStatistics("UserEnterRoom") + msg.length.toDouble / 1024)
                  downloadStatistics.update("UserEnterRoom_num", downloadStatistics("UserEnterRoom_num") + 1)
                case _: UserLeftRoom =>
                  downloadStatistics.update("UserLeftRoom", downloadStatistics("UserLeftRoom") + msg.length.toDouble / 1024)
                  downloadStatistics.update("UserLeftRoom_num", downloadStatistics("UserLeftRoom_num") + 1)
                case _: BeAttacked =>
                  downloadStatistics.update("BeAttacked", downloadStatistics("BeAttacked") + msg.length.toDouble / 1024)
                  downloadStatistics.update("BeAttacked_num", downloadStatistics("BeAttacked_num") + 1)
                case _: EatFood =>
                  downloadStatistics.update("EatFood", downloadStatistics("EatFood") + msg.length.toDouble / 1024)
                  downloadStatistics.update("EatFood_num", downloadStatistics("EatFood_num") + 1)
                case _: MM =>
                  downloadStatistics.update("MM", downloadStatistics("MM") + msg.length.toDouble / 1024)
                  downloadStatistics.update("MM_num", downloadStatistics("MM_num") + 1)
//                  log.debug(s"MM: ${downloadStatistics("MM_num")}")

                case _: MouseClickDownLeft =>
                  downloadStatistics.update("MouseClickDownLeft", downloadStatistics("MouseClickDownLeft") + msg.length.toDouble / 1024)
                  downloadStatistics.update("MouseClickDownLeft_num", downloadStatistics("MouseClickDownLeft_num") + 1)
//                  log.debug(s"MouseClickDownLeft: ${downloadStatistics("MouseClickDownLeft_num")}")

                case _: MouseClickUpRight =>
                  downloadStatistics.update("MouseClickUpRight", downloadStatistics("MouseClickUpRight") + msg.length.toDouble / 1024)
                  downloadStatistics.update("MouseClickUpRight_num", downloadStatistics("MouseClickUpRight_num") + 1)
                case _: MouseClickDownRight =>
                  downloadStatistics.update("MouseClickDownRight", downloadStatistics("MouseClickDownRight") + msg.length.toDouble / 1024)
                  downloadStatistics.update("MouseClickDownRight", downloadStatistics("MouseClickDownRight") + 1)
                case _: Ranks =>
                  downloadStatistics.update("Ranks", downloadStatistics("Ranks") + msg.length.toDouble / 1024)
                  downloadStatistics.update("Ranks_num", downloadStatistics("Ranks_num") + 1)
                case _: PingPackage =>
                  downloadStatistics.update("PingPackage", downloadStatistics("PingPackage") + msg.length.toDouble / 1024)
                  downloadStatistics.update("PingPackage_num", downloadStatistics("PingPackage_num") + 1)
                case _: UserMap =>
                  downloadStatistics.update("UserMap", downloadStatistics("UserMap") + msg.length.toDouble / 1024)
                  downloadStatistics.update("UserMap_num", downloadStatistics("UserMap_num") + 1)
                case _: GenerateFood =>
                  downloadStatistics.update("GenerateFood", downloadStatistics("GenerateFood") + msg.length.toDouble / 1024)
                  downloadStatistics.update("GenerateFood_num", downloadStatistics("GenerateFood_num") + 1)
                case RestartYourInfo =>
                  downloadStatistics.update("RestartYourInfo", downloadStatistics("RestartYourInfo") + msg.length.toDouble / 1024)
                  downloadStatistics.update("RestartYourInfo_num", downloadStatistics("RestartYourInfo_num") + 1)
                case x =>
                  downloadStatistics.update("others", downloadStatistics("others") + msg.length.toDouble / 1024)

              }
//              if (System.currentTimeMillis() - timer > period) {
//                timer = System.currentTimeMillis()
//                log.info(showStatistics)
//                log.debug(s"showtime: ${System.currentTimeMillis() - timer}")
//              }

            case Left(e) =>
          }
          BinaryMessage.Strict(ByteString(wrap.ws))

        case t: ThorGame.ReplayFrameData =>
          BinaryMessage.Strict(ByteString(t.ws))

        case x =>
          log.debug(s"akka stream receive unknown msg=$x")
          TextMessage.apply("")
      }.withAttributes(ActorAttributes.supervisionStrategy(decider))
  }

  private val decider: Supervision.Decider = {
    e: Throwable =>
      e.printStackTrace()
      log.error(s"WS stream failed with $e")
      Supervision.Resume
  }

  private def getUserActor(ctx: ActorContext[Command], id: String, userInfo: UserInfo): ActorRef[UserActor.Command] = {
    val childName = s"UserActor-$id"
    ctx.child(childName).getOrElse {
      val actor = ctx.spawn(UserActor.create(id, userInfo), childName)
      ctx.watchWith(actor, ChildDead(childName, actor))
      actor
    }.upcast[UserActor.Command]
  }

  private def getUserActorOpt(ctx: ActorContext[Command], id: String): Option[ActorRef[UserActor.Command]] = {
    val childName = s"UserActor-$id"
    ctx.child(childName).map(_.upcast[UserActor.Command])
  }

}
