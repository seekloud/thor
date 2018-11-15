package com.neo.sk.thor.core

import java.util.concurrent.atomic.AtomicLong

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer, TimerScheduler}
import com.neo.sk.thor.core.UserActor.JoinRoom
import org.slf4j.LoggerFactory
import com.neo.sk.thor.common.AppSettings.personLimit

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

  case class LeftRoom(playerId: String, name:String) extends Command

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
              Behaviors.same

            case LeftRoom(uid, name) =>
              roomInUse.find(_._2.exists(_._1 == uid)) match{
                case Some(t) =>
                  roomInUse.put(t._1,t._2.filterNot(_._1 == uid))
                  getRoomActor(ctx,t._1) ! RoomActor.LeftRoom(uid,name,roomInUse(t._1))
                  if(roomInUse(t._1).isEmpty && t._1 > 1l)roomInUse.remove(t._1)
                case None => log.debug(s"该玩家不在任何房间")
              }
              Behaviors.same

            case ChildDead(child,childRef) =>
              log.debug(s"roomManager 不再监管room:$child,$childRef")
              ctx.unwatch(childRef)
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
