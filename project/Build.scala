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
            libraryDependencies ++= Libs.appDependencies,
            libraryDependencies ++= Libs.testDependencies,
            templatesImport ++= Seq(
                "utils.ViewHelper._",
                "forms._",
                "io.sphere.client.model._",
                "io.sphere.client.shop.model._"
            )
        ):_*
    )

    object Libs {
        val appDependencies = Seq(
            "io.sphere"             %%  "sphere-play-sdk"   %   "0.36",
            "javax.mail"            %   "mail"              %   "1.4.7",
            "org.jsoup"             %   "jsoup"             %   "1.7.1",
            "commons-codec"         %   "commons-codec"     %   "1.8",
            "com.google.code.gson"  %   "gson"              %   "2.2.4",
            "de.paymill"            %   "paymill-java"      %   "2.6-ct"
        )
        val testDependencies = Seq(
            "org.mockito"   %   "mockito-all"       %       "1.9.5"     %   "test"
        )
    }
}