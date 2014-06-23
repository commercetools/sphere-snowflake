import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "sphere-snowflake"
    val appVersion      = "1.0-SNAPSHOT"
    val appDependencies = Seq(javaCore, javaJdbc)

    // Only compile .less files directly in the stylesheets directory
    def customLessEntryPoints(base: File): PathFinder = base / "app" / "assets" / "stylesheets" * "*.less"

    lazy val main = play.Project(appName, appVersion, appDependencies).settings(
        Seq(
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
            "io.sphere"             %%  "sphere-play-sdk"     %   "0.54.0" exclude("org.scala-stm", "scala-stm_2.10.0"),
            "com.github.jknack"     %   "handlebars"          %   "1.2.1",
            "com.github.jknack"     %   "handlebars-jackson2" %   "1.2.1",
            "javax.mail"            %   "mail"                %   "1.4.7",
            "org.jsoup"             %   "jsoup"               %   "1.7.1",
            "commons-codec"         %   "commons-codec"       %   "1.8",
            "com.google.code.gson"  %   "gson"                %   "2.2.4",
            "de.paymill"            %   "paymill-java"        %   "2.6"
        )
        val testDependencies = Seq(
            "org.mockito"           %   "mockito-all"         %   "1.9.5"     %   "test",
            "com.typesafe.play"     %%  "play-test"           %   "2.2.1"     %   "test" exclude("com.novocode", "junit-interface"),
            "com.novocode"          %   "junit-interface"     %   "0.9"       %   "test"
        )
    }
}
