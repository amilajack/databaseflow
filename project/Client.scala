import Dependencies.{ Serialization, Utils }
import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin.autoImport._
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import playscalajs.ScalaJSPlay
import sbt.Keys._
import sbt._

object Client {
  private[this] val clientSettings = Shared.commonSettings ++ Seq(
    persistLauncher := false,
    libraryDependencies ++= Seq(
      "be.doeraene" %%% "scalajs-jquery" % "0.9.0",
      "com.lihaoyi" %%% "scalatags" % "0.6.0",
      "com.lihaoyi" %%% "utest" % Dependencies.Testing.uTestVersion % "test"
    ),
    testFrameworks += new TestFramework("utest.runner.Framework"),
    scalaJSStage in Global := FastOptStage,
    scapegoatIgnoredFiles := Seq(".*/JsonUtils.scala", ".*/JsonSerializers.scala")
  )

  lazy val client = (project in file("client"))
    .settings(clientSettings: _*)
    .enablePlugins(ScalaJSPlugin, ScalaJSPlay)
    .dependsOn(Shared.sharedJs)

  private[this] val chartingSettings = Shared.commonSettings ++ Seq(
    persistLauncher := false,
    libraryDependencies ++= Seq(
      "be.doeraene" %%% "scalajs-jquery" % "0.9.0",
      "com.lihaoyi" %%% "scalatags" % "0.6.0",
      "com.lihaoyi" %%% "upickle" % Serialization.version,
      "com.beachape" %%% "enumeratum-upickle" % Utils.enumeratumVersion
    ),
    scalaJSStage in Global := FastOptStage,
    scapegoatIgnoredFiles := Seq(".*/JsonUtils.scala", ".*/JsonSerializers.scala")
  )

  lazy val charting = (project in file("charting"))
    .settings(chartingSettings: _*)
    .enablePlugins(ScalaJSPlugin, ScalaJSPlay)
}
