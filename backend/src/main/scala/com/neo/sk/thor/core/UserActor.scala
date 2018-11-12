package com.neo.sk.thor.core

import org.slf4j.LoggerFactory

/**
  * @author Jingyi
  * @version 创建时间：2018/11/9
  */
object UserActor {

  private val log = LoggerFactory.getLogger(this.getClass)

  sealed trait Command
}
