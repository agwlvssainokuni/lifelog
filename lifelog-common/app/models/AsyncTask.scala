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

package models

import java.sql.Connection
import java.util.Date

import scala.language.implicitConversions

import anorm._
import anorm.SQL
import anorm.SqlParser._
import anorm.sqlToSimple
import anorm.toParameterValue

case class AsyncTask(name: String, status: AsyncTask.Status, startDtm: Option[Date], endDtm: Option[Date], totalCount: Option[Long], okCount: Option[Long], ngCount: Option[Long]) {
  var id: Pk[Long] = NotAssigned
  var memberId: Pk[Long] = NotAssigned
}

object AsyncTask {

  sealed trait Status { val code: Int }
  object New extends Status { val code = 1 }
  object Started extends Status { val code = 2 }
  object Running extends Status { val code = 3 }
  object OkEnd extends Status { val code = 4 }
  object NgEnd extends Status { val code = 5 }
  object Unknown extends Status { val code = 0 }

  implicit def status2int(status: Status) = status.code

  implicit def int2status(status: Int) = status match {
    case s if s == New.code => New
    case s if s == Started.code => Started
    case s if s == Running.code => Running
    case s if s == OkEnd.code => OkEnd
    case s if s == NgEnd.code => NgEnd
    case _ => Unknown
  }

  val parser = long("async_tasks.id") ~ long("async_tasks.member_id") ~ str("async_tasks.name") ~ int("async_tasks.status") ~ (date("async_tasks.start_dtm")?) ~ (date("async_tasks.end_dtm")?) ~ (long("async_tasks.total_count")?) ~ (long("async_tasks.ok_count")?) ~ (long("async_tasks.ng_count")?) map {
    case id ~ memberId ~ name ~ status ~ startDtm ~ endDtm ~ totalCount ~ okCount ~ ngCount =>
      val entity = AsyncTask(name, status, startDtm, endDtm, totalCount, okCount, ngCount)
      entity.id = Id(id)
      entity.memberId = Id(memberId)
      entity
  }

  def count(memberId: Long)(implicit c: Connection) =
    SQL("""
        SELECT COUNT(*) FROM async_tasks
        WHERE
            member_id = {memberId}
        """).on(
      'memberId -> memberId).single(scalar[Long])

  def list(memberId: Long, pageNo: Long, pageSize: Long)(implicit c: Connection) =
    SQL("""
        SELECT id, member_id, name, status, start_dtm, end_dtm, total_count, ok_count, ng_count
        FROM async_tasks
        WHERE
            member_id = {memberId}
        ORDER BY
            id DESC
        LIMIT {limit} OFFSET {offset}
        """).on(
      'memberId -> memberId,
      'limit -> pageSize,
      'offset -> pageSize * pageNo).list(parser)

  def find(memberId: Long, id: Long)(implicit c: Connection) =
    SQL("""
        SELECT id, member_id, name, status, start_dtm, end_dtm, total_count, ok_count, ng_count
        FROM async_tasks
        WHERE
            member_id = {memberId}
            AND
            id = {id}
        """).on(
      'memberId -> memberId, 'id -> id).singleOpt(parser)

  def create(memberId: Long, task: AsyncTask)(implicit c: Connection) =
    SQL("""
        INSERT INTO async_tasks (
            member_id,
            name,
            status,
            start_dtm,
            end_dtm,
            total_count,
            ok_count,
            ng_count,
            updated_at
        ) VALUES (
            {memberId},
            {name},
            {status},
            {startDtm},
            {endDtm},
            {totalCount},
            {okCount},
            {ngCount},
            CURRENT_TIMESTAMP
        )
        """).on(
      'memberId -> memberId,
      'name -> task.name, 'status -> (task.status: Int), 'startDtm -> task.startDtm, 'endDtm -> task.endDtm,
      'totalCount -> task.totalCount, 'okCount -> task.okCount, 'ngCount -> task.ngCount).executeUpdate() match {
        case 1 => SQL("""SELECT IDENTITY() FROM dual""").singleOpt(scalar[Long])
        case _ => None
      }

  def update(memberId: Long, id: Long, task: AsyncTask)(implicit c: Connection) =
    SQL("""
        UPDATE async_tasks
        SET
            name = {name},
            status = {status},
            start_dtm = {startDtm},
            end_dtm = {endDtm},
            total_count = {totalCount},
            ok_count = {okCount},
            ng_count = {ngCount},
            updated_at = CURRENT_TIMESTAMP
        WHERE
            member_id = {memberId}
            AND
            id = {id}
        """).on(
      'memberId -> memberId, 'id -> id,
      'name -> task.name, 'status -> (task.status: Int), 'startDtm -> task.startDtm, 'endDtm -> task.endDtm,
      'totalCount -> task.totalCount, 'okCount -> task.okCount, 'ngCount -> task.ngCount).executeUpdate() match {
        case 1 => true
        case _ => false
      }

  def delete(memberId: Long, id: Long)(implicit c: Connection) =
    SQL("""
        DELETE FROM async_tasks
        WHERE
            member_id = {memberId}
            AND
            id = {id}
        """).on(
      'memberId -> memberId, 'id -> id).executeUpdate() match {
        case 1 => true
        case _ => false
      }

  def tryLock(memberId: Long, id: Long)(implicit c: Connection) =
    SQL("""
        SELECT id FROM async_tasks
        WHERE
            member_id = {memberId}
            AND
            id = {id}
        FOR UPDATE
        """).on(
      'memberId -> memberId, 'id -> id).singleOpt(scalar[Long])

}
