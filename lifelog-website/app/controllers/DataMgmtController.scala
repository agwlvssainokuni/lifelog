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

import scala.annotation.implicitNotFound
import scala.io.Source

import DataMgmtForm.FILE
import PageParam.implicitPageParam
import akka.actor._
import akka.actor.actorRef2Scala
import akka.routing._
import anorm._
import models._
import play.api.Play.current
import play.api.db._
import play.api.libs._
import play.api.libs.concurrent._
import play.api.libs.iteratee._
import play.api.mvc._

object DataMgmtController extends Controller with ActionBuilder {

  val actor = Akka.system.actorOf(Props[DataMgmtActor].withRouter(
    RoundRobinRouter(resizer = Some(DefaultResizer()))), "dataMgmt")

  def index() = AuthnCustomAction { memberId =>
    implicit conn => implicit req =>
      Ok(views.html.datamgmt.index())
  }

  def dietlogExport() = Authenticated { memberId =>
    Action {
      sendFile("dietlog", Concurrent.unicast[String]({ channel =>
        actor ! Export.Task(channel, DietLog.stream(memberId)(_))
      }))
    }
  }

  def dietlogImport() = Authenticated { memberId =>
    Action { implicit req =>
      req.body.asMultipartFormData match {
        case Some(body) => body.file(FILE) match {
          case Some(file) =>
            println("import")
            Redirect(routes.DataMgmtController.index().url + "#dietlog")
          case None => BadRequest
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

class DataMgmtActor extends Actor {
  def receive = {
    case Export.Task(channel, stream) => Export(channel, stream)
  }
}

object Export {

  type ExportChannel = Concurrent.Channel[String]
  type ExportSource = Connection => Stream[SqlRow]

  case class Task(channel: ExportChannel, stream: ExportSource)

  def apply(channel: ExportChannel, stream: ExportSource) =
    try {
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
            val h = row.metaData.ms.map(m => escape(m.column.alias.getOrElse(""))).mkString(",")
            channel.push(h + newline)
          }
          channel.push(record + newline)
        }
      }
      channel.eofAndEnd()
    } catch {
      case ex: Exception => channel.end(ex)
    }

  private def newline = "\r\n"

  private def escape(s: String) =
    "\"" + s.flatMap(c => if (c == '"') "\"\"" else c.toString) + "\""
}

object Import {

  import scala.io.Source
  import _root_.common.io.CsvParser

  type RecordHandler = (Connection, Seq[String]) => Option[Long]

  case class Task(file: File, handler: RecordHandler)

  def apply(file: File, handler: RecordHandler) =
    try {
      DB.withTransaction { conn =>
        val source = new CsvParser(Source.fromFile(file))
        try {
          for (record <- source) {
            handler(conn, record)
          }
        } finally {
          source.close
        }
      }
    } finally {
      file.delete()
    }

}
