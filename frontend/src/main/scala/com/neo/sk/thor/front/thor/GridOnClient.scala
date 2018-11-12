package com.neo.sk.thor.front.thor
import com.neo.sk.thor.shared.ptcl._
import com.neo.sk.thor.shared.ptcl.config.ThorGameConfig
import com.neo.sk.thor.shared.ptcl.model.Point
import com.neo.sk.thor.shared.ptcl.protocol.ThorGame.UserActionEvent
import com.neo.sk.thor.shared.ptcl.thor.ThorSchema

class GridOnClient(override val boundary: Point) extends ThorSchema {

  override def debug(msg: String):Unit = println(msg)

  override def info(msg: String):Unit = println(msg)


}
