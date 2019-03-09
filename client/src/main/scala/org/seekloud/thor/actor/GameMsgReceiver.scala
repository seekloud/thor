package org.seekloud.thor.actor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{Behaviors, StashBuffer, TimerScheduler}
import org.seekloud.thor.shared.ptcl.protocol.ThorGame.WsMsgSource
import org.slf4j.LoggerFactory

/**
  * User: TangYaruo
  * Date: 2019/3/7
  * Time: 11:29
  */
object  GameMsgReceiver {

  private val log = LoggerFactory.getLogger(this.getClass)

  def create(): Behavior[WsMsgSource] = {
    Behaviors.setup[WsMsgSource] { ctx =>
      Behaviors.withTimers[WsMsgSource] { t =>
        implicit val stashBuffer: StashBuffer[WsMsgSource] = StashBuffer[WsMsgSource](Int.MaxValue)
        implicit val timer: TimerScheduler[WsMsgSource] = t
        working()
      }
    }
  }

  def working(): Behavior[WsMsgSource] =
    Behaviors.receive[WsMsgSource] { (ctx, msg) =>
      msg match {
        case x =>
          log.warn(s"unknown msg: $x")
          Behaviors.unhandled
      }
    }





}
