package org.seekloud.thor.core

import java.util.concurrent.atomic.AtomicLong

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, TimerScheduler}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.{ActorAttributes, Supervision}
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import org.seekloud.thor.shared.ptcl.protocol.ThorGame._
import org.seekloud.utils.byteObject.MiddleBufferInJvm
import org.slf4j.LoggerFactory

/**
  * @author Jingyi
  * @version 创建时间：2018/11/9
  */
object UserManager {

  private val log = LoggerFactory.getLogger(this.getClass)

  trait Command

  final case class ChildDead[U](name: String, childRef: ActorRef[U]) extends Command

  final case class GetWebSocketFlow(name:String,replyTo:ActorRef[Flow[Message,Message,Any]], roomId:Option[Long] = None) extends Command

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
    Behaviors.receive[Command]{
      (ctx, msg) =>
        msg match {
          case GetWebSocketFlow(name,replyTo, roomIdOpt) =>

            val playerInfo = UserInfo(uidGenerator.getAndIncrement().toString, name)

            val userActor = getUserActor(ctx, playerInfo.playerId, playerInfo)
            replyTo ! getWebSocketFlow(userActor)
            userActor ! UserActor.StartGame(roomIdOpt)
            Behaviors.same

          case ChildDead(child, childRef) =>
            ctx.unwatch(childRef)
            Behaviors.same

          case unknown =>
            log.error(s"${ctx.self.path} recv an unknown msg when idle:${unknown}")
            Behaviors.same
        }
    }
  }

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
          log.warn(s"parse front msg failed when json parse,s=${s}")
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
            case Right(req) => UserActor.WsMessage(Some(req))
            case Left(e) =>
              log.error(s"decode binaryMessage failed,error:${e.message}")
              UserActor.WsMessage(None)
          }
      }.via(UserActor.flow(userActor))
      .map {
        case t: Wrap =>
          BinaryMessage.Strict(ByteString(t.ws))

        case x =>
          log.debug(s"akka stream receive unknown msg=${x}")
          TextMessage.apply("")
      }.withAttributes(ActorAttributes.supervisionStrategy(decider))
  }

  private val decider: Supervision.Decider = {
    e: Throwable =>
      e.printStackTrace()
      log.error(s"WS stream failed with $e")
      Supervision.Resume
  }

  private def getUserActor(ctx: ActorContext[Command],id: String, userInfo: UserInfo):ActorRef[UserActor.Command] = {
    val childName = s"UserActor-${id}"
    ctx.child(childName).getOrElse{
      val actor = ctx.spawn(UserActor.create(id, userInfo),childName)
      ctx.watchWith(actor,ChildDead(childName,actor))
      actor
    }.upcast[UserActor.Command]
  }

}
