package org.seekloud.thor.front.common

import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.HTMLElement
import scala.language.implicitConversions
import scala.xml.Elem

/**
  * User: Taoz
  * Date: 12/26/2016
  * Time: 1:36 PM
  */
trait Component {

  def render: Elem

}

object Component {
  implicit def component2Element(comp: Component): Elem = comp.render
}