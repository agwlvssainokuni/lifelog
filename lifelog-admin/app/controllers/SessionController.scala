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

import SessionFormDef._
import common.FlashName._
import models._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import routes.HomeController.{ index => home }
import routes.{ SessionController => route }
import views.html.{ session => view }

object SessionFormDef {

  val LOGIN_ID = "loginId"
  val LOGIN_ID_MIN = 1
  val LOGIN_ID_MAX = 32

  val PASSWORD = "password"
  val PASSWORD_MIN = 1
  val PASSWORD_MAX = 32

  val URI = "uri"
  val URI_MIN = 1
  val URI_MAX = 256
}

object SessionController extends Controller with ActionBuilder {

  val loginForm: Form[(String, String, Option[String])] = Form(tuple(
    LOGIN_ID -> nonEmptyText(LOGIN_ID_MIN, LOGIN_ID_MAX),
    PASSWORD -> nonEmptyText(PASSWORD_MIN, PASSWORD_MAX),
    URI -> optional(text(URI_MIN, URI_MAX))))

  def index() = CustomAction { implicit conn =>
    implicit req =>
      Ok(view.index(loginForm.fill(("", "", flash.get(Uri)))))
  }

  def login() = CustomAction { implicit conn =>
    implicit req =>
      loginForm.bindFromRequest().fold(
        error => Ok(view.index(error)),
        login => {
          val (loginId, passwd, uri) = login
          Admin.authenticate(loginId, passwd) match {
            case Some(adminId) =>
              val redirTo = uri.fold(home())(Call("GET", _))
              Redirect(redirTo).withSession(Security.username -> adminId.toString)
            case None =>
              Ok(view.index(loginForm.fill(login).withGlobalError("login.failed")))
          }
        })
  }

  def logout() = CustomAction { implicit conn =>
    implicit req =>
      Redirect(route.index()).withNewSession.flashing(
        Success -> Logout)
  }

}
