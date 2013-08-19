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

import java.util.Calendar

import akka.actor._
import akka.actor.actorRef2Scala
import akka.routing._
import models._
import play.api.Play.current
import play.api.db._
import play.api.libs.concurrent._

trait Task {
  def apply()
}

class TaskActor extends Actor {
  def receive = {
    case task: Task => task()
  }
}

object TaskActor {
  private val actor = Akka.system.actorOf(Props[TaskActor].withRouter(
    RoundRobinRouter(resizer = Some(DefaultResizer()))))
  def !(task: Task) = actor ! task
}

trait TaskUtil {

  def taskCreate(memberId: Long, name: String) =
    DB.withTransaction { implicit c =>
      AsyncTask.create(memberId,
        AsyncTask(name, AsyncTask.New, None, None, None, None, None))
    }

  def taskStarted(memberId: Long, id: Long) =
    lockAndUpdate(memberId, id) {
      case AsyncTask(name, _, _, _, _, _, _) =>
        AsyncTask(name, AsyncTask.Started, Some(now()), None, None, None, None)
    }

  def taskRunning(memberId: Long, id: Long) =
    lockAndUpdate(memberId, id) {
      case AsyncTask(name, _, startDtm, _, _, _, _) =>
        AsyncTask(name, AsyncTask.Running, startDtm, None, None, None, None)
    }

  def taskOkEnd(memberId: Long, id: Long, totalCount: Long, okCount: Option[Long] = None, ngCount: Option[Long] = None) =
    lockAndUpdate(memberId, id) {
      case AsyncTask(name, _, startDtm, _, _, _, _) =>
        AsyncTask(name, AsyncTask.OkEnd, startDtm, Some(now()), Some(totalCount), okCount, ngCount)
    }

  def taskNgEnd(memberId: Long, id: Long) =
    lockAndUpdate(memberId, id) {
      case AsyncTask(name, _, startDtm, _, _, _, _) =>
        AsyncTask(name, AsyncTask.NgEnd, startDtm, Some(now()), None, None, None)
    }

  private def now() = Calendar.getInstance().getTime

  private def lockAndUpdate(memberId: Long, id: Long)(next: AsyncTask => AsyncTask) =
    DB.withTransaction { implicit c =>
      AsyncTask.tryLock(memberId, id) match {
        case Some(_) => AsyncTask.find(memberId, id) match {
          case Some(task) => AsyncTask.update(memberId, id, next(task))
          case _ => false
        }
        case _ => false
      }
    }

}
