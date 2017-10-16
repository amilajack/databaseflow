package services.scalaexport.file

import models.scalaexport.{ScalaFile, TwirlFile}
import services.scalaexport.config.{ExportConfiguration, ExportModel}

object TwirlViewFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val args = model.pkFields.map(field => s"model.${field.propertyName}").mkString(", ")
    val file = TwirlFile(model.viewPackage, model.propertyName + "View")
    file.add(s"@(user: models.user.User, model: ${model.modelClass}, debug: Boolean)(")
    file.add("    implicit request: Request[AnyContent], session: Session, flash: Flash, traceData: util.tracing.TraceData")
    val toInterp = model.pkFields.map(c => "${model." + c.propertyName + "}").mkString(", ")
    file.add(s""")@traceData.logViewClass(getClass)@views.html.admin.layout.page(user, "explore", s"${model.title} [$toInterp]") {""", 1)

    file.add("""<div class="collection with-header">""", 1)
    file.add("<div class=\"collection-header\">", 1)
    if (model.pkFields.nonEmpty) {
      val onClick = s"""onclick="return confirm('Are you sure you want to remove this ${model.title}?')""""
      file.add(s"""<div class="right"><a class="theme-text" href="@${model.routesClass}.editForm($args)">Edit</a></div>""")
      file.add(s"""<div class="right"><a class="theme-text remove-link" $onClick href="@${model.routesClass}.remove($args)">Remove</a></div>""")
    }
    file.add("<h5>", 1)
    file.add(s"""<i class="fa @models.template.Icons.${model.propertyName}"></i>""")
    val toTwirl = model.pkFields.map(c => "@model." + c.propertyName).mkString(", ")
    file.add(s"""${model.title} [$toTwirl]""")
    file.add("</h5>", -1)
    file.add("</div>", -1)

    file.add("<div class=\"collection-item\">", 1)
    file.add("<table class=\"highlight\">", 1)
    file.add("<tbody>", 1)
    model.fields.foreach { field =>
      file.add("<tr>", 1)
      file.add(s"<th>${field.title}</th>")
      model.foreignKeys.find(_.references.forall(_.source == field.columnName)) match {
        case Some(fk) if config.getModelOpt(fk.targetTable).isDefined =>
          file.add("<td>", 1)
          val tgt = config.getModel(fk.targetTable)
          if (tgt.pkFields.forall(f => fk.references.map(_.target).contains(f.columnName))) {

          } else {
            throw new IllegalStateException(s"FK [$fk] does not match PK [${tgt.pkFields.map(_.columnName).mkString(", ")}]...")
          }

          file.add(s"@model.${field.propertyName}")
          if (field.notNull) {
            file.add(s"""<a class="theme-text" href="@${tgt.routesClass}.view(model.${field.propertyName})"><i class="fa @models.template.Icons.${tgt.propertyName}"></i></a>""")
          } else {
            file.add(s"@model.${field.propertyName}.map { v =>", 1)
            file.add(s"""<a class="theme-text" href="@${tgt.routesClass}.view(v)"><i class="fa @models.template.Icons.${tgt.propertyName}"></i></a>""")
            file.add("}", -1)
          }
          file.add("</td>", -1)
        case _ => file.add(s"<td>@model.${field.propertyName}</td>")
      }
      file.add("</tr>", -1)
    }
    file.add("</tbody>", -1)
    file.add("</table>", -1)
    file.add("</div>", -1)
    file.add("</div>", -1)
    addReferences(config, model, file)
    file.add("}", -1)

    file
  }

  def addReferences(config: ExportConfiguration, model: ExportModel, file: TwirlFile) = if (model.validReferences(config).nonEmpty) {
    val args = model.pkFields.map(field => s"model.${field.propertyName}").mkString(", ")
    file.add()
    file.add("""<ul id="model-relations" class="collapsible" data-collapsible="expandable">""", 1)
    model.transformedReferences(config).foreach { r =>
      val src = r._3
      val srcField = r._4
      val tgtField = r._2
      val relArgs = s"""data-table="${src.propertyName}" data-field="${srcField.propertyName}" data-singular="${src.title}" data-plural="${src.plural}""""
      val relAttrs = s"""id="relation-${src.propertyName}-${srcField.propertyName}" $relArgs"""
      val relUrl = src.routesClass + s".by${srcField.className}(model.${tgtField.propertyName}, limit = Some(5))"
      file.add(s"""<li $relAttrs data-url="@$relUrl">""", 1)
      file.add("""<div class="collapsible-header">""", 1)
      file.add(s"""<i class="fa @models.template.Icons.${src.propertyName}"></i>""")
      file.add(s"""<span class="title">${src.plural}</span>&nbsp;by ${srcField.title}""")
      file.add("</div>", -1)
      file.add(s"""<div class="collapsible-body"><span>Loading...</span></div>""")
      file.add("</li>", -1)
    }
    file.add("</ul>", -1)
    file.add("@views.html.components.includeScalaJs(debug)")
    file.add(s"""<script>$$(function() { new RelationService('@${model.routesClass}.relationCounts($args)') });</script>""")
  }
}
