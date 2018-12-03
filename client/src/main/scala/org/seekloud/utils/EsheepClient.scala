package com.neo.sk.utils

import com.neo.sk.tank.common.AppSettings
import com.neo.sk.tank.model._
import com.neo.sk.tank.shared.ptcl.ErrorRsp
import org.slf4j.LoggerFactory
import com.neo.sk.tank.App.{executor}
import scala.concurrent.Future
import com.neo.sk.tank.App.executor
import com.neo.sk.utils.HttpUtil
/**
  * @author Jingyi
  * @version 创建时间：2018/12/3
  */
object EsheepClient extends HttpUtil {
  import io.circe._
  import io.circe.generic.auto._
  import io.circe.parser.decode
  import io.circe.syntax._

  private val log = LoggerFactory.getLogger(this.getClass)

//  private val baseUrl = s"${AppSettings.esheepProtocol}://${AppSettings.esheepHost}:${AppSettings.esheepPort}"
  private val baseUrl = s"http://flowdev.neoap.com"


}
