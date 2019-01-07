package org.seekloud.thor.core

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer, TimerScheduler}
import org.seekloud.thor.Boot.executor
import org.seekloud.thor.core.game.ThorSchemaServerImpl
import org.seekloud.thor.shared.ptcl.protocol.ThorGame.{MouseClickDownLeft, MouseMove}
import org.slf4j.LoggerFactory

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.util.Random

/**
  * User: XuSiRan
  * Date: 2018/12/26
  * Time: 12:24
  */
object RobotActor {
  private val log = LoggerFactory.getLogger(this.getClass)

  trait Command

  private case class TimeOut(msg: String) extends Command

  private final case class SwitchBehavior(
    name: String,
    behavior: Behavior[Command],
    durationOpt: Option[FiniteDuration] = None,
    timeOut: TimeOut = TimeOut("busy time error")
  ) extends Command

  private final case object BehaviorChangeKey
  private final case object MouseMoveKey
  private final case object MouseMoveGoOnKey
  private final case object MouseLeftDownKey

  private final case object AutoMouseMove extends Command

  private final case class AutoMouseMoveGoOn(
    theta: Float,
    distance: Float
  ) extends Command

  private final case object AutoMouseLeftDown extends Command

  final case object RobotDead extends Command

  private[this] def switchBehavior(
    ctx: ActorContext[Command],
    behaviorName: String,
    behavior: Behavior[Command],
    durationOpt: Option[FiniteDuration] = None,
    timeOut: TimeOut = TimeOut("busy time error"))
    (implicit stashBuffer: StashBuffer[Command],
      timer: TimerScheduler[Command]): Behavior[Command] = {
    timer.cancel(BehaviorChangeKey)
    durationOpt.foreach(timer.startSingleTimer(BehaviorChangeKey, timeOut, _))
    stashBuffer.unstashAll(ctx, behavior)
  }

  private def busy()(
    implicit stashBuffer: StashBuffer[Command],
    timer: TimerScheduler[Command]
  ): Behavior[Command] =
    Behaviors.receive[Command] { (ctx, msg) =>
      msg match {
        case SwitchBehavior(name, b, durationOpt, timeOut) =>
          switchBehavior(ctx, name, b, durationOpt, timeOut)

        case TimeOut(m) =>
          log.debug(s"${ctx.self.path} is time out when busy, msg=$m")
          Behaviors.stopped

        case x =>
          stashBuffer.stash(x)
          Behavior.same

      }
    }

  def init(
    roomActor: ActorRef[RoomActor.Command],
    thorSchema: ThorSchemaServerImpl,
    botId: String,
    botName: String
  ): Behavior[Command] =
    Behaviors.setup[Command] { ctx =>
      log.info(s"*** is starting...")
      implicit val stashBuffer: StashBuffer[Command] = StashBuffer[Command](Int.MaxValue)
      val actionSerialNumGenerator = new AtomicInteger(0)
      Behaviors.withTimers[Command] { implicit timer =>
        timer.startSingleTimer(MouseMoveKey, AutoMouseMove, 1.seconds)
        timer.startSingleTimer(MouseLeftDownKey, AutoMouseLeftDown, 2.2.seconds)
        switchBehavior(ctx, "idle", idle(roomActor, thorSchema, botId, botName, actionSerialNumGenerator))
      }
    }

  private def idle(
    roomActor: ActorRef[RoomActor.Command],
    thorSchema: ThorSchemaServerImpl,
    botId: String,
    botName: String,
    actionSerialNumGenerator: AtomicInteger
  )(
    implicit stashBuffer: StashBuffer[Command],
    timer: TimerScheduler[Command]
  ): Behavior[Command] ={
    Behaviors.receive[Command]{(ctx, msg) =>
      msg match {
        case AutoMouseMove =>
          def sendBackendMove(thetaList: List[Float], num: Int): Unit = {
            val data = MouseMove(botId, thetaList(num), 128f, thorSchema.systemFrame, actionSerialNumGenerator.getAndIncrement())
            roomActor ! RoomActor.WsMessage(botId, data)
            if(num < thetaList.length - 1)
              ctx.system.scheduler.scheduleOnce(50.millis){
                sendBackendMove(thetaList, num + 1)
              }
          }
          val random = new Random()
          val theta = thorSchema.adventurerMap.get(botId) match{
            case None =>
              0
            case Some(adventurer) =>
              adventurer.position match{
                case a if a.x + 20 > thorSchema.boundary.x => random.nextFloat() * math.Pi - 1.5 * math.Pi
                case b if b.x - 20 < 0 => random.nextFloat() * math.Pi - 0.5 * math.Pi
                case c if c.y + 20 > thorSchema.boundary.y => - random.nextFloat() * math.Pi
                case d if d.y - 20 < 0 => random.nextFloat() * math.Pi
                case _ => random.nextFloat() * 2 * math.Pi - math.Pi
              }
          }
          val direction = if(theta == 0) theta.toFloat else thorSchema.adventurerMap(botId).direction

          if(math.abs(theta - direction) > 0.1){ //角度差大于0.3才执行

            val tDirection = {
              if(theta - direction > math.Pi) theta - direction - 2 * math.Pi
              else if(theta - direction < -math.Pi) theta - direction + 2 * math.Pi
              else theta - direction
            }
            val increment = (1 to (math.abs(tDirection) / 0.2).toInt).map(_ => if(tDirection > 0) 0.2f else -0.2f)
            val thetaList = increment.scanLeft(direction)(_ + _).map(t => if(t > math.Pi) t - 2 * math.Pi.toFloat else if(t < -math.Pi) t + 2 * math.Pi.toFloat else t)

            sendBackendMove(thetaList.toList, 0)

            timer.cancel(MouseMoveKey)
            timer.startSingleTimer(MouseMoveKey, AutoMouseMove, 2.seconds)
          }
          else{
            timer.cancel(MouseMoveKey)
            timer.startSingleTimer(MouseMoveKey, AutoMouseMove, 2.seconds)
          }
          Behavior.same

        case AutoMouseLeftDown =>
          val data = MouseClickDownLeft(botId, thorSchema.systemFrame, actionSerialNumGenerator.getAndIncrement())
          roomActor ! RoomActor.WsMessage(botId, data)
          timer.cancel(MouseLeftDownKey)
          timer.startSingleTimer(MouseLeftDownKey, AutoMouseLeftDown, 2.2.seconds)
          Behaviors.same

        case RobotDead =>
          timer.cancel(MouseMoveKey)
          timer.cancel(MouseMoveGoOnKey)
          timer.cancel(MouseLeftDownKey)
          ctx.system.scheduler.scheduleOnce(2.seconds){
            thorSchema.robotJoinGame(botId, botName, ctx.self)
          }
          ctx.system.scheduler.scheduleOnce(4.seconds){
            timer.startSingleTimer(MouseLeftDownKey, AutoMouseLeftDown, 2.2.seconds)
            timer.startSingleTimer(MouseMoveKey, AutoMouseMove, 2.seconds)
          }
          Behaviors.same

        case _ =>
          Behaviors.same
      }
    }
  }

}
