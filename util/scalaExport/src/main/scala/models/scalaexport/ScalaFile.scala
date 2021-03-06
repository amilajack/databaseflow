package models.scalaexport

object ScalaFile {
  val sharedSrc = "shared/src/main/scala"
}

case class ScalaFile(
    override val pkg: Seq[String], override val key: String, root: Option[String] = None
) extends OutputFile(root.getOrElse("app"), pkg, key, key + ".scala") {

  private[this] var imports = Set.empty[(String, String)]

  def addImport(p: String, c: String) = imports += (p -> c)

  override def prefix = {
    val importString = if (imports.isEmpty) {
      ""
    } else {
      imports.toSeq.groupBy(_._1).mapValues(_.map(_._2)).toList.sortBy(_._1).map { i =>
        i._2.size match {
          case 1 => s"import ${i._1}.${i._2.head}"
          case _ => s"import ${i._1}.{${i._2.sorted.mkString(", ")}}"
        }
      }.mkString("\n") + "\n\n"
    }

    s"/* Generated File */\npackage ${pkg.mkString(".")}\n\n$importString"
  }
}
