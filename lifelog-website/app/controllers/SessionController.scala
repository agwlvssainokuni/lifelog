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

import SessionForm.loginForm
import common.FlashName.Logout
import common.FlashName.Success
import common.FlashName.Uri
import models._
import play.api.mvc._
import routes.HomeController.{index => home}
import routes.{SessionController => route}
import views.html.{session => view}

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
          val (email, passwd, uri) = login
          Member.authenticate(email, passwd) match {
            case Some(memberId) =>
              val redirTo = uri.fold(home())(Call("GET", _))
              Redirect(redirTo).withSession(Security.username -> memberId.toString)
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
