import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "milmsearch"
  val appVersion      = "0.2-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    "postgresql" % "postgresql" % "9.1-901.jdbc4"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
