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

import scala.reflect.ClassTag

import play.api.Play.current
import play.api.db.DB
import play.api.mvc._

trait CustomActionBuilder extends Authentication {
  self: Controller =>

  def CustomAction(block: Request[AnyContent] => Result): EssentialAction =
    Action { request =>
      block(request)
    }

  def CustomAction(block: Request[AnyContent] => Connection => Result)(implicit c: ClassTag[Connection]): EssentialAction =
    Action { request =>
      DB.withTransaction { connection =>
        block(request)(connection)
      }
    }

  def AuthnCustomAction(block: Long => Request[AnyContent] => Result): EssentialAction =
    withAuthenticated { id => CustomAction(block(id)) }

  def AuthnCustomAction(block: Long => Request[AnyContent] => Connection => Result)(implicit c: ClassTag[Connection]): EssentialAction =
    withAuthenticated { id => CustomAction(block(id)) }

}
