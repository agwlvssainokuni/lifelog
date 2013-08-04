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

package views.member

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Calendar._

import models.Member

object fmt {

  def patternBirthday = "yyyy/MM/dd"
  def patternAge = "#0 '歳'"

  def birthday(item: Member) =
    for {
      bd <- item.birthday
      f = new SimpleDateFormat(patternBirthday)
    } yield f.format(bd)

  def age(item: Member) =
    for {
      bd <- item.birthday
      bday = Calendar.getInstance() match { case d => d.setTime(bd); d }
      ag = computeAge(bday)(Calendar.getInstance())
      f = new DecimalFormat(patternAge)
    } yield f.format(ag)

  def computeAge(from: Calendar)(to: Calendar) = {
    def interval(field: Int) = to.get(field) - from.get(field)
    (interval(YEAR) - 1) + (
      interval(MONTH) match {
        case m if (m > 0) => 1
        case m if (m < 0) => 0
        case _ =>
          interval(DAY_OF_MONTH) match {
            case d if (d >= 0) => 1
            case _ => 0
          }
      })
  }

}
