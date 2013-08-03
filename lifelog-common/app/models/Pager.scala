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

case class Pager(no: Option[Long], size: Option[Long], itemCount: Long, pageCount: Long) {
  val pageNo = Pager.pageNo(no)
  val pageSize = Pager.pageSize(size)
  def hasPrev = pageNo - 1 >= 0
  def hasNext = pageNo + 1 < pageCount
}

object Pager {

  def pageNo(no: Option[Long]) = no.getOrElse(0L)
  def pageSize(size: Option[Long]) = size.getOrElse(5L)

  def apply(no: Option[Long], size: Option[Long], itemCount: Long): Pager = {
    val ps = pageSize(size)
    (pageNo(no), itemCount / ps + (if (itemCount % ps == 0L) 0L else 1L)) match {
      case (_, pc) if pc <= 0L =>
        Pager(Some(0L), size, itemCount, 0L)
      case (pn, pc) if pn <= 0L =>
        Pager(Some(0L), size, itemCount, pc)
      case (pn, pc) if (pc <= pn) =>
        Pager(Some(pc - 1L), size, itemCount, pc)
      case (pn, pc) =>
        Pager(Some(pn), size, itemCount, pc)
    }
  }
}
