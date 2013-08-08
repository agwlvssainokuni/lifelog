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

import common.FlashName._
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
      flash(res).get(Uri) must beSome.which(_ == "/members")
    }
    "/members/add" in new TestApp {
      route(FakeRequest(GET, "/members/add")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get(Uri) must beSome.which(_ == "/members/add")
      }
    }
    "/members/add" in new TestApp {
      route(FakeRequest(POST, "/members/add")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get(Uri) must beSome.which(_ == "/members/add")
      }
    }
    "/members/1" in new TestApp {
      route(FakeRequest(GET, "/members/1")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get(Uri) must beSome.which(_ == "/members/1")
      }
    }
    "/members/1" in new TestApp {
      route(FakeRequest(POST, "/members/1")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get(Uri) must beSome.which(_ == "/members/1")
      }
    }
    "/members/1/passwd" in new TestApp {
      route(FakeRequest(GET, "/members/1/passwd")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get(Uri) must beSome.which(_ == "/members/1/passwd")
      }
    }
    "/members/1/passwd" in new TestApp {
      route(FakeRequest(POST, "/members/1/passwd")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get(Uri) must beSome.which(_ == "/members/1/passwd")
      }
    }
    "/members/1/delete" in new TestApp {
      route(FakeRequest(GET, "/members/1/delete")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get(Uri) must beSome.which(_ == "/members/1/delete")
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

  "MemberController#list()" should {
  }

  "MemberController#add()" should {

    "FORM構造が表示される。" in new TestApp {
      route(FakeRequest(GET, "/members/add").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must not contain ("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/members/add" method="POST" data-ajax="false">""")
        content must contain("""<label for="email" class="">メールアドレス</label>""")
        content must contain("""<input type="text" id="email" name="email" value="" >""")
        content must contain("""<label for="nickname" class="">ニックネーム</label>""")
        content must contain("""<input type="text" id="nickname" name="nickname" value="" >""")
        content must contain("""<label for="birthday" class="">生年月日(省略可)</label>""")
        content must contain("""<input type="date" id="birthday" name="birthday" value="" >""")
        content must contain("""<input type="submit" value="登録する" data-theme="a" />""")
      }
    }
  }

  "MemberController#create()" should {

    "入力値が適正ならば、/members/:id に転送される。データが作成される。" in new TestApp {
      route(FakeRequest(POST, "/members/add").withSession(session).withFormUrlEncodedBody(
        "email" -> "name0@domain0", "nickname" -> "nickname000", "birthday" -> "1980/01/01")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/members/11")
        flash(res).get(Success) must beSome.which(_ == Create)
      }
      DB.withTransaction { implicit c =>
        Member.exists("name0@domain0") must beSome
      }
    }

    "入力値が適正ならば、/members/:id に転送される。データが作成される。(生年月日は省略可)" in new TestApp {
      route(FakeRequest(POST, "/members/add").withSession(session).withFormUrlEncodedBody(
        "email" -> "name0@domain0", "nickname" -> "nickname000", "birthday" -> "")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/members/11")
        flash(res).get(Success) must beSome.which(_ == Create)
      }
      DB.withTransaction { implicit c =>
        Member.exists("name0@domain0") must beSome
      }
    }

    "メールアドレスが入力不正(必須NG)ならば、再入力を促す。" in new TestApp {
      route(FakeRequest(POST, "/members/add").withSession(session).withFormUrlEncodedBody(
        "email" -> "", "nickname" -> "nickname000", "birthday" -> "1980/01/01")) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must contain("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/members/add" method="POST" data-ajax="false">""")
        content must contain("""<label for="email" class="error">メールアドレス</label>""")
        content must contain("""<input type="text" id="email" name="email" value="" >""")
        content must contain("""<label for="nickname" class="">ニックネーム</label>""")
        content must contain("""<input type="text" id="nickname" name="nickname" value="nickname000" >""")
        content must contain("""<label for="birthday" class="">生年月日(省略可)</label>""")
        content must contain("""<input type="date" id="birthday" name="birthday" value="1980/01/01" >""")
        content must contain("""<input type="submit" value="登録する" data-theme="a" />""")
      }
    }

    "ニックネームが入力不正(必須NG)ならば、再入力を促す。" in new TestApp {
      route(FakeRequest(POST, "/members/add").withSession(session).withFormUrlEncodedBody(
        "email" -> "name0@domain0", "nickname" -> "", "birthday" -> "1980/01/01")) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must contain("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/members/add" method="POST" data-ajax="false">""")
        content must contain("""<label for="email" class="">メールアドレス</label>""")
        content must contain("""<input type="text" id="email" name="email" value="name0@domain0" >""")
        content must contain("""<label for="nickname" class="error">ニックネーム</label>""")
        content must contain("""<input type="text" id="nickname" name="nickname" value="" >""")
        content must contain("""<label for="birthday" class="">生年月日(省略可)</label>""")
        content must contain("""<input type="date" id="birthday" name="birthday" value="1980/01/01" >""")
        content must contain("""<input type="submit" value="登録する" data-theme="a" />""")
      }
    }

    "メールアドレスが入力不正(形式不正)ならば、再入力を促す。" in new TestApp {
      route(FakeRequest(POST, "/members/add").withSession(session).withFormUrlEncodedBody(
        "email" -> "localpartonly", "nickname" -> "nickname000", "birthday" -> "1980/01/01")) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must contain("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/members/add" method="POST" data-ajax="false">""")
        content must contain("""<label for="email" class="error">メールアドレス</label>""")
        content must contain("""<input type="text" id="email" name="email" value="localpartonly" >""")
        content must contain("""<label for="nickname" class="">ニックネーム</label>""")
        content must contain("""<input type="text" id="nickname" name="nickname" value="nickname000" >""")
        content must contain("""<label for="birthday" class="">生年月日(省略可)</label>""")
        content must contain("""<input type="date" id="birthday" name="birthday" value="1980/01/01" >""")
        content must contain("""<input type="submit" value="登録する" data-theme="a" />""")
      }
    }

    "生年月日が入力不正(形式不正)ならば、再入力を促す。" in new TestApp {
      route(FakeRequest(POST, "/members/add").withSession(session).withFormUrlEncodedBody(
        "email" -> "name0@domain0", "nickname" -> "nickname000", "birthday" -> "invalid")) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must contain("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/members/add" method="POST" data-ajax="false">""")
        content must contain("""<label for="email" class="">メールアドレス</label>""")
        content must contain("""<input type="text" id="email" name="email" value="name0@domain0" >""")
        content must contain("""<label for="nickname" class="">ニックネーム</label>""")
        content must contain("""<input type="text" id="nickname" name="nickname" value="nickname000" >""")
        content must contain("""<label for="birthday" class="error">生年月日(省略可)</label>""")
        content must contain("""<input type="date" id="birthday" name="birthday" value="invalid" >""")
        content must contain("""<input type="submit" value="登録する" data-theme="a" />""")
      }
    }

    "メールアドレスが入力不正(一意性NG)ならば、再入力を促す。" in new TestApp {
      route(FakeRequest(POST, "/members/add").withSession(session).withFormUrlEncodedBody(
        "email" -> "name1@domain1", "nickname" -> "nickname000", "birthday" -> "1980/01/01")) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must contain("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/members/add" method="POST" data-ajax="false">""")
        content must contain("""<label for="email" class="error">メールアドレス</label>""")
        content must contain("""<input type="text" id="email" name="email" value="name1@domain1" >""")
        content must contain("""<label for="nickname" class="">ニックネーム</label>""")
        content must contain("""<input type="text" id="nickname" name="nickname" value="nickname000" >""")
        content must contain("""<label for="birthday" class="">生年月日(省略可)</label>""")
        content must contain("""<input type="date" id="birthday" name="birthday" value="1980/01/01" >""")
        content must contain("""<input type="submit" value="登録する" data-theme="a" />""")
      }
    }
  }

  "MemberController#edit(id)" should {

    "FORM構造が表示される。" in new TestApp {
      route(FakeRequest(GET, "/members/1").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must not contain ("""<h3 class="success">メンバーアカウントを登録しました。</h3>""")
        content must not contain ("""<h3 class="success">メンバーアカウントを変更しました。</h3>""")
        content must not contain ("""<h3 class="success">パスワードを変更しました。</h3>""")
        content must not contain ("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/members/1" method="POST" data-ajax="false">""")
        content must contain("""<label for="email" class="">メールアドレス</label>""")
        content must contain("""<input type="text" id="email" name="email" value="name1@domain1" >""")
        content must contain("""<label for="nickname" class="">ニックネーム</label>""")
        content must contain("""<input type="text" id="nickname" name="nickname" value="ニックネーム1" >""")
        content must contain("""<label for="birthday" class="">生年月日(省略可)</label>""")
        content must contain("""<input type="date" id="birthday" name="birthday" value="1981/01/01" >""")
        content must contain("""<input type="submit" value="変更する" data-theme="a" />""")
      }
    }

    "flashメッセージ：メンバーアカウント登録後。" in new TestApp {
      route(FakeRequest(GET, "/members/1").withSession(session).withFlash(
        Success -> Create)) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must contain("""<h3 class="success">メンバーアカウントを登録しました。</h3>""")
        content must not contain ("""<h3 class="success">メンバーアカウントを変更しました。</h3>""")
        content must not contain ("""<h3 class="success">パスワードを変更しました。</h3>""")
        content must not contain ("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
      }
    }

    "flashメッセージ：メンバーアカウント変更後。" in new TestApp {
      route(FakeRequest(GET, "/members/1").withSession(session).withFlash(
        Success -> Update)) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must not contain ("""<h3 class="success">メンバーアカウントを登録しました。</h3>""")
        content must contain("""<h3 class="success">メンバーアカウントを変更しました。</h3>""")
        content must not contain ("""<h3 class="success">パスワードを変更しました。</h3>""")
        content must not contain ("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
      }
    }

    "flashメッセージ：パスワード変更後。" in new TestApp {
      route(FakeRequest(GET, "/members/1").withSession(session).withFlash(
        Success -> UpdatePw)) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must not contain ("""<h3 class="success">メンバーアカウントを登録しました。</h3>""")
        content must not contain ("""<h3 class="success">メンバーアカウントを変更しました。</h3>""")
        content must contain("""<h3 class="success">パスワードを変更しました。</h3>""")
        content must not contain ("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
      }
    }
  }

  "MemberController#update(id)" should {

    "入力値が適正ならば、/members/:id に転送される。データが更新される。(メールアドレス変更なし)" in new TestApp {
      route(FakeRequest(POST, "/members/1").withSession(session).withFormUrlEncodedBody(
        "email" -> "name1@domain1", "nickname" -> "nickname000", "birthday" -> "1980/01/01")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/members/1")
        flash(res).get(Success) must beSome.which(_ == Update)
      }
      DB.withTransaction { implicit c =>
        Member.find(1) must beSome.which { member =>
          member.email must_== "name1@domain1"
          member.nickname must_== "nickname000"
          views.member.fmt.birthday(member) must beSome.which(_ == "1980/01/01")
        }
      }
    }

    "入力値が適正ならば、/members/:id に転送される。データが更新される。(メールアドレス変更あり)" in new TestApp {
      route(FakeRequest(POST, "/members/1").withSession(session).withFormUrlEncodedBody(
        "email" -> "name0@domain0", "nickname" -> "nickname000", "birthday" -> "1980/01/01")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/members/1")
        flash(res).get(Success) must beSome.which(_ == Update)
      }
      DB.withTransaction { implicit c =>
        Member.find(1) must beSome.which { member =>
          member.email must_== "name0@domain0"
          member.nickname must_== "nickname000"
          views.member.fmt.birthday(member) must beSome.which(_ == "1980/01/01")
        }
      }
    }

    "入力値が適正ならば、/members/:id に転送される。データが更新される。(生年月日省略可)" in new TestApp {
      route(FakeRequest(POST, "/members/1").withSession(session).withFormUrlEncodedBody(
        "email" -> "name0@domain0", "nickname" -> "nickname000", "birthday" -> "")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/members/1")
        flash(res).get(Success) must beSome.which(_ == Update)
      }
      DB.withTransaction { implicit c =>
        Member.find(1) must beSome.which { member =>
          member.email must_== "name0@domain0"
          member.nickname must_== "nickname000"
          views.member.fmt.birthday(member) must beNone
        }
      }
    }

    "メールアドレスが入力不正(必須NG)ならば、再入力を促す。" in new TestApp {
      route(FakeRequest(POST, "/members/1").withSession(session).withFormUrlEncodedBody(
        "email" -> "", "nickname" -> "nickname000", "birthday" -> "1980/01/01")) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must contain("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/members/1" method="POST" data-ajax="false">""")
        content must contain("""<label for="email" class="error">メールアドレス</label>""")
        content must contain("""<input type="text" id="email" name="email" value="" >""")
        content must contain("""<label for="nickname" class="">ニックネーム</label>""")
        content must contain("""<input type="text" id="nickname" name="nickname" value="nickname000" >""")
        content must contain("""<label for="birthday" class="">生年月日(省略可)</label>""")
        content must contain("""<input type="date" id="birthday" name="birthday" value="1980/01/01" >""")
        content must contain("""<input type="submit" value="変更する" data-theme="a" />""")
      }
    }

    "ニックネームが入力不正(必須NG)ならば、再入力を促す。" in new TestApp {
      route(FakeRequest(POST, "/members/1").withSession(session).withFormUrlEncodedBody(
        "email" -> "name0@domain0", "nickname" -> "", "birthday" -> "1980/01/01")) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must contain("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/members/1" method="POST" data-ajax="false">""")
        content must contain("""<label for="email" class="">メールアドレス</label>""")
        content must contain("""<input type="text" id="email" name="email" value="name0@domain0" >""")
        content must contain("""<label for="nickname" class="error">ニックネーム</label>""")
        content must contain("""<input type="text" id="nickname" name="nickname" value="" >""")
        content must contain("""<label for="birthday" class="">生年月日(省略可)</label>""")
        content must contain("""<input type="date" id="birthday" name="birthday" value="1980/01/01" >""")
        content must contain("""<input type="submit" value="変更する" data-theme="a" />""")
      }
    }

    "メールアドレスが入力不正(形式不正)ならば、再入力を促す。" in new TestApp {
      route(FakeRequest(POST, "/members/1").withSession(session).withFormUrlEncodedBody(
        "email" -> "localpartonly", "nickname" -> "nickname000", "birthday" -> "1980/01/01")) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must contain("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/members/1" method="POST" data-ajax="false">""")
        content must contain("""<label for="email" class="error">メールアドレス</label>""")
        content must contain("""<input type="text" id="email" name="email" value="localpartonly" >""")
        content must contain("""<label for="nickname" class="">ニックネーム</label>""")
        content must contain("""<input type="text" id="nickname" name="nickname" value="nickname000" >""")
        content must contain("""<label for="birthday" class="">生年月日(省略可)</label>""")
        content must contain("""<input type="date" id="birthday" name="birthday" value="1980/01/01" >""")
        content must contain("""<input type="submit" value="変更する" data-theme="a" />""")
      }
    }

    "生年月日が入力不正(形式不正)ならば、再入力を促す。" in new TestApp {
      route(FakeRequest(POST, "/members/1").withSession(session).withFormUrlEncodedBody(
        "email" -> "name0@domain0", "nickname" -> "nickname000", "birthday" -> "invalid")) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must contain("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/members/1" method="POST" data-ajax="false">""")
        content must contain("""<label for="email" class="">メールアドレス</label>""")
        content must contain("""<input type="text" id="email" name="email" value="name0@domain0" >""")
        content must contain("""<label for="nickname" class="">ニックネーム</label>""")
        content must contain("""<input type="text" id="nickname" name="nickname" value="nickname000" >""")
        content must contain("""<label for="birthday" class="error">生年月日(省略可)</label>""")
        content must contain("""<input type="date" id="birthday" name="birthday" value="invalid" >""")
        content must contain("""<input type="submit" value="変更する" data-theme="a" />""")
      }
    }

    "メールアドレスが入力不正(一意性NG)ならば、再入力を促す。" in new TestApp {
      route(FakeRequest(POST, "/members/1").withSession(session).withFormUrlEncodedBody(
        "email" -> "name10@domain10", "nickname" -> "nickname000", "birthday" -> "1980/01/01")) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must contain("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/members/1" method="POST" data-ajax="false">""")
        content must contain("""<label for="email" class="error">メールアドレス</label>""")
        content must contain("""<input type="text" id="email" name="email" value="name10@domain10" >""")
        content must contain("""<label for="nickname" class="">ニックネーム</label>""")
        content must contain("""<input type="text" id="nickname" name="nickname" value="nickname000" >""")
        content must contain("""<label for="birthday" class="">生年月日(省略可)</label>""")
        content must contain("""<input type="date" id="birthday" name="birthday" value="1980/01/01" >""")
        content must contain("""<input type="submit" value="変更する" data-theme="a" />""")
      }
    }
  }

  "MemberController#editPw(id)" should {

    "FORM構造が表示される。" in new TestApp {
      route(FakeRequest(GET, "/members/1/passwd").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must not contain ("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/members/1/passwd" method="POST" data-ajax="false">""")
        content must contain("""<label for="passwd" class="">パスワード</label>""")
        content must contain("""<input type="password" id="passwd" name="passwd" >""")
        content must contain("""<label for="passwdConf" class="">確認</label>""")
        content must contain("""<input type="password" id="passwdConf" name="passwdConf" >""")
        content must contain("""<input type="submit" value="変更する" data-theme="a" />""")
      }
    }
  }

  "MemberController#updatePw(id)" should {

    "入力値が適正ならば、/members/:id に転送される。" in new TestApp {
      route(FakeRequest(POST, "/members/1/passwd").withSession(session).withFormUrlEncodedBody(
        "passwd" -> "passwd000", "passwdConf" -> "passwd000")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/members/1")
        flash(res).get(Success) must beSome.which(_ == UpdatePw)
      }
    }

    "パスワードが入力不正(必須NG)ならば、再入力を促す。" in new TestApp {
      route(FakeRequest(POST, "/members/1/passwd").withSession(session).withFormUrlEncodedBody(
        "passwd" -> "", "passwdConf" -> "passwd000")) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must contain("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/members/1/passwd" method="POST" data-ajax="false">""")
        content must contain("""<label for="passwd" class="error">パスワード</label>""")
        content must contain("""<input type="password" id="passwd" name="passwd" >""")
        content must contain("""<label for="passwdConf" class="">確認</label>""")
        content must contain("""<input type="password" id="passwdConf" name="passwdConf" >""")
        content must contain("""<input type="submit" value="変更する" data-theme="a" />""")
      }
    }

    "確認が入力不正(必須NG)ならば、再入力を促す。" in new TestApp {
      route(FakeRequest(POST, "/members/1/passwd").withSession(session).withFormUrlEncodedBody(
        "passwd" -> "passwd000", "passwdConf" -> "")) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must contain("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/members/1/passwd" method="POST" data-ajax="false">""")
        content must contain("""<label for="passwd" class="">パスワード</label>""")
        content must contain("""<input type="password" id="passwd" name="passwd" >""")
        content must contain("""<label for="passwdConf" class="error">確認</label>""")
        content must contain("""<input type="password" id="passwdConf" name="passwdConf" >""")
        content must contain("""<input type="submit" value="変更する" data-theme="a" />""")
      }
    }

    "パスワードと確認が同じでなければ、再入力を促す。" in new TestApp {
      route(FakeRequest(POST, "/members/1/passwd").withSession(session).withFormUrlEncodedBody(
        "passwd" -> "passwd000", "passwdConf" -> "passwd001")) must beSome.which { res =>
        status(res) must equalTo(OK)
        val content = contentAsString(res)
        content must contain("""<h3 class="error">値が不適切です。入力し直してください。</h3>""")
        content must contain("""<form action="/members/1/passwd" method="POST" data-ajax="false">""")
        content must contain("""<label for="passwd" class="">パスワード</label>""")
        content must contain("""<input type="password" id="passwd" name="passwd" >""")
        content must contain("""<label for="passwdConf" class="">確認</label>""")
        content must contain("""<input type="password" id="passwdConf" name="passwdConf" >""")
        content must contain("""<input type="submit" value="変更する" data-theme="a" />""")
      }
    }
  }

  "MemberController#delete(id)" should {

    "/members に転送される。" in new TestApp {
      route(FakeRequest(GET, "/members/1/delete").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/members")
        flash(res).get(Success) must beSome.which(_ == Delete)
      }
    }
  }

}
