scalaVersion := "2.10.3"

scalacOptions ++= Seq(
  "-language:postfixOps",
  "-feature",
  "-encoding",
  "UTF-8"
)

shellPrompt := { state =>
  (try
    Seq("git", "branch").lines_!.find(_.head == '*').map(_.drop(2))
  catch {
    case ex: java.io.IOException => None
  }) match {
    case Some(branch) => "[%s%s %s%s] $ ".format(
      scala.Console.CYAN,
      Project.extract(state).currentProject.id,
      branch,
      scala.Console.RESET)
    case None => "[%s] $ ".format(
      Project.extract(state).currentProject.id)
  }
}

play.Project.playScalaSettings
