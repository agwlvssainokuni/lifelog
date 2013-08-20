/*
 * Copyright 2013 agwlvssainokuni
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

package batches.common

import org.specs2.mutable.Specification

import play.api._

class LaunchSpec extends Specification {

  implicit val mode = Mode.Test

  "Batch" should {
    "インスタンス化 正常" in {
      Batch(classOf[LaunchSpecTest1])(Seq()) must beSome.which(_ == 0)
    }
    "インスタンス化 異常(インスタンス化できない)" in {
      Batch(classOf[LaunchSpecTest2])(Seq()) must beNone
    }
  }

}

class LaunchSpecTest1 extends Batch {
  override def apply(args: Seq[String]): Int = 0
}

class LaunchSpecTest2(dummy: Int) extends Batch {
  override def apply(args: Seq[String]): Int = 0
}