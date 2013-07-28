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
import play.api.Play.current
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.db.DB
import play.api.mvc._
import views.html.{ admin => view }
import routes.{ AdminController => route }

object AdminController extends Controller with CustomActionBuilder {

  val adminForm: Form[Admin] = Form(mapping(
    "loginId" -> nonEmptyText(1, 32),
    "nickname" -> nonEmptyText(1, 255))(Admin.apply)(Admin.unapply))

  val passwdForm: Form[Passwd] = Form(mapping(
    "passwd" -> nonEmptyText(1, 32),
    "passwdConf" -> nonEmptyText(1, 32))(Passwd.apply)(Passwd.unapply))

  def list(pn: Long = 0, ps: Long = 5) = AuthnCustomAction { adminId =>
    implicit req => implicit conn =>
      Ok(view.list())
  }

  def add() = AuthnCustomAction { adminId =>
    implicit req => implicit conn =>
      Ok(view.add(adminForm))
  }

  def create() = AuthnCustomAction { adminId =>
    implicit req => implicit conn =>
      adminForm.bindFromRequest().fold(
        error => {
          Ok(view.add(error))
        },
        admin => {
          // TODO 登録処理を実装。
          val id = 1L
          Redirect(route.edit(id)).flashing(
            "success" -> "create")
        })
  }

  def edit(id: Long) = AuthnCustomAction { adminId =>
    implicit req => implicit conn =>
      // TODO DBから管理アカウント情報を取得するようにする。
      val admin = if (id == 999L) None else Some(Admin("login" + id, "nickname" + id))
      admin match {
        case Some(a) => Ok(view.edit(id, adminForm.fill(a)))
        case None => NotFound
      }
  }

  def update(id: Long) = AuthnCustomAction { adminId =>
    implicit req => implicit conn =>
      // TODO 存在確認
      if (id == 999L) NotFound
      else
        adminForm.bindFromRequest().fold(
          error => {
            Ok(view.edit(id, error))
          },
          admin => {
            // TODO 変更処理を実装する。
            Redirect(route.edit(id)).flashing(
              "success" -> "update")
          })
  }

  def editPw(id: Long) = AuthnCustomAction { adminId =>
    implicit req => implicit conn =>
      // TODO DBから管理アカウント情報を取得するようにする。
      val admin = if (id == 999L) None else Some(Admin("login" + id, "nickname" + id))
      admin match {
        case Some(a) => Ok(view.editPw(id, passwdForm.fill(Passwd("", ""))))
        case None => NotFound
      }
  }

  def updatePw(id: Long) = AuthnCustomAction { adminId =>
    implicit req => implicit conn =>
      // TODO 存在確認
      if (id == 999L) NotFound
      else
        passwdForm.bindFromRequest().fold(
          error => {
            Ok(view.editPw(id, error))
          },
          passwd => {
            if (passwd.passwd == passwd.passwdConf) {
              // TODO パスワード変更処理を実装する。
              Redirect(route.edit(id)).flashing(
                "success" -> "updatePw")
            } else {
              Ok(view.editPw(id, passwdForm.fill(passwd).withError(
                "unmatch", "passwd.unmatch")))
            }
          })
  }

  def delete(id: Long) = AuthnCustomAction { adminId =>
    implicit req => implicit conn =>
      // TODO 存在確認
      if (id == 999L) NotFound
      else {
        // TODO 削除処理を実装する。
        Redirect(route.list()).flashing(
          "success" -> "delete")
      }
  }

}
