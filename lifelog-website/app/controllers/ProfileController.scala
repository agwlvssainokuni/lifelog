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
import common.FlashName._
import models._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.mvc._
import routes.{ ProfileController => route }
import views.html.{ profile => view }

object ProfileController extends Controller with ActionBuilder {

  val profileForm: Form[Profile] = Form(mapping(
    "email" -> email.verifying(minLength(1), maxLength(256)),
    "nickname" -> nonEmptyText(1, 256),
    "birthday" -> optional(date("yyyy/MM/dd")))(Profile.apply)(Profile.unapply))

  val passwdForm: Form[Passwd] = Form(mapping(
    "passwd" -> nonEmptyText(1, 32),
    "passwdConf" -> nonEmptyText(1, 32))(Passwd.apply)(Passwd.unapply))

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
              if (passwd.passwd == passwd.passwdConf) {
                Member.updatePw(memberId, passwd.passwd) match {
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
