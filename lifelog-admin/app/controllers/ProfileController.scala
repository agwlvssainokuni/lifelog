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

import PageParam.implicitPageParam
import ProfileFormDef._
import common.FlashName._
import models._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import routes.{ ProfileController => route }
import views.html.{ profile => view }

object ProfileFormDef {

  val LOGIN_ID = "loginId"
  val LOGIN_ID_MIN = 1
  val LOGIN_ID_MAX = 32

  val NICKNAME = "nickname"
  val NICKNAME_MIN = 1
  val NICKNAME_MAX = 256

  val PASSWORD = "password"
  val PASSWORD_MIN = 1
  val PASSWORD_MAX = 32

  val CONFIRM = "confirm"
  val CONFIRM_MIN = 1
  val CONFIRM_MAX = 32
}

object ProfileController extends Controller with ActionBuilder {

  val profileForm: Form[Profile] = Form(mapping(
    LOGIN_ID -> nonEmptyText(LOGIN_ID_MIN, LOGIN_ID_MAX),
    NICKNAME -> nonEmptyText(NICKNAME_MIN, NICKNAME_MAX))(Profile.apply)(Profile.unapply))

  val passwdForm: Form[(String, String)] = Form(tuple(
    PASSWORD -> nonEmptyText(PASSWORD_MIN, PASSWORD_MAX),
    CONFIRM -> nonEmptyText(CONFIRM_MIN, CONFIRM_MAX)))

  def edit() = AuthnCustomAction { adminId =>
    implicit conn => implicit req =>
      Admin.find(adminId) match {
        case Some(Admin(loginId, nickname)) =>
          Ok(view.edit(profileForm.fill(Profile(loginId, nickname))))
        case None => NotFound
      }
  }

  def update() = AuthnCustomAction { adminId =>
    implicit conn => implicit req =>
      Admin.tryLock(adminId) match {
        case Some(_) =>
          profileForm.bindFromRequest().fold(
            error => {
              Ok(view.edit(error))
            },
            prof => {
              Admin.update(adminId, Admin(prof.loginId, prof.nickname)) match {
                case true =>
                  Redirect(route.edit()).flashing(
                    Success -> Update)
                case false => BadRequest
              }
            })
        case None => NotFound
      }
  }

  def editPw() = AuthnCustomAction { adminId =>
    implicit conn => implicit req =>
      Admin.tryLock(adminId) match {
        case Some(id) =>
          Ok(view.editPw(passwdForm))
        case None => NotFound
      }
  }

  def updatePw() = AuthnCustomAction { adminId =>
    implicit conn => implicit req =>
      Admin.tryLock(adminId) match {
        case Some(_) =>
          passwdForm.bindFromRequest().fold(
            error => {
              Ok(view.editPw(error))
            },
            passwd => {
              val (pass, conf) = passwd
              if (pass == conf) {
                Admin.updatePw(adminId, pass) match {
                  case true =>
                    Redirect(route.edit()).flashing(
                      Success -> UpdatePw)
                  case false => BadRequest
                }
              } else {
                Ok(view.editPw(passwdForm.fill(passwd).withError(
                  "unmatch", "passwd.unmatch")))
              }
            })
        case None => NotFound
      }
  }

}
