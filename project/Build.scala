import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "milmsearch"
  val appVersion      = "0.2-SNAPSHOT"
  val buildScalaVersion = "2.10.1"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    "postgresql" % "postgresql" % "9.1-901.jdbc4",
    "org.apache.commons" % "commons-email" % "1.3.1",
    "nu.validator.htmlparser" % "htmlparser" % "1.4",
    "org.specs2" %% "specs2" % "2.0" % "test",
    "org.elasticsearch" % "elasticsearch" % "0.90.3"
  )

  val buildSettings = playScalaSettings ++ Seq (
    version      := appVersion,
    scalaVersion := buildScalaVersion
  )

  val main = play.Project(appName, appVersion, appDependencies, settings = buildSettings).settings(
    // Add your own project settings here      
  )

}
