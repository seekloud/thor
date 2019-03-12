package org.seekloud.thor.actor

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer, TimerScheduler}
import io.grpc.Server
import org.seekloud.thor.bot.BotServer
import org.seekloud.thor.controller.BotController
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext

/**
  * User: TangYaruo
  * Date: 2019/3/7
  * Time: 11:32
  */
object SdkServerHandler {

  private val log = LoggerFactory.getLogger(this.getClass)

  sealed trait Command

  final case class BuildBotServer(
    port: Int,
    executionContext: ExecutionContext,
    botActor: ActorRef[BotActor.Command],
    botController: BotController
  ) extends Command

  final case object Shutdown extends Command

  private[this] def switchBehavior(ctx: ActorContext[Command],
    behaviorName: String,
    behavior: Behavior[Command])
    (implicit stashBuffer: StashBuffer[Command]) = {
    log.debug(s"${ctx.self.path} becomes $behaviorName behavior.")
    stashBuffer.unstashAll(ctx, behavior)
  }

  def create(): Behavior[Command] =
    Behaviors.setup[Command] { ctx =>
      Behaviors.withTimers[Command] { implicit timer =>
        implicit val stashBuffer: StashBuffer[Command] = StashBuffer[Command](Int.MaxValue)
        switchBehavior(ctx, "idle", idle())
      }

    }

  private def idle()(
    implicit stashBuffer: StashBuffer[Command],
    timer: TimerScheduler[Command]
  ): Behavior[Command] =
    Behaviors.receive[Command] { (ctx, msg) =>
      msg match {
        case msg: BuildBotServer =>
          val server = BotServer.build(msg.port, msg.executionContext, msg.botActor, msg.botController)
          server.start()
          log.info(s"Server started at ${msg.port}...")
          sys.addShutdownHook {
            log.info("JVM SHUT DOWN.")
            server.shutdown()
            log.info("SHUT DOWN.")
          }
          switchBehavior(ctx, "working", working(server))
        case x =>
          log.warn(s"unknown msg: $x")
          Behaviors.unhandled
      }
    }


  private def working(botServer: Server)(
    implicit stashBuffer: StashBuffer[Command],
    timer: TimerScheduler[Command]
  ): Behavior[Command] =
    Behaviors.receive[Command] { (ctx, msg) =>
      msg match {
        case Shutdown =>
          botServer.shutdown()
          Behaviors.stopped

        case x =>
          log.warn(s"unknown msg: $x")
          Behaviors.unhandled
      }
    }


}
