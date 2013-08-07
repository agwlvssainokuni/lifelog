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

import anorm._
import anorm.SqlParser._
import play.api.Play.current
import play.api.cache.Cache
import play.api.libs.Crypto

case class Admin(loginId: String, nickname: String) {
  var id: Pk[Long] = NotAssigned
}

object Admin {

  val parser = {
    long("admins.id") ~ str("admins.login_id") ~ str("admins.nickname") map {
      case id ~ loginId ~ nickname =>
        val entity = Admin(loginId, nickname)
        entity.id = Id(id)
        entity
    }
  }

  def count()(implicit c: Connection): Long =
    SQL("""
        SELECT COUNT(*) FROM admins
        """).single(scalar[Long])

  def list(pageNo: Long, pageSize: Long)(implicit c: Connection): Seq[Admin] =
    SQL("""
        SELECT id, login_id, nickname FROM admins
        ORDER BY id
        LIMIT {limit} OFFSET {offset}
        """).on(
      'limit -> pageSize,
      'offset -> pageSize * pageNo).list(parser)

  def find(id: Long)(implicit c: Connection): Option[Admin] =
    SQL("""
        SELECT id, login_id, nickname FROM admins
        WHERE
            id = {id}
        """).on(
      'id -> id).singleOpt(parser)

  def get(id: Long)(implicit c: Connection): Option[Admin] =
    Cache.getOrElse(cacheName(id), 0) {
      find(id)
    }

  def create(admin: Admin)(implicit c: Connection): Option[Long] =
    SQL("""
        INSERT INTO admins (
            login_id,
            nickname,
            passwd,
            updated_at
        ) VALUES (
            {loginId},
            {nickname},
            '',
            CURRENT_TIMESTAMP
        )
        """).on(
      'loginId -> admin.loginId, 'nickname -> admin.nickname).executeUpdate() match {
        case 1 =>
          SQL("""SELECT currval('admins_id_seq') FROM dual""").singleOpt(scalar[Long])
        case _ => None
      }

  def update(id: Long, admin: Admin)(implicit c: Connection): Boolean =
    SQL("""
        UPDATE admins
        SET
            login_id = {loginId},
            nickname = {nickname},
            updated_at = CURRENT_TIMESTAMP
        WHERE
            id = {id}
        """).on(
      'id -> id,
      'loginId -> admin.loginId, 'nickname -> admin.nickname).executeUpdate() match {
        case 1 =>
          Cache.remove(cacheName(id))
          true
        case _ => false
      }

  def updatePw(id: Long, passwd: String)(implicit c: Connection): Boolean =
    SQL("""
        UPDATE admins
        SET
            passwd = {passwd},
            updated_at = CURRENT_TIMESTAMP
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
        DELETE FROM admins
        WHERE
            id = {id}
        """).on(
      'id -> id).executeUpdate() match {
        case 1 =>
          Cache.remove(cacheName(id))
          true
        case _ => false
      }

  def authenticate(loginId: String, passwd: String)(implicit c: Connection): Option[Long] =
    SQL("""
        SELECT id FROM admins
        WHERE
            login_id = {loginId}
            AND
            passwd = {passwd}
        """).on(
      'loginId -> loginId, 'passwd -> passwdHash(passwd)).singleOpt(scalar[Long])

  def tryLock(id: Long)(implicit c: Connection): Option[Long] =
    SQL("""
        SELECT id FROM admins
        WHERE
            id = {id}
        FOR UPDATE
        """).on(
      'id -> id).singleOpt(scalar[Long])

  def exists(loginId: String)(implicit c: Connection): Option[Long] =
    SQL("""
        SELECT id FROM admins
        WHERE
            login_id = {loginId}
        FOR UPDATE
        """).on(
      'loginId -> loginId).singleOpt(scalar[Long])

  private def cacheName(id: Long): String = "admin." + id

  private def passwdHash(passwd: String): String = Crypto.sign(passwd)

}
