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

import java.util._

import models._
import play.api.data._
import play.api.data.Forms._

object DietLogForm {

  val DATE = "date"
  val DATE_PATTERN = "yyyy/MM/dd"

  val TIME = "time"
  val TIME_PATTERN = "HH:mm:ss"

  val WEIGHT = "weight"
  val WEIGHT_PRECISION = 4
  val WEIGHT_SCALE = 1

  val FATRATE = "fatRate"
  val FATRATE_PRECISION = 3
  val FATRATE_SCALE = 1

  val HEIGHT = "height"
  val HEIGHT_PRECISION = 4
  val HEIGHT_SCALE = 1

  val NOTE = "note"
  val NOTE_MIN = 1
  val NOTE_MAX = 1024

  val dietLogForm: Form[DietLog] = Form(mapping(
    DATE -> date(DATE_PATTERN),
    TIME -> date(TIME_PATTERN),
    WEIGHT -> bigDecimal(WEIGHT_PRECISION, WEIGHT_SCALE),
    FATRATE -> bigDecimal(FATRATE_PRECISION, FATRATE_SCALE),
    HEIGHT -> optional(bigDecimal(HEIGHT_PRECISION, HEIGHT_SCALE)),
    NOTE -> optional(text(NOTE_MIN, NOTE_MAX)))(apply)(unapply))

  def apply(date: Date, time: Date, weight: BigDecimal, fatRate: BigDecimal, height: Option[BigDecimal], note: Option[String]): DietLog = {
    val dtm = new Date(date.getTime() + time.getTime())
    DietLog(dtm, weight, fatRate, height, note)
  }

  def unapply(item: DietLog): Option[(Date, Date, BigDecimal, BigDecimal, Option[BigDecimal], Option[String])] = {
    val time = item.dtm.getTime() / (60L * 60L * 24L)
    Some((new Date(item.dtm.getTime() - time), new Date(time), item.weight, item.fatRate, item.height, item.note))
  }

}
