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

package controllers

import java.util.Calendar
import java.util.Calendar._
import java.util.Date

import org.specs2.execute.AsResult
import org.specs2.execute.Result
import org.specs2.mutable.Specification

import FlashUtil._
import models._
import play.api.db._
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

class MemberControllerSpec extends Specification {

  val session = Security.username -> "1"

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
          i <- 1 to 10
          email = "name" + i + "@domain" + i
          nickname = "ニックネーム" + i
          birthday = newDate(1980 + i, i, i)
        } Member.create(Member(email, nickname, Some(birthday)))
      }
      t
    }
  }

  "未ログインの場合は、ログイン画面に転送される" should {
    "/members" in new TestApp {
      val res = route(FakeRequest(GET, "/members")).get
      status(res) must equalTo(SEE_OTHER)
      header(LOCATION, res) must beSome.which(_ == "/login")
      flash(res).get("uri") must beSome.which(_ == "/members")
    }
    "/members/add" in new TestApp {
      route(FakeRequest(GET, "/members/add")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get("uri") must beSome.which(_ == "/members/add")
      }
    }
    "/members/add" in new TestApp {
      route(FakeRequest(POST, "/members/add")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get("uri") must beSome.which(_ == "/members/add")
      }
    }
    "/members/1" in new TestApp {
      route(FakeRequest(GET, "/members/1")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get("uri") must beSome.which(_ == "/members/1")
      }
    }
    "/members/1" in new TestApp {
      route(FakeRequest(POST, "/members/1")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get("uri") must beSome.which(_ == "/members/1")
      }
    }
    "/members/1/passwd" in new TestApp {
      route(FakeRequest(GET, "/members/1/passwd")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get("uri") must beSome.which(_ == "/members/1/passwd")
      }
    }
    "/members/1/passwd" in new TestApp {
      route(FakeRequest(POST, "/members/1/passwd")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get("uri") must beSome.which(_ == "/members/1/passwd")
      }
    }
    "/members/1/delete" in new TestApp {
      route(FakeRequest(GET, "/members/1/delete")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get("uri") must beSome.which(_ == "/members/1/delete")
      }
    }
  }

  "対象の存在しない id を指定すると 404 (Not Found)。" should {
    "edit(id): GET /members/999" in new TestApp {
      route(FakeRequest(GET, "/members/999").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(NOT_FOUND)
      }
    }
    "update(id): POST /members/999" in new TestApp {
      route(FakeRequest(POST, "/members/999").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(NOT_FOUND)
      }
    }
    "editPw(id): GET /members/999/passwd" in new TestApp {
      route(FakeRequest(GET, "/members/999/passwd").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(NOT_FOUND)
      }
    }
    "updatePw(id): POST /members/999/passwd" in new TestApp {
      route(FakeRequest(POST, "/members/999/passwd").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(NOT_FOUND)
      }
    }
    "delete(id): GET /members/999/delete" in new TestApp {
      route(FakeRequest(GET, "/members/999/delete").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(NOT_FOUND)
      }
    }
  }

  "タイトル" should {
    "/members" in new TestApp {
      route(FakeRequest(GET, "/members").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(OK)
        contentType(res) must beSome.which(_ == "text/html")
        val content = contentAsString(res)
        content must contain("""<title>LifeLog/Admin - メンバーアカウント一覧</title>""")
        content must contain("""<h1>LifeLog/Admin - メンバーアカウント一覧</h1>""")
      }
    }

    "/members/add" in new TestApp {
      route(FakeRequest(GET, "/members/add").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(OK)
        contentType(res) must beSome.which(_ == "text/html")
        val content = contentAsString(res)
        content must contain("""<title>LifeLog/Admin - メンバーアカウント登録</title>""")
        content must contain("""<h1>LifeLog/Admin - メンバーアカウント登録</h1>""")
      }
    }

    "/members/:id" in new TestApp {
      route(FakeRequest(GET, "/members/1").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(OK)
        contentType(res) must beSome.which(_ == "text/html")
        val content = contentAsString(res)
        content must contain("""<title>LifeLog/Admin - メンバーアカウント変更</title>""")
        content must contain("""<h1>LifeLog/Admin - メンバーアカウント変更</h1>""")
      }
    }

    "/members/:id/passwd" in new TestApp {
      route(FakeRequest(GET, "/members/1/passwd").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(OK)
        contentType(res) must beSome.which(_ == "text/html")
        val content = contentAsString(res)
        content must contain("""<title>LifeLog/Admin - メンバーアカウントパスワード</title>""")
        content must contain("""<h1>LifeLog/Admin - メンバーアカウントパスワード</h1>""")
      }
    }
  }

}
