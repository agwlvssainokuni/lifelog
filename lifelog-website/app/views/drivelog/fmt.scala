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

package views.drivelog

import java.text.DecimalFormat
import java.text.SimpleDateFormat

import models.DriveLog
import models.RefuelLog

object fmt {

  def patternDt = "yyyy-MM-dd"
  def patternTripmeter = "#0.0 'km'"
  def patternFuelometer = "#0.0 'km/l'"
  def patternRemaining = "#0 'km'"
  def patternOdometer = "#0 'km'"

  def patternUnit = "#0 '¥/l'"
  def patternQuantity = "##0.00 'l'"
  def patternPrice = "##0 '¥'"
  def patternPriceByCalc = "##0.00 '¥'"

  def dt(item: DriveLog) =
    (new SimpleDateFormat(patternDt)).format(item.dt)

  def tripmeter(item: DriveLog) =
    (new DecimalFormat(patternTripmeter)).format(item.tripmeter)

  def fuelometer(item: DriveLog) =
    (new DecimalFormat(patternFuelometer)).format(item.fuelometer)

  def remaining(item: DriveLog) =
    (new DecimalFormat(patternRemaining)).format(item.remaining)

  def odometer(item: DriveLog) =
    (new DecimalFormat(patternOdometer)).format(item.odometer)

  def unit(item: RefuelLog) =
    (new DecimalFormat(patternUnit)).format(item.unit)

  def quantity(item: RefuelLog) =
    (new DecimalFormat(patternQuantity)).format(item.quantity)

  def price(item: RefuelLog) =
    (new DecimalFormat(patternPrice)).format(item.price)

  def priceByCalc(item: RefuelLog) =
    (new DecimalFormat(patternPriceByCalc)).format(item.unit * item.quantity)

}
