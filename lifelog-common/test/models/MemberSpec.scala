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

import java.util.Calendar
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.MONTH
import java.util.Calendar.YEAR
import java.util.Date

import org.specs2.execute.AsResult
import org.specs2.execute.Result
import org.specs2.mutable.Specification

import play.api.db._
import play.api.test.WithApplication

class MemberSpec extends Specification {

  private def newDate(year: Int, month: Int, day: Int): Date =
    Calendar.getInstance() match {
      case cal =>
        cal.set(YEAR, year)
        cal.set(MONTH, month - 1)
        cal.set(DAY_OF_MONTH, day)
        cal.getTime()
    }

  abstract class TestApp extends WithApplication {
    override def around[T: AsResult](t: => T): Result = super.around {
      DB.withTransaction { implicit c =>
        for {
          id <- 1 to 10
          email = "name" + id + "@domain" + id
          nickname = "ニックネーム" + id
          birthday = newDate(1980 + id, id, id)
        } Member.create(Member(email, nickname, Some(birthday))) must beSome.which(_ == id)
      }
      t
    }
  }

  "Member#count()" should {
    "データ数は10件" in new TestApp {
      DB.withTransaction { implicit c =>
        Member.count() must_== 10
      }
    }
  }

  "Member#list()" should {
    "0ページ目は5件" in new TestApp {
      DB.withTransaction { implicit c =>
        val list = Member.list(0, 5)
        list.size must_== 5
        for {
          (Member(email, _, _), i) <- list.zipWithIndex
        } email must equalTo("name" + (i + 1) + "@domain" + (i + 1))
      }
    }
    "1ページ目は5件" in new TestApp {
      DB.withTransaction { implicit c =>
        val list = Member.list(1L, 5L)
        list.size must_== 5
        for {
          (Member(email, _, _), i) <- list.zipWithIndex
        } email must equalTo("name" + (i + 6) + "@domain" + (i + 6))
      }
    }
    "2ページ目は0件" in new TestApp {
      DB.withTransaction { implicit c =>
        val list = Member.list(2, 5)
        list must beEmpty
      }
    }
  }

  "Member#find()" should {
    "1〜10はname1@domain1〜name10@domain10" in new TestApp {
      DB.withTransaction { implicit c =>
        for (id <- 1L to 10L)
          Member.find(id) must beSome.which {
            case Member(email, _, _) => email must_== ("name" + id + "@domain" + id)
          }
      }
    }
    "99は存在しない" in new TestApp {
      DB.withTransaction { implicit c =>
        Member.find(99L) must beNone
      }
    }
  }

  "Member#update()" should {
    "id=1のデータを更新できる" in new TestApp {
      DB.withTransaction { implicit c =>
        Member.update(1L, Member("nameA@domainA", "ニックネームA", None)) must beTrue
        Member.find(1L) must beSome.which {
          case Member(email, nickname, birthday) =>
            email must_== "nameA@domainA"
            nickname must_== "ニックネームA"
            birthday must beNone
        }
      }
    }
    "id=99のデータは存在しないので更新できない" in new TestApp {
      DB.withTransaction { implicit c =>
        Member.update(99L, Member("nameA@domainA", "ニックネームA", None)) must beFalse
        Member.find(99L) must beNone
      }
    }
  }

  "Member#updatePw()" should {
    "id=1のデータを更新できる" in new TestApp {
      DB.withTransaction { implicit c =>
        Member.authenticate("name1@domain1", "password") must beNone
        Member.updatePw(1L, "password") must beTrue
        Member.authenticate("name1@domain1", "password") must beSome
      }
    }
    "id=99のデータは存在しないので更新できない" in new TestApp {
      DB.withTransaction { implicit c =>
        Member.updatePw(99L, "password") must beFalse
      }
    }
  }

  "Member#delete()" should {
    "id=1のデータを削除できる" in new TestApp {
      DB.withTransaction { implicit c =>
        Member.count() must_== 10L
        Member.delete(1L) must beTrue
        Member.count() must_== 9L
      }
    }
    "id=99のデータは存在しないので削除できない" in new TestApp {
      DB.withTransaction { implicit c =>
        Member.count() must_== 10L
        Member.delete(99L) must beFalse
        Member.count() must_== 10L
      }
    }
  }

  "Member#authenticate()" should {
    "name1@domain1で認証できる" in new TestApp {
      DB.withTransaction { implicit c =>
        Member.authenticate("name1@domain1", "password") must beNone
        Member.updatePw(1L, "password") must beTrue
        Member.authenticate("name1@domain1", "password") must beSome
      }
    }
  }

  "Member#tryLock()" should {
    "id=1のデータをロックできる" in new TestApp {
      DB.withTransaction { implicit c =>
        Member.tryLock(1L) must beSome.which(_ == 1L)
      }
    }
    "id=99のデータは存在しないのでロックできない" in new TestApp {
      DB.withTransaction { implicit c =>
        Member.tryLock(99L) must beNone
      }
    }
  }

  "Member#exists()" should {
    "name1@domain1は存在する" in new TestApp {
      DB.withTransaction { implicit c =>
        Member.exists("name1@domain1") must beSome.which(_ == 1L)
      }
    }
    "name0@domain0は存在しない" in new TestApp {
      DB.withTransaction { implicit c =>
        Member.exists("name0@domain0") must beNone
      }
    }
  }

}
