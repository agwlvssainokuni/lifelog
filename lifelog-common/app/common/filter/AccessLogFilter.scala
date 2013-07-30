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

package common.filter

import play.api.mvc.Filter
import play.api.mvc.Result
import play.api.mvc.RequestHeader
import org.slf4j.Logger
import org.slf4j.LoggerFactory

case class AccessLogFilter(excludes: Seq[String] = Seq()) extends Filter {

  val loggerBegin = LoggerFactory.getLogger("accessLog.begin")
  val loggerEnd = LoggerFactory.getLogger("accessLog.end")

  override def apply(next: RequestHeader => Result)(request: RequestHeader): Result = {
    val logEnabled = !excludes.exists(request.path.startsWith(_))
    try {
      if (logEnabled) logBegin(request)
      next(request)
    } finally {
      if (logEnabled) logEnd(request)
    }
  }

  def logBegin(req: RequestHeader) = if (loggerBegin.isInfoEnabled) {
    loggerBegin.info(req.uri)
  }

  def logEnd(req: RequestHeader) = if (loggerEnd.isInfoEnabled) {
    loggerEnd.info(req.uri)
  }
}
