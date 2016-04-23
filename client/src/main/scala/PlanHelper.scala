import java.util.UUID

import models._
import models.template._
import org.scalajs.jquery.{ jQuery => $ }
import ui.ProgressManager
import utils.JQueryUtils

trait PlanHelper {
  this: DatabaseFlow =>
  protected[this] def handlePlanResultResponse(pr: PlanResultResponse) = {
    //Logging.info(s"Received plan with [${pr.plan.maxRows}] rows.")
    val occurred = new scalajs.js.Date(pr.result.occurred.toDouble)
    val content = QueryPlanTemplate.forPlan(pr, occurred.toISOString, occurred.toString)
    ProgressManager.completeProgress(pr.result.queryId, pr.id, Icons.queryPlan, pr.result.title, content, QueryPlanTemplate.actions)

    val workspace = $(s"#workspace-${pr.result.queryId}")
    val panel = $(s"#${pr.id}", workspace)

    workOutPlanWidth(pr.id)

    val chart = $(s".plan-chart", panel)
    val raw = $(s".plan-raw", panel)

    JQueryUtils.clickHandler($("a", panel), _.toggleClass("open"))

    val planViewToggle = $(s".plan-view-toggle", panel)
    var showingChart = true

    JQueryUtils.clickHandler(planViewToggle, (f) => {
      if (showingChart) {
        planViewToggle.text("View Plan Chart")
        chart.hide()
        raw.show()
      } else {
        planViewToggle.text("View Raw Plan")
        chart.show()
        raw.hide()
      }
      showingChart = !showingChart
    })
  }

  protected[this] def handlePlanErrorResponse(per: PlanErrorResponse) = {
    val occurred = new scalajs.js.Date(per.error.occurred.toDouble)
    val content = ErrorTemplate.forPlanError(per, occurred.toISOString, occurred.toString)

    ProgressManager.completeProgress(per.error.queryId, per.id, Icons.error, "Plan Error", content, Nil)
  }

  private[this] def workOutPlanWidth(id: UUID) = {
    val container = $("#" + id)
    val tree = $(".tree", container)
    val rootNode = $(".root-node", container)

    val originalTreeWidth = tree.width()
    var treeWidth = originalTreeWidth
    var nodeWidth = rootNode.width()
    var continue = true
    while (continue) {
      treeWidth = tree.width(treeWidth + 500).width()
      val newNodeWidth = rootNode.width()
      if (newNodeWidth == nodeWidth) {
        continue = false
      } else {
        nodeWidth = newNodeWidth
      }
    }
    tree.width(nodeWidth + 20)
  }
}
