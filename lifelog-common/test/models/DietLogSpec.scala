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

package models

import java.util._
import java.util.Calendar._

import org.specs2.execute.AsResult
import org.specs2.execute.Result
import org.specs2.mutable.Specification

import play.api.db.DB
import play.api.test.WithApplication

class DietLogSpec extends Specification {

  def newDate(diff: Int) = Calendar.getInstance() match {
    case cal =>
      cal.add(DATE, diff)
      cal.getTime()
  }

  abstract class TestApp extends WithApplication {
    override def around[T: AsResult](t: => T): Result = super.around {
      DB.withTransaction { implicit c =>
        for {
          i <- 1 to 5
          email = "user" + i + "@domain"
          nickname = "ニックネーム" + i
          id <- Member.create(Member(email, nickname, None))
          j <- 1 to id.toInt
        } DietLog.create(DietLog(id, newDate(-j),
          BigDecimal("7" + i + "." + j),
          BigDecimal("2" + i + "." + j),
          Some(BigDecimal("17" + i + "." + j)),
          Some("メモ: " + i + "," + j)))
      }
      t
    }
  }

  "DietLog#count()" should {
    "memberIdごとの件数を取得できる" in new TestApp {
      DB.withTransaction { implicit c =>
        for (i <- 1L to 5L) {
          DietLog.count(i) must_== i
        }
      }
    }
    "memberId=99のデータは存在しない" in new TestApp {
      DB.withTransaction { implicit c =>
        DietLog.count(99L) must_== 0L
      }
    }
  }

  "DieteLog#list()" should {
    "memberIdごとのリストを取得できる (日時降順でソート)" in new TestApp {
      DB.withTransaction { implicit c =>
        for (i <- 1L to 5L) {
          val list = DietLog.list(i, 0L, 10L)
          list.size must_== i
          for ((log, j) <- list.zipWithIndex) {
            log.weight must_== BigDecimal("7" + i + "." + (j + 1))
            log.fatRate must_== BigDecimal("2" + i + "." + (j + 1))
            log.height must beSome.which(_ == BigDecimal("17" + i + "." + (j + 1)))
            log.note must beSome.which(_ == "メモ: " + i + "," + (j + 1))
          }
        }
      }
    }
    "memberId=99のデータは存在しない" in new TestApp {
      DB.withTransaction { implicit c =>
        DietLog.list(99L, 0L, 10L) must beEmpty
      }
    }
    "全5件のデータ (memberId=5) をページサイズ3でペジネーション" in new TestApp {
      DB.withTransaction { implicit c =>
        val list0 = DietLog.list(5L, 0L, 3L)
        list0.size must_== 3
        for ((log, j) <- list0.zipWithIndex) {
          log.weight must_== BigDecimal("75." + (j + 1))
          log.fatRate must_== BigDecimal("25." + (j + 1))
          log.height must beSome.which(_ == BigDecimal("175." + (j + 1)))
          log.note must beSome.which(_ == "メモ: 5," + (j + 1))
        }
        val list1 = DietLog.list(5L, 1L, 3L)
        list1.size must_== 2
        for ((log, j) <- list1.zipWithIndex) {
          log.weight must_== BigDecimal("75." + (j + 4))
          log.fatRate must_== BigDecimal("25." + (j + 4))
          log.height must beSome.which(_ == BigDecimal("175." + (j + 4)))
          log.note must beSome.which(_ == "メモ: 5," + (j + 4))
        }
        DietLog.list(5L, 2L, 3L) must beEmpty
      }
    }
  }

}
