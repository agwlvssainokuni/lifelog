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

case class Pager(no: Option[Long], size: Long) {
  def adjust(totalCount: Long) = {
    val pageNo = no.getOrElse(0L)
    (totalCount / size + (if (totalCount % size <= 0L) 0L else 1L)) match {
      case 0 => Pager(Some(0L), size)
      case totalPage =>
        if (pageNo < totalPage)
          Pager(Some(pageNo), size)
        else
          Pager(Some(totalPage - 1L), size)
    }
  }
}
