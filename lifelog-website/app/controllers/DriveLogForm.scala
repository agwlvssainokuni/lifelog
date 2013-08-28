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

import models.DriveLog
import play.api.data._
import play.api.data.Forms._

object DriveLogForm {

  val DATE = "date"
  val DATE_PATTERN = "yyyy-MM-dd"

  val TRIPMETER = "tripmeter"
  val TRIPMETER_PRECISION = 5
  val TRIPMETER_SCALE = 1

  val FUELOMETER = "fuelometer"
  val FUELOMETER_PRECISION = 3
  val FUELOMETER_SCALE = 1

  val REMAINING = "remaining"
  val REMAINING_PRECISION = 4
  val REMAINING_SCALE = 0

  val ODOMETER = "odometer"
  val ODOMETER_PRECISION = 9
  val ODOMETER_SCALE = 0

  val NOTE = "note"
  val NOTE_MIN = 1
  val NOTE_MAX = 1024

  val driveLogForm: Form[DriveLog] = Form(mapping(
    DATE -> date(DATE_PATTERN),
    TRIPMETER -> bigDecimal(TRIPMETER_PRECISION, TRIPMETER_SCALE),
    FUELOMETER -> bigDecimal(FUELOMETER_PRECISION, FUELOMETER_SCALE),
    REMAINING -> bigDecimal(REMAINING_PRECISION, REMAINING_SCALE),
    ODOMETER -> bigDecimal(ODOMETER_PRECISION, ODOMETER_SCALE),
    NOTE -> optional(text(NOTE_MIN, NOTE_MAX)))(apply)(unapply))

  def apply(date: Date, tripmeter: BigDecimal, fuelometer: BigDecimal, remaining: BigDecimal, odometer: BigDecimal, note: Option[String]): DriveLog = {
    DriveLog(date, tripmeter, fuelometer, remaining, odometer, note)
  }

  def unapply(item: DriveLog): Option[(Date, BigDecimal, BigDecimal, BigDecimal, BigDecimal, Option[String])] = {
    Some((item.dt, item.tripmeter, item.fuelometer, item.remaining, item.odometer, item.note))
  }

}
