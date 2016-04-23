package services.sandbox

import utils.{ ApplicationContext, Logging }

import scala.concurrent.Future

object SandboxTask {
  val all = Seq(
    Testbed,
    ShowSettings,
    DatabaseTest,
    HtmlSandbox,
    RebuildMaster
  )

  val byId = all.map(x => x.id -> x).toMap
}

trait SandboxTask extends Logging {
  def id: String
  def name: String
  def description: String
  def run(ctx: ApplicationContext): Future[String]
}
