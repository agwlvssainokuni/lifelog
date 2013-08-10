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

  def newDate(i: Int) = Calendar.getInstance() match {
    case cal =>
      cal.add(DATE, i)
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

}
