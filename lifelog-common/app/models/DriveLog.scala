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

import java.math.{ BigDecimal => JBigDecimal }
import java.sql.Connection
import java.util.Date

import anorm._
import anorm.SQL
import anorm.SqlParser._
import anorm.SqlParser.{ get => pget }
import anorm.sqlToSimple
import anorm.toParameterValue
import play.api.Play.current
import play.api.cache.Cache

case class DriveLog(dt: Date, tripmeter: BigDecimal, fuelometer: BigDecimal, remaining: BigDecimal, odometer: BigDecimal, note: Option[String]) {
  var id: Pk[Long] = NotAssigned
  var refuel: Option[Boolean] = None
}

object DriveLog {

  val parser: RowParser[DriveLog] = {
    long("drive_logs.id") ~ date("drive_logs.dt") ~ pget[JBigDecimal]("drive_logs.tripmeter") ~ pget[JBigDecimal]("drive_logs.fuelometer") ~ pget[JBigDecimal]("drive_logs.remaining") ~ pget[JBigDecimal]("drive_logs.odometer") ~ (str("drive_logs.note")?) map {
      case id ~ dt ~ tripmeter ~ fuelometer ~ remaining ~ odometer ~ note =>
        val entity = DriveLog(dt, BigDecimal(tripmeter), BigDecimal(fuelometer), BigDecimal(remaining), BigDecimal(odometer), note)
        entity.id = Id(id)
        entity
    }
  }

  val parserWithRefuel: RowParser[DriveLog] = {
    parser ~ int("refuel") map {
      case entity ~ refuel =>
        entity.refuel = Option(refuel > 0)
        entity
    }
  }

  def count(memberId: Long)(implicit c: Connection): Long =
    SQL("""
        SELECT COUNT(*) FROM drive_logs
        WHERE
            member_id = {memberId}
        """).on(
      'memberId -> memberId).single(scalar[Long])

  def list(memberId: Long, pageNo: Long, pageSize: Long)(implicit c: Connection): Seq[DriveLog] =
    SQL("""
        SELECT
            id, dt, tripmeter, fuelometer, remaining, odometer, note,
            CASE WHEN EXISTS (SELECT * FROM refuel_logs WHERE refuel_logs.drive_log_id = drive_logs.id) THEN 1 ELSE 0 END AS refuel
        FROM drive_logs
        WHERE
            member_id = {memberId}
        """).on(
      'memberid -> memberId).list(parserWithRefuel)

  def find(memberId: Long, id: Long)(implicit c: Connection): Option[DriveLog] =
    SQL("""
        SELECT
            id, dt, tripmeter, fuelometer, remaining, odometer, note
        FROM drive_logs
        WHERE
            member_id = {memberId}
            AND
            id = {id}
        """).on(
      'memberId -> memberId, 'id -> id).singleOpt(parser)

  def get(memberId: Long, id: Long)(implicit c: Connection): Option[DriveLog] =
    Cache.getOrElse(cacheName(id), 0) {
      find(memberId, id)
    }

  def create(memberId: Long, log: DriveLog)(implicit c: Connection): Option[Long] =
    SQL("""
        INSERT INTO drive_logs (
            dt,
            tripmeter,
            fuelometer,
            remaining,
            odometer,
            note
        ) VALUES (
            {dt},
            {tripmeter},
            {fuelometer},
            {remaining},
            {odometer},
            {note}
        )
        """).on(
      'memberId -> memberId,
      'dt -> log.dt,
      'tripmeter -> log.tripmeter.bigDecimal, 'fuelometer -> log.fuelometer.bigDecimal, 'remaining -> log.remaining.bigDecimal, 'odometer -> log.odometer.bigDecimal, 'note -> log.note).executeUpdate() match {
        case 1 =>
          SQL("""SELECT IDENTITY() FROM dual""").singleOpt(scalar[Long])
        case _ => None
      }

  def update(memberId: Long, id: Long, log: DriveLog)(implicit c: Connection): Boolean =
    SQL("""
        UPDATE drive_logs
        SET
            dt = {dt},
            tripmeter = {tripmeter},
            fuelometer = {fuelometer},
            remaining = {remaining},
            odometer = {odometer},
            note = {note}
        WHERE
            member_id = {memberId}
            AND
            id = {id}
        """).on(
      'memberId -> memberId, 'id -> id,
      'dt -> log.dt,
      'tripmeter -> log.tripmeter.bigDecimal, 'fuelometer -> log.fuelometer.bigDecimal, 'remaining -> log.remaining.bigDecimal, 'odometer -> log.odometer.bigDecimal, 'note -> log.note).executeUpdate() match {
        case 1 =>
          Cache.remove(cacheName(id))
          true
        case _ => false
      }

  def delete(memberId: Long, id: Long)(implicit c: Connection): Boolean =
    SQL("""
        DELETE FROM drive_logs
        WHERE
            member_id = {memberId}
            AND
            id = {id}
        """).on(
      'memberId -> memberId, 'id -> id).executeUpdate() match {
        case 1 =>
          Cache.remove(cacheName(id))
          true
        case _ => false
      }

  def tryLock(memberId: Long, id: Long)(implicit c: Connection): Option[Long] =
    SQL("""
        SELECT id FROM drive_logs
        WHERE
            member_id = {memberId}
            AND
            id = {id}
        FOR UPDATE
        """).on(
      'memberId -> memberId, 'id -> id).singleOpt(scalar[Long])

  private def cacheName(id: Long): String = "driveLog." + id

}
