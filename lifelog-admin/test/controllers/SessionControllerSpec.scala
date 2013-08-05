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

import common.FlashName._
import play.api.test._
import play.api.test.Helpers._

class SessionControllerSpec extends Specification {

  "SessionController#index" should {

    "ログイン画面が表示される。" in new WithApplication {
      route(FakeRequest(GET, "/login")) must beSome.which { res =>
        status(res) must equalTo(OK)
        contentType(res) must beSome.which(_ == "text/html")
        val content = contentAsString(res)
        content must contain("""<label for="loginId" class="">ログインID</label>""")
        content must contain("""<label for="passwd" class="">パスワード</label>""")
        content must not contain ("ログアウトしました。")
        content must not contain ("ログインし直してください。")
        content must not contain ("""<input type="hidden" name="uri" value="/members" />""")
      }
    }

    "ログアウトから転送されて、ログイン画面が表示される。" in new WithApplication {
      route(FakeRequest(GET, "/login").withFlash(Success -> Logout)) must beSome.which { res =>
        status(res) must equalTo(OK)
        contentType(res) must beSome.which(_ == "text/html")
        val content = contentAsString(res)
        content must contain("""<label for="loginId" class="">ログインID</label>""")
        content must contain("""<label for="passwd" class="">パスワード</label>""")
        content must contain("ログアウトしました。")
        content must not contain ("ログインし直してください。")
        content must not contain ("""<input type="hidden" name="uri" value="/members" />""")
      }
    }

    "セッション切れから転送されて、ログイン画面が表示される。" in new WithApplication {
      route(FakeRequest(GET, "/login").withFlash(Error -> Unauthorized)) must beSome.which { res =>
        status(res) must equalTo(OK)
        contentType(res) must beSome.which(_ == "text/html")
        val content = contentAsString(res)
        content must contain("""<label for="loginId" class="">ログインID</label>""")
        content must contain("""<label for="passwd" class="">パスワード</label>""")
        content must not contain ("ログアウトしました。")
        content must contain("ログインし直してください。")
        content must not contain ("""<input type="hidden" name="uri" value="/members" />""")
      }
    }

    "ログイン後転送先の指定ありで、ログイン画面が表示される。" in new WithApplication {
      route(FakeRequest(GET, "/login").withFlash("uri" -> "/members")) must beSome.which { res =>
        status(res) must equalTo(OK)
        contentType(res) must beSome.which(_ == "text/html")
        val content = contentAsString(res)
        content must contain("""<label for="loginId" class="">ログインID</label>""")
        content must contain("""<label for="passwd" class="">パスワード</label>""")
        content must not contain ("ログアウトしました。")
        content must not contain ("ログインし直してください。")
        content must contain("""<input type="hidden" name="uri" value="/members" />""")
      }
    }
  }

  "SessionController#login" should {

    "ログインできる。ホームに転送される。" in new WithApplication {
      route(FakeRequest(POST, "/login").withFormUrlEncodedBody(
        "loginId" -> "superadmin", "passwd" -> "p@ssw0rd")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/")
        contentType(res) must beNone
        contentAsString(res) must beEmpty
      }
    }

    "ログインできる。指定されたログイン後転送先に転送される。" in new WithApplication {
      route(FakeRequest(POST, "/login").withFormUrlEncodedBody(
        "loginId" -> "superadmin", "passwd" -> "p@ssw0rd", "uri" -> "/members")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/members")
        contentType(res) must beNone
        contentAsString(res) must beEmpty
      }
    }

    "ログインID入力不正 (必須違反) でNG。" in new WithApplication {
      route(FakeRequest(POST, "/login").withFormUrlEncodedBody(
        "loginId" -> "", "passwd" -> "p@ssw0rd")) must beSome.which { res =>
        status(res) must equalTo(OK)
        header(LOCATION, res) must beNone
        contentType(res) must beSome.which(_ == "text/html")
        val content = contentAsString(res)
        content must contain("値が不適切です。入力し直してください。")
        content must not contain ("ログインIDまたはパスワードが異なっています。")
        content must contain("""<label for="loginId" class="error">ログインID</label>""")
        content must contain("""<label for="passwd" class="">パスワード</label>""")
      }
    }

    "パスワード入力不正 (必須違反) でNG。" in new WithApplication {
      route(FakeRequest(POST, "/login").withFormUrlEncodedBody(
        "loginId" -> "superadmin", "passwd" -> "")) must beSome.which { res =>
        status(res) must equalTo(OK)
        header(LOCATION, res) must beNone
        contentType(res) must beSome.which(_ == "text/html")
        val content = contentAsString(res)
        content must contain("値が不適切です。入力し直してください。")
        content must not contain ("ログインIDまたはパスワードが異なっています。")
        content must contain("""<label for="loginId" class="">ログインID</label>""")
        content must contain("""<label for="passwd" class="error">パスワード</label>""")
      }
    }

    "ログインID/パスワード不適合でNG。" in new WithApplication {
      route(FakeRequest(POST, "/login").withFormUrlEncodedBody(
        "loginId" -> "superadmin", "passwd" -> "NOMATCH")) must beSome.which { res =>
        status(res) must equalTo(OK)
        header(LOCATION, res) must beNone
        contentType(res) must beSome.which(_ == "text/html")
        val content = contentAsString(res)
        content must not contain ("値が不適切です。入力し直してください。")
        content must contain("ログインIDまたはパスワードが異なっています。")
        content must contain("""<label for="loginId" class="">ログインID</label>""")
        content must contain("""<label for="passwd" class="">パスワード</label>""")
      }
    }
  }

  "SessionController#logout" should {
    "ログアウトする。" in new WithApplication {
      route(FakeRequest(GET, "/logout")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        contentType(res) must beEmpty
        flash(res).get(Success) must beSome.which(_ == Logout)
      }
    }
  }
}
