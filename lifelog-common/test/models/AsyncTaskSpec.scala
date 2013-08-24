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

import java.util.Calendar
import java.util.Calendar.DATE
import java.util.Calendar.HOUR

import org.specs2.execute.AsResult
import org.specs2.execute.Result
import org.specs2.mutable.Specification

import play.api.db._
import play.api.test._

class AsyncTaskSpec extends Specification {

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
          i <- 1 to 5
          email = "user" + i + "@domain"
          nickname = "ニックネーム" + i
          id <- Member.create(Member(email, nickname, None))
          j <- 1 to id.toInt
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

  "AsyncTask#count()" should {
    "memberIdごとの件数を取得できる" in new TestApp {
      DB.withTransaction { implicit c =>
        for (i <- 1L to 5L) {
          AsyncTask.count(i) must_== i
        }
      }
    }
    "memberId=99のデータは存在しない" in new TestApp {
      DB.withTransaction { implicit c =>
        AsyncTask.count(99L) must_== 0L
      }
    }
  }

  "AsyncTask#list()" should {
    "memberIdごとのリストを取得できる (日時降順でソート)" in new TestApp {
      DB.withTransaction { implicit c =>
        for (i <- 1L to 5L) {
          val list = AsyncTask.list(i, 0L, 10L)
          list.size must_== i
        }
      }
    }
    "memberId=99のデータは存在しない" in new TestApp {
      DB.withTransaction { implicit c =>
        AsyncTask.list(99L, 0L, 10L) must beEmpty
      }
    }
    "全5件のデータ (memberId=5) をページサイズ3でペジネーション" in new TestApp {
      DB.withTransaction { implicit c =>
        val list0 = AsyncTask.list(5L, 0L, 3L)
        list0.size must_== 3
        val list1 = AsyncTask.list(5L, 1L, 3L)
        list1.size must_== 2
        AsyncTask.list(5L, 2L, 3L) must beEmpty
      }
    }
  }

  "AsyncTask#find()" should {
    "memberIdとidを指定してエンティティを取得できる (1, 1)" in new TestApp {
      DB.withTransaction { implicit c =>
        AsyncTask.find(1L, 1L) must beSome
      }
    }
    "memberIdとidの組合せが不適切な場合はエンティティを取得できない (1, 2)" in new TestApp {
      DB.withTransaction { implicit c =>
        AsyncTask.find(1L, 2L) must beNone
      }
    }
    "memberIdが存在しない場合はエンティティを取得できない (99L, 1L)" in new TestApp {
      DB.withTransaction { implicit c =>
        AsyncTask.find(99L, 1L) must beNone
      }
    }
    "idが存在しない場合はエンティティを取得できない (1L, 99L)" in new TestApp {
      DB.withTransaction { implicit c =>
        AsyncTask.find(1L, 99L) must beNone
      }
    }
    "memberIdとidの両方が存在しない場合はエンティティを取得できない (99L, 99L)" in new TestApp {
      DB.withTransaction { implicit c =>
        AsyncTask.find(99L, 99L) must beNone
      }
    }
  }

  "AsyncTask#update()" should {
    "memberIdとidを指定してエンティティを更新できる (1, 1)" in new TestApp {
      DB.withTransaction { implicit c =>
        val orig = AsyncTask.find(1L, 1L).get
        val task = AsyncTask("name", AsyncTask.NgEnd, None, None, None, None, None)
        AsyncTask.update(1L, 1L, task) must beTrue
        AsyncTask.find(1L, 1L) must beSome.which { task =>
          task.name must_!= orig.name
          task.status must_!= orig.status
          task.startDtm must beNone
          task.endDtm must beNone
          task.totalCount must beNone
          task.okCount must beNone
          task.ngCount must beNone
        }
      }
    }
    "memberIdとidの組合せが不適切な場合はエンティティを更新できない (1, 2)" in new TestApp {
      DB.withTransaction { implicit c =>
        val task = AsyncTask("name", AsyncTask.NgEnd, None, None, None, None, None)
        AsyncTask.update(1L, 2L, task) must beFalse
      }
    }
    "memberIdが存在しない場合はエンティティを更新できない (99L, 1L)" in new TestApp {
      DB.withTransaction { implicit c =>
        val task = AsyncTask("name", AsyncTask.NgEnd, None, None, None, None, None)
        AsyncTask.update(99L, 1L, task) must beFalse
      }
    }
    "idが存在しない場合はエンティティを更新できない (1L, 99L)" in new TestApp {
      DB.withTransaction { implicit c =>
        val task = AsyncTask("name", AsyncTask.NgEnd, None, None, None, None, None)
        AsyncTask.update(1L, 99L, task) must beFalse
      }
    }
    "memberIdとidの両方が存在しない場合はエンティティを更新できない (99L, 99L)" in new TestApp {
      DB.withTransaction { implicit c =>
        val task = AsyncTask("name", AsyncTask.NgEnd, None, None, None, None, None)
        AsyncTask.update(99L, 99L, task) must beFalse
      }
    }
  }

  "AsyncTask#delete()" should {
    "memberIdとidを指定してエンティティを削除できる (1, 1)" in new TestApp {
      DB.withTransaction { implicit c =>
        AsyncTask.find(1L, 1L) must beSome
        AsyncTask.delete(1L, 1L) must beTrue
        AsyncTask.find(1L, 1L) must beNone
      }
    }
    "memberIdとidの組合せが不適切な場合はエンティティを削除できない (1, 2)" in new TestApp {
      DB.withTransaction { implicit c =>
        AsyncTask.delete(1L, 2L) must beFalse
      }
    }
    "memberIdが存在しない場合はエンティティを削除できない (99L, 1L)" in new TestApp {
      DB.withTransaction { implicit c =>
        AsyncTask.delete(99L, 1L) must beFalse
      }
    }
    "idが存在しない場合はエンティティを削除できない (1L, 99L)" in new TestApp {
      DB.withTransaction { implicit c =>
        AsyncTask.delete(1L, 99L) must beFalse
      }
    }
    "memberIdとidの両方が存在しない場合はエンティティを削除できない (99L, 99L)" in new TestApp {
      DB.withTransaction { implicit c =>
        AsyncTask.delete(99L, 99L) must beFalse
      }
    }
  }

}
