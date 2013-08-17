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
import java.util.Calendar

import scala.Array.canBuildFrom
import scala.collection.breakOut
import scala.io.Source

import DataMgmtForm._
import PageParam.implicitPageParam
import akka.actor._
import akka.actor.actorRef2Scala
import akka.routing._
import anorm._
import common.FlashName
import models._
import play.api.Play.current
import play.api.db._
import play.api.libs._
import play.api.libs.concurrent._
import play.api.libs.iteratee._
import play.api.mvc._

object DataMgmtController extends Controller with ActionBuilder with AsyncTaskUtil {

  val actor = Akka.system.actorOf(Props[DataMgmtActor].withRouter(
    RoundRobinRouter(resizer = Some(DefaultResizer()))))

  def index() = AuthnCustomAction { memberId =>
    implicit conn => implicit req =>
      Ok(views.html.datamgmt.index())
  }

  def dietlogExport() = Authenticated { memberId =>
    Action {
      taskCreate(memberId, "dietlog export") match {
        case Some(taskId) =>
          sendFile("dietlog", Concurrent.unicast[String]({ channel =>
            actor ! Export.Task(memberId, taskId, channel, DietLog.stream(memberId)(_))
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
            actor ! Import.Task(filePart.ref.file, dietlogImportHandler(memberId))
            Redirect(routes.DataMgmtController.index().url + "#dietlog").flashing(
              FlashName.Success -> FlashName.Import)
          case None =>
            Redirect(routes.DataMgmtController.index().url + "#dietlog").flashing(
              FlashName.Error -> FlashName.Import)
        }
        case None => BadRequest
      }
    }
  }

  private def dietlogImportHandler(memberId: Long)(conn: Connection, param: Map[String, String]) =
    dietlog.recordForm.bind(param).fold(
      error => None,
      log => {
        implicit val c = conn
        val (dtm, weight, fatRate, height, note) = log
        DietLog.create(memberId, DietLog(dtm, weight, fatRate, height, note))
      })

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

class DataMgmtActor extends Actor {
  def receive = {
    case Export.Task(memberId, id, channel, stream) => Export(memberId, id, channel, stream)
    case Import.Task(file, handler) => Import(file, handler)
  }
}

object Export extends AsyncTaskUtil {

  type ExportChannel = Concurrent.Channel[String]
  type ExportSource = Connection => Stream[SqlRow]

  case class Task(memberId: Long, id: Long, channel: ExportChannel, stream: ExportSource)

  def apply(memberId: Long, id: Long, channel: ExportChannel, stream: ExportSource) =
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
            val h = row.metaData.ms.map(m => escape(m.column.alias.getOrElse(""))).mkString(",")
            channel.push(h + newline)
          }
          channel.push(record + newline)
          count = i
        }
      }
      channel.eofAndEnd()
      taskOkEnd(memberId, id, (count + 1).toLong, None, None)
    } catch {
      case ex: Exception =>
        channel.end(ex)
        taskNgEnd(memberId, id)
    }

  private def newline = "\r\n"

  private def escape(s: String) =
    "\"" + s.flatMap(c => if (c == '"') "\"\"" else c.toString) + "\""
}

object Import {

  type RecordHandler = (Connection, Map[String, String]) => Option[Long]

  case class Task(file: File, handler: RecordHandler)

  def apply(file: File, handler: RecordHandler) =
    try {
      val source = new _root_.common.io.CsvParser(Source.fromFile(file))
      try {
        DB.withTransaction { conn =>
          for {
            header <- if (source.hasNext)
              Some(source.next.map(a => camelCase(a)))
            else
              None
          } yield {
            (for {
              record <- source
              param: Map[String, String] = header.zip(record).map(a => a._1 -> a._2)(breakOut)
            } yield {
              handler(conn, param) match {
                case Some(_) => (1, 1, 0)
                case None => (1, 0, 1)
              }
            }).foldLeft((0, 0, 0)) {
              case ((total, ok, ng), (a, b, c)) => (total + a, ok + b, ng + c)
            }
          }
        }
      } finally {
        source.close
      }
    } finally {
      file.delete()
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

}

trait AsyncTaskUtil {

  def taskCreate(memberId: Long, name: String) =
    DB.withTransaction { implicit c =>
      AsyncTask.create(memberId,
        AsyncTask(name, AsyncTask.New, None, None, None, None, None))
    }

  def taskStarted(memberId: Long, id: Long) =
    DB.withTransaction { implicit c =>
      AsyncTask.tryLock(memberId, id) match {
        case Some(_) => AsyncTask.find(memberId, id) match {
          case Some(AsyncTask(name, _, _, _, _, _, _)) =>
            AsyncTask.update(memberId, id,
              AsyncTask(name, AsyncTask.Started, Some(Calendar.getInstance.getTime), None, None, None, None))
          case _ => false
        }
        case _ => false
      }
    }

  def taskRunning(memberId: Long, id: Long) =
    DB.withTransaction { implicit c =>
      AsyncTask.tryLock(memberId, id) match {
        case Some(_) => AsyncTask.find(memberId, id) match {
          case Some(AsyncTask(name, _, startDtm, _, _, _, _)) =>
            AsyncTask.update(memberId, id,
              AsyncTask(name, AsyncTask.Running, startDtm, None, None, None, None))
          case _ => false
        }
        case _ => false
      }
    }

  def taskOkEnd(memberId: Long, id: Long, totalCount: Long, okCount: Option[Long] = None, ngCount: Option[Long] = None) =
    DB.withTransaction { implicit c =>
      AsyncTask.tryLock(memberId, id) match {
        case Some(_) => AsyncTask.find(memberId, id) match {
          case Some(AsyncTask(name, _, startDtm, _, _, _, _)) =>
            AsyncTask.update(memberId, id,
              AsyncTask(name, AsyncTask.OkEnd, startDtm, Some(Calendar.getInstance.getTime), Some(totalCount), okCount, ngCount))
          case _ => false
        }
        case _ => false
      }
    }

  def taskNgEnd(memberId: Long, id: Long) =
    DB.withTransaction { implicit c =>
      AsyncTask.tryLock(memberId, id) match {
        case Some(_) => AsyncTask.find(memberId, id) match {
          case Some(AsyncTask(name, _, startDtm, _, _, _, _)) =>
            AsyncTask.update(memberId, id,
              AsyncTask(name, AsyncTask.NgEnd, startDtm, Some(Calendar.getInstance.getTime), None, None, None))
          case _ => false
        }
        case _ => false
      }
    }

}
