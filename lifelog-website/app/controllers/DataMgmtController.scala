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

import java.io.File
import java.sql.Connection

import scala.Array.canBuildFrom
import scala.io.Source

import DataMgmtForm.FILE
import DataMgmtForm.dietlog
import PageParam.implicitPageParam
import anorm._
import common.FlashName
import models._
import play.api.Play.current
import play.api.db._
import play.api.libs._
import play.api.libs.iteratee._
import play.api.mvc._

object DataMgmtController extends Controller with ActionBuilder with TaskUtil {

  def index() = AuthnCustomAction { memberId =>
    implicit conn => implicit req =>
      Ok(views.html.datamgmt.index())
  }

  def dietlogExport() = Authenticated { memberId =>
    Action {
      taskCreate(memberId, "dietlog export") match {
        case Some(taskId) =>
          sendFile("dietlog", Concurrent.unicast[String]({ channel =>
            TaskActor ! Export(memberId, taskId, channel)(DietLog.stream(memberId)(_))
          }))
        case None =>
          Redirect(routes.DataMgmtController.index().url + "#dietlog").flashing(
            FlashName.Error -> FlashName.Task)
      }
    }
  }

  def dietlogImport() = Authenticated { memberId =>
    Action { req =>
      req.body.asMultipartFormData match {
        case Some(body) => body.file(FILE) match {
          case Some(filePart) =>
            taskCreate(memberId, "dietlog import " + filePart.filename) match {
              case Some(taskId) =>
                TaskActor ! Import(memberId, taskId, filePart.ref.file) { (conn, param) =>
                  dietlog.recordForm.bind(param).fold(
                    error => None,
                    log => {
                      implicit val c = conn
                      val (dtm, weight, fatRate, height, note) = log
                      DietLog.create(memberId, DietLog(dtm, weight, fatRate, height, note))
                    })
                }
                Redirect(routes.DataMgmtController.index().url + "#dietlog").flashing(
                  FlashName.Success -> FlashName.Import)
              case None =>
                Redirect(routes.DataMgmtController.index().url + "#dietlog").flashing(
                  FlashName.Error -> FlashName.Task)
            }
          case None =>
            Redirect(routes.DataMgmtController.index().url + "#dietlog").flashing(
              FlashName.Error -> FlashName.Import)
        }
        case None => BadRequest
      }
    }
  }

  private def sendFile(basename: String, content: Enumerator[String]): ChunkedResult[String] =
    Ok.stream(content).withHeaders(
      CONTENT_TYPE -> MimeTypes.forExtension("csv").get,
      CONTENT_DISPOSITION -> ("""attachment; filename="%s"""".format(filename(basename))))

  private def filename(basename: String) = {
    val sdf = new java.text.SimpleDateFormat("yyyyMMddHHmmss")
    val dtm = sdf.format(java.util.Calendar.getInstance.getTime)
    "%s_%s.csv".format(basename, dtm)
  }

}

case class Export(memberId: Long, id: Long, channel: Concurrent.Channel[String])(
  stream: Connection => Stream[Row]) extends Task with TaskUtil {

  override def apply() =
    try {
      taskStarted(memberId, id)
      var count = -1
      DB.withTransaction { conn =>
        for {
          (row, i) <- stream(conn).zipWithIndex
          record = (row.asList.map {
            case Some(v) => escape(v.toString)
            case None => ""
            case v => escape(v.toString)
          }).mkString(",")
        } {
          if (i <= 0) {
            taskRunning(memberId, id)
            val h = (row.metaData.ms.map { m =>
              escape(camelCase(m.column.alias.getOrElse("")))
            }).mkString(",")
            channel.push(h + newline)
          }
          channel.push(record + newline)
          count = i
        }
      }
      channel.eofAndEnd()
      taskOkEnd(memberId, id, (count + 1).toLong, None, None)
    } catch {
      case ex: Exception => channel.end(ex); taskNgEnd(memberId, id)
    }

  private def camelCase(name: String) =
    (for {
      (part, i) <- name.split("_").zipWithIndex
      (ch, j) <- part.zipWithIndex
    } yield {
      if (i <= 0)
        Character.toLowerCase(ch)
      else if (j <= 0)
        Character.toUpperCase(ch)
      else
        Character.toLowerCase(ch)
    }).mkString

  private def newline = "\r\n"

  private def escape(s: String) =
    "\"" + s.flatMap(c => if (c == '"') "\"\"" else c.toString) + "\""
}

case class Import(memberId: Long, id: Long, file: File)(
  handler: (Connection, Map[String, String]) => Option[Long]) extends Task with TaskUtil {

  override def apply() =
    try {
      taskStarted(memberId, id)
      val source = new _root_.common.io.CsvParser(Source.fromFile(file))
      try {
        val result = DB.withTransaction { conn =>
          for {
            header <- if (source.hasNext) Some(source.next) else None
          } yield {
            taskRunning(memberId, id)
            (for {
              record <- source
              param = header.zip(record).map(a => a._1 -> a._2).toMap
            } yield {
              handler(conn, param) match {
                case Some(_) => (1, 0)
                case None => (0, 1)
              }
            }).foldLeft((0, 0, 0)) {
              case ((total, ok, ng), (a, b)) => (total + 1, ok + a, ng + b)
            }
          }
        }
        result match {
          case Some((totalCount, okCount, ngCount)) =>
            taskOkEnd(memberId, id, totalCount, Some(okCount), Some(ngCount))
          case _ =>
            taskNgEnd(memberId, id)
        }
      } finally {
        source.close
      }
    } catch {
      case ex: Exception => taskNgEnd(memberId, id); throw ex
    } finally {
      file.delete()
    }

}
