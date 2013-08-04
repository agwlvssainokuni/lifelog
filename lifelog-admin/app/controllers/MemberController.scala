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

  def list(pn: Option[Long], ps: Option[Long]) = AuthnCustomAction { adminId =>
    implicit conn => implicit req =>
      val pager = Pager(pn, ps, 0L)
      val list = Seq()
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
              if (passwd.passwd == passwd.passwdConf)
                Member.updatePw(id, passwd.passwd) match {
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
