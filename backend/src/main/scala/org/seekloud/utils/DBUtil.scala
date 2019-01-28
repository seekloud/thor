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

import com.zaxxer.hikari.HikariDataSource
import org.slf4j.LoggerFactory
import slick.jdbc.PostgresProfile.api._
import slick.util.AsyncExecutor
import org.seekloud.thor.Boot.executor
/**
 * User: Taoz
 * Date: 2/9/2015
 * Time: 4:33 PM
 */
object DBUtil {
  val log = LoggerFactory.getLogger(this.getClass)
  private val dataSource = createDataSource()

  import org.seekloud.thor.common.AppSettings._

  private def createDataSource() = {

    val dataSource = new org.postgresql.ds.PGSimpleDataSource()

    //val dataSource = new MysqlDataSource()

    log.info(s"connect to db: $slickUrl")
    dataSource.setUrl(slickUrl)
    dataSource.setUser(slickUser)
    dataSource.setPassword(slickPassword)

    val hikariDS = new HikariDataSource()
    hikariDS.setDataSource(dataSource)
    hikariDS.setMaximumPoolSize(slickMaximumPoolSize)
    hikariDS.setConnectionTimeout(slickConnectTimeout)
    hikariDS.setIdleTimeout(slickIdleTimeout)
    hikariDS.setMaxLifetime(slickMaxLifetime)
    hikariDS.setAutoCommit(true)
    hikariDS
  }

  val asyncExecutor : AsyncExecutor = AsyncExecutor.apply("AsyncExecutor.default",30,-1)

  val db = Database.forDataSource(dataSource, Some(slickMaximumPoolSize),asyncExecutor)




}