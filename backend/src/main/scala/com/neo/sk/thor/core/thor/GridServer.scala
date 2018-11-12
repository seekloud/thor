package com.neo.sk.thor.core.thor

import akka.actor.typed.ActorContext
import com.neo.sk.thor.core.RoomActor
import com.neo.sk.thor.shared.ptcl.model
import com.neo.sk.thor.shared.ptcl.protocol.ThorGame._
import com.neo.sk.thor.shared.ptcl.thor.Grid
import org.slf4j.Logger

/**
  * @author Jingyi
  * @version 创建时间：2018/11/12
  */
class GridServer(
                  ctx:ActorContext[RoomActor.Command],
                  log:Logger,
                  dispatch:WsMsgServer => Unit,
                  dispatchTo:(Long,WsMsgServer) => Unit,
                  override val boundary: model.Point
                ) extends Grid{

}
