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

import common.FlashName._
import models._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import views.html.{ session => view }

case class Login(loginId: String, passwd: String, uri: Option[String])

object SessionController extends Controller with ActionBuilder {

  val loginForm: Form[Login] = Form(mapping(
    "loginId" -> nonEmptyText(1, 32),
    "passwd" -> nonEmptyText(1, 32),
    "uri" -> optional(text(1, 256)))(Login.apply)(Login.unapply))

  def index() = CustomAction { implicit conn =>
    implicit req =>
      Ok(view.index(loginForm.fill(Login("", "", flash.get("uri")))))
  }

  def login() = CustomAction { implicit conn =>
    implicit req =>
      loginForm.bindFromRequest().fold(
        error => Ok(view.index(error)),
        login => {
          val result = Admin.authenticate(login.loginId, login.passwd)
          result match {
            case Some(adminId) =>
              val redirTo = login.uri.map(Call("GET", _)).getOrElse(
                routes.HomeController.index())
              Redirect(redirTo).withSession(Security.username -> adminId.toString)
            case None =>
              Ok(view.index(loginForm.fill(login).withError("login", "login.failed")))
          }
        })
  }

  def logout() = CustomAction { implicit conn =>
    implicit req =>
      Redirect(routes.SessionController.index()).withNewSession.flashing(
        Success -> Logout)
  }

}
