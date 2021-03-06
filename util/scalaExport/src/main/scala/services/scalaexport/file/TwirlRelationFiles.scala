package services.scalaexport.file

import models.scalaexport.TwirlFile
import services.scalaexport.config.{ExportConfiguration, ExportField, ExportModel}

object TwirlRelationFiles {
  private[this] val viewArgs = "orderBy: Option[String], orderAsc: Boolean, limit: Int, offset: Int"

  private[this] def writeTable(model: ExportModel, refFields: Seq[ExportField], listFile: TwirlFile) = {
    val searchColumns = model.fields.filter(_.inSearch)
    val viewCall = model.routesClass + ".by" + refFields.map(_.className).mkString
    val refProps = refFields.map(_.propertyName).mkString(", ")
    val refArgs = refFields.map(r => r.propertyName + ": " + r.scalaTypeFull).mkString(", ")

    listFile.add(s"@(user: models.user.SystemUser, $refArgs, modelSeq: Seq[${model.modelClass}], $viewArgs)(", 2)
    listFile.add("implicit request: Request[AnyContent], session: Session, flash: Flash, traceData: util.tracing.TraceData")
    listFile.add(s")@traceData.logViewClass(getClass)", -2)

    listFile.add("@views.html.admin.explore.list(", 1)
    listFile.add("user = user,")
    listFile.add(s"""model = "${model.title}",""")
    listFile.add(s"""modelPlural = "${model.plural}",""")
    listFile.add(s"icon = models.template.Icons.${model.propertyName},")
    listFile.add("cols = Seq(", 1)
    searchColumns.foreach {
      case c if searchColumns.lastOption.contains(c) => listFile.add(s""""${c.propertyName}" -> "${c.title}"""")
      case c => listFile.add(s""""${c.propertyName}" -> "${c.title}",""")
    }
    listFile.add("),", -1)
    listFile.add("orderBy = orderBy,")
    listFile.add("orderAsc = orderAsc,")
    listFile.add("totalCount = None,")
    listFile.add(s"rows = modelSeq.map(model => ${model.viewHtmlPackage.mkString(".")}.${model.propertyName}DataRow(model)),")
    listFile.add("calls = models.result.web.ListCalls(", 1)
    listFile.add(s"orderBy = Some($viewCall($refProps, _, _, Some(limit), Some(0))),")
    listFile.add(s"search = None,")
    listFile.add(s"next = $viewCall($refProps, orderBy, orderAsc, Some(limit), Some(offset + limit)),")
    listFile.add(s"prev = $viewCall($refProps, orderBy, orderAsc, Some(limit), Some(offset - limit)),")
    listFile.add("),", -1)
    listFile.add("limit = limit,")
    listFile.add("offset = offset,")
    listFile.add("q = None,")
    listFile.add("fullUI = false")
    listFile.add(")", -1)

  }

  def export(config: ExportConfiguration, model: ExportModel) = {
    model.foreignKeys.flatMap {
      case fk if fk.references.lengthCompare(1) == 0 =>
        val refFields = fk.references.map(r => model.getField(r.source))
        val listFile = TwirlFile(model.viewPackage, model.propertyName + "By" + refFields.map(_.className).mkString)
        writeTable(model, refFields, listFile)
        Some(listFile)
      case _ => None
    }
  }
}
