package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.{ExportHelper, ExportTable}

object ReferencesHelper {
  def writeFields(et: ExportTable, file: ScalaFile) = if (et.references.nonEmpty) {
    et.references.foreach { ref =>
      if (ref.pkg != et.pkg) {
        file.addImport(("models" +: ref.pkg).mkString("."), ref.cls + "Schema")
      }
      file.add("Field(", 1)
      file.add(s"""name = "${ref.name}",""")
      file.add(s"""fieldType = ListType(${ref.cls}Schema.${ExportHelper.toIdentifier(ref.cls)}Type),""")
      //file.add(s"""arguments = CommonSchema.limitArg :: CommonSchema.offsetArg :: Nil,""")

      val relationRef = s"${ref.cls}Schema.${ExportHelper.toIdentifier(ref.cls)}By${ExportHelper.toClassName(ref.prop)}"
      val fetcherRef = relationRef + "Fetcher"
      if (ref.notNull) {
        file.add(s"resolve = ctx => $fetcherRef.deferRelSeq($relationRef, ctx.value.${ref.tgt})")
      } else {
        file.add(s"resolve = ctx => $fetcherRef.deferRelSeqOpt($relationRef, ctx.value.${ref.tgt})")
      }
      val comma = if (et.references.lastOption.contains(ref) && et.t.foreignKeys.isEmpty) { "" } else { "," }
      file.add(")" + comma, -1)
    }
  }
}