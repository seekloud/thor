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

import akka.actor.typed.{ActorRef, Behavior, PostStop}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.{ActorContext, StashBuffer, TimerScheduler}
import org.seekloud.thor.Boot.{executor, userManager}
import org.seekloud.thor.core.ESheepLinkClient.Command
import org.seekloud.thor.models.SlickTables
import org.seekloud.thor.models.DAO.ESheepReplayDAO
import org.seekloud.thor.protocol.ESheepProtocol._
import org.seekloud.thor.protocol.ReplayProtocol._
import org.seekloud.thor.shared.ptcl.CommonRsp

import scala.concurrent.Future
/**
  * User: XuSiRan
  * Date: 2018/11/28
  * Time: 12:48
  */
object GameRecordGetter {

  trait Command

  case class GetRecordList(
                            lastId: Long,
                            count: Int,
                            replyTo: ActorRef[GetReplyRecordRsp]) extends Command
  case class GetRecordListByTime(
                                  startTime: Long,
                                  endTime: Long,
                                  lastId: Long,
                                  count: Int,
                                  replyTo: ActorRef[GetReplyRecordRsp]) extends Command
  case class GetRecordListByPlayer(
                                  playerId: String,
                                  lastId: Long,
                                  count: Int,
                                  replyTo: ActorRef[GetReplyRecordRsp]) extends Command

  def idle(): Behavior[Command] ={
    Behaviors.receive[Command]{ (ctx, msg) =>
      msg match{
        case GetRecordList(lastId, count, replyTo) =>
          ESheepReplayDAO.getRecordList(lastId, count).foreach{ list =>
            val rspList = list.groupBy(_._1.recordId)
            val rspData = rspList.toList.map{ l =>
              if(l._2.head._2.isEmpty){
                val record = l._2.head._1
                ESheepRePlayInfo(record.recordId, record.roomId, record.startTime, record.endTime, 0, Nil)
              }
              else {
                val record = l._2.head._1
                val userList = l._2.map(t => (t._2.get.userId, t._2.get.userNickname))
                ESheepRePlayInfo(record.recordId, record.roomId, record.startTime, record.endTime, l._2.length, userList)
              }
            }.sortBy(_.recordId)
            replyTo ! GetReplyRecordRsp(rspData)
          }
          Behaviors.same
        case GetRecordListByTime(startTime, endTime, lastId, count, replyTo) =>
          ESheepReplayDAO.getRecordListByTime(startTime, endTime, lastId, count).foreach{ list =>
            val rspList = list.groupBy(_._1.recordId)
            val rspData = rspList.toList.map{ l =>
              if(l._2.head._2.isEmpty){
                val record = l._2.head._1
                ESheepRePlayInfo(record.recordId, record.roomId, record.startTime, record.endTime, 0, Nil)
              }
              else {
                val record = l._2.head._1
                val userList = l._2.map(t => (t._2.get.userId, t._2.get.userNickname))
                ESheepRePlayInfo(record.recordId, record.roomId, record.startTime, record.endTime, l._2.length, userList)
              }
            }.sortBy(_.recordId)
            replyTo ! GetReplyRecordRsp(rspData)
          }
          Behaviors.same
        case GetRecordListByPlayer(playerId, lastId, count, replyTo) =>
          ESheepReplayDAO.getRecordListByPlayer(playerId, lastId, count).foreach{ list =>
            val rspList = list.groupBy(_._1.recordId)
            val rspData = rspList.toList.map{ l =>
              if(l._2.head._2.isEmpty){
                val record = l._2.head._1
                ESheepRePlayInfo(record.recordId, record.roomId, record.startTime, record.endTime, 0, Nil)
              }
              else {
                val record = l._2.head._1
                val userList = l._2.map(t => (t._2.get.userId, t._2.get.userNickname))
                ESheepRePlayInfo(record.recordId, record.roomId, record.startTime, record.endTime, l._2.length, userList)
              }
            }.sortBy(_.recordId)
            replyTo ! GetReplyRecordRsp(rspData)
          }
          Behaviors.same

        case _ =>
          // 无效消息不接受工作
          Behaviors.same
      }
    }
  }

}
