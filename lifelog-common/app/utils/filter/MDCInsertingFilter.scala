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

package utils.filter

import scala.Array.canBuildFrom
import scala.concurrent.Future

import org.slf4j.MDC

import play.api.mvc._

case class MDCInsertingFilter(header: Seq[String] = Seq("User-Agent", "X-Forwarded-For")) extends Filter {

  val headerPair = header.map { name =>
    val mdcName = for {
      (p, i) <- name.split("-").zipWithIndex
      head = if (i == 0) p.substring(0, 1).toLowerCase else p.substring(0, 1).toUpperCase
      tail = p.substring(1).toLowerCase
    } yield head + tail
    (name, mdcName.mkString)
  }

  override def apply(next: RequestHeader => Future[SimpleResult])(request: RequestHeader): Future[SimpleResult] = {
    try {
      insertMDC(request)
      next(request)
    } finally {
      clearMDC()
    }
  }

  def insertMDC(request: RequestHeader) = {
    MDC.put("method", request.method)
    MDC.put("path", request.path)
    MDC.put("remoteAddr", request.remoteAddress)
    for {
      (name, mdcName) <- headerPair
      value <- request.headers.get(name)
    } {
      MDC.put(mdcName, value)
    }
    request.session.get(Security.username).foreach { id =>
      MDC.put(Security.username, id)
    }
  }

  def clearMDC() = {
    MDC.remove("method")
    MDC.remove("path")
    MDC.remove("remoteAddr")
    for ((name, mdcName) <- headerPair) {
      MDC.remove(mdcName)
    }
    MDC.remove(Security.username)
  }

}
