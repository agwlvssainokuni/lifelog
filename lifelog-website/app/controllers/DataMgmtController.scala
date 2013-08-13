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

import scala.concurrent.ExecutionContext.Implicits.global

import PageParam.implicitPageParam
import models._
import play.api.libs.iteratee._
import play.api.mvc._

object DataMgmtController extends Controller with ActionBuilder {

  def index() = AuthnCustomAction { memberId =>
    implicit conn => implicit req =>
      Ok(views.html.datamgmt.index())
  }

  def dietlogExport() = AuthnCustomAction { memberId =>
    implicit conn => implicit req =>
      val content = (for {
        (row, i) <- DietLog.stream(memberId).zipWithIndex
        record = (row.asList.map {
          case Some(v) => escape(v.toString)
          case None => ""
          case v => escape(v.toString)
        }).mkString(",")
      } yield {
        if (i <= 0) {
          val h = row.metaData.ms.map(m => escape(m.column.alias.getOrElse(""))).mkString(",")
          Seq(h + newline, record + newline)
        } else {
          Seq(record + newline)
        }
      }).flatten
      sendFile("dietlog", content)
  }

  def dietlogImport() = AuthnCustomAction { memberId =>
    implicit conn => implicit req =>
      NotImplemented
  }

  private def sendFile[T](basename: String, content: Stream[String]) =
    Ok.stream(Enumerator.enumerate(content)).withHeaders(
      CONTENT_TYPE -> play.api.libs.MimeTypes.forExtension("csv").getOrElse(play.api.http.ContentTypes.BINARY),
      CONTENT_DISPOSITION -> ("""attachment; filename="%s"""".format(filename(basename))))

  private def filename(basename: String) = {
    val sdf = new java.text.SimpleDateFormat("yyyyMMddHHmmss")
    val dtm = sdf.format(java.util.Calendar.getInstance.getTime)
    "%s_%s.csv".format(basename, dtm)
  }

  private def newline = "\r\n"

  private def escape(s: String) =
    "\"" + s.flatMap(c => if (c == '"') "\"\"" else c.toString) + "\""

}
