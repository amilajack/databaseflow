import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin.autoImport._
import com.typesafe.sbt.digest.Import._
import com.typesafe.sbt.gzip.Import._
import com.typesafe.sbt.jse.JsEngineImport.JsEngineKeys
import com.typesafe.sbt.less.Import._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.debian.DebianPlugin
import com.typesafe.sbt.packager.docker.DockerPlugin
import com.typesafe.sbt.packager.jdkpackager.JDKPackagerPlugin
import com.typesafe.sbt.packager.linux.LinuxPlugin
import com.typesafe.sbt.packager.rpm.RpmPlugin
import com.typesafe.sbt.packager.universal.UniversalPlugin
import com.typesafe.sbt.packager.windows.WindowsPlugin
import com.typesafe.sbt.web.Import._
import com.typesafe.sbt.web.SbtWeb
import play.routes.compiler.InjectedRoutesGenerator
import play.sbt.routes.RoutesKeys.routesGenerator
import play.sbt.PlayImport.PlayKeys._
import playscalajs.PlayScalaJS.autoImport._
import sbt.Keys._
import sbt.Project.projectToRef
import sbt._

object Server {
  private[this] val dependencies = {
    import Dependencies._
    Seq(
      Cache.ehCache, Akka.actor, Akka.logging, Play.playFilters, Play.playWs,
      Authentication.silhouette, Authentication.hasher, Authentication.persistence, Authentication.crypto,
      WebJars.jquery, WebJars.fontAwesome, WebJars.materialize, WebJars.moment, WebJars.mousetrap, WebJars.plotly,
      Export.csv, Export.xlsx, Ui.swing, Utils.crypto, Utils.scalaGuice, Utils.commonsIo,
      Akka.testkit, Play.playTest, Testing.scalaTest
    )
  }

  private[this] lazy val serverSettings = Shared.commonSettings ++ Seq(
    name := Shared.projectId,
    maintainer := "Kyle Unverferth <kyle@databaseflow.com>",
    description := "Database Flow",

    resolvers += Resolver.jcenterRepo,
    libraryDependencies ++= dependencies,

    scalaJSProjects := Seq(Client.client, Client.charting),
    routesGenerator := InjectedRoutesGenerator,
    externalizeResources := false,

    // Sbt-Web
    JsEngineKeys.engineType := JsEngineKeys.EngineType.Node,
    pipelineStages := Seq(scalaJSProd, digest, gzip),
    includeFilter in (Assets, LessKeys.less) := "*.less",
    excludeFilter in (Assets, LessKeys.less) := "_*.less",
    LessKeys.compress in Assets := true,

    // Code Quality
    scapegoatIgnoredFiles := Seq(".*/Routes.scala", ".*/ReverseRoutes.scala", ".*/JavaScriptReverseRoutes.scala", ".*/*.template.scala")
  )

  lazy val server = {
    val ret = Project(
      id = Shared.projectId,
      base = file(".")
    ).enablePlugins(
      SbtWeb, play.sbt.PlayScala, JavaAppPackaging,
      UniversalPlugin, LinuxPlugin, DebianPlugin, RpmPlugin, DockerPlugin, WindowsPlugin, JDKPackagerPlugin
    ).settings(serverSettings: _*).aggregate(projectToRef(Client.client)).settings(Packaging.settings: _*)

    Shared.withProjects(ret, Seq(Shared.sharedJvm, Database.dblibs, Utilities.metrics, Utilities.licenseModels))
  }
}
