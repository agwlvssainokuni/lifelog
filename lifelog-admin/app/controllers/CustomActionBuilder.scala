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

import java.sql.Connection

import play.api.Play.current
import play.api.db.DB
import play.api.mvc._

trait CustomActionBuilder {
  self: Controller =>

  def Authenticated(action: Long => EssentialAction): EssentialAction =
    Security.Authenticated(
      req => req.session.get(Security.username).map { id =>
        id.toLong
      },
      req => {
        Redirect(routes.SessionController.index()).flashing(
          "error" -> "unauthorized",
          "uri" -> req.uri)
      })(id => action(id))

  def CustomAction(block: Connection => Request[AnyContent] => Result): EssentialAction =
    Action { request =>
      DB.withTransaction { connection =>
        block(connection)(request)
      }
    }

  def AuthnCustomAction(block: Long => Connection => Request[AnyContent] => Result): EssentialAction =
    Authenticated { id => CustomAction(block(id)) }

}