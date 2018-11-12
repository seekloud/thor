package com.neo.sk.thor.front.thor
import com.neo.sk.thor.shared.ptcl._
import com.neo.sk.thor.shared.ptcl.model.Point
import com.neo.sk.thor.shared.ptcl.thor.Grid

class GridOnServer(override val boundary: Point) extends Grid {
  override def debug(msg: String):Unit = println(msg)

  override def info(msg: String):Unit = println(msg)
}
