package services.scalaexport.file

import models.scalaexport.ThriftFile
import services.scalaexport.config.{ExportEnum, ExportField, ExportModel}

object ThriftModelFile {
  def export(model: ExportModel, enums: Seq[ExportEnum]) = {
    val file = ThriftFile(model.modelPackage, model.className)

    file.add("namespace java " + model.modelPackage.mkString("."))
    file.add()

    val parent = model.modelPackage.map(_ => "../").mkString
    file.add(s"""include "${parent}common.thrift"""")
    file.add(s"""include "${parent}result.thrift"""")
    file.add()

    file.add(s"struct ${model.className} {", 1)
    model.fields.foreach { field =>
      file.add(s"${field.idx + 1}: ${field.thriftVisibility} ${field.thriftType} ${field.propertyName};")
    }
    file.add("}", -1)
    file.add()
    file.add(s"struct ${model.className}Result {", 1)
    file.add("1: required list<result.Filter> filters;")
    file.add("2: required list<result.OrderBy> orderBys;")
    file.add("3: required common.int totalCount;")
    file.add("4: required result.PagingOptions paging;")
    file.add(s"5: required list<${model.className}> results;")
    file.add("6: required common.int durationMs;")
    file.add("7: required common.LocalDateTime occurred;")
    file.add("}", -1)

    file
  }
}
