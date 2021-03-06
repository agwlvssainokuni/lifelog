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

import java.util.Date

import org.specs2.execute.AsResult
import org.specs2.execute.Result
import org.specs2.mutable.Specification

import controllers.common.FlashName.Delete
import controllers.common.FlashName.Success
import controllers.common.FlashName.Uri
import models.DietLog
import models.Member
import play.api.db._
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

class DietLogControllerSpec extends Specification {

  val session = Security.username -> "1"
  val session2 = Security.username -> "2"

  abstract class TestApp extends WithApplication {
    override def around[T: AsResult](t: => T): Result = super.around {
      DB.withTransaction { implicit c =>
        Member.create(Member("user1@domain", "ニックネーム", None)) must beSome
        for {
          memberId <- Member.create(Member("user2@domain", "ニックネーム", None))
          i <- 1 to 10
          dtm = new Date
          weight = BigDecimal("75.0")
          fatRate = BigDecimal("20.02")
          height = BigDecimal("185.0")
        } DietLog.create(memberId, DietLog(dtm, weight, fatRate, Some(height), None))
      }
      t
    }
  }

  "未ログインの場合は、ログイン画面に転送される" should {
    "list() GET /dietlogs" in new TestApp {
      route(FakeRequest(GET, "/dietlogs")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get(Uri) must beSome.which(_ == "/dietlogs")
      }
    }
    "add() GET /dietlogs/add" in new TestApp {
      route(FakeRequest(GET, "/dietlogs/add")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get(Uri) must beSome.which(_ == "/dietlogs/add")
      }
    }
    "create() POST /dietlogs/add" in new TestApp {
      route(FakeRequest(POST, "/dietlogs/add")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get(Uri) must beSome.which(_ == "/dietlogs/add")
      }
    }
    "edit(id) GET /dietlogs/1" in new TestApp {
      route(FakeRequest(GET, "/dietlogs/1")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get(Uri) must beSome.which(_ == "/dietlogs/1")
      }
    }
    "update(id) POST /dietlogs/1" in new TestApp {
      route(FakeRequest(POST, "/dietlogs/1")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get(Uri) must beSome.which(_ == "/dietlogs/1")
      }
    }
    "delete(id) GET /dietlogs/1/delete" in new TestApp {
      route(FakeRequest(GET, "/dietlogs/1/delete")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get(Uri) must beSome.which(_ == "/dietlogs/1/delete")
      }
    }
  }

  "対象の存在しない id を指定すると 404 (Not Found)" should {
    "edit(id) GET /dietlogs/999" in new TestApp {
      route(FakeRequest(GET, "/dietlogs/999").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(NOT_FOUND)
      }
    }
    "update(id) POST /dietlogs/999" in new TestApp {
      route(FakeRequest(POST, "/dietlogs/999").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(NOT_FOUND)
      }
    }
    "delete(id) GET /dietlogs/999/delete" in new TestApp {
      route(FakeRequest(GET, "/dietlogs/999/delete").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(NOT_FOUND)
      }
    }
  }

  "所有者の異なる id を指定すると 404 (Not Found)" should {
    "edit(id) GET /dietlogs/1" in new TestApp {
      route(FakeRequest(GET, "/dietlogs/1").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(NOT_FOUND)
      }
    }
    "update(id) POST /dietlogs/1" in new TestApp {
      route(FakeRequest(POST, "/dietlogs/1").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(NOT_FOUND)
      }
    }
    "delete(id) GET /dietlogs/1/delete" in new TestApp {
      route(FakeRequest(GET, "/dietlogs/1/delete").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(NOT_FOUND)
      }
    }
  }

  "タイトル" should {
    "list() GET /dietlogs" in new TestApp {
      route(FakeRequest(GET, "/dietlogs").withSession(session2)) must beSome.which { res =>
        status(res) must equalTo(OK)
        contentType(res) must beSome.which(_ == "text/html")
        val content = contentAsString(res)
        content must contain("""<title>LifeLog - ダイエット記録 一覧</title>""")
        content must contain("""<h1>LifeLog - ダイエット記録 一覧</h1>""")
      }
    }
    "add() GET /dietlogs/add" in new TestApp {
      route(FakeRequest(GET, "/dietlogs/add").withSession(session2)) must beSome.which { res =>
        status(res) must equalTo(OK)
        contentType(res) must beSome.which(_ == "text/html")
        val content = contentAsString(res)
        content must contain("""<title>LifeLog - ダイエット記録 登録</title>""")
        content must contain("""<h1>LifeLog - ダイエット記録 登録</h1>""")
      }
    }
    "edit(id) GET /dietlogs/1" in new TestApp {
      route(FakeRequest(GET, "/dietlogs/1").withSession(session2)) must beSome.which { res =>
        status(res) must equalTo(OK)
        contentType(res) must beSome.which(_ == "text/html")
        val content = contentAsString(res)
        content must contain("""<title>LifeLog - ダイエット記録 変更</title>""")
        content must contain("""<h1>LifeLog - ダイエット記録 変更</h1>""")
      }
    }
  }

  "DietLogController#list()" should {
    "flashメッセージなし" in new TestApp {
      route(FakeRequest(GET, "/dietlogs").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(OK)
        contentType(res) must beSome.which(_ == "text/html")
        val content = contentAsString(res)
        content must not contain ("""<h3 class="success">ダイエット記録を削除しました。</h3>""")
      }
    }
    "flashメッセージあり" in new TestApp {
      route(FakeRequest(GET, "/dietlogs").withSession(session).withFlash(
        Success -> Delete)) must beSome.which { res =>
        status(res) must equalTo(OK)
        contentType(res) must beSome.which(_ == "text/html")
        val content = contentAsString(res)
        content must contain("""<h3 class="success">ダイエット記録を削除しました。</h3>""")
      }
    }
    "ページネーション：データなし" in new TestApp {
      route(FakeRequest(GET, "/dietlogs").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(OK)
        contentType(res) must beSome.which(_ == "text/html")
        val content = contentAsString(res)
        content must contain("""<li><a href="/dietlogs?pn=-1"
			class=" ui-disabled "
			data-icon="arrow-l">PREV</a></li>""")
        content must contain("""<li><a href="/dietlogs?pn=1"
			class=" ui-disabled "
			data-icon="arrow-r">NEXT</a></li>""")
      }
    }
    "ページネーション：データ10件、ページ指定なし" in new TestApp {
      route(FakeRequest(GET, "/dietlogs").withSession(session2)) must beSome.which { res =>
        status(res) must equalTo(OK)
        contentType(res) must beSome.which(_ == "text/html")
        val content = contentAsString(res)
        content must contain("""<li><a href="/dietlogs?pn=-1"
			class=" ui-disabled "
			data-icon="arrow-l">PREV</a></li>""")
        content must contain("""<li><a href="/dietlogs?pn=1"
			class=""
			data-icon="arrow-r">NEXT</a></li>""")
      }
    }
    "ページネーション：データ10件、0ページ目" in new TestApp {
      route(FakeRequest(GET, "/dietlogs?pn=0").withSession(session2)) must beSome.which { res =>
        status(res) must equalTo(OK)
        contentType(res) must beSome.which(_ == "text/html")
        val content = contentAsString(res)
        content must contain("""<li><a href="/dietlogs?pn=-1"
			class=" ui-disabled "
			data-icon="arrow-l">PREV</a></li>""")
        content must contain("""<li><a href="/dietlogs?pn=1"
			class=""
			data-icon="arrow-r">NEXT</a></li>""")
      }
    }
    "ページネーション：データ10件、1ページ目" in new TestApp {
      route(FakeRequest(GET, "/dietlogs?pn=1").withSession(session2)) must beSome.which { res =>
        status(res) must equalTo(OK)
        contentType(res) must beSome.which(_ == "text/html")
        val content = contentAsString(res)
        content must contain("""<li><a href="/dietlogs?pn=0"
			class=""
			data-icon="arrow-l">PREV</a></li>""")
        content must contain("""<li><a href="/dietlogs?pn=2"
			class=" ui-disabled "
			data-icon="arrow-r">NEXT</a></li>""")
      }
    }
    "ページネーション：データ10件、2ページ目 => 1ページ目を表示" in new TestApp {
      route(FakeRequest(GET, "/dietlogs?pn=2").withSession(session2)) must beSome.which { res =>
        status(res) must equalTo(OK)
        contentType(res) must beSome.which(_ == "text/html")
        val content = contentAsString(res)
        content must contain("""<li><a href="/dietlogs?pn=0"
			class=""
			data-icon="arrow-l">PREV</a></li>""")
        content must contain("""<li><a href="/dietlogs?pn=2"
			class=" ui-disabled "
			data-icon="arrow-r">NEXT</a></li>""")
      }
    }
  }

  "DietLogController#add()" should {
    "DUMMY" in new TestApp {}
  }

  "DietLogController#create()" should {
    "DUMMY" in new TestApp {}
  }

  "DietLogController#edit(id)" should {
    "DUMMY" in new TestApp {}
  }

  "DietLogController#update(id)" should {
    "DUMMY" in new TestApp {}
  }

  "DietLogController#delete(id)" should {
    "DUMMY" in new TestApp {}
  }

}
