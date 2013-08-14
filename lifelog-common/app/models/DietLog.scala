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

import anorm._
import anorm.SqlParser._
import play.api.Play.current
import play.api.cache.Cache

case class DietLog(dtm: Date, weight: BigDecimal, fatRate: BigDecimal, height: Option[BigDecimal], note: Option[String]) {
  var id: Pk[Long] = NotAssigned
  var memberId: Pk[Long] = NotAssigned
}

object DietLog {

  val parser: RowParser[DietLog] = {
    import java.math.{ BigDecimal => JBigDecimal }
    import SqlParser.{ get => pget }
    long("diet_logs.id") ~ long("diet_logs.member_id") ~ date("diet_logs.dtm") ~ pget[JBigDecimal]("diet_logs.weight") ~ pget[JBigDecimal]("diet_logs.fat_rate") ~ (pget[JBigDecimal]("diet_logs.height")?) ~ (str("diet_logs.note")?) map {
      case id ~ memberId ~ dtm ~ weight ~ fatRate ~ height ~ note =>
        val entity = DietLog(dtm, BigDecimal(weight), BigDecimal(fatRate), height.map(BigDecimal(_)), note)
        entity.id = Id(id)
        entity.memberId = Id(memberId)
        entity
    }
  }

  def count(memberId: Long)(implicit c: Connection): Long =
    SQL("""
        SELECT COUNT(*) FROM diet_logs
        WHERE
            member_id = {memberId}
        """).on(
      'memberId -> memberId).single(scalar[Long])

  def list(memberId: Long, pageNo: Long, pageSize: Long)(implicit c: Connection): Seq[DietLog] =
    SQL("""
        SELECT id, member_id, dtm, weight, fat_rate, height, note FROM diet_logs
        WHERE
            member_id = {memberId}
        ORDER BY
            dtm DESC, id DESC
        LIMIT {limit} OFFSET {offset}
        """).on(
      'memberId -> memberId,
      'limit -> pageSize,
      'offset -> pageSize * pageNo).list(parser)

  def last(memberId: Long)(implicit c: Connection): Option[DietLog] =
    SQL("""
        SELECT id, member_id, dtm, weight, fat_rate, height, note FROM diet_logs
        WHERE
            member_id = {memberId}
        ORDER BY
            dtm DESC, id DESC
        LIMIT 1 OFFSET 0
        """).on(
      'memberId -> memberId).singleOpt(parser)

  def find(memberId: Long, id: Long)(implicit c: Connection): Option[DietLog] =
    SQL("""
        SELECT id, member_id, dtm, weight, fat_rate, height, note FROM diet_logs
        WHERE
            member_id = {memberId}
            AND
            id = {id}
        """).on(
      'memberId -> memberId, 'id -> id).singleOpt(parser)

  def get(memberId: Long, id: Long)(implicit c: Connection): Option[DietLog] =
    Cache.getOrElse(cacheName(id), 0) {
      find(memberId, id)
    }

  def create(memberId: Long, log: DietLog)(implicit c: Connection): Option[Long] =
    SQL("""
        INSERT INTO diet_logs (
            member_id,
            dtm,
            weight,
            fat_rate,
            height,
            note,
            updated_at
        ) VALUES (
            {memberId},
            {dtm},
            {weight},
            {fatRate},
            {height},
            {note},
            CURRENT_TIMESTAMP
        )
        """).on(
      'memberId -> memberId,
      'dtm -> log.dtm,
      'weight -> log.weight.bigDecimal, 'fatRate -> log.fatRate.bigDecimal, 'height -> log.height.map(_.bigDecimal), 'note -> log.note).executeUpdate() match {
        case 1 =>
          SQL("""SELECT IDENTITY() FROM dual""").singleOpt(scalar[Long])
        case _ => None
      }

  def update(memberId: Long, id: Long, log: DietLog)(implicit c: Connection): Boolean =
    SQL("""
        UPDATE diet_logs
        SET
            dtm = {dtm},
            weight = {weight},
            fat_rate = {fatRate},
            height = {height},
            note = {note},
            updated_at = CURRENT_TIMESTAMP
        WHERE
            member_id = {memberId}
            AND
            id = {id}
        """).on(
      'memberId -> memberId, 'id -> id,
      'dtm -> log.dtm,
      'weight -> log.weight.bigDecimal, 'fatRate -> log.fatRate.bigDecimal, 'height -> log.height.map(_.bigDecimal), 'note -> log.note).executeUpdate() match {
        case 1 =>
          Cache.remove(cacheName(id))
          true
        case _ => false
      }

  def delete(memberId: Long, id: Long)(implicit c: Connection): Boolean =
    SQL("""
        DELETE FROM diet_logs
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
        SELECT id FROM diet_logs
        WHERE
            member_id = {memberId}
            AND
            id = {id}
        """).on(
      'memberId -> memberId, 'id -> id).singleOpt(scalar[Long])

  def stream(memberId: Long)(implicit c: Connection) =
    SQL("""
        SELECT id, member_id, dtm, weight, fat_rate, height, note
        FROM diet_logs
        WHERE
            member_id = {memberId}
        ORDER BY id
        """).on(
      'memberId -> memberId)()

  private def cacheName(id: Long): String = "dietLog." + id

}
