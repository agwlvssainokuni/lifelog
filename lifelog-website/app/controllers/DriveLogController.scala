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

import controllers.DriveLogForm.driveLogForm
import controllers.common.FlashName.Create
import controllers.common.FlashName.Success
import models.DriveLog
import models.Pager
import play.api.mvc._
import routes.{ DriveLogController => route }
import views.html.{ drivelog => view }

object DriveLogController extends Controller with ActionBuilder {

  def list(pn: Option[Long], ps: Option[Long]) = AuthnCustomAction { memberId =>
    implicit conn => implicit req =>
      val pager = Pager(pn, ps, DriveLog.count(memberId))
      val list = DriveLog.list(memberId, pager.pageNo, pager.pageSize)
      Ok(view.list(pager, list))
  }

  def add() = AuthnCustomAction { memberId =>
    implicit conn => implicit req =>
      val driveLog = DriveLog.last(memberId) match {
        case Some(l) => DriveLog(new Date, l.tripmeter, l.fuelometer, l.remaining, l.odometer, None)
        case None => DriveLog(new Date, BigDecimal(0.0), BigDecimal(0.0), BigDecimal(0), BigDecimal(0), None)
      }
      Ok(view.add(driveLogForm.fill(driveLog)))
  }

  def create() = AuthnCustomAction { memberId =>
    implicit conn => implicit req =>
      driveLogForm.bindFromRequest().fold(
        error => {
          Ok(view.add(error))
        },
        driveLog => {
          DriveLog.create(memberId, driveLog) match {
            case Some(id) =>
              Redirect(route.edit(id)).flashing(
                Success -> Create)
            case None =>
              Ok(view.add(driveLogForm.fill(driveLog)))
          }
        })
  }

  def edit(id: Long) = AuthnCustomAction { memberId =>
    implicit conn => implicit req =>
      NotImplemented
  }

  def update(id: Long) = AuthnCustomAction { memberId =>
    implicit conn => implicit req =>
      NotImplemented
  }

  def delete(id: Long) = AuthnCustomAction { memberId =>
    implicit conn => implicit req =>
      NotImplemented
  }

}
