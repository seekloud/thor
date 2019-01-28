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
  * 合并两个框架
  */
trait MiddleFrame {
  /**
    * @param width canvas宽
    * @param height canvas长
    * */
  def createCanvas(width: Double, height: Double): MiddleCanvas

  /**
    * @param url 图片路径（初始版本中HTML和Fx为了统一放置在对应路径中）
    * */
  def createImage(url: String): MiddleImage
}
