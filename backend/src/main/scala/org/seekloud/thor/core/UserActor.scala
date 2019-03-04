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

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer, TimerScheduler}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Flow
import akka.stream.typed.scaladsl.{ActorSink, ActorSource}
import org.seekloud.thor.shared.ptcl.protocol.ThorGame._
import org.slf4j.LoggerFactory
import org.seekloud.thor.Boot.roomManager
import org.seekloud.thor.core.game.{AdventurerServer, ThorGameConfigServerImpl, ThorSchemaServerImpl}
import org.seekloud.thor.shared.ptcl.config.ThorGameConfigImpl
import org.seekloud.byteobject.ByteObject._
import org.seekloud.byteobject.MiddleBufferInJvm
import org.seekloud.thor.shared.ptcl.component.Adventurer
import org.seekloud.thor.protocol.ReplayProtocol.{ChangeRecordMsg, GetRecordFrameMsg, GetUserInRecordMsg}
import org.seekloud.thor.shared.ptcl.ErrorRsp
import org.seekloud.thor.shared.ptcl.protocol.ThorGame
import org.seekloud.thor.shared.ptcl.protocol.ThorGame.UserMapReq

import scala.collection.mutable
import scala.language.implicitConversions
import scala.concurrent.duration._

/**
  * @author Jingyi
  * @version 创建时间：2018/11/9
  */
object UserActor {

  private val log = LoggerFactory.getLogger(this.getClass)
  private final val InitTime = Some(5.minutes)

  trait Command

  case class WsMessage(msg: Option[WsMsgFront]) extends Command

  case object CompleteMsgFront extends Command

  case class FailMsgFront(ex: Throwable) extends Command

  case class DispatchMsg(msg: WsMsgSource) extends Command

  case class DispatchMap(map: List[(Byte, (String, String))]) extends Command

  case class StartGame(roomId: Option[Long]) extends Command

  case class JoinRoom(playerId: String, name: String, userActor: ActorRef[UserActor.Command], roomIdOpt: Option[Long] = None) extends Command with RoomManager.Command

  case class LeftRoom[U](actorRef: ActorRef[U]) extends Command

  case class JoinRoomSuccess(adventurer: AdventurerServer, playerId: String, shortId: Byte, roomActor: ActorRef[RoomActor.Command], config: ThorGameConfigImpl, playerIdMap: List[(Byte, (String, String))]) extends Command

  final case class JoinRoomSuccess4Watch(watchedPlayer: Adventurer, config: ThorGameConfigImpl, roomActor: ActorRef[RoomActor.Command], gameState: GridSyncState, playerIdMap: List[(Byte, (String, String))]) extends Command

  case class JoinRoomFail4Watch(msg: String) extends Command

  case class UserFrontActor(actor: ActorRef[WsMsgSource]) extends Command

  case class ChangeUserInfo(info: UserInfo) extends Command

  final case class StartWatching(roomId: Long, watchedPlayerId: String) extends Command

  case class ChangeWatchedPlayerId(playerInfo: UserInfo, watchedPlayerId: String) extends Command with UserManager.Command


  case object ChangeBehaviorToInit extends Command

  private final case object BehaviorChangeKey

  case class TimeOut(msg: String) extends Command

  /*replay*/
  case class StartReplay(recordId: Long, playerId: String, frame: Int) extends Command


  private[this] def switchBehavior(ctx: ActorContext[Command],
    behaviorName: String, behavior: Behavior[Command], durationOpt: Option[FiniteDuration] = None, timeOut: TimeOut = TimeOut("busy time error"))
    (implicit stashBuffer: StashBuffer[Command],
      timer: TimerScheduler[Command]) = {
    log.debug(s"${ctx.self.path} becomes $behaviorName behavior.")
    timer.cancel(BehaviorChangeKey)
    durationOpt.foreach(timer.startSingleTimer(BehaviorChangeKey, timeOut, _))
    stashBuffer.unstashAll(ctx, behavior)
  }

  private def sink(actor: ActorRef[Command]) = ActorSink.actorRef[Command](
    ref = actor,
    onCompleteMessage = CompleteMsgFront,
    onFailureMessage = FailMsgFront.apply
  )

  def flow(actor: ActorRef[UserActor.Command]): Flow[WsMessage, WsMsgSource, Any] = {
    val in = Flow[WsMessage].to(sink(actor))
    val out =
      ActorSource.actorRef[WsMsgSource](
        completionMatcher = {
          case CompleteMsgServer =>
        },
        failureMatcher = {
          case FailMsgServer(e) => e
        },
        bufferSize = 256,
        overflowStrategy = OverflowStrategy.dropHead
      ).mapMaterializedValue(outActor => actor ! UserFrontActor(outActor))
    Flow.fromSinkAndSource(in, out)
  }

  def create(playerId: String, userInfo: UserInfo): Behavior[Command] = {
    Behaviors.setup[Command] { ctx =>
      log.debug(s"${ctx.self.path} is starting...")
      implicit val stashBuffer: StashBuffer[Command] = StashBuffer[Command](Int.MaxValue)
      Behaviors.withTimers[Command] { implicit timer =>
        implicit val sendBuffer: MiddleBufferInJvm = new MiddleBufferInJvm(8192)
        switchBehavior(ctx, "init", init(playerId, userInfo), InitTime, TimeOut("init"))
      }
    }
  }

  private def init(playerId: String, userInfo: UserInfo)(
    implicit stashBuffer: StashBuffer[Command],
    sendBuffer: MiddleBufferInJvm,
    timer: TimerScheduler[Command]
  ): Behavior[Command] = {
    Behaviors.receive[Command] {
      (ctx, msg) =>
        msg match {
          case UserFrontActor(frontActor) =>
            ctx.watchWith(frontActor, LeftRoom(frontActor))
            switchBehavior(ctx, "idle", idle(playerId, userInfo, System.currentTimeMillis(), frontActor))

          case ChangeUserInfo(info) =>
            init(playerId, info)

          case LeftRoom(actor) =>
            log.info("init leftRoom")
            ctx.unwatch(actor)
            Behaviors.stopped

          case msg: GetUserInRecordMsg =>
//            log.debug(s"--------------------$userInfo")
            getGameReplay(ctx, msg.recordId) ! msg
            Behaviors.same

          case ChangeBehaviorToInit =>
            Behaviors.same

          case msg: GetRecordFrameMsg =>
            getGameReplay(ctx, msg.recordId) ! msg
            Behaviors.same

          case TimeOut(m) =>
            log.debug(s"${ctx.self.path} is time out when busy,msg=${m}")
            Behaviors.stopped

          case unknown =>
            stashBuffer.stash(unknown)
            Behavior.same
        }
    }
  }

  private def idle(playerId: String, userInfo: UserInfo, startTime: Long, frontActor: ActorRef[WsMsgSource])(
    implicit stashBuffer: StashBuffer[Command],
    timer: TimerScheduler[Command],
    sendBuffer: MiddleBufferInJvm
  ): Behavior[Command] = {
    Behaviors.receive[Command] {
      (ctx, msg) =>
        msg match {
          case StartGame(roomIdOpt) =>
            roomManager ! JoinRoom(playerId, userInfo.name, ctx.self, roomIdOpt)
            Behaviors.same

          case ChangeUserInfo(info) =>
            idle(playerId, info, startTime, frontActor)


          case msg@StartReplay(rid, uid, f) =>
//            log.info(s"UserActor [$playerId] get msg $msg.")
            getGameReplay(ctx, rid) ! GameReplay.InitReplay(frontActor, uid, f)
            switchBehavior(ctx, "replay", replay(uid, rid, userInfo, startTime, frontActor))

          case JoinRoomSuccess(adventurer, playerId, shortId, roomActor, config, playerIdMap) =>
//            log.debug(s"$playerId join room success")
            val ws = YourInfo(config, playerId, userInfo.name, shortId, playerIdMap).asInstanceOf[WsMsgServer].fillMiddleBuffer(sendBuffer).result()
            frontActor ! Wrap(ws)
            switchBehavior(ctx, "play", play(playerId, userInfo, adventurer, startTime, frontActor, roomActor))

          case StartWatching(roomId, watchedUserId) =>
//            log.debug(s"start watching $watchedUserId")
            roomManager ! RoomActor.JoinRoom4Watch(playerId, roomId, watchedUserId, ctx.self)
            switchBehavior(ctx, "watchInit", watchInit(playerId, userInfo, roomId, watchedUserId, frontActor))

          case LeftRoom(actor) =>
            ctx.unwatch(actor)
            switchBehavior(ctx, "init", init(playerId, userInfo), InitTime, TimeOut("init"))

          case WsMessage(reqOpt) =>
            reqOpt match {
              //TODO 此处reStart未修改，因为用途不明
              case Some(RestartGame) =>
//                log.debug(s"restart")
                roomManager ! JoinRoom(userInfo.playerId, userInfo.name, ctx.self)
                idle(userInfo.playerId, userInfo.copy(name = userInfo.name), startTime, frontActor)

              case _ =>
                Behaviors.same
            }

          case ChangeBehaviorToInit =>
            dispatchTo(frontActor, RebuildWebSocket)
            ctx.unwatch(frontActor)
            switchBehavior(ctx, "init", init(playerId, userInfo), InitTime, TimeOut("init"))

          case DispatchMsg(m) =>
            println(s"Dispatch")
//            import scala.language.implicitConversions
//            import org.seekloud.byteobject.ByteObject._
//            import org.seekloud.byteobject.MiddleBufferInJvm
//
//            val buffer = new MiddleBufferInJvm(m.asInstanceOf[Wrap].ws)
//            bytesDecode[PingPackage](buffer) match {
//              case Right(req) => log.debug(s"$req")
//              case Left(e) =>
//                log.error(s"decode binaryMessage failed,error:${e.message}")
//            }

            Behaviors.same

          case unknownMsg =>
            log.warn(s"unknown msg: $unknownMsg")
            Behavior.unhandled
        }
    }
  }

  private def replay(uId: String,
    recordId: Long,
    userInfo: UserInfo,
    startTime: Long,
    frontActor: ActorRef[ThorGame.WsMsgSource])(
    implicit stashBuffer: StashBuffer[Command],
    timer: TimerScheduler[Command],
    sendBuffer: MiddleBufferInJvm
  ): Behavior[Command] =
    Behaviors.receive[Command] { (ctx, msg) =>
      msg match {
        /**
          * 本消息内转换为初始状态并给前端发送异地登录消息*/
        case ChangeBehaviorToInit =>
          dispatchTo(frontActor, ThorGame.RebuildWebSocket)
          ctx.unwatch(frontActor)
          switchBehavior(ctx, "init", init(uId, userInfo), InitTime, TimeOut("init"))

        case ChangeUserInfo(info) =>
          replay(uId, recordId, info, startTime, frontActor)

        case LeftRoom(actor) =>
          ctx.unwatch(actor)
          switchBehavior(ctx, "init", init(uId, userInfo), InitTime, TimeOut("init"))

        case msg: GetUserInRecordMsg =>
//          log.debug(s"${ctx.self.path} receives a msg=$msg")
          if (msg.recordId != recordId) {
            msg.replyTo ! ErrorRsp(10002, "you are watching the other record")
          } else {
            getGameReplay(ctx, msg.recordId) ! msg
          }

          Behaviors.same

        case msg: ChangeRecordMsg =>
          ctx.self ! UserActor.StartReplay(msg.rid, msg.playerId, msg.f)
          switchBehavior(ctx, "idle", idle(uId, userInfo, startTime, frontActor))

        case msg: GetRecordFrameMsg =>
//          log.debug(s"${ctx.self.path} receives a msg=$msg")
          if (msg.recordId != recordId) {
            msg.replyTo ! ErrorRsp(10002, "you are watching the other record")
          } else {
            getGameReplay(ctx, msg.recordId) ! msg
          }
          Behaviors.same

        case unknown =>
          log.warn(s"unknown msg: $unknown")
          Behavior.same
      }
    }

  private def watchInit(playerId: String, userInfo: UserInfo, roomId: Long, watchedPlayerId: String, frontActor: ActorRef[WsMsgSource])(
    implicit stashBuffer: StashBuffer[Command],
    timer: TimerScheduler[Command],
    sendBuffer: MiddleBufferInJvm
  ): Behavior[Command] =
    Behaviors.receive[Command] { (ctx, msg) =>
      msg match {
        case ChangeUserInfo(info) =>
          watchInit(playerId, info, roomId, watchedPlayerId, frontActor)

        case JoinRoomSuccess4Watch(watchedPlayer, config, roomActor, state, pMap) =>
//          log.debug(s"$playerId join room 4 watch success")
          frontActor ! Wrap(YourInfo(config, watchedPlayer.playerId, watchedPlayer.name, watchedPlayer.byteId, pMap).asInstanceOf[WsMsgServer].fillMiddleBuffer(sendBuffer).result())
          frontActor ! Wrap(state.asInstanceOf[WsMsgServer].fillMiddleBuffer(sendBuffer).result())
          switchBehavior(ctx, "watch", watch(playerId, userInfo, roomId, watchedPlayer.playerId, frontActor, roomActor))

        case JoinRoomFail4Watch(error) =>
          log.debug(s"join room 4 watch failed $msg")
          frontActor ! Wrap(WsMsgErrorRsp(1, error).asInstanceOf[WsMsgServer].fillMiddleBuffer(sendBuffer).result())
//          frontActor ! CompleteMsgServer
          switchBehavior(ctx, "init", init(playerId, userInfo), InitTime, TimeOut("init"))

        case DispatchMsg(m) =>
          frontActor ! m
          Behaviors.same

        case ChangeBehaviorToInit =>
          frontActor ! Wrap(RebuildWebSocket.asInstanceOf[WsMsgServer].fillMiddleBuffer(sendBuffer).result())
          ctx.unwatch(frontActor)
          switchBehavior(ctx, "init", init(playerId, userInfo), InitTime, TimeOut("init"))

        case LeftRoom(actor) =>
          ctx.unwatch(actor)
          switchBehavior(ctx, "init", init(playerId, userInfo), InitTime, TimeOut("init"))

        case unknown =>
          stashBuffer.stash(unknown)
          Behavior.same
      }

    }

  private def watch(
    playerId: String,
    userInfo: UserInfo,
    roomId: Long,
    watchedPlayerId: String,
    frontActor: ActorRef[WsMsgSource],
    roomActor: ActorRef[RoomActor.Command])(
    implicit stashBuffer: StashBuffer[Command],
    timer: TimerScheduler[Command],
    sendBuffer: MiddleBufferInJvm
  ): Behavior[Command] =
    Behaviors.receive[Command] { (ctx, msg) =>
      msg match {
        case ChangeUserInfo(info) =>
          watch(playerId, info, roomId, watchedPlayerId, frontActor, roomActor)

        case DispatchMsg(m) =>
          if (m.asInstanceOf[Wrap].isKillMsg && m.asInstanceOf[Wrap].deadId == playerId) {
            frontActor ! m
            switchBehavior(ctx, "watchInit", watchInit(playerId, userInfo, roomId, watchedPlayerId, frontActor))
          } else {
            frontActor ! m
            Behaviors.same
          }

        case WsMessage(reqOpt) =>
          reqOpt match {
            case Some(t: UserActionEvent) =>
              roomActor ! RoomActor.WsMessage(playerId, t)
            case Some(t: PingPackage) =>
              frontActor ! Wrap(t.asInstanceOf[WsMsgServer].fillMiddleBuffer(sendBuffer).result())
            case _ =>
          }
          Behaviors.same

        case msg: ChangeWatchedPlayerId =>
          ctx.self ! StartWatching(roomId, msg.watchedPlayerId)
          switchBehavior(ctx, "idle", idle(playerId, userInfo, System.currentTimeMillis(), frontActor))

        case ChangeBehaviorToInit =>
          frontActor ! Wrap(RebuildWebSocket.asInstanceOf[WsMsgServer].fillMiddleBuffer(sendBuffer).result())
          roomActor ! RoomActor.LeftRoom4Watch(playerId, watchedPlayerId)
          ctx.unwatch(frontActor)
          switchBehavior(ctx, "init", init(playerId, userInfo), InitTime, TimeOut("init"))

        case LeftRoom(actor) =>
          ctx.unwatch(actor)
          roomActor ! RoomActor.LeftRoom4Watch(playerId, watchedPlayerId)
          Behaviors.stopped

        case JoinRoomSuccess4Watch(watchedPlayer, config, roomActor, state, pMap) =>
          val ws = RestartYourInfo.asInstanceOf[WsMsgServer].fillMiddleBuffer(sendBuffer).result()
          frontActor ! Wrap(ws)
          Behaviors.same

        case unknownMsg =>
          log.warn(s"${ctx.self.path} receive an unknown msg=$unknownMsg")
          Behavior.same

      }

    }

  private def play(
    playerId: String,
    userInfo: UserInfo,
    adventurer: AdventurerServer,
    startTime: Long,
    frontActor: ActorRef[WsMsgSource],
    roomActor: ActorRef[RoomActor.Command])(
    implicit stashBuffer: StashBuffer[Command],
    timer: TimerScheduler[Command],
    sendBuffer: MiddleBufferInJvm
  ): Behavior[Command] = {
    Behaviors.receive[Command] {
      (ctx, msg) =>
        msg match {
          case WsMessage(m) =>
            m match {
              case Some(event: UserActionEvent) =>
                roomActor ! RoomActor.WsMessage(playerId, event)
              case Some(RestartGame) =>
//                log.debug(s"restartGame ${userInfo.name}")
//                JoinRoom(userInfo.playerId, userInfo.name, ctx.self)
                roomManager ! RoomManager.reStartJoinRoom(userInfo.playerId, userInfo.name, ctx.self)
              case Some(UserMapReq) =>
                roomActor ! RoomActor.UserMap(ctx.self)
              case Some(event: PingPackage) =>
                frontActor ! Wrap(event.asInstanceOf[WsMsgServer].fillMiddleBuffer(sendBuffer).result())
              case _ =>
            }
            Behaviors.same

          case DispatchMsg(m) =>
//            println(s"DispatchMsg")
            if (m.asInstanceOf[Wrap].isKillMsg && m.asInstanceOf[Wrap].deadId == playerId) { //玩家死亡
//              log.debug(s"deadmsg $m")
              frontActor ! m
              roomManager ! RoomManager.BeDead(playerId, userInfo.name)
//              roomManager ! RoomManager.BeDead(playerId, userInfo.name)
//              switchBehavior(ctx, "idle", idle(playerId, userInfo, startTime, frontActor))
              Behaviors.same
            } else {
              frontActor ! m
              Behaviors.same
            }

          case DispatchMap(map) =>
            val msg = Wrap(UserMap(map).asInstanceOf[WsMsgServer].fillMiddleBuffer(sendBuffer).result())
            frontActor ! msg
            Behaviors.same

          case LeftRoom(actor) =>
            ctx.unwatch(actor)
            roomManager ! RoomManager.LeftRoom(playerId, userInfo.name)
            Behaviors.stopped

          case ChangeBehaviorToInit =>
            frontActor ! Wrap(RebuildWebSocket.asInstanceOf[WsMsgServer].fillMiddleBuffer(sendBuffer).result())
            roomManager ! RoomManager.LeftRoom(playerId, userInfo.name)
            ctx.unwatch(frontActor)
            switchBehavior(ctx, "init", init(playerId, userInfo), InitTime, TimeOut("init"))

          case JoinRoomSuccess(adventurer, playerId, shortId, roomActor, config, playerIdMap) =>
//            log.debug(s"$playerId join room success")
//            val ws = YourInfo(config, playerId, userInfo.name, shortId, playerIdMap).asInstanceOf[WsMsgServer].fillMiddleBuffer(sendBuffer).result()
            val ws = RestartYourInfo.asInstanceOf[WsMsgServer].fillMiddleBuffer(sendBuffer).result()
            frontActor ! Wrap(ws)
            Behaviors.same

          case unknownMsg =>
            log.debug(s"unknown msg: $unknownMsg")
            Behavior.same
        }
    }
  }

  import org.seekloud.byteobject.ByteObject._

  private def dispatchTo(subscriber: ActorRef[WsMsgSource], msg: WsMsgServer)(implicit sendBuffer: MiddleBufferInJvm) = {
    subscriber ! WsData(List(msg).fillMiddleBuffer(sendBuffer).result())
  }

  private def getGameReplay(ctx: ActorContext[Command], recordId: Long): ActorRef[GameReplay.Command] = {
    val childName = s"gameReplay--$recordId"
    ctx.child(childName).getOrElse {
      val actor = ctx.spawn(GameReplay.create(recordId), childName)
      actor
    }.upcast[GameReplay.Command]
  }


}