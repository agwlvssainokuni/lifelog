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

import controllers.ProfileForm.CONFIRM
import controllers.ProfileForm.LOGIN_ID
import controllers.ProfileForm.NICKNAME
import controllers.ProfileForm.PASSWORD
import controllers.common.FlashName.Success
import controllers.common.FlashName.Update
import controllers.common.FlashName.UpdatePw
import controllers.common.FlashName.Uri
import models.Admin
import play.api.db._
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

class ProfileControllerSpec extends Specification {

  val session = Security.username -> "1"

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
    "/profile" in new TestApp {
      val res = route(FakeRequest(GET, "/profile")).get
      status(res) must equalTo(SEE_OTHER)
      header(LOCATION, res) must beSome.which(_ == "/login")
      flash(res).get(Uri) must beSome.which(_ == "/profile")
    }
    "/profile" in new TestApp {
      val res = route(FakeRequest(POST, "/profile")).get
      status(res) must equalTo(SEE_OTHER)
      header(LOCATION, res) must beSome.which(_ == "/login")
      flash(res).get(Uri) must beSome.which(_ == "/profile")
    }
    "/profile/passwd" in new TestApp {
      route(FakeRequest(GET, "/profile/passwd")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get(Uri) must beSome.which(_ == "/profile/passwd")
      }
    }
    "/profile/passwd" in new TestApp {
      route(FakeRequest(POST, "/profile/passwd")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get(Uri) must beSome.which(_ == "/profile/passwd")
      }
    }
  }

  "タイトル" should {
    "/profile" in new TestApp {
      route(FakeRequest(GET, "/profile").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(OK)
        contentType(res) must beSome.which(_ == "text/html")
        val content = contentAsString(res)
        content must contain("""<title>LifeLog/Admin - プロファイル変更</title>""")
        content must contain("""<h1>LifeLog/Admin - プロファイル変更</h1>""")
      }
    }
    "/profile/passwd" in new TestApp {
      route(FakeRequest(GET, "/profile/passwd").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(OK)
        contentType(res) must beSome.which(_ == "text/html")
        val content = contentAsString(res)
        content must contain("""<title>LifeLog/Admin - パスワード変更</title>""")
        content must contain("""<h1>LifeLog/Admin - パスワード変更</h1>""")
      }
    }
  }

  "対象の存在しないアカウントでログインしていると 404 (Not Found)。" should {
    val session = Security.username -> "999"
    "edit(): GET /profile" in new TestApp {
      route(FakeRequest(GET, "/profile").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(NOT_FOUND)
      }
    }
    "update(): POST /profile" in new TestApp {
      route(FakeRequest(POST, "/profile").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(NOT_FOUND)
      }
    }
    "editPw(): GET /profile/passwd" in new TestApp {
      route(FakeRequest(GET, "/profile/passwd").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(NOT_FOUND)
      }
    }
    "updatePw(): POST /profile/passwd" in new TestApp {
      route(FakeRequest(POST, "/profile/passwd").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(NOT_FOUND)
      }
    }
  }

  "ProfileController#edit()" should {

    "FORM構造が表示される。" in new TestApp {
      route(FakeRequest(GET, "/profile").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must not contain ("""<h3 class="success">プロファイルを変更しました。</h3>""")
        content must not contain ("""<h3 class="success">パスワードを変更しました。</h3>""")
        content must not contain ("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/profile" method="POST" data-ajax="false">""")
        content must contain("""<label for="loginId" class="">ログインID</label>""")
        content must contain("""<input type="text" id="loginId" name="loginId" value="login1" readonly="true">""")
        content must contain("""<label for="nickname" class="">ニックネーム</label>""")
        content must contain("""<input type="text" id="nickname" name="nickname" value="ニックネーム1" >""")
        content must contain("""<input type="submit" value="変更する" data-theme="a" />""")
      }
    }

    "flashメッセージ：プロファイル変更後。" in new TestApp {
      route(FakeRequest(GET, "/profile").withSession(session).withFlash(
        Success -> Update)) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must contain("""<h3 class="success">プロファイルを変更しました。</h3>""")
        content must not contain ("""<h3 class="success">パスワードを変更しました。</h3>""")
        content must not contain ("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
      }
    }

    "flashメッセージ：パスワード変更後。" in new TestApp {
      route(FakeRequest(GET, "/profile").withSession(session).withFlash(
        Success -> UpdatePw)) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must not contain ("""<h3 class="success">プロファイルを変更しました。</h3>""")
        content must contain("""<h3 class="success">パスワードを変更しました。</h3>""")
        content must not contain ("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
      }
    }
  }

  "ProfileController#update()" should {

    "入力値が適正ならば、/profile に転送される。" in new TestApp {
      route(FakeRequest(POST, "/profile").withSession(session).withFormUrlEncodedBody(
        LOGIN_ID -> "login000", NICKNAME -> "nickname000")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/profile")
        flash(res).get(Success) must beSome.which(_ == Update)
      }
    }

    "ニックネームが入力不正(必須NG)ならば、再入力を促す。" in new TestApp {
      route(FakeRequest(POST, "/profile").withSession(session).withFormUrlEncodedBody(
        LOGIN_ID -> "login000", NICKNAME -> "")) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must contain("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/profile" method="POST" data-ajax="false">""")
        content must contain("""<label for="loginId" class="">ログインID</label>""")
        content must contain("""<input type="text" id="loginId" name="loginId" value="login000" readonly="true">""")
        content must contain("""<label for="nickname" class="error">ニックネーム</label>""")
        content must contain("""<input type="text" id="nickname" name="nickname" value="" >""")
        content must contain("""<input type="submit" value="変更する" data-theme="a" />""")
      }
    }
  }

  "ProfileController#editPw()" should {

    "FORM構造が表示される。" in new TestApp {
      route(FakeRequest(GET, "/profile/passwd").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must not contain ("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/profile/passwd" method="POST" data-ajax="false">""")
        content must contain("""<label for="password" class="">パスワード</label>""")
        content must contain("""<input type="password" id="password" name="password" >""")
        content must contain("""<label for="confirm" class="">確認</label>""")
        content must contain("""<input type="password" id="confirm" name="confirm" >""")
        content must contain("""<input type="submit" value="変更する" data-theme="a" />""")
      }
    }
  }

  "ProfileController#updatePw()" should {

    "入力値が適正ならば、/profile に転送される。" in new TestApp {
      route(FakeRequest(POST, "/profile/passwd").withSession(session).withFormUrlEncodedBody(
        PASSWORD -> "passwd000", CONFIRM -> "passwd000")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/profile")
        flash(res).get(Success) must beSome.which(_ == UpdatePw)
      }
    }

    "パスワードが入力不正(必須NG)ならば、再入力を促す。" in new TestApp {
      route(FakeRequest(POST, "/profile/passwd").withSession(session).withFormUrlEncodedBody(
        PASSWORD -> "", CONFIRM -> "passwd000")) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must contain("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/profile/passwd" method="POST" data-ajax="false">""")
        content must contain("""<label for="password" class="error">パスワード</label>""")
        content must contain("""<input type="password" id="password" name="password" >""")
        content must contain("""<label for="confirm" class="">確認</label>""")
        content must contain("""<input type="password" id="confirm" name="confirm" >""")
        content must contain("""<input type="submit" value="変更する" data-theme="a" />""")
      }
    }

    "確認が入力不正(必須NG)ならば、再入力を促す。" in new TestApp {
      route(FakeRequest(POST, "/profile/passwd").withSession(session).withFormUrlEncodedBody(
        PASSWORD -> "passwd000", CONFIRM -> "")) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must contain("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/profile/passwd" method="POST" data-ajax="false">""")
        content must contain("""<label for="password" class="">パスワード</label>""")
        content must contain("""<input type="password" id="password" name="password" >""")
        content must contain("""<label for="confirm" class="error">確認</label>""")
        content must contain("""<input type="password" id="confirm" name="confirm" >""")
        content must contain("""<input type="submit" value="変更する" data-theme="a" />""")
      }
    }

    "パスワードと確認が同じでなければ、再入力を促す。" in new TestApp {
      route(FakeRequest(POST, "/profile/passwd").withSession(session).withFormUrlEncodedBody(
        PASSWORD -> "passwd000", CONFIRM -> "passwd001")) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must contain("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/profile/passwd" method="POST" data-ajax="false">""")
        content must contain("""<label for="password" class="">パスワード</label>""")
        content must contain("""<input type="password" id="password" name="password" >""")
        content must contain("""<label for="confirm" class="">確認</label>""")
        content must contain("""<input type="password" id="confirm" name="confirm" >""")
        content must contain("""<input type="submit" value="変更する" data-theme="a" />""")
      }
    }
  }

}
