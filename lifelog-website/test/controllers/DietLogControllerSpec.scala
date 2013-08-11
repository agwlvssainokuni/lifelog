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

import SessionForm._
import common.FlashName._
import models._
import play.api.db._
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

class DietLogControllerSpec extends Specification {

  val session = Security.username -> "1"

  abstract class TestApp extends WithApplication {
    override def around[T: AsResult](t: => T): Result = super.around {
      DB.withTransaction { implicit c =>
        Member.create(Member("user@domain", "ニックネーム", None)) must beSome
      }
      t
    }
  }

  "未ログインの場合は、ログイン画面に転送される" should {
    "/dietlog" in new TestApp {
      route(FakeRequest(GET, "/dietlog")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get(Uri) must beSome.which(_ == "/dietlog")
      }
    }
    "/dietlog/add" in new TestApp {
      route(FakeRequest(GET, "/dietlog/add")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get(Uri) must beSome.which(_ == "/dietlog/add")
      }
    }
    "/dietlog/add" in new TestApp {
      route(FakeRequest(POST, "/dietlog/add")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get(Uri) must beSome.which(_ == "/dietlog/add")
      }
    }
    "/dietlog/1" in new TestApp {
      route(FakeRequest(GET, "/dietlog/1")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get(Uri) must beSome.which(_ == "/dietlog/1")
      }
    }
    "/dietlog/1" in new TestApp {
      route(FakeRequest(POST, "/dietlog/1")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get(Uri) must beSome.which(_ == "/dietlog/1")
      }
    }
    "/dietlog/1/delete" in new TestApp {
      route(FakeRequest(GET, "/dietlog/1/delete")) must beSome.which { res =>
        status(res) must equalTo(SEE_OTHER)
        header(LOCATION, res) must beSome.which(_ == "/login")
        flash(res).get(Uri) must beSome.which(_ == "/dietlog/1/delete")
      }
    }
  }

}
