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

import MemberFormDef._
import PageParam.implicitPageParam
import common.FlashName._
import models._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.mvc._
import routes.{ MemberController => route }
import views.html.{ member => view }

object MemberFormDef {

  val EMAIL = "email"
  val EMAIL_MIN = 1
  val EMAIL_MAX = 256

  val NICKNAME = "nickname"
  val NICKNAME_MIN = 1
  val NICKNAME_MAX = 256

  val BIRTHDAY = "birthday"
  val BIRTHDAY_PATTERN = "yyyy/MM/dd"

  val PASSWORD = "password"
  val PASSWORD_MIN = 1
  val PASSWORD_MAX = 32

  val CONFIRM = "confirm"
  val CONFIRM_MIN = 1
  val CONFIRM_MAX = 32
}

object MemberController extends Controller with ActionBuilder {

  val memberForm: Form[Member] = Form(mapping(
    EMAIL -> email.verifying(minLength(EMAIL_MIN), maxLength(EMAIL_MAX)),
    NICKNAME -> nonEmptyText(NICKNAME_MIN, NICKNAME_MAX),
    BIRTHDAY -> optional(date(BIRTHDAY_PATTERN)))(Member.apply)(Member.unapply))

  val passwdForm: Form[(String, String)] = Form(tuple(
    PASSWORD -> nonEmptyText(PASSWORD_MIN, PASSWORD_MAX),
    CONFIRM -> nonEmptyText(CONFIRM_MIN, CONFIRM_MAX)))

  def list(pn: Option[Long], ps: Option[Long]) = AuthnCustomAction { adminId =>
    implicit conn => implicit req =>
      val pager = Pager(pn, ps, Member.count())
      val list = Member.list(pager.pageNo, pager.pageSize)
      Ok(view.list(pager, list))
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
          if (Member.exists(member.email).isDefined)
            Ok(view.add(memberForm.fill(member).withError(
              "email", "uniqueness")))
          else
            Member.create(member) match {
              case Some(id) =>
                Redirect(route.edit(id)).flashing(
                  Success -> Create)
              case None =>
                Ok(view.add(memberForm.fill(member)))
            }
        })
  }

  def edit(id: Long) = AuthnCustomAction { adminId =>
    implicit conn => implicit req =>
      Member.find(id) match {
        case Some(m) =>
          Ok(view.edit(id, memberForm.fill(m)))
        case None => NotFound
      }
  }

  def update(id: Long) = AuthnCustomAction { adminId =>
    implicit conn => implicit req =>
      Member.tryLock(id) match {
        case Some(_) =>
          memberForm.bindFromRequest().fold(
            error => {
              Ok(view.edit(id, error))
            },
            member => {
              if (Member.find(id).get.email != member.email && Member.exists(member.email).isDefined)
                Ok(view.edit(id, memberForm.fill(member).withError(
                  "email", "uniqueness")))
              else
                Member.update(id, member) match {
                  case true =>
                    Redirect(route.edit(id)).flashing(
                      Success -> Update)
                  case false => BadRequest
                }
            })
        case None => NotFound
      }
  }

  def editPw(id: Long) = AuthnCustomAction { adminId =>
    implicit conn => implicit req =>
      Member.find(id) match {
        case Some(_) =>
          Ok(view.editPw(id, passwdForm))
        case None => NotFound
      }
  }

  def updatePw(id: Long) = AuthnCustomAction { adminId =>
    implicit conn => implicit req =>
      Member.tryLock(id) match {
        case Some(_) =>
          passwdForm.bindFromRequest().fold(
            error => {
              Ok(view.editPw(id, error))
            },
            passwd => {
              val (pass, conf) = passwd
              if (pass == conf)
                Member.updatePw(id, pass) match {
                  case true =>
                    Redirect(route.edit(id)).flashing(
                      Success -> UpdatePw)
                  case false => BadRequest
                }
              else
                Ok(view.editPw(id, passwdForm.fill(passwd).withError(
                  "unmatch", "passwd.unmatch")))
            })
        case None => NotFound
      }
  }

  def delete(id: Long) = AuthnCustomAction { adminId =>
    implicit conn => implicit req =>
      Member.tryLock(id) match {
        case Some(_) =>
          Member.delete(id) match {
            case true =>
              Redirect(route.list(None, None)).flashing(
                Success -> Delete)
            case false => BadRequest
          }
        case None => NotFound
      }
  }

}
