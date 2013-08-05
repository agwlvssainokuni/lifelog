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

class HomeControllerSpec extends Specification {

  val session = Security.username -> "0"

  "HomeController#index()" should {

    "ログインしていると、ホーム画面が表示される。" in new WithApplication {
      route(FakeRequest(GET, "/").withSession(session)) must beSome.which { res =>
        status(res) must equalTo(OK)
        contentType(res) must beSome.which(_ == "text/html")
        val content = contentAsString(res)
        content must contain("""<title>LifeLog/Admin - メニュー</title>""")
        content must contain("""<h1>LifeLog/Admin - メニュー</h1>""")
      }
    }

    "ログインしていないと、ログイン画面に転送される。" in new WithApplication {
      route(FakeRequest(GET, "/")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get("uri") must beSome.which(_ == "/")
        contentType(res) must beNone
        contentAsString(res) must beEmpty
      }
    }
  }

}
