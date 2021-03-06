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

import controllers.common.FlashName.Error
import controllers.common.FlashName.Uri
import play.api.mvc._

trait ActionBuilder extends controllers.common.ActionBuilder {
  self: Controller =>

  def Authenticated(action: Long => EssentialAction): EssentialAction =
    Security.Authenticated(
      req => req.session.get(Security.username).map { id =>
        id.toLong
      },
      req => {
        Redirect(routes.SessionController.index()).flashing(
          Error -> common.FlashName.Unauthorized,
          Uri -> req.uri)
      })(id => action(id))

}
