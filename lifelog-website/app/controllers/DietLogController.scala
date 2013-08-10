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

import PageParam.implicitPageParam
import play.api.mvc.Controller

object DietLogController extends Controller with ActionBuilder {

  def list(pn: Option[Long], ps: Option[Long]) = AuthnCustomAction { memberId =>
    implicit conn => implicit req =>
      NotImplemented
  }

  def add() = AuthnCustomAction { memberId =>
    implicit conn => implicit req =>
      NotImplemented
  }

  def create() = AuthnCustomAction { memberId =>
    implicit conn => implicit req =>
      NotImplemented
  }

  def edit(id: Long) = AuthnCustomAction { memberId =>
    implicit conn => implicit req =>
      NotImplemented
  }

  def update(id: Long) = AuthnCustomAction { memberId =>
    implicit conn => implicit req =>
      NotImplemented
  }

  def delete(id: Long) = AuthnCustomAction { memberId =>
    implicit conn => implicit req =>
      NotImplemented
  }

}
