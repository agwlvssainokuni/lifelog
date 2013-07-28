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

import org.specs2.mutable.Specification

import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

class AdminControllerSpec extends Specification {

  val session = Security.username -> "0"

  "未ログインの場合は、ログイン画面に転送される" should {
    "/admins" in new WithApplication {
      val res = route(FakeRequest(GET, "/admins")).get
      status(res) must equalTo(SEE_OTHER)
      header(LOCATION, res) must beSome.which(_ == "/login")
      flash(res).get("uri") must beSome.which(_ == "/admins")
    }
    "/admins/add" in new WithApplication {
      route(FakeRequest(GET, "/admins/add")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get("uri") must beSome.which(_ == "/admins/add")
      }
    }
    "/admins/add" in new WithApplication {
      route(FakeRequest(POST, "/admins/add")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get("uri") must beSome.which(_ == "/admins/add")
      }
    }
    "/admins/1" in new WithApplication {
      route(FakeRequest(GET, "/admins/1")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get("uri") must beSome.which(_ == "/admins/1")
      }
    }
    "/admins/1" in new WithApplication {
      route(FakeRequest(POST, "/admins/1")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get("uri") must beSome.which(_ == "/admins/1")
      }
    }
    "/admins/1/passwd" in new WithApplication {
      route(FakeRequest(GET, "/admins/1/passwd")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get("uri") must beSome.which(_ == "/admins/1/passwd")
      }
    }
    "/admins/1/passwd" in new WithApplication {
      route(FakeRequest(POST, "/admins/1/passwd")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get("uri") must beSome.which(_ == "/admins/1/passwd")
      }
    }
    "/admins/1/delete" in new WithApplication {
      route(FakeRequest(GET, "/admins/1/delete")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get("uri") must beSome.which(_ == "/admins/1/delete")
      }
    }
  }

  "タイトル" should {
    "/admins" in new WithApplication {
      route(FakeRequest(GET, "/admins").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(OK)
        contentType(res) must beSome.which(_ == "text/html")
        val content = contentAsString(res)
        content must contain("""<title>LifeLog/Admin - 管理アカウント一覧</title>""")
        content must contain("""<h1>LifeLog/Admin - 管理アカウント一覧</h1>""")
      }
    }

    "/admins/add" in new WithApplication {
      route(FakeRequest(GET, "/admins/add").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(OK)
        contentType(res) must beSome.which(_ == "text/html")
        val content = contentAsString(res)
        content must contain("""<title>LifeLog/Admin - 管理アカウント登録</title>""")
        content must contain("""<h1>LifeLog/Admin - 管理アカウント登録</h1>""")
      }
    }

    "/admins/:id" in new WithApplication {
      route(FakeRequest(GET, "/admins/1").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(OK)
        contentType(res) must beSome.which(_ == "text/html")
        val content = contentAsString(res)
        content must contain("""<title>LifeLog/Admin - 管理アカウント変更</title>""")
        content must contain("""<h1>LifeLog/Admin - 管理アカウント変更</h1>""")
      }
    }

    "/admins/:id/passwd" in new WithApplication {
      route(FakeRequest(GET, "/admins/1/passwd").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(OK)
        contentType(res) must beSome.which(_ == "text/html")
        val content = contentAsString(res)
        content must contain("""<title>LifeLog/Admin - 管理アカウントパスワード</title>""")
        content must contain("""<h1>LifeLog/Admin - 管理アカウントパスワード</h1>""")
      }
    }
  }

  "AdminController#list()" should {
  }

  "AdminController#add()" should {

    "FORM構造が表示される。" in new WithApplication {
      route(FakeRequest(GET, "/admins/add").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must contain("""<form action="/admins/add" method="POST" data-ajax="false">""")
        content must contain("""<input type="submit" value="登録する" data-theme="a" />""")
      }
    }
  }

  "AdminController#create()" should {
  }

  "AdminController#edit(id)" should {

    "FORM構造が表示される。" in new WithApplication {
      route(FakeRequest(GET, "/admins/1").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must contain("""<form action="/admins/1" method="POST" data-ajax="false">""")
        content must contain("""<input type="submit" value="変更する" data-theme="a" />""")
      }
    }
  }

  "AdminController#update(id)" should {
  }

  "AdminController#editPw(id)" should {

    "FORM構造が表示される。" in new WithApplication {
      route(FakeRequest(GET, "/admins/1/passwd").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must contain("""<form action="/admins/1/passwd" method="POST" data-ajax="false">""")
        content must contain("""<input type="submit" value="変更する" data-theme="a" />""")
      }
    }
  }

  "AdminController#updatePw(id)" should {
  }

  "AdminController#delete(id)" should {
  }

}
