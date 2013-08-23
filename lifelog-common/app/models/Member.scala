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
import anorm.SQL
import anorm.SqlParser._
import anorm.sqlToSimple
import anorm.toParameterValue
import play.api.Play.current
import play.api.cache.Cache
import play.api.libs._

case class Member(email: String, nickname: String, birthday: Option[Date]) {
  var id: Pk[Long] = NotAssigned
}

object Member {

  val parser = {
    long("members.id") ~ str("members.email") ~ str("members.nickname") ~ (date("members.birthday")?) map {
      case id ~ email ~ nickname ~ birthday =>
        val entity = Member(email, nickname, birthday)
        entity.id = Id(id)
        entity
    }
  }

  def count()(implicit c: Connection): Long =
    SQL("""
        SELECT COUNT(*) FROM members
        """).single(scalar[Long])

  def list(pageNo: Long, pageSize: Long)(implicit c: Connection): Seq[Member] =
    SQL("""
        SELECT id, email, nickname, birthday FROM members
        ORDER BY id
        LIMIT {limit} OFFSET {offset}
        """).on(
      'limit -> pageSize,
      'offset -> pageSize * pageNo).list(parser)

  def find(id: Long)(implicit c: Connection): Option[Member] =
    SQL("""
        SELECT id, email, nickname, birthday FROM members
        WHERE
            id = {id}
        """).on(
      'id -> id).singleOpt(parser)

  def get(id: Long)(implicit c: Connection): Option[Member] =
    Cache.getOrElse(cacheName(id)) {
      find(id)
    }

  def create(member: Member)(implicit c: Connection): Option[Long] =
    SQL("""
        INSERT INTO members (
            email,
            nickname,
            birthday,
            passwd,
            updated_at
        ) VALUES (
            {email},
            {nickname},
            {birthday},
            '',
            CURRENT_TIMESTAMP
        )
        """).on(
      'email -> member.email, 'nickname -> member.nickname, 'birthday -> member.birthday).executeUpdate() match {
        case 1 =>
          SQL("""SELECT IDENTITY() FROM dual""").singleOpt(scalar[Long])
        case _ => None
      }

  def update(id: Long, member: Member)(implicit c: Connection): Boolean =
    SQL("""
        UPDATE members
        SET
            email = {email},
            nickname = {nickname},
            birthday = {birthday},
            updated_at = CURRENT_TIMESTAMP
        WHERE
            id = {id}
        """).on(
      'id -> id,
      'email -> member.email, 'nickname -> member.nickname, 'birthday -> member.birthday).executeUpdate() match {
        case 1 =>
          Cache.remove(cacheName(id))
          true
        case _ => false
      }

  def updatePw(id: Long, passwd: String)(implicit c: Connection): Boolean =
    SQL("""
        UPDATE members
        SET
            passwd = {passwd}
        WHERE
            id = {id}
        """).on(
      'id -> id,
      'passwd -> passwdHash(passwd)).executeUpdate() match {
        case 1 =>
          Cache.remove(cacheName(id))
          true
        case _ => false
      }

  def delete(id: Long)(implicit c: Connection): Boolean =
    SQL("""
        DELETE FROM members
        WHERE
            id = {id}
        """).on(
      'id -> id).executeUpdate() match {
        case 1 =>
          Cache.remove(cacheName(id))
          true
        case _ => false
      }

  def authenticate(email: String, passwd: String)(implicit c: Connection): Option[Long] =
    SQL("""
        SELECT id FROM members
        WHERE
            email = {email}
            AND
            passwd = {passwd}
        """).on(
      'email -> email, 'passwd -> passwdHash(passwd)).singleOpt(scalar[Long])

  def tryLock(id: Long)(implicit c: Connection): Option[Long] =
    SQL("""
        SELECT id FROM members
        WHERE
            id = {id}
        FOR UPDATE
        """).on(
      'id -> id).singleOpt(scalar[Long])

  def exists(email: String)(implicit c: Connection): Option[Long] =
    SQL("""
        SELECT id FROM members
        WHERE
            email = {email}
        """).on(
      'email -> email).singleOpt(scalar[Long])

  private def cacheName(id: Long): String = "member." + id

  private def passwdHash(passwd: String): String = Crypto.sign(passwd)

}
