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

package views.dietlog

import java.text.DecimalFormat
import java.text.SimpleDateFormat

import models._

object fmt {

  def patternDtm = "yyyy-MM-dd HH:mm"
  def patternWeight = "#0.0 'kg'"
  def patternFatRate = "#0.0 '%'"
  def patternHeight = "##0.0 'cm'"

  def dtm(item: DietLog) =
    (new SimpleDateFormat(patternDtm)).format(item.dtm)

  def weight(item: DietLog) =
    (new DecimalFormat(patternWeight)).format(item.weight)

  def fatRate(item: DietLog) =
    (new DecimalFormat(patternFatRate)).format(item.fatRate)

  def height(item: DietLog) =
    for {
      h <- item.height
      f = new DecimalFormat(patternHeight)
    } yield f.format(h)

}
