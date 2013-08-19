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

import DietLogForm._
import play.api.data._
import play.api.data.Forms._

object DataMgmtForm {

  val FILE = "file"

  val dietlogForm: Form[String] = Form(single(
    FILE -> ignored("")))

  object dietlog {
    import DietLogForm._
    val ID = "id"
    val ID_MIN = 1L
    val DTM = "dtm"
    val DTM_PATTERN = DATE_PATTERN + " " + TIME_PATTERN
    val recordForm = Form(tuple(
      ID -> optional(longNumber(ID_MIN)),
      DTM -> date(DTM_PATTERN),
      WEIGHT -> bigDecimal(WEIGHT_PRECISION, WEIGHT_SCALE),
      FATRATE -> bigDecimal(FATRATE_PRECISION, FATRATE_SCALE),
      HEIGHT -> optional(bigDecimal(HEIGHT_PRECISION, HEIGHT_SCALE)),
      NOTE -> optional(text(NOTE_MIN, NOTE_MAX))))
  }

}
