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

import java.util.Date

import play.api.Play.current
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.db.DB
import play.api.mvc._
import views.html.{ member => view }

case class Member(email: String, nickname: String, height: Option[BigDecimal], birthday: Option[Date])

case class Passwd(passwd: String, passwdConf: String)

object MemberController extends Controller with Authentication {

  val memberForm: Form[Member] = Form(mapping(
    "email" -> email.verifying(minLength(1), maxLength(256)),
    "nickname" -> nonEmptyText(1, 256),
    "height" -> optional(bigDecimal),
    "birthday" -> optional(date("yyyy/MM/ddÏÏß")))(Member.apply)(Member.unapply))

  val passwdForm: Form[Passwd] = Form(mapping(
    "passwd" -> nonEmptyText(1, 32),
    "passwdConf" -> nonEmptyText(1, 32))(Passwd.apply)(Passwd.unapply))

  def list(pn: Long = 0, ps: Long = 5) = withAuthenticated { adminId =>
    Action {
      Ok(view.list())
    }
  }

  def add() = withAuthenticated { adminId =>
    Action {
      Ok(view.add(memberForm))
    }
  }

  def create() = withAuthenticated { adminId =>
    Action { implicit req =>
      DB.withTransaction { implicit c =>
        memberForm.bindFromRequest().fold(
          error => {
            Ok(view.add(error))
          },
          member => {
            val id = 1L
            Redirect(routes.MemberController.edit(id)).flashing(
              "success" -> "add")
          })
      }
    }
  }

  def edit(id: Long) = withAuthenticated { adminId =>
    Action {
      Ok(view.edit(id, memberForm))
    }
  }

  def update(id: Long) = withAuthenticated { adminId =>
    Action { implicit req =>
      DB.withTransaction { implicit c =>
        memberForm.bindFromRequest().fold(
          error => Ok(view.edit(id, error)),
          member => {
            Redirect(routes.MemberController.edit(id)).flashing(
              "success" -> "edit")
          })
      }
    }
  }

  def editPw(id: Long) = withAuthenticated { adminId =>
    Action {
      Ok(view.editPw(id, passwdForm))
    }
  }

  def updatePw(id: Long) = TODO

  def delete(id: Long) = TODO

}
