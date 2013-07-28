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

import play.api.Play.current
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.db.DB
import play.api.mvc._
import views.html.{ admin => view }
import routes.{ AdminController => route }

object AdminController extends Controller with Authentication {

  def list(pn: Long = 0, ps: Long = 5) = withAuthenticated { adminId =>
    Action { implicit req =>
      DB.withTransaction { implicit c =>
        Ok(view.list())
      }
    }
  }

  def add() = withAuthenticated { adminId =>
    Action { implicit req =>
      DB.withTransaction { implicit c =>
        Ok(view.add())
      }
    }
  }

  def create() = withAuthenticated { adminId =>
    Action { implicit req =>
      DB.withTransaction { implicit c =>
        Redirect(route.edit(1L)).flashing(
          "success" -> "create")
      }
    }
  }

  def edit(id: Long) = withAuthenticated { adminId =>
    Action { implicit req =>
      DB.withTransaction { implicit c =>
        Ok(view.edit(id))
      }
    }
  }

  def update(id: Long) = withAuthenticated { adminId =>
    Action { implicit req =>
      DB.withTransaction { implicit c =>
        Redirect(route.edit(id)).flashing(
          "success" -> "update")
      }
    }
  }

  def editPw(id: Long) = withAuthenticated { adminId =>
    Action { implicit req =>
      DB.withTransaction { implicit c =>
        Ok(view.editPw(id))
      }
    }
  }

  def updatePw(id: Long) = withAuthenticated { adminId =>
    Action { implicit req =>
      DB.withTransaction { implicit c =>
        Redirect(route.edit(id)).flashing(
          "success" -> "updatePw")
      }
    }
  }

  def delete(id: Long) = withAuthenticated { adminId =>
    Action { implicit req =>
      DB.withTransaction { implicit c =>
        Redirect(route.list()).flashing(
          "success" -> "delete")
      }
    }
  }

}
