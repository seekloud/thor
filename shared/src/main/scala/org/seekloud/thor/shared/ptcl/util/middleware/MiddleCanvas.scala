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

package org.seekloud.thor.shared.ptcl.util.middleware

/**
  * copied from tank
  * 合并框架中的canvas
  * 本文件中定义画图中使用的函数并在实例中重写
  */
trait MiddleCanvas {

  def getCtx: MiddleContext

  def getWidth(): Double

  def getHeight(): Double

  def setWidth(h: Any): Unit //设置宽

  def setHeight(h: Any): Unit //设置高

  def change2Image(): Any //转换快照
}
