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

import anorm._
import anorm.SQL
import anorm.SqlParser._
import anorm.SqlParser.{ get => pget }
import anorm.sqlToSimple
import anorm.toParameterValue
import play.api.Play.current
import play.api.cache.Cache

case class RefuelLog(unit: BigDecimal, quantity: BigDecimal, price: BigDecimal, note: Option[String]) {
  var id: Pk[Long] = NotAssigned
}

object RefuelLog {

  val parser = long("refuel_logs.id") ~ pget[JBigDecimal]("refuel_logs.unit") ~ pget[JBigDecimal]("refuel_logs.quantity") ~ pget[JBigDecimal]("refuel_logs.price") ~ (str("refuel_logs.note")?) map {
    case id ~ unit ~ quantity ~ price ~ note =>
      val entity = RefuelLog(BigDecimal(unit), BigDecimal(quantity), BigDecimal(price), note)
      entity.id = Id(id)
      entity
  }

  def count(memberId: Long)(implicit c: Connection): Long =
    SQL("""
        SELECT COUNT(*)
        FROM
            refuel_logs
            JOIN
            drive_logs
            ON
                refuel_logs.id = drive_logs.id
        WHERE
            drive_logs.member_id = {memberId}
        """).on('memberId -> memberId).single(scalar[Long])

  def list(memberId: Long, pageNo: Long, pageSize: Long)(implicit c: Connection): Seq[RefuelLog] =
    SQL("""
        SELECT refuel_logs.id, refuel_logs.unit, refuel_logs.quantity, refuel_logs.price, refuel_logs.note
        FROM
            refuel_logs
            JOIN
            drive_logs
            ON
                refuel_logs.id = drive_logs.id
        WHERE
            drive_logs.member_id = {memberId}
        ORDER BY
            drive_logs.dt DESC, drive_logs.id DESC
        LIMIT {limit} OFFSET {offset}
        """).on(
      'memberId -> memberId, 'limit -> pageSize, 'offset -> pageSize * pageNo).list(parser)

  def find(memberId: Long, id: Long)(implicit c: Connection): Option[RefuelLog] =
    SQL("""
        SELECT refuel_logs.id, refuel_logs.unit, refuel_logs.quantity, refuel_logs.price, refuel_logs.note
        FROM
            refuel_logs
            JOIN
            drive_logs
            ON
                refuel_logs.id = drive_logs.id
        WHERE
            drive_logs.member_id = {memberId}
            AND
            drive_logs.id = {id}
        """).on(
      'memberId -> memberId, 'id -> id).singleOpt(parser)

  def get(memberId: Long, id: Long)(implicit c: Connection): Option[RefuelLog] =
    Cache.getOrElse(cacheName(id), 0) {
      find(memberId, id)
    }

  def create(id: Long, log: RefuelLog)(implicit c: Connection): Option[Long] =
    SQL("""
        INSERT INTO refuel_logs (
            id,
            unit,
            quantity,
            price,
            note,
            updated_at
        ) VALUES (
            {id},
            {unit},
            {quantity},
            {price},
            {note},
            CURRENT_TIMESTAMP
        )
        """).on(
      'id -> id,
      'unit -> log.unit.bigDecimal, 'quantity -> log.quantity.bigDecimal, 'price -> log.price.bigDecimal, 'note -> log.note).executeUpdate() match {
        case 1 => Some(id)
        case _ => None
      }

  def update(id: Long, log: RefuelLog)(implicit c: Connection): Boolean =
    SQL("""
        UPDATE refuel_logs
        SET
            unit = {unit},
            quantity = {quantity},
            price = {price},
            note = {note},
            updated_at = CURRENT_TIMESTAMP
        WHERE
            id = {id}
        """).on(
      'id -> id,
      'unit -> log.unit.bigDecimal, 'quantity -> log.quantity.bigDecimal, 'price -> log.price.bigDecimal, 'note -> log.note).executeUpdate() match {
        case 1 =>
          Cache.remove(cacheName(id))
          true
        case _ => false
      }

  def delete(id: Long)(implicit c: Connection): Boolean =
    SQL("""
        DELETE FROM refuel_logs
        WHERE
            id = {id}
        """).on(
      'id -> id).executeUpdate() match {
        case 1 =>
          Cache.remove(cacheName(id))
          true
        case _ => false
      }

  def tryLock(memberId: Long, id: Long)(implicit c: Connection): Option[Long] =
    SQL("""
        SELECT refuel_logs.id
        FROM
            refuel_logs
            JOIN
            drive_logs
            ON
                refuel_logs.id = drive_logs.id
        WHERE
            drive_logs.member_id = {memberId}
            AND
            drive_logs.id = {id}
        FOR UPDATE
        """).on(
      'memberId -> memberId, 'id -> id).singleOpt(scalar[Long])

  private def cacheName(id: Long) = "refuelLog." + id

}
