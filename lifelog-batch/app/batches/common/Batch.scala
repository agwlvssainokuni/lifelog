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

import scala.util.control.NonFatal

import play.api._

trait Batch extends (Array[String] => Int)

object Launcher extends App {

  val BASEDIR = "app.basedir"
  val MODE = "app.mode"

  val basedir = Option(System.getProperty(BASEDIR)) match {
    case Some(path) => new File(path)
    case None => new File(".")
  }

  val mode = Option(System.getProperty(MODE)) match {
    case Some("prod") => Mode.Prod
    case Some("test") => Mode.Test
    case _ => Mode.Dev
  }

  val application = new DefaultApplication(basedir, getClass().getClassLoader(), None, mode)

  Play.start(application)
  val exitCode = try {
    for {
      name <- args.headOption
      batch <- batch(name)
    } yield batch(args.tail)
  } finally {
    Play.stop()
  }

  exitCode match {
    case Some(code) =>
      if (mode == Mode.Prod) System.exit(code) else ()
    case _ => ()
  }

  def batch(name: String): Option[Batch] = {
    try {
      Some(Class.forName(name).newInstance().asInstanceOf[Batch])
    } catch {
      case NonFatal(ex) => None
    }
  }
}
