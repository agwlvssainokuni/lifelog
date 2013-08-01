import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "lifelog"
  val appVersion      = "0.0"

  val commonDeps = Seq(
    jdbc,
    anorm,
    "org.fluentd" % "fluent-logger" % "0.2.10"
  )

  val adminDeps = Seq(
  )

  val websiteDeps = Seq(
  )

  val commonProj = play.Project(
    appName + "-common",
    appVersion,
    commonDeps,
    path = file(appName + "-common")
  )

  val adminProj = play.Project(
    appName + "-admin",
    appVersion,
    adminDeps,
    path = file(appName + "-admin")
  ).dependsOn(commonProj)

  val websiteProj = play.Project(
    appName + "-website",
    appVersion,
    websiteDeps,
    path = file(appName + "-website")
  ).dependsOn(commonProj)

  val main = play.Project(
    appName,
    appVersion
  ).dependsOn(
    adminProj,
    websiteProj
  ).aggregate(
    adminProj,
    websiteProj
  )

}
