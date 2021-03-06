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

import controllers.SessionForm.loginForm
import controllers.common.FlashName.Logout
import controllers.common.FlashName.Success
import controllers.common.FlashName.Uri
import models.Admin
import play.api.mvc._
import routes.{ HomeController => home }
import routes.{ SessionController => route }
import views.html.{ session => view }

object SessionController extends Controller with ActionBuilder {

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
              val redirTo = uri.fold(home.index())(Call("GET", _))
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
