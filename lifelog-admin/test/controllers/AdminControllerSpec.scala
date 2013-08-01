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

import org.specs2.execute.AsResult
import org.specs2.execute.Result
import org.specs2.mutable.Specification

import models.Admin
import play.api.db._
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

class AdminControllerSpec extends Specification {

  val session = Security.username -> "0"

  abstract class TestApp extends WithApplication {
    override def around[T: AsResult](t: => T): Result = super.around {
      DB.withTransaction { implicit c =>
        for {
          i <- 1 to 9
          loginId = "login" + i
          nickname = "ニックネーム" + i
        } Admin.create(Admin(loginId, nickname))
      }
      t
    }
  }

  "未ログインの場合は、ログイン画面に転送される" should {
    "/admins" in new TestApp {
      val res = route(FakeRequest(GET, "/admins")).get
      status(res) must equalTo(SEE_OTHER)
      header(LOCATION, res) must beSome.which(_ == "/login")
      flash(res).get("uri") must beSome.which(_ == "/admins")
    }
    "/admins/add" in new TestApp {
      route(FakeRequest(GET, "/admins/add")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get("uri") must beSome.which(_ == "/admins/add")
      }
    }
    "/admins/add" in new TestApp {
      route(FakeRequest(POST, "/admins/add")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get("uri") must beSome.which(_ == "/admins/add")
      }
    }
    "/admins/1" in new TestApp {
      route(FakeRequest(GET, "/admins/1")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get("uri") must beSome.which(_ == "/admins/1")
      }
    }
    "/admins/1" in new TestApp {
      route(FakeRequest(POST, "/admins/1")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get("uri") must beSome.which(_ == "/admins/1")
      }
    }
    "/admins/1/passwd" in new TestApp {
      route(FakeRequest(GET, "/admins/1/passwd")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get("uri") must beSome.which(_ == "/admins/1/passwd")
      }
    }
    "/admins/1/passwd" in new TestApp {
      route(FakeRequest(POST, "/admins/1/passwd")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get("uri") must beSome.which(_ == "/admins/1/passwd")
      }
    }
    "/admins/1/delete" in new TestApp {
      route(FakeRequest(GET, "/admins/1/delete")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get("uri") must beSome.which(_ == "/admins/1/delete")
      }
    }
  }

  "対象の存在しない id を指定すると 404 (Not Found)。" should {
    "edit(id): GET /admins/999" in new TestApp {
      route(FakeRequest(GET, "/admins/999").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(NOT_FOUND)
      }
    }
    "update(id): POST /admins/999" in new TestApp {
      route(FakeRequest(POST, "/admins/999").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(NOT_FOUND)
      }
    }
    "editPw(id): GET /admins/999/passwd" in new TestApp {
      route(FakeRequest(GET, "/admins/999/passwd").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(NOT_FOUND)
      }
    }
    "updatePw(id): POST /admins/999/passwd" in new TestApp {
      route(FakeRequest(POST, "/admins/999/passwd").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(NOT_FOUND)
      }
    }
    "delete(id): GET /admins/999/delete" in new TestApp {
      route(FakeRequest(GET, "/admins/999/delete").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(NOT_FOUND)
      }
    }
  }

  "自分と同じ id を指定すると、一覧画面に転送される。" should {
    "edit(id): GET /admins/0" in new TestApp {
      route(FakeRequest(GET, "/admins/0").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/admins")
        flash(res).get("error") must beSome.which(_ == "notPermitted")
      }
    }
    "update(id): POST /admins/0" in new TestApp {
      route(FakeRequest(POST, "/admins/0").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/admins")
        flash(res).get("error") must beSome.which(_ == "notPermitted")
      }
    }
    "editPw(id): GET /admins/0/passwd" in new TestApp {
      route(FakeRequest(GET, "/admins/0/passwd").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/admins")
        flash(res).get("error") must beSome.which(_ == "notPermitted")
      }
    }
    "updatePw(id): POST /admins/0/passwd" in new TestApp {
      route(FakeRequest(POST, "/admins/0/passwd").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/admins")
        flash(res).get("error") must beSome.which(_ == "notPermitted")
      }
    }
    "delete(id): GET /admins/0/delete" in new TestApp {
      route(FakeRequest(GET, "/admins/0/delete").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/admins")
        flash(res).get("error") must beSome.which(_ == "notPermitted")
      }
    }
  }

  "タイトル" should {
    "/admins" in new TestApp {
      route(FakeRequest(GET, "/admins").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(OK)
        contentType(res) must beSome.which(_ == "text/html")
        val content = contentAsString(res)
        content must contain("""<title>LifeLog/Admin - 管理アカウント一覧</title>""")
        content must contain("""<h1>LifeLog/Admin - 管理アカウント一覧</h1>""")
      }
    }

    "/admins/add" in new TestApp {
      route(FakeRequest(GET, "/admins/add").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(OK)
        contentType(res) must beSome.which(_ == "text/html")
        val content = contentAsString(res)
        content must contain("""<title>LifeLog/Admin - 管理アカウント登録</title>""")
        content must contain("""<h1>LifeLog/Admin - 管理アカウント登録</h1>""")
      }
    }

    "/admins/:id" in new TestApp {
      route(FakeRequest(GET, "/admins/1").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(OK)
        contentType(res) must beSome.which(_ == "text/html")
        val content = contentAsString(res)
        content must contain("""<title>LifeLog/Admin - 管理アカウント変更</title>""")
        content must contain("""<h1>LifeLog/Admin - 管理アカウント変更</h1>""")
      }
    }

    "/admins/:id/passwd" in new TestApp {
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

    "FORM構造が表示される。" in new TestApp {
      route(FakeRequest(GET, "/admins/add").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must not contain ("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/admins/add" method="POST" data-ajax="false">""")
        content must contain("""<label for="loginId" class="">ログインID</label>""")
        content must contain("""<input type="text" id="loginId" name="loginId" value="" >""")
        content must contain("""<label for="nickname" class="">ニックネーム</label>""")
        content must contain("""<input type="text" id="nickname" name="nickname" value="" >""")
        content must contain("""<input type="submit" value="登録する" data-theme="a" />""")
      }
    }
  }

  "AdminController#create()" should {

    "入力値が適正ならば、/admins/:id に転送される。" in new TestApp {
      route(FakeRequest(POST, "/admins/add").withSession(session).withFormUrlEncodedBody(
        "loginId" -> "login000", "nickname" -> "nickname000")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/admins/10")
        flash(res).get("success") must beSome.which(_ == "create")
      }
    }

    "ログインIDが入力不正(必須NG)ならば、再入力を促す。" in new TestApp {
      route(FakeRequest(POST, "/admins/add").withSession(session).withFormUrlEncodedBody(
        "loginId" -> "", "nickname" -> "nickname000")) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must contain("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/admins/add" method="POST" data-ajax="false">""")
        content must contain("""<label for="loginId" class="error">ログインID</label>""")
        content must contain("""<input type="text" id="loginId" name="loginId" value="" >""")
        content must contain("""<label for="nickname" class="">ニックネーム</label>""")
        content must contain("""<input type="text" id="nickname" name="nickname" value="nickname000" >""")
        content must contain("""<input type="submit" value="登録する" data-theme="a" />""")
      }
    }

    "ニックネームが入力不正(必須NG)ならば、再入力を促す。" in new TestApp {
      route(FakeRequest(POST, "/admins/add").withSession(session).withFormUrlEncodedBody(
        "loginId" -> "login000", "nickname" -> "")) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must contain("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/admins/add" method="POST" data-ajax="false">""")
        content must contain("""<label for="loginId" class="">ログインID</label>""")
        content must contain("""<input type="text" id="loginId" name="loginId" value="login000" >""")
        content must contain("""<label for="nickname" class="error">ニックネーム</label>""")
        content must contain("""<input type="text" id="nickname" name="nickname" value="" >""")
        content must contain("""<input type="submit" value="登録する" data-theme="a" />""")
      }
    }
  }

  "AdminController#edit(id)" should {

    "FORM構造が表示される。" in new TestApp {
      route(FakeRequest(GET, "/admins/1").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must not contain ("""<h3 class="success">管理アカウントを登録しました。</h3>""")
        content must not contain ("""<h3 class="success">管理アカウントを変更しました。</h3>""")
        content must not contain ("""<h3 class="success">パスワードを変更しました。</h3>""")
        content must not contain ("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/admins/1" method="POST" data-ajax="false">""")
        content must contain("""<label for="loginId" class="">ログインID</label>""")
        content must contain("""<input type="text" id="loginId" name="loginId" value="login1" >""")
        content must contain("""<label for="nickname" class="">ニックネーム</label>""")
        content must contain("""<input type="text" id="nickname" name="nickname" value="ニックネーム1" >""")
        content must contain("""<input type="submit" value="変更する" data-theme="a" />""")
      }
    }

    "flashメッセージ：管理アカウント登録後。" in new TestApp {
      route(FakeRequest(GET, "/admins/1").withSession(session).withFlash(
        "success" -> "create")) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must contain("""<h3 class="success">管理アカウントを登録しました。</h3>""")
        content must not contain ("""<h3 class="success">管理アカウントを変更しました。</h3>""")
        content must not contain ("""<h3 class="success">パスワードを変更しました。</h3>""")
        content must not contain ("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
      }
    }

    "flashメッセージ：管理アカウント変更後。" in new TestApp {
      route(FakeRequest(GET, "/admins/1").withSession(session).withFlash(
        "success" -> "update")) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must not contain ("""<h3 class="success">管理アカウントを登録しました。</h3>""")
        content must contain("""<h3 class="success">管理アカウントを変更しました。</h3>""")
        content must not contain ("""<h3 class="success">パスワードを変更しました。</h3>""")
        content must not contain ("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
      }
    }

    "flashメッセージ：パスワード変更後。" in new TestApp {
      route(FakeRequest(GET, "/admins/1").withSession(session).withFlash(
        "success" -> "updatePw")) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must not contain ("""<h3 class="success">管理アカウントを登録しました。</h3>""")
        content must not contain ("""<h3 class="success">管理アカウントを変更しました。</h3>""")
        content must contain("""<h3 class="success">パスワードを変更しました。</h3>""")
        content must not contain ("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
      }
    }
  }

  "AdminController#update(id)" should {

    "入力値が適正ならば、/admins/:id に転送される。" in new TestApp {
      route(FakeRequest(POST, "/admins/1").withSession(session).withFormUrlEncodedBody(
        "loginId" -> "login000", "nickname" -> "nickname000")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/admins/1")
        flash(res).get("success") must beSome.which(_ == "update")
      }
    }

    "ログインIDが入力不正(必須NG)ならば、再入力を促す。" in new TestApp {
      route(FakeRequest(POST, "/admins/1").withSession(session).withFormUrlEncodedBody(
        "loginId" -> "", "nickname" -> "nickname000")) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must contain("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/admins/1" method="POST" data-ajax="false">""")
        content must contain("""<label for="loginId" class="error">ログインID</label>""")
        content must contain("""<input type="text" id="loginId" name="loginId" value="" >""")
        content must contain("""<label for="nickname" class="">ニックネーム</label>""")
        content must contain("""<input type="text" id="nickname" name="nickname" value="nickname000" >""")
        content must contain("""<input type="submit" value="変更する" data-theme="a" />""")
      }
    }

    "ニックネームが入力不正(必須NG)ならば、再入力を促す。" in new TestApp {
      route(FakeRequest(POST, "/admins/1").withSession(session).withFormUrlEncodedBody(
        "loginId" -> "login000", "nickname" -> "")) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must contain("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/admins/1" method="POST" data-ajax="false">""")
        content must contain("""<label for="loginId" class="">ログインID</label>""")
        content must contain("""<input type="text" id="loginId" name="loginId" value="login000" >""")
        content must contain("""<label for="nickname" class="error">ニックネーム</label>""")
        content must contain("""<input type="text" id="nickname" name="nickname" value="" >""")
        content must contain("""<input type="submit" value="変更する" data-theme="a" />""")
      }
    }
  }

  "AdminController#editPw(id)" should {

    "FORM構造が表示される。" in new TestApp {
      route(FakeRequest(GET, "/admins/1/passwd").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must not contain ("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/admins/1/passwd" method="POST" data-ajax="false">""")
        content must contain("""<label for="passwd" class="">パスワード</label>""")
        content must contain("""<input type="password" id="passwd" name="passwd" >""")
        content must contain("""<label for="passwdConf" class="">確認</label>""")
        content must contain("""<input type="password" id="passwdConf" name="passwdConf" >""")
        content must contain("""<input type="submit" value="変更する" data-theme="a" />""")
      }
    }
  }

  "AdminController#updatePw(id)" should {

    "入力値が適正ならば、/admins/:id に転送される。" in new TestApp {
      route(FakeRequest(POST, "/admins/1/passwd").withSession(session).withFormUrlEncodedBody(
        "passwd" -> "passwd000", "passwdConf" -> "passwd000")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/admins/1")
        flash(res).get("success") must beSome.which(_ == "updatePw")
      }
    }

    "パスワードが入力不正(必須NG)ならば、再入力を促す。" in new TestApp {
      route(FakeRequest(POST, "/admins/1/passwd").withSession(session).withFormUrlEncodedBody(
        "passwd" -> "", "passwdConf" -> "passwd000")) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must contain("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/admins/1/passwd" method="POST" data-ajax="false">""")
        content must contain("""<label for="passwd" class="error">パスワード</label>""")
        content must contain("""<input type="password" id="passwd" name="passwd" >""")
        content must contain("""<label for="passwdConf" class="">確認</label>""")
        content must contain("""<input type="password" id="passwdConf" name="passwdConf" >""")
        content must contain("""<input type="submit" value="変更する" data-theme="a" />""")
      }
    }

    "確認が入力不正(必須NG)ならば、再入力を促す。" in new TestApp {
      route(FakeRequest(POST, "/admins/1/passwd").withSession(session).withFormUrlEncodedBody(
        "passwd" -> "passwd000", "passwdConf" -> "")) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must contain("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/admins/1/passwd" method="POST" data-ajax="false">""")
        content must contain("""<label for="passwd" class="">パスワード</label>""")
        content must contain("""<input type="password" id="passwd" name="passwd" >""")
        content must contain("""<label for="passwdConf" class="error">確認</label>""")
        content must contain("""<input type="password" id="passwdConf" name="passwdConf" >""")
        content must contain("""<input type="submit" value="変更する" data-theme="a" />""")
      }
    }

    "パスワードと確認が同じでなければ、再入力を促す。" in new TestApp {
      route(FakeRequest(POST, "/admins/1/passwd").withSession(session).withFormUrlEncodedBody(
        "passwd" -> "passwd000", "passwdConf" -> "passwd001")) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must contain("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/admins/1/passwd" method="POST" data-ajax="false">""")
        content must contain("""<label for="passwd" class="">パスワード</label>""")
        content must contain("""<input type="password" id="passwd" name="passwd" >""")
        content must contain("""<label for="passwdConf" class="">確認</label>""")
        content must contain("""<input type="password" id="passwdConf" name="passwdConf" >""")
        content must contain("""<input type="submit" value="変更する" data-theme="a" />""")
      }
    }
  }

  "AdminController#delete(id)" should {

    "/admins に転送される。" in new TestApp {
      route(FakeRequest(GET, "/admins/1/delete").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/admins")
        flash(res).get("success") must beSome.which(_ == "delete")
      }
    }
  }

}
