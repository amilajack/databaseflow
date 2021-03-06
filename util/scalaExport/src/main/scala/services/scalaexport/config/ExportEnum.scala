package services.scalaexport.config

import services.scalaexport.ExportHelper

case class ExportEnum(
    pkg: List[String] = Nil,
    name: String,
    className: String,
    values: Seq[String],
    ignored: Boolean = false
) {
  val propertyName = ExportHelper.toIdentifier(className)
  val modelPackage = "models" +: pkg
  val tablePackage = "models" +: "table" +: pkg
  val fullClassName = (modelPackage :+ className).mkString(".")
}
