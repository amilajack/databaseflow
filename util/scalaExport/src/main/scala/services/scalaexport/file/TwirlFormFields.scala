package services.scalaexport.file

import models.scalaexport.OutputFile
import models.schema.ColumnType
import services.scalaexport.config.ExportField

object TwirlFormFields {
  def inputFor(field: ExportField, file: OutputFile) = {
    val prop = field.propertyName
    field.t match {
      case ColumnType.BooleanType =>
        file.add("<div>", 1)
        file.add(s"""<input id="input-$prop-true" type="radio" name="$prop" value="true" @if(model.$prop) { checked="checked" } />""")
        file.add(s"""<label for="input-$prop-true">True</label>""")
        file.add(s"""<input id="input-$prop-false" type="radio" name="$prop" value="false" @if(!model.$prop) { checked="checked" } />""")
        file.add(s"""<label for="input-$prop-false">False</label>""")
        file.add("</div>", -1)
      case _ =>
        file.add(s"""<input id="input-$prop" type="text" name="$prop" value="@model.$prop" />""")
    }
  }
}
