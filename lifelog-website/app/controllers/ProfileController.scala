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

import controllers.PageParam.implicitPageParam
import controllers.ProfileForm.passwdForm
import controllers.ProfileForm.profileForm
import controllers.common.FlashName.Success
import controllers.common.FlashName.Update
import controllers.common.FlashName.UpdatePw
import models.Member
import models.Profile
import play.api.mvc._
import routes.{ ProfileController => route }
import views.html.{ profile => view }

object ProfileController extends Controller with ActionBuilder {

  def edit() = AuthnCustomAction { memberId =>
    implicit conn => implicit req =>
      Member.find(memberId) match {
        case Some(Member(email, nickname, birthday)) =>
          Ok(view.edit(profileForm.fill(Profile(email, nickname, birthday))))
        case None => NotFound
      }
  }

  def update() = AuthnCustomAction { memberId =>
    implicit conn => implicit req =>
      Member.tryLock(memberId) match {
        case Some(_) =>
          profileForm.bindFromRequest().fold(
            error => {
              Ok(view.edit(error))
            },
            prof => {
              Member.update(memberId, Member(prof.email, prof.nickname, prof.birthday)) match {
                case true =>
                  Redirect(route.edit()).flashing(
                    Success -> Update)
                case false => BadRequest
              }
            })
        case None => NotFound
      }
  }

  def editPw() = AuthnCustomAction { memberId =>
    implicit conn => implicit req =>
      Member.tryLock(memberId) match {
        case Some(id) =>
          Ok(view.editPw(passwdForm))
        case None => NotFound
      }
  }

  def updatePw() = AuthnCustomAction { memberId =>
    implicit conn => implicit req =>
      Member.tryLock(memberId) match {
        case Some(_) =>
          passwdForm.bindFromRequest().fold(
            error => {
              Ok(view.editPw(error))
            },
            passwd => {
              val (pass, conf) = passwd
              if (pass == conf) {
                Member.updatePw(memberId, pass) match {
                  case true =>
                    Redirect(route.edit()).flashing(
                      Success -> UpdatePw)
                  case false => BadRequest
                }
              } else {
                Ok(view.editPw(passwdForm.fill(passwd).withGlobalError(
                  "error.password.unmatch")))
              }
            })
        case None => NotFound
      }
  }

}
