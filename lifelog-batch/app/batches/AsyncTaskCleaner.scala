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

package batches

import scala.util.control.NonFatal

import anorm._
import anorm.SQL
import anorm.SqlParser._
import anorm.sqlToSimple
import anorm.toParameterValue
import batches.common.Batch
import batches.common.BatchStatus
import batches.common.Launch
import play.api.Play.current
import play.api.db._

object AsyncTaskCleaner extends App with Launch {
  launch(classOf[AsyncTaskCleaner])(args)
}

class AsyncTaskCleaner extends Batch {

  val defaultKeep = 100

  override def apply(args: Seq[String]) = {

    val keep = (args.headOption.map { a =>
      try a.toInt catch { case NonFatal(ex) => defaultKeep }
    }).getOrElse(defaultKeep)

    val count = DB.withTransaction { implicit c =>
      val result = for {
        row <- SQL("""SELECT id FROM members""")()
        memberId = row("id")(Column.rowToLong)
        (id, i) <- SQL("""
            SELECT id FROM async_tasks
            WHERE
                member_id = {memberId}
            ORDER BY id DESC
            """).on(
          'memberId -> memberId).list(scalar[Long]).zipWithIndex
        if i >= keep
      } yield {
        SQL("""
            DELETE FROM async_tasks
            WHERE
                member_id = {memberId}
                AND
                id = {id}
            """).on(
          'memberId -> memberId, 'id -> id).executeUpdate()
      }
      result.sum
    }

    if (count > 0) BatchStatus.Ok else BatchStatus.Warn
  }
}
