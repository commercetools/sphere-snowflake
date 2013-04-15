import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "sphere-snowflake"
    val appVersion      = "1.0-SNAPSHOT"
    val appDependencies = Seq(javaCore, javaJdbc)

    // Only compile .less files directly in the stylesheets directory
    def customLessEntryPoints(base: File): PathFinder = (
        (base / "app" / "assets" / "stylesheets" * "*.less")
    )

    lazy val main = play.Project(appName, appVersion, appDependencies).settings(
        Seq(
            resolvers += "sphere" at "http://public-repo.ci.cloud.commercetools.de/content/repositories/releases",
            lessEntryPoints <<= baseDirectory(customLessEntryPoints),
            libraryDependencies ++= Seq(
                Libs.sphereSDK,
                Libs.jSoup,
                Libs.pMock,
                Libs.pMockAPI
            ),
            templatesImport ++= Seq(
                "utils.ViewHelper._",
                "forms._",
                "io.sphere.client.model._",
                "io.sphere.client.shop.model._"
            )
        ):_*
    )

    object Libs {
        lazy val sphereSDK  = "io.sphere" %% "sphere-play-sdk" % "0.27" withSources()
        lazy val jSoup      = "org.jsoup" % "jsoup" % "1.7.1"
        lazy val pMock      = "org.powermock" % "powermock-module-junit4" % "1.5" % "test"
        lazy val pMockAPI   = "org.powermock" % "powermock-api-mockito" % "1.5" % "test"
    }
}