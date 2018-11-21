package org.seekloud.thor.front.common

import scala.language.implicitConversions
import scala.xml.Elem

trait Page extends Component {

  def render: Elem

}

object Page{
  implicit def page2Element(page: Page): Elem = page.render
}