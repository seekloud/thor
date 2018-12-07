package org.seekloud.thor.common

import java.util.concurrent.TimeUnit

import com.typesafe.config.{Config, ConfigFactory}

/**
  * User: TangYaruo
  * Date: 2018/12/7
  * Time: 16:40
  */
object AppSettings {
  private implicit class RichConfig(config: Config) {
    val noneValue = "none"

    def getOptionalString(path: String): Option[String] =
      if (config.getAnyRef(path) == noneValue) None
      else Some(config.getString(path))

    def getOptionalLong(path: String): Option[Long] =
      if (config.getAnyRef(path) == noneValue) None
      else Some(config.getLong(path))

    def getOptionalDurationSeconds(path: String): Option[Long] =
      if (config.getAnyRef(path) == noneValue) None
      else Some(config.getDuration(path, TimeUnit.SECONDS))
  }

  val config = ConfigFactory.parseResources("product.conf").withFallback(ConfigFactory.load())


}
