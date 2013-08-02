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

import models._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import routes.{ ProfileController => route }
import views.html.{ profile => view }

object ProfileController extends Controller with CustomActionBuilder {

  val profileForm: Form[Profile] = Form(mapping(
    "loginId" -> nonEmptyText(1, 32),
    "nickname" -> nonEmptyText(1, 256))(Profile.apply)(Profile.unapply))

  val passwdForm: Form[Passwd] = Form(mapping(
    "passwd" -> nonEmptyText(1, 32),
    "passwdConf" -> nonEmptyText(1, 32))(Passwd.apply)(Passwd.unapply))

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
                    "success" -> "update")
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
              if (passwd.passwd == passwd.passwdConf) {
                Admin.updatePw(adminId, passwd.passwd) match {
                  case true =>
                    Redirect(route.edit()).flashing(
                      "success" -> "updatePw")
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
