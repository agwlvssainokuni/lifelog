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

package batches.common

import java.io.File

import scala.language.implicitConversions
import scala.util.control.NonFatal

import play.api._

trait Batch extends (Seq[String] => BatchStatus)

sealed trait BatchStatus {
  def code: Int
}

object BatchStatus {
  object Ok extends BatchStatus { override def code: Int = 0 }
  object Warn extends BatchStatus { override def code: Int = 50 }
  object Error extends BatchStatus { override def code: Int = 100 }
  object Fatal extends BatchStatus { override def code: Int = 255 }
}

object Batch {

  val BASEDIR = "app.basedir"
  val MODE = "app.mode"

  val basedir = Option(System.getProperty(BASEDIR)) match {
    case Some(path) => new File(path)
    case None => new File(".")
  }

  implicit val mode = Option(System.getProperty(MODE)).map(_.toLowerCase) match {
    case Some("prod") => Mode.Prod
    case Some("test") => Mode.Test
    case _ => Mode.Dev
  }

  def apply[T <: Batch](klass: Class[T])(args: Seq[String])(implicit m: Mode.Mode): Option[BatchStatus] = {
    val application = new DefaultApplication(basedir, classOf[Batch].getClassLoader(), None, m)
    Play.start(application)
    try {
      for {
        batch <- instantiate(klass)
      } yield batch(args)
    } finally {
      Play.stop()
    }
  }

  def instantiate[T](klass: Class[T]): Option[T] = {
    try Some(klass.newInstance()) catch {
      case NonFatal(ex) => None
    }
  }
}

trait Launch {
  def launch[T <: Batch](klass: Class[T])(args: Seq[String])(implicit m: Mode.Mode): Unit =
    Batch(klass)(args) match {
      case Some(batchStatus) if m == Mode.Prod =>
        sys.exit(batchStatus.code)
      case None if m == Mode.Prod =>
        sys.exit(BatchStatus.Fatal.code)
      case _ => ()
    }
}
