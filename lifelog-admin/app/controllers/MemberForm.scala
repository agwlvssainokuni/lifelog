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

import models.Member
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._

object MemberForm {

  val EMAIL = "email"
  val EMAIL_MIN = 1
  val EMAIL_MAX = 256

  val NICKNAME = "nickname"
  val NICKNAME_MIN = 1
  val NICKNAME_MAX = 256

  val BIRTHDAY = "birthday"
  val BIRTHDAY_PATTERN = "yyyy-MM-dd"

  val PASSWORD = "password"
  val PASSWORD_MIN = 1
  val PASSWORD_MAX = 32

  val CONFIRM = "confirm"
  val CONFIRM_MIN = 1
  val CONFIRM_MAX = 32

  val memberForm: Form[Member] = Form(mapping(
    EMAIL -> email.verifying(minLength(EMAIL_MIN), maxLength(EMAIL_MAX)),
    NICKNAME -> nonEmptyText(NICKNAME_MIN, NICKNAME_MAX),
    BIRTHDAY -> optional(date(BIRTHDAY_PATTERN)))(Member.apply)(Member.unapply))

  val passwdForm: Form[(String, String)] = Form(tuple(
    PASSWORD -> nonEmptyText(PASSWORD_MIN, PASSWORD_MAX),
    CONFIRM -> nonEmptyText(CONFIRM_MIN, CONFIRM_MAX)))
}
