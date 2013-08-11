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

import DietLogForm._
import PageParam.implicitPageParam
import common.FlashName._
import models._
import play.api.mvc._
import routes.{ DietLogController => route }
import views.html.{ dietlog => view }

object DietLogController extends Controller with ActionBuilder {

  def list(pn: Option[Long], ps: Option[Long]) = AuthnCustomAction { memberId =>
    implicit conn => implicit req =>
      val pager = Pager(pn, ps, DietLog.count(memberId))
      val list = DietLog.list(memberId, pager.pageNo, pager.pageSize)
      Ok(view.list(pager, list))
  }

  def add() = AuthnCustomAction { memberId =>
    implicit conn => implicit req =>
      val dietLog = DietLog.last(memberId) match {
        case Some(l) => DietLog(new Date, l.weight, l.fatRate, l.height, None)
        case None => DietLog(new Date, BigDecimal(0), BigDecimal(0), None, None)
      }
      Ok(view.add(dietLogForm.fill(dietLog)))
  }

  def create() = AuthnCustomAction { memberId =>
    implicit conn => implicit req =>
      dietLogForm.bindFromRequest().fold(
        error => {
          Ok(view.add(error))
        },
        dietLog => {
          DietLog.create(memberId, dietLog) match {
            case Some(id) =>
              Redirect(route.edit(id)).flashing(
                Success -> Create)
            case None =>
              Ok(view.add(dietLogForm.fill(dietLog)))
          }
        })
  }

  def edit(id: Long) = AuthnCustomAction { memberId =>
    implicit conn => implicit req =>
      DietLog.find(memberId, id) match {
        case Some(dietLog) =>
          Ok(view.edit(id, dietLogForm.fill(dietLog)))
        case None => NotFound
      }
  }

  def update(id: Long) = AuthnCustomAction { memberId =>
    implicit conn => implicit req =>
      DietLog.tryLock(memberId, id) match {
        case Some(_) =>
          dietLogForm.bindFromRequest().fold(
            error => {
              Ok(view.edit(id, error))
            },
            dietLog => {
              DietLog.update(memberId, id, dietLog) match {
                case true =>
                  Redirect(route.edit(id)).flashing(
                    Success -> Update)
                case false => BadRequest
              }
            })
        case None => NotFound
      }
  }

  def delete(id: Long) = AuthnCustomAction { memberId =>
    implicit conn => implicit req =>
      DietLog.tryLock(memberId, id) match {
        case Some(_) =>
          DietLog.delete(memberId, id) match {
            case true =>
              Redirect(route.list(None, None)).flashing(
                Success -> Delete)
            case false => BadRequest
          }
        case None => NotFound
      }
  }

}
