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

package batches

import java.util.Calendar
import java.util.Calendar.DATE
import java.util.Calendar.HOUR

import org.specs2.execute.AsResult
import org.specs2.execute.Result
import org.specs2.mutable.Specification

import batches.common.BatchStatus
import models._
import play.api._
import play.api.db._
import play.api.test.WithApplication

class AsyncTaskCleanerSpec extends Specification {

  def newDate(date: Int, hour: Int) = Calendar.getInstance() match {
    case cal =>
      cal.add(DATE, date)
      cal.add(HOUR, hour)
      cal.getTime()
  }

  abstract class TestApp extends WithApplication {
    override def around[T: AsResult](t: => T): Result = super.around {
      DB.withTransaction { implicit c =>
        for {
          i <- 1 to 150
          email = "user" + i + "@domain"
          nickname = "ニックネーム" + i
          id <- Member.create(Member(email, nickname, None))
          j <- 1 to i
          name = "タスク名/" + i + "/" + j
          startDtm = Some(newDate(i, 0))
          endDtm = Some(newDate(i, j))
          totalCount = Some((i + j).toLong)
          okCount = Some(i.toLong)
          ngCount = Some(j.toLong)
        } {
          val task = AsyncTask(name, AsyncTask.OkEnd, startDtm, endDtm, totalCount, okCount, ngCount)
          AsyncTask.create(id, task)
        }
      }
      t
    }
  }

  def taskList(memberId: Long)(implicit app: Application): Seq[AsyncTask] =
    DB.withTransaction { implicit c =>
      AsyncTask.list(memberId, 0L, 150L)
    }

  "AsyncTaskCleaner" should {

    "引数なし (100件残し)" in new TestApp {
      val keep = 100
      val before = for (i <- 1 to 150) yield taskList(i.toLong)
      (new AsyncTaskCleaner)(Seq()) must_== BatchStatus.Ok
      for ((list, i) <- before.zipWithIndex) {
        taskList((i + 1).toLong) must_== list.take(keep)
      }
    }

    "引数指定で110件残し (デフォルトより大きい)" in new TestApp {
      val keep = 110
      val before = for (i <- 1 to 150) yield taskList(i.toLong)
      (new AsyncTaskCleaner)(Seq(keep.toString)) must_== BatchStatus.Ok
      for ((list, i) <- before.zipWithIndex) {
        taskList((i + 1).toLong) must_== list.take(keep)
      }
    }

    "引数指定で90件残し (デフォルトより小さい)" in new TestApp {
      val keep = 90
      val before = for (i <- 1 to 150) yield taskList(i.toLong)
      (new AsyncTaskCleaner)(Seq(keep.toString)) must_== BatchStatus.Ok
      for ((list, i) <- before.zipWithIndex) {
        taskList((i + 1).toLong) must_== list.take(keep)
      }
    }

    "削除対象なし" in new WithApplication {
      (new AsyncTaskCleaner)(Seq()) must_== BatchStatus.Warn
    }
  }
}
