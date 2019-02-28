/*
 * Copyright 2018 seekloud (https://github.com/seekloud)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.seekloud.thor.front.common

import mhtml.{Rx, Var}
import org.scalajs.dom
import org.scalajs.dom.raw.Event

/**
  * User: Taoz
  * Date: 4/15/2018
  * Time: 1:46 PM
  */
trait PageSwitcher{

  def getCurrentHash: String = dom.window.location.hash

  protected val currentHashVar: Rx[List[String]] = PageSwitcher.currentPageHash

  def switchPageByHash(): Unit = PageSwitcher.switchPageByHash()

}

object PageSwitcher extends PageSwitcher{
  private val currentPageHash: Var[List[String]] = Var(Nil)


  def hashStr2Seq(str: String): IndexedSeq[String] = {
    if (str.length == 0) {
      IndexedSeq.empty[String]
    } else if (str.startsWith("#/")) {
      val t = str.substring(2).split("/").toIndexedSeq
      if (t.nonEmpty) {
        t
      } else IndexedSeq.empty[String]
    } else {
      println("Error hash string:" + str + ". hash string must start with [#/]")
      IndexedSeq.empty[String]
    }
  }

  override def switchPageByHash(): Unit = {
    //    println("PageSwitcher.switchPageByHash: " + getCurrentHash)
    currentPageHash := hashStr2Seq(getCurrentHash).toList
  }

  dom.window.onhashchange = { _: Event =>
    //    println("PageSwitcher.onhashchange: " + getCurrentHash)
    switchPageByHash()
  }

}
