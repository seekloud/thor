package org.seekloud.thor.core

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.{ActorContext, StashBuffer, TimerScheduler}
import org.seekloud.byteobject.MiddleBufferInJvm
import org.seekloud.thor.common.AppSettings
import org.seekloud.thor.models.DAO.recordDao
import org.seekloud.thor.shared.ptcl.protocol.ThorGame
import org.seekloud.utils.ESSFSupport.initFileReader
import org.slf4j.LoggerFactory

import scala.concurrent.duration.FiniteDuration

/**
  * User: TangYaruo
  * Date: 2018/11/29
  * Time: 16:46
  */
object GameReplay {

  private val log = LoggerFactory.getLogger(this.getClass)

  sealed trait Command

  private final case object BehaviorChangeKey

  private final case object GameLoopKey


  final case class SwitchBehavior(
    name: String,
    behavior: Behavior[Command],
    durationOpt: Option[FiniteDuration] = None,
    timeOut: TimeOut = TimeOut("busy time error")
  ) extends Command

  case class TimeOut(msg: String) extends Command

  case object GameLoop extends Command

  private[this] def switchBehavior(ctx: ActorContext[Command],
    behaviorName: String, behavior: Behavior[Command], durationOpt: Option[FiniteDuration] = None, timeOut: TimeOut = TimeOut("busy time error"))
    (implicit stashBuffer: StashBuffer[Command],
      timer: TimerScheduler[Command]) = {
    log.debug(s"${ctx.self.path} becomes $behaviorName behavior.")
    timer.cancel(BehaviorChangeKey)
    durationOpt.foreach(timer.startSingleTimer(BehaviorChangeKey, timeOut, _))
    stashBuffer.unstashAll(ctx, behavior)
  }

  case class InitReplay(userActor: ActorRef[ThorGame.WsMsgSource], userId: String, f: Int) extends Command

  case object GetUserListInRecord extends Command


//  def create(recordId:Long):Behavior[Command] = {
//    Behaviors.setup[Command]{ctx=>
//      log.info(s"${ctx.self.path} is starting..")
//      implicit val stashBuffer: StashBuffer[Command] = StashBuffer[Command](Int.MaxValue)
//      implicit val sendBuffer: MiddleBufferInJvm = new MiddleBufferInJvm(81920)
//      Behaviors.withTimers[Command] { implicit timer =>
//        recordDao.getRecordById(recordId).map {
//          case Some(r)=>
//            try{
//              val replay=initFileReader(r.filePath)
//              val info=replay.init()
//              ctx.self ! SwitchBehavior("work",
//                work(
//                  replay,
//                  metaDataDecode(info.simulatorMetadata).right.get,
//                  initStateDecode(info.simulatorInitState).right.get.asInstanceOf[TankGameEvent.TankGameSnapshot].state.f,
//                  info.frameCount,
//                  userMapDecode(replay.getMutableInfo(AppSettings.essfMapKeyName).getOrElse(Array[Byte]())).right.get.m
//                ))
//            }catch {
//              case e:Throwable=>
//                log.error("error---"+e.getMessage)
//                ctx.self ! SwitchBehavior("initError",initError)
//            }
//          case None=>
//            log.debug(s"record--$recordId didn't exist!!")
//            ctx.self ! SwitchBehavior("initError",initError)
//        }
//        switchBehavior(ctx,"busy",busy())
//      }
//    }
//  }

  private def busy()(
    implicit stashBuffer:StashBuffer[Command],
    timer:TimerScheduler[Command]
  ): Behavior[Command] =
    Behaviors.receive[Command] { (ctx, msg) =>
      msg match {
        case SwitchBehavior(name, behavior,durationOpt,timeOut) =>
          switchBehavior(ctx,name,behavior,durationOpt,timeOut)

        case TimeOut(m) =>
          log.debug(s"${ctx.self.path} is time out when busy,msg=${m}")
          Behaviors.stopped

        case unknowMsg =>
          stashBuffer.stash(unknowMsg)
          Behavior.same
      }
    }


}
