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

package org.seekloud.utils

import scalikejdbc._

/**
  * User: Taoz
  * Date: 1/7/2017
  * Time: 7:40 PM
  */
object ScalikeJdbcUtil {


  val conn: java.sql.Connection = ConnectionPool.borrow()



  def test1() = {
    def init() = {
      val newDB = ThreadLocalDB.create(conn)
      newDB.begin()
    }
    // after that..
    def action() = {
      val db = ThreadLocalDB.load()

      db readOnly{ implicit session =>

      }
    }
    def finalize() = {
      try { ThreadLocalDB.load().close() } catch { case e: Exception => e.printStackTrace() }
    }

  }


  def test2() = {
    // default
    DB readOnly { implicit session =>
      // ...
    }


  }



}
