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