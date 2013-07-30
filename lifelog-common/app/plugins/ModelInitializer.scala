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

package plugins

import anorm._
import anorm.SqlParser._
import models._
import play.api._
import play.api.db.DB

class ModelInitializer(implicit app: Application) extends Plugin {

  val adminId = 0L
  val adminPwd = "p@ssw0rd"

  override def enabled: Boolean = true

  override def onStart(): Unit = {
    DB.withTransaction { implicit c =>
      for {
        id <- Admin.tryLock(adminId)
        passwd <- SQL("""
            SELECT passwd FROM admins WHERE id = {id}
            """).on(
          'id -> id).singleOpt(scalar[String])
        if (passwd.isEmpty)
      } Admin.updatePw(id, adminPwd)
    }
  }

}
