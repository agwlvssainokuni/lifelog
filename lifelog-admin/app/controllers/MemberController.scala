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

import FlashUtil._
import PageParam.implicitPageParam
import models._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.mvc._
import routes.{ MemberController => route }
import views.html.{ member => view }

object MemberController extends Controller with CustomActionBuilder {

  val memberForm: Form[Member] = Form(mapping(
    "email" -> email.verifying(minLength(1), maxLength(256)),
    "nickname" -> nonEmptyText(1, 256),
    "birthday" -> optional(date("yyyy/MM/dd")))(Member.apply)(Member.unapply))

  val passwdForm: Form[Passwd] = Form(mapping(
    "passwd" -> nonEmptyText(1, 32),
    "passwdConf" -> nonEmptyText(1, 32))(Passwd.apply)(Passwd.unapply))

  def list(pn: Option[Long] = None, ps: Long = 5L) = AuthnCustomAction { adminId =>
    implicit conn => implicit req =>
      val totalCount = 0L
      val pager = Pager(pn, ps).adjust(totalCount)
      Ok(view.list(totalCount, pager, Seq()))
  }

  def add() = AuthnCustomAction { adminId =>
    implicit conn => implicit req =>
      Ok(view.add(memberForm))
  }

  def create() = AuthnCustomAction { adminId =>
    implicit conn => implicit req =>
      memberForm.bindFromRequest().fold(
        error => {
          Ok(view.add(error))
        },
        member => {
          val id = 1L
          Redirect(route.edit(id)).flashing(
            Success -> Create)
        })
  }

  def edit(id: Long) = AuthnCustomAction { adminId =>
    implicit conn => implicit req =>
      Ok(view.edit(id, memberForm))
  }

  def update(id: Long) = AuthnCustomAction { adminId =>
    implicit conn => implicit req =>
      memberForm.bindFromRequest().fold(
        error => {
          Ok(view.edit(id, error))
        },
        member => {
          Redirect(route.edit(id)).flashing(
            Success -> Update)
        })
  }

  def editPw(id: Long) = AuthnCustomAction { adminId =>
    implicit conn => implicit req =>
      Ok(view.editPw(id, passwdForm))
  }

  def updatePw(id: Long) = AuthnCustomAction { adminId =>
    implicit conn => implicit req =>
      passwdForm.bindFromRequest().fold(
        error => {
          Ok(view.editPw(id, error))
        },
        passwd => {
          Redirect(route.edit(id)).flashing(
            Success -> UpdatePw)
        })
  }

  def delete(id: Long) = AuthnCustomAction { adminId =>
    implicit conn => implicit req =>
      Redirect(route.list(None)).flashing(
        Success -> Delete)
  }

}
