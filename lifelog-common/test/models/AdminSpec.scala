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

import org.specs2.execute.AsResult
import org.specs2.execute.Result
import org.specs2.mutable.Specification

import play.api.db.DB
import play.api.test.WithApplication

class AdminSpec extends Specification {

  abstract class TestApp extends WithApplication {
    override def around[T: AsResult](t: => T): Result = super.around {
      DB.withTransaction { implicit c =>
        for {
          id <- 1 to 9
          loginId = "login" + id
          nickname = "ニックネーム" + id
        } Admin.create(Admin(loginId, nickname))
      }
      t
    }
  }

  "Admin#count()" should {
    "データ数は10件" in new TestApp {
      DB.withTransaction { implicit c =>
        Admin.count() must_== 10L
      }
    }
  }

  "Admin#list()" should {
    "0ページ目は5件" in new TestApp {
      DB.withTransaction { implicit c =>
        val list = Admin.list(0, 5)
        list.size must_== 5
        for {
          (Admin(loginId, _), i) <- list.zipWithIndex
          if (i > 0)
        } loginId must_== "login" + i
      }
    }
    "1ページ目は5件" in new TestApp {
      DB.withTransaction { implicit c =>
        val list = Admin.list(1, 5)
        list.size must_== 5
        for {
          (Admin(loginId, _), i) <- list.zipWithIndex
        } loginId must_== "login" + (i + 5)
      }
    }
    "2ページ目は0件" in new TestApp {
      DB.withTransaction { implicit c =>
        val list = Admin.list(2, 5)
        list must beEmpty
      }
    }
  }

  "Admin#find()" should {
    "0はsuperadmin" in new TestApp {
      DB.withTransaction { implicit c =>
        Admin.find(0L) must beSome.which {
          case Admin(loginId, _) => loginId must_== "superadmin"
        }
      }
    }
    "1〜9はlogin1〜login9" in new TestApp {
      DB.withTransaction { implicit c =>
        for (id <- 1L to 9L)
          Admin.find(id) must beSome.which {
            case Admin(loginId, _) => loginId must_== "login" + id
          }
      }
    }
    "10は存在しない" in new TestApp {
      DB.withTransaction { implicit c =>
        Admin.find(10L) must beNone
      }
    }
  }

  "Admin#update()" should {
    "id=1のデータを更新できる" in new TestApp {
      DB.withTransaction { implicit c =>
        Admin.update(1L, Admin("loginA", "ニックネームA")) must beTrue
        Admin.find(1L) must beSome.which {
          case Admin(loginId, nickname) =>
            loginId must_== "loginA"
            nickname must_== "ニックネームA"
        }
      }
    }
    "id=10のデータは存在しないので更新できない" in new TestApp {
      DB.withTransaction { implicit c =>
        Admin.update(10L, Admin("loginZ", "ニックネームZ")) must beFalse
        Admin.find(10L) must beNone
      }
    }
  }

  "Admin#updatePw()" should {
    "id=1のデータを更新できる" in new TestApp {
      DB.withTransaction { implicit c =>
        Admin.authenticate("login1", "password") must beNone
        Admin.updatePw(1L, "password") must beTrue
        Admin.authenticate("login1", "password") must beSome
      }
    }
    "id=10のデータは存在しないので更新できない" in new TestApp {
      DB.withTransaction { implicit c =>
        Admin.updatePw(10L, "password") must beFalse
      }
    }
  }

  "Admin#delete()" should {
    "id=1のデータを削除できる" in new TestApp {
      DB.withTransaction { implicit c =>
        Admin.count() must_== 10L
        Admin.delete(1L) must beTrue
        Admin.count() must_== 9L
      }
    }
    "id=10のデータは存在しないので削除できない" in new TestApp {
      DB.withTransaction { implicit c =>
        Admin.count() must_== 10L
        Admin.delete(10L) must beFalse
        Admin.count() must_== 10L
      }
    }
  }

  "Admin#authenticate()" should {
    "login1で認証できる" in new TestApp {
      DB.withTransaction { implicit c =>
        Admin.authenticate("login1", "password") must beNone
        Admin.updatePw(1L, "password") must beTrue
        Admin.authenticate("login1", "password") must beSome
      }
    }
  }

  "Admin#tryLock()" should {
    "id=1のデータをロックできる" in new TestApp {
      DB.withTransaction { implicit c =>
        Admin.tryLock(1L) must beSome.which(_ == 1L)
      }
    }
    "id=10のデータは存在しないのでロックできない" in new TestApp {
      DB.withTransaction { implicit c =>
        Admin.tryLock(10L) must beNone
      }
    }
  }

  "Admin#exists()" should {
    "login1は存在する" in new TestApp {
      DB.withTransaction { implicit c =>
        Admin.exists("login1") must beSome.which(_ == 1)
      }
    }
    "login10は存在しない" in new TestApp {
      DB.withTransaction { implicit c =>
        Admin.exists("login10") must beNone
      }
    }
  }

}
