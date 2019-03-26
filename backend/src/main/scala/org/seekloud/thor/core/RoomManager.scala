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
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer, TimerScheduler}
import org.seekloud.thor.common.AppSettings
import org.seekloud.thor.core.UserActor.{CreateRoom, JoinRoom, JoinRoomFail}
import org.slf4j.LoggerFactory
import org.seekloud.thor.common.AppSettings.personLimit
import org.seekloud.thor.protocol.ESheepProtocol._
import org.seekloud.thor.shared.ptcl.SuccessRsp
import org.seekloud.thor.shared.ptcl.protocol.CommonProtocol.{GeneralRoom, GetRoom4GARsp, PswError, RoomNotExist, VerifyPswRsp}

import scala.collection.mutable

/**
  * User: TangYaruo
  * Date: 2018/11/12
  * Time: 11:36
  */
object RoomManager {

  private val log = LoggerFactory.getLogger(this.getClass)

  trait Command

  private case class TimeOut(msg: String) extends Command

  private case class ChildDead[U](name: String, childRef: ActorRef[U]) extends Command

  case class GetRoomList(replyTo: ActorRef[GetRoomListRsp]) extends Command

  case class GetRoom4GA(replyTo: ActorRef[GetRoom4GARsp]) extends Command

  case class VerifyPwd(roomId: Long, pwd: String, replyTo: ActorRef[VerifyPswRsp]) extends Command

  case class GetRoomPlayerList(roomId: Long, replyTo: ActorRef[GetRoomPlayerListRsp]) extends Command

  case class GetRoomByPlayer(playerId: String, replyTo: ActorRef[GetRoomIdRsp]) extends Command

  case class LeftRoom(playerId: String, name: String) extends Command

  case class reStartJoinRoom(userId: String, name: String, userActor: ActorRef[UserActor.Command]) extends Command

  case class BeDead(playerId: String, name: String) extends Command

  def create(): Behavior[Command] = {
    log.debug(s"RoomManager start...")
    Behaviors.setup[Command] {
      ctx =>
        implicit val stashBuffer: StashBuffer[Command] = StashBuffer[Command](Int.MaxValue)
        Behaviors.withTimers[Command] {
          implicit timer =>
            val roomIdGenerator = new AtomicLong(1L)
            val roomInUse = mutable.HashMap((1l, ("", "default", List.empty[(String, String)], System.currentTimeMillis())))
            idle(roomIdGenerator, roomInUse)
        }
    }
  }

  def idle(roomIdGenerator: AtomicLong,
    roomInUse: mutable.HashMap[Long, (String, String, List[(String, String)], Long)]) // roomId => (psw, roomName, List[userId, userName], createTime)
    (implicit stashBuffer: StashBuffer[Command], timer: TimerScheduler[Command]): Behaviors.Receive[Command] = {
    Behaviors.receive[Command] {
      (ctx, msg) =>
        msg match {
          case JoinRoom(userId, name, userActor, roomIdOpt, pswOpt, None) =>
            //              log.debug(s"$name joinRoom. roomInUse before: $roomInUse")
            //为新用户分配房间
            roomIdOpt match {
              case Some(roomId) => //用户指定roomId
                roomInUse.get(roomId) match {
                  case Some(info) =>
                    if (info._1 == pswOpt.getOrElse("")) { //加入房间密码正确
                      if (info._2.length < personLimit) {
                        //房间可加入
                        roomInUse.put(roomId, (info._1, info._2, (userId, name) :: info._3, info._4))
                        getRoomActor(ctx, roomId) ! RoomActor.JoinRoom(roomId, userId, name, userActor)

                      } else {
                        userActor ! UserActor.JoinRoomFail(s"房间-${roomId}人已满！")
                      }

                    } else {
                      log.info(s"user-$userId joinRoom error: wrong pwd: ${pswOpt.getOrElse("")}.right one: ${info._1}")
                      userActor ! UserActor.JoinRoomFail(s"房间-${roomId}密码错误！")
                    }
                  case None => //指定房间不存在直接创建，默认无密码
                    roomInUse.put(roomId, ("", s"$name's room", List((userId, name)), System.currentTimeMillis()))
                    getRoomActor(ctx, roomId) ! RoomActor.JoinRoom(roomId, userId, name, userActor)

                }


              case None => //随机分配room, 加入无密码的
                roomInUse.find(p => p._2._2.length < personLimit && p._2._1.isEmpty).toList.sortBy(_._1).headOption match {
                  case Some(t) =>
                    roomInUse.put(t._1, (t._2._1, t._2._2, (userId, name) :: t._2._3, t._2._4))
                    getRoomActor(ctx, t._1) ! RoomActor.JoinRoom(t._1, userId, name, userActor)
                  case None => //无可用房间，创建新房间,默认无密码
                    var roomId = roomIdGenerator.getAndIncrement()
                    while (roomInUse.exists(_._1 == roomId)) roomId = roomIdGenerator.getAndIncrement()
                    roomInUse.put(roomId, ("", s"$name's room", List((userId, name)), System.currentTimeMillis()))
                    getRoomActor(ctx, roomId) ! RoomActor.JoinRoom(roomId, userId, name, userActor)
                }
            }
            //              log.debug(s"$name joinRoom. roomInUse after: $roomInUse")
            Behaviors.same

          case msg: CreateRoom =>
            log.debug(s"get msg: $msg")
            var roomId = roomIdGenerator.getAndIncrement()
            while (roomInUse.exists(_._1 == roomId)) roomId = roomIdGenerator.getAndIncrement()
            roomInUse.put(roomId, (msg.pwd.getOrElse(""), msg.name, List((msg.playerId, msg.name)), System.currentTimeMillis()))
            getRoomActor(ctx, roomId, msg.frameRate) ! RoomActor.JoinRoom(roomId, msg.playerId, msg.name, msg.replyTo)
            msg.replyTo ! UserActor.CreateRoomSuccess(roomId)
            Behaviors.same

          case msg: VerifyPwd =>
            roomInUse.get(msg.roomId) match {
              case Some(room) =>
                if (room._1 == msg.pwd) {
                  msg.replyTo ! VerifyPswRsp()
                } else {
                  msg.replyTo ! PswError
                }
              case None =>
                msg.replyTo ! RoomNotExist
            }

            Behaviors.same

          case reStartJoinRoom(userId, name, userActor) =>
            roomInUse.find(_._2._3.exists(_._1 == userId)) match {
              case Some(t) =>
                getRoomActor(ctx, t._1) ! RoomActor.JoinRoom(t._1, userId, name, userActor)
              case None =>
                log.debug(s"$name reStartJoinRoom. but not find it")
            }
            Behaviors.same

          case RoomActor.JoinRoom4Watch(uid, roomId, playerId, userActor4Watch) =>
            //              log.debug(s"${ctx.self.path} recv a msg=${msg}")
            roomInUse.get(roomId) match {
              case Some(set) =>
                if (set._3.exists(p => p._1 == playerId)) {
                  getRoomActor(ctx, roomId) ! RoomActor.JoinRoom4Watch(uid, roomId, playerId, userActor4Watch)
                } else {
                  userActor4Watch ! UserActor.JoinRoomFail4Watch("您所观察的用户不在房间里")
                }
              case None => userActor4Watch ! UserActor.JoinRoomFail4Watch("您所观察的房间不存在")
            }
            Behaviors.same

          case LeftRoom(uid, name) =>
            //              log.debug(s"$name leftRoom. roomInUse before $roomInUse")
            roomInUse.find(_._2._3.exists(_._1 == uid)) match {
              case Some(t) =>
                roomInUse.put(t._1, (t._2._1, t._2._2, t._2._3.filterNot(_._1 == uid), t._2._4))
                getRoomActor(ctx, t._1) ! RoomActor.LeftRoom(uid, name, roomInUse(t._1)._3)
                if (roomInUse(t._1)._2.isEmpty && t._1 > 1l) roomInUse.remove(t._1)
              case None => log.debug(s"LeftRoom 玩家 $name 不在任何房间")
            }
            //              log.debug(s"$name leftRoom. roomInUse after $roomInUse")

            Behaviors.same

          case BeDead(playerId, name) =>
            roomInUse.find(_._2._3.exists(_._1 == playerId)) match {
              case Some(t) =>
                //                  roomInUse.put(t._1,t._2.filterNot(_._1 == playerId))
                getRoomActor(ctx, t._1) ! RoomActor.BeDead(playerId, name, roomInUse(t._1)._3)
                if (roomInUse(t._1)._2.isEmpty && t._1 > 1l) roomInUse.remove(t._1)
              case None => log.debug(s"BeDead 玩家 $name 不在任何房间")
            }
            Behaviors.same

          case ChildDead(child, childRef) =>
            log.debug(s"roomManager 不再监管room:$child,$childRef")
            ctx.unwatch(childRef)
            Behaviors.same

          case GetRoomList(replyTo) =>
            val roomList = roomInUse.map{ r =>
              Room(
                r._1,
                r._2._2,
                r._2._4
              )
            }.toList
            replyTo ! GetRoomListRsp(Some(RoomList(roomList)))
            Behaviors.same

          case GetRoomByPlayer(playerId, replyTo) =>
            val userExist = roomInUse.map { roomMap => (roomMap._1, roomMap._2._3.exists(t => t._1.equals(playerId))) }
            userExist.find(_._2 == true) match {
              case Some((roomId, _)) => replyTo ! GetRoomIdRsp(Some(RoomId(roomId)))
              case None => replyTo ! GetRoomIdRsp(None, 200003, "there isn't a room which has the user")
            }
            Behaviors.same

          case GetRoomPlayerList(roomId, replyTo) =>
            roomInUse.get(roomId) match {
              case Some(userList) =>
                val playerDataList = userList._3.map(t => PlayerData(t._1, t._2))
                replyTo ! GetRoomPlayerListRsp(Some(PlayerList(playerDataList)))
              case None =>
                replyTo ! GetRoomPlayerListRsp(None, 200005, "room is not exist")
            }
            Behaviors.same

          case msg: GetRoom4GA =>
            val roomList = roomInUse.map { r =>
              val hasPsw = if (r._2._1.isEmpty) 0 else 1
              s"${r._1}-${r._2._2}-${r._2._3.length}-${r._2._4}-$hasPsw"
            }.toList
            msg.replyTo ! GetRoom4GARsp(GeneralRoom(roomList))
            Behaviors.same

          case x =>
            log.warn(s"unknown msg: $x")
            Behaviors.unhandled
        }
    }
  }

  private def getRoomActor(ctx: ActorContext[Command], roomId: Long, frameRate: Int = AppSettings.frameRate) = {
    val childName = s"room_$roomId"
    ctx.child(childName).getOrElse {
      val actor = ctx.spawn(RoomActor.create(roomId, frameRate), childName)
      ctx.watchWith(actor, ChildDead(childName, actor))
      actor

    }.upcast[RoomActor.Command]
  }
}
