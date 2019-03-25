package org.seekloud.thor.actor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import io.grpc.stub.StreamObserver
import org.seekloud.esheepapi.pb.api.{CurrentFrameRsp, ObservationRsp, ObservationWithInfoRsp, State}
import org.seekloud.thor.bot.BotServer
import org.seekloud.thor.controller.BotController
import org.seekloud.thor.shared.ptcl.model.Constants.GameState
import org.slf4j.LoggerFactory

/**
  * User: TangYaruo
  * Date: 2019/3/7
  * Time: 11:27
  */
object GrpcStreamSender {

  private val log = LoggerFactory.getLogger(this.getClass)

  sealed trait Command

  case class NewFrame(frame: Long) extends Command

  case class FrameObserver(frameObserver: StreamObserver[CurrentFrameRsp]) extends Command

  case class ObservationObserver(observationObserver: StreamObserver[ObservationWithInfoRsp]) extends Command

  case class NewObservation(observation: ObservationRsp) extends Command

  case object LeaveRoom extends Command


  def create(botController: BotController): Behavior[Command] = {
    Behaviors.setup[Command] { ctx =>
      val fStream = new StreamObserver[CurrentFrameRsp] {
        override def onNext(value: CurrentFrameRsp): Unit = {}

        override def onCompleted(): Unit = {}

        override def onError(t: Throwable): Unit = {}
      }
      val oStream = new StreamObserver[ObservationWithInfoRsp] {
        override def onNext(value: ObservationWithInfoRsp): Unit = {}

        override def onCompleted(): Unit = {}

        override def onError(t: Throwable): Unit = {}
      }
      working(botController, fStream, oStream)
    }
  }


  def working(botController: BotController, frameObserver: StreamObserver[CurrentFrameRsp], oObserver: StreamObserver[ObservationWithInfoRsp]): Behavior[Command] = {
    Behaviors.receive[Command] { (ctx, msg) =>
      msg match {

        case ObservationObserver(observationObserver) =>
          working(botController, frameObserver, observationObserver)

        case FrameObserver(fObserver) =>
          working(botController, fObserver, oObserver)

        case NewFrame(frame) =>
          val rsp = CurrentFrameRsp(frame)
          try {
            frameObserver.onNext(rsp)
            Behavior.same
          } catch {
            case e: Exception =>
              log.warn(s"frameObserver error: ${e.getMessage}")
              Behavior.stopped
          }

        case NewObservation(observation) =>
          BotServer.state = if (botController.gameState == GameState.stop) State.killed else State.in_game
          val botInfo = botController.getBotInformation
          val rsp = ObservationWithInfoRsp(observation.layeredObservation, observation.humanObservation,
            botInfo._1, botInfo._2, if (BotServer.state == State.in_game) 1 else 0, botController.frameCount,
            0, BotServer.state, "ok")

          try {
            if (oObserver != null) {
              oObserver.onNext(rsp)
            }
          } catch {
            case e: Exception =>
              log.warn(s"oObserver error: ${e.getMessage}")

          }
          Behaviors.same

        case LeaveRoom =>
          oObserver.onCompleted()
          frameObserver.onCompleted()
          Behaviors.stopped
      }
    }
  }


}
