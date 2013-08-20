scalaVersion := "2.10.2"

scalacOptions ++= Seq(
  "-language:postfixOps",
  "-feature",
  "-encoding",
  "UTF-8"
)

shellPrompt := { state =>
  "[%s%s %s%s] $ ".format(
    scala.Console.CYAN,
    Project.extract(state).currentProject.id,
    "git branch".lines_!.find{_.head == '*'}.map{_.drop(2)}.getOrElse("-"),
    scala.Console.RESET
  )
}
