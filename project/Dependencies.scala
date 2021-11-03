import sbt._

object Dependencies {
  private val sttpVersion = "3.3.15"
  lazy val sttp = "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats" % sttpVersion
  lazy val catsEffect = "org.typelevel" %% "cats-effect" % "3.2.9"

}
