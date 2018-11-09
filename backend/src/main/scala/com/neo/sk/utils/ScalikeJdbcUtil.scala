package com.neo.sk.utils

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
