package org.seekloud.thor.core

import java.util.concurrent.atomic.AtomicLong

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer, TimerScheduler}
import org.seekloud.thor.core.UserActor.JoinRoom
import org.slf4j.LoggerFactory
import org.seekloud.thor.common.AppSettings.personLimit
import org.seekloud.thor.protocol.ESheepProtocol._

import scala.collection.mutable

/**
  * User: TangYaruo
  * Date: 2018/11/12
  * Time: 11:36
  */
object RoomManager {

  private val log = LoggerFactory.getLogger(this.getClass)

  trait Command
  private case class TimeOut(msg:String) extends Command
  private case class ChildDead[U](name:String,childRef:ActorRef[U]) extends Command

  case class GetRoomList(replyTo: ActorRef[GetRoomListRsp]) extends Command

  case class GetRoomPlayerList(roomId: Long, replyTo: ActorRef[GetRoomPlayerListRsp]) extends Command

  case class GetRoomByPlayer(playerId: String, replyTo: ActorRef[GetRoomIdRsp]) extends Command

  case class LeftRoom(playerId: String, name:String) extends Command

  case class reStartJoinRoom(userId: String, name: String, userActor: ActorRef[UserActor.Command]) extends Command

  case class BeDead(playerId: String, name:String) extends Command

  def create():Behavior[Command] = {
    log.debug(s"RoomManager start...")
    Behaviors.setup[Command]{
      ctx =>
        implicit val stashBuffer = StashBuffer[Command](Int.MaxValue)
        Behaviors.withTimers[Command]{
          implicit timer =>
            val roomIdGenerator = new AtomicLong(1L)
            val roomInUse = mutable.HashMap((1l,List.empty[(String, String)]))
            idle(roomIdGenerator,roomInUse)
        }
    }
  }

  def idle(roomIdGenerator:AtomicLong,
           roomInUse:mutable.HashMap[Long,List[(String, String)]]) // roomId => List[userId, userName]
          (implicit stashBuffer: StashBuffer[Command],timer:TimerScheduler[Command]) = {
      Behaviors.receive[Command]{
        (ctx, msg) =>
          msg match {
            case JoinRoom(userId, name, userActor, roomIdOpt) =>
              log.debug(s"$name joinRoom. roomInUse before: $roomInUse")
              //为新用户分配房间
              roomIdOpt match {
                case Some(roomId) => //用户指定roomId
                  roomInUse.get(roomId) match {
                    case Some(list) => roomInUse.put(roomId, (userId, name) :: list)
                    case None => roomInUse.put(roomId, List((userId, name)))
                  }
                  getRoomActor(ctx, roomId) ! RoomActor.JoinRoom(roomId, userId, name, userActor)


                case None => //随机分配room
                  roomInUse.find(p => p._2.length < personLimit).toList.sortBy(_._1).headOption match{
                    case Some(t) =>
                      roomInUse.put(t._1,(userId, name) :: t._2)
                      getRoomActor(ctx,t._1) ! RoomActor.JoinRoom(t._1, userId, name, userActor)
                    case None =>  //创建新房间
                      var roomId = roomIdGenerator.getAndIncrement()
                      while(roomInUse.exists(_._1 == roomId))roomId = roomIdGenerator.getAndIncrement()
                      roomInUse.put(roomId,List((userId, name)))
                      getRoomActor(ctx,roomId) ! RoomActor.JoinRoom(roomId, userId, name, userActor)
                  }
              }
              log.debug(s"$name joinRoom. roomInUse after: $roomInUse")
              Behaviors.same

            case reStartJoinRoom(userId, name, userActor)=>
              roomInUse.find(_._2.exists(_._1 == userId)) match{
                case Some(t) =>
                  getRoomActor(ctx,t._1) ! RoomActor.JoinRoom(t._1, userId, name, userActor)
                case None =>
                  log.debug(s"$name reStartJoinRoom. but not find it")
              }
              Behaviors.same

            case RoomActor.JoinRoom4Watch(uid,roomId,playerId,userActor4Watch) =>
              log.debug(s"${ctx.self.path} recv a msg=${msg}")
              roomInUse.get(roomId) match {
                case Some(set) =>
                  set.exists(p => p._1 == playerId) match {
                    case false => userActor4Watch ! UserActor.JoinRoomFail4Watch("您所观察的用户不在房间里")
                    case _ => getRoomActor(ctx,roomId) ! RoomActor.JoinRoom4Watch(uid,roomId,playerId,userActor4Watch)
                  }
                case None => userActor4Watch ! UserActor.JoinRoomFail4Watch("您所观察的房间不存在")
              }
              Behaviors.same

            case LeftRoom(uid, name) =>
              log.debug(s"$name leftRoom. roomInUse before $roomInUse")
              roomInUse.find(_._2.exists(_._1 == uid)) match{
                case Some(t) =>
                  roomInUse.put(t._1,t._2.filterNot(_._1 == uid))
                  getRoomActor(ctx,t._1) ! RoomActor.LeftRoom(uid,name,roomInUse(t._1))
                  if(roomInUse(t._1).isEmpty && t._1 > 1l)roomInUse.remove(t._1)
                case None => log.debug(s"LeftRoom 玩家 $name 不在任何房间")
              }
              log.debug(s"$name leftRoom. roomInUse after $roomInUse")

              Behaviors.same

            case BeDead(playerId, name) =>
              roomInUse.find(_._2.exists(_._1 == playerId)) match{
                case Some(t) =>
//                  roomInUse.put(t._1,t._2.filterNot(_._1 == playerId))
                  getRoomActor(ctx,t._1) ! RoomActor.BeDead(playerId,name,roomInUse(t._1))
                  if(roomInUse(t._1).isEmpty && t._1 > 1l)roomInUse.remove(t._1)
                case None => log.debug(s"BeDead 玩家 $name 不在任何房间")
              }
              Behaviors.same

            case ChildDead(child,childRef) =>
              log.debug(s"roomManager 不再监管room:$child,$childRef")
              ctx.unwatch(childRef)
              Behaviors.same

            case GetRoomList(replyTo) =>
              val roomList = roomInUse.keys.toList
              replyTo ! GetRoomListRsp(Some(RoomList(roomList)))
              Behaviors.same

            case GetRoomByPlayer(playerId, replyTo) =>
              val userExist = roomInUse.map{roomMap => (roomMap._1, roomMap._2.exists(t => t._1.equals(playerId)))}
              userExist.find(_._2 == true) match {
                case Some((roomId, _)) => replyTo ! GetRoomIdRsp(Some(RoomId(roomId)))
                case None => replyTo ! GetRoomIdRsp(None, 200003, "there isn't a room which has the user")
              }
              Behaviors.same

            case GetRoomPlayerList(roomId, replyTo) =>
              roomInUse.get(roomId) match{
                case Some(userList) =>
                  val playerDataList = userList.map(t => PlayerData(t._1, t._2))
                  replyTo ! GetRoomPlayerListRsp(Some(PlayerList(playerDataList)))
                case None =>
                  replyTo ! GetRoomPlayerListRsp(None, 200005, "room is not exist")
              }
              Behaviors.same


            case unknow =>
              Behaviors.same
          }
      }
  }

  private def getRoomActor(ctx:ActorContext[Command],roomId:Long) = {
    val childName = s"room_$roomId"
    ctx.child(childName).getOrElse{
      val actor = ctx.spawn(RoomActor.create(roomId),childName)
      ctx.watchWith(actor,ChildDead(childName,actor))
      actor

    }.upcast[RoomActor.Command]
  }
}
