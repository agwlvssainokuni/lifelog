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
        } {
          DietLog(newDate(-j),
            BigDecimal("7" + i + "." + j),
            BigDecimal("2" + i + "." + j),
            Some(BigDecimal("17" + i + "." + j)),
            Some("メモ: " + i + "," + j)) match {
              case log => DietLog.create(id, log)
            }
        }
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

  "DietLog#last()" should {
    "memberId=5の最新のエンティティを取得できる" in new TestApp {
      DB.withTransaction { implicit c =>
        DietLog.last(5L) must beSome.which { log =>
          log.weight must_== BigDecimal("75.1")
          log.fatRate must_== BigDecimal("25.1")
          log.height must beSome.which(_ == BigDecimal("175.1"))
          log.note must beSome.which(_ == "メモ: 5,1")
        }
      }
    }
    "memberId=99のデータは存在しない" in new TestApp {
      DB.withTransaction { implicit c =>
        DietLog.last(99L) must beNone
      }
    }
  }

  "DietLog#find()" should {
    "memberIdとidを指定してエンティティを取得できる (1, 1)" in new TestApp {
      DB.withTransaction { implicit c =>
        DietLog.find(1L, 1L) must beSome
      }
    }
    "memberIdとidの組合せが不適切な場合はエンティティを取得できない (1, 2)" in new TestApp {
      DB.withTransaction { implicit c =>
        DietLog.find(1L, 2L) must beNone
      }
    }
    "memberIdが存在しない場合はエンティティを取得できない (99L, 1L)" in new TestApp {
      DB.withTransaction { implicit c =>
        DietLog.find(99L, 1L) must beNone
      }
    }
    "idが存在しない場合はエンティティを取得できない (1L, 99L)" in new TestApp {
      DB.withTransaction { implicit c =>
        DietLog.find(1L, 99L) must beNone
      }
    }
    "memberIdとidの両方が存在しない場合はエンティティを取得できない (99L, 99L)" in new TestApp {
      DB.withTransaction { implicit c =>
        DietLog.find(99L, 99L) must beNone
      }
    }
  }

  "DietLog#update()" should {
    "memberIdとidを指定してエンティティを更新できる (1, 1)" in new TestApp {
      DB.withTransaction { implicit c =>
        val orig = DietLog.find(1L, 1L).get
        DietLog(newDate(0), BigDecimal("70.0"), BigDecimal("20.0"), Some(BigDecimal("170.0")), Some("メモ")) match {
          case log => DietLog.update(1L, 1L, log) must beTrue
        }
        DietLog.find(1L, 1L) must beSome.which { log =>
          log.dtm must_!= orig.dtm
          log.weight must_!= orig.weight
          log.fatRate must_!= orig.fatRate
          log.height must beSome.which(h => orig.height must beSome.which(_ != h))
          log.note must beSome.which(n => orig.note must beSome.which(_ != n))
        }
      }
    }
    "memberIdとidの組合せが不適切な場合はエンティティを更新できない (1, 2)" in new TestApp {
      DB.withTransaction { implicit c =>
        DietLog(newDate(0), BigDecimal("70.0"), BigDecimal("20.0"), Some(BigDecimal("170.0")), Some("メモ")) match {
          case log => DietLog.update(1L, 2L, log) must beFalse
        }
      }
    }
    "memberIdが存在しない場合はエンティティを更新できない (99L, 1L)" in new TestApp {
      DB.withTransaction { implicit c =>
        DietLog(newDate(0), BigDecimal("70.0"), BigDecimal("20.0"), Some(BigDecimal("170.0")), Some("メモ")) match {
          case log => DietLog.update(99L, 1L, log) must beFalse
        }
      }
    }
    "idが存在しない場合はエンティティを更新できない (1L, 99L)" in new TestApp {
      DB.withTransaction { implicit c =>
        DietLog(newDate(0), BigDecimal("70.0"), BigDecimal("20.0"), Some(BigDecimal("170.0")), Some("メモ")) match {
          case log => DietLog.update(1L, 99L, log) must beFalse
        }
      }
    }
    "memberIdとidの両方が存在しない場合はエンティティを更新できない (99L, 99L)" in new TestApp {
      DB.withTransaction { implicit c =>
        DietLog(newDate(0), BigDecimal("70.0"), BigDecimal("20.0"), Some(BigDecimal("170.0")), Some("メモ")) match {
          case log => DietLog.update(99L, 99L, log) must beFalse
        }
      }
    }
  }

  "DietLog#delete()" should {
    "memberIdとidを指定してエンティティを削除できる (1, 1)" in new TestApp {
      DB.withTransaction { implicit c =>
        DietLog.find(1L, 1L) must beSome
        DietLog.delete(1L, 1L) must beTrue
        DietLog.find(1L, 1L) must beNone
      }
    }
    "memberIdとidの組合せが不適切な場合はエンティティを削除できない (1, 2)" in new TestApp {
      DB.withTransaction { implicit c =>
        DietLog.delete(1L, 2L) must beFalse
      }
    }
    "memberIdが存在しない場合はエンティティを削除できない (99L, 1L)" in new TestApp {
      DB.withTransaction { implicit c =>
        DietLog.delete(99L, 1L) must beFalse
      }
    }
    "idが存在しない場合はエンティティを削除できない (1L, 99L)" in new TestApp {
      DB.withTransaction { implicit c =>
        DietLog.delete(1L, 99L) must beFalse
      }
    }
    "memberIdとidの両方が存在しない場合はエンティティを削除できない (99L, 99L)" in new TestApp {
      DB.withTransaction { implicit c =>
        DietLog.delete(99L, 99L) must beFalse
      }
    }
  }

}
