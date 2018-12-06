package org.seekloud.thor.game

import org.seekloud.thor.controller.PlayGameController
import org.seekloud.thor.core.PlayGameActor
import org.seekloud.thor.shared.ptcl.protocol.ThorGame
import org.seekloud.thor.view.PlayGameView

/**
  * User: TangYaruo
  * Date: 2018/11/25
  * Time: 17:52
  */
final case class NetworkLatency(latency: Long)

trait NetworkInfo {
  this: PlayGameController =>

  private var lastPingTime = System.currentTimeMillis()
  private val PingTimes = 10
  private var latency: Long = 0L
  private var receiveNetworkLatencyList: List[NetworkLatency] = Nil

  def ping(): Unit = {
    val curTime = System.currentTimeMillis()
    if (curTime - lastPingTime > 1000) {
      startPing()
      lastPingTime = curTime
    }
  }

  private def startPing(): Unit = {
    this.playGameActor ! PlayGameActor.DispatchMsg(ThorGame.PingPackage(System.currentTimeMillis()))
  }

  protected def receivePingPackage(p: ThorGame.PingPackage): Unit = {
    receiveNetworkLatencyList = NetworkLatency(System.currentTimeMillis() - p.sendTime) :: receiveNetworkLatencyList
    if (receiveNetworkLatencyList.size < PingTimes) {
      Shortcut.scheduleOnce(() => startPing(), 10)
    } else {
      latency = receiveNetworkLatencyList.map(_.latency).sum / receiveNetworkLatencyList.size
      receiveNetworkLatencyList = Nil
    }
  }

  protected def getNetworkLatency = latency



}
