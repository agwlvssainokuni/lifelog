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

package views.asynctask

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date

import models._

object fmt {

  def patternDtm = "yyyy-MM-dd HH:mm:ss"
  def patternCount = "#,##0 '件'"

  def status(item: AsyncTask) = item.status match {
    case AsyncTask.New => "登録"
    case AsyncTask.Started => "軌道中"
    case AsyncTask.Running => "実行中"
    case AsyncTask.OkEnd => "正常終了"
    case AsyncTask.NgEnd => "異常終了"
    case _ => "状態不定"
  }

  def startDtm(item: AsyncTask) = dtm(item.startDtm)

  def endDtm(item: AsyncTask) = dtm(item.endDtm)

  def totalCount(item: AsyncTask) = count(item.totalCount)

  def okCount(item: AsyncTask) = count(item.okCount)

  def ngCount(item: AsyncTask) = count(item.ngCount)

  private def dtm(dtm: Option[Date]) =
    (dtm.map { (new SimpleDateFormat(patternDtm)).format(_) }).getOrElse("-")

  private def count(count: Option[Long]) =
    (count.map { (new DecimalFormat(patternCount)).format(_) }).getOrElse("-")

}
