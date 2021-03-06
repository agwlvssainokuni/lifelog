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

import controllers.AdminForm.adminForm
import controllers.AdminForm.passwdForm
import controllers.PageParam.implicitPageParam
import controllers.common.FlashName.Create
import controllers.common.FlashName.Delete
import controllers.common.FlashName.Error
import controllers.common.FlashName.Permission
import controllers.common.FlashName.Success
import controllers.common.FlashName.Update
import controllers.common.FlashName.UpdatePw
import models.Admin
import models.Pager
import play.api.mvc._
import routes.{ AdminController => route }
import views.html.{ admin => view }

object AdminController extends Controller with ActionBuilder {

  def list(pn: Option[Long], ps: Option[Long]) = AuthnCustomAction { adminId =>
    implicit conn => implicit req =>
      val pager = Pager(pn, ps, Admin.count())
      val list = Admin.list(pager.pageNo, pager.pageSize)
      Ok(view.list(adminId, pager, list))
  }

  def add() = AuthnCustomAction { adminId =>
    implicit conn => implicit req =>
      Ok(view.add(adminForm))
  }

  def create() = AuthnCustomAction { adminId =>
    implicit conn => implicit req =>
      adminForm.bindFromRequest().fold(
        error => {
          Ok(view.add(error))
        },
        admin => {
          if (Admin.exists(admin.loginId).isDefined)
            Ok(view.add(adminForm.fill(admin).withError(
              "loginId", "uniqueness")))
          else
            Admin.create(admin) match {
              case Some(id) =>
                Redirect(route.edit(id)).flashing(
                  Success -> Create)
              case None =>
                Ok(view.add(adminForm.fill(admin)))
            }
        })
  }

  def edit(id: Long) = AuthnCustomAction { adminId =>
    implicit conn => implicit req =>
      Admin.find(id) match {
        case Some(a) if a.id.get == adminId =>
          Redirect(route.list(None, None)).flashing(
            Error -> Permission)
        case Some(a) =>
          Ok(view.edit(id, adminForm.fill(a)))
        case None => NotFound
      }
  }

  def update(id: Long) = AuthnCustomAction { adminId =>
    implicit conn => implicit req =>
      Admin.tryLock(id) match {
        case Some(i) if i == adminId =>
          Redirect(route.list(None, None)).flashing(
            Error -> Permission)
        case Some(_) =>
          adminForm.bindFromRequest().fold(
            error => {
              Ok(view.edit(id, error))
            },
            admin => {
              if (Admin.find(id).get.loginId != admin.loginId && Admin.exists(admin.loginId).isDefined)
                Ok(view.edit(id, adminForm.fill(admin).withError(
                  "loginId", "uniqueness")))
              else
                Admin.update(id, admin) match {
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
      Admin.find(id) match {
        case Some(a) if a.id.get == adminId =>
          Redirect(route.list(None, None)).flashing(
            Error -> Permission)
        case Some(_) =>
          Ok(view.editPw(id, passwdForm.fill(("", ""))))
        case None => NotFound
      }
  }

  def updatePw(id: Long) = AuthnCustomAction { adminId =>
    implicit conn => implicit req =>
      Admin.tryLock(id) match {
        case Some(i) if i == adminId =>
          Redirect(route.list(None, None)).flashing(
            Error -> Permission)
        case Some(_) =>
          passwdForm.bindFromRequest().fold(
            error => {
              Ok(view.editPw(id, error))
            },
            passwd => {
              val (pass, conf) = passwd
              if (pass == conf)
                Admin.updatePw(id, pass) match {
                  case true =>
                    Redirect(route.edit(id)).flashing(
                      Success -> UpdatePw)
                  case false => BadRequest
                }
              else
                Ok(view.editPw(id, passwdForm.fill(passwd).withGlobalError(
                  "error.password.unmatch")))
            })
        case None => NotFound
      }
  }

  def delete(id: Long) = AuthnCustomAction { adminId =>
    implicit conn => implicit req =>
      Admin.tryLock(id) match {
        case Some(i) if i == adminId =>
          Redirect(route.list(None, None)).flashing(
            Error -> Permission)
        case Some(_) =>
          Admin.delete(id) match {
            case true =>
              Redirect(route.list(None, None)).flashing(
                Success -> Delete)
            case false => BadRequest
          }
        case None => NotFound
      }
  }

}
