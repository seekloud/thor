package org.seekloud.thor.actor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import io.grpc.stub.StreamObserver
import org.seekloud.esheepapi.pb.api.{CurrentFrameRsp, ObservationRsp, ObservationWithInfoRsp}
import org.seekloud.thor.bot.BotServer
import org.seekloud.thor.controller.BotController
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
          //TODO 根据具体情况重写
//          BotServer.state = if (botController.getLiveState) State.in_game else State.killed
//          val rsp = ObservationWithInfoRsp(observation.layeredObservation, observation.humanObservation,
//            botController.getScore._2.l, botController.getScore._2.k,
//            if (botController.getLiveState) 1 else 0, botController.getFrameCount,
//            0, BotServer.state, "ok")


          try {
//            oObserver.onNext(rsp)
            Behavior.same
          } catch {
            case e: Exception =>
              log.warn(s"ooObserver error: ${e.getMessage}")
              Behavior.stopped
          }

        case LeaveRoom =>
          oObserver.onCompleted()
          frameObserver.onCompleted()
          Behaviors.stopped
      }
    }
  }


}
