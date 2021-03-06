package models.schema

import enumeratum._

sealed abstract class ColumnType(
  val key: String,
  val asScala: String,
  val fromString: String,
  val requiredImport: Option[String] = None,
  val isNumeric: Boolean = false,
) extends EnumEntry {

  val asScalaFull = requiredImport match {
    case Some(pkg) => pkg + "." + asScala
    case None => asScala
  }

  val className = getClass.getSimpleName.stripSuffix("$")

  def classNameForSqlType(s: String) = className

  override def toString = key
}

object ColumnType extends Enum[ColumnType] {
  case object StringType extends ColumnType("string", "String", "xxx")

  case object BooleanType extends ColumnType("boolean", "Boolean", "xxx == \"true\"")
  case object ByteType extends ColumnType("byte", "Byte", "xxx.toInt.toByte")
  case object ShortType extends ColumnType("short", "Short", "xxx.toInt.toShort", isNumeric = true)
  case object IntegerType extends ColumnType("integer", "Int", "xxx.toInt", isNumeric = true)
  case object LongType extends ColumnType("long", "Long", "xxx.toLong", isNumeric = true)
  case object FloatType extends ColumnType("float", "Float", "xxx.toFloat", isNumeric = true)
  case object DoubleType extends ColumnType("double", "Double", "xxx.toDouble", isNumeric = true)
  case object BigDecimalType extends ColumnType("decimal", "BigDecimal", "BigDecimal(xxx)", isNumeric = true)

  case object DateType extends ColumnType("date", "LocalDate", "util.DateUtils.fromDateString(xxx)", requiredImport = Some("java.time"))
  case object TimeType extends ColumnType("time", "LocalTime", "util.DateUtils.fromTimeString(xxx)", requiredImport = Some("java.time"))
  case object TimestampType extends ColumnType("timestamp", "LocalDateTime", "util.DateUtils.fromIsoString(xxx)", requiredImport = Some("java.time"))

  case object RefType extends ColumnType("ref", "String", "xxx")
  case object XmlType extends ColumnType("xml", "String", "xxx")
  case object UuidType extends ColumnType("uuid", "UUID", "UUID.fromString(xxx)", requiredImport = Some("java.util"))

  case object ObjectType extends ColumnType("object", "String", "xxx")
  case object StructType extends ColumnType("struct", "String", "xxx")
  case object JsonType extends ColumnType("json", "Json", "util.JsonSerializers.toJson(xxx)", requiredImport = Some("io.circe"))

  case object EnumType extends ColumnType("enum", "String", "xxx")
  case object CodeType extends ColumnType("code", "String", "xxx")
  case object TagsType extends ColumnType("hstore", "Seq[models.tag.Tag]", "models.tag.Tag.seqFromString(xxx)")

  case object ByteArrayType extends ColumnType("byteArray", "xxx.split(\",\").map(_.toInt.toByte)", "Array[Byte]")
  case object ArrayType extends ColumnType("array", "Array[Any]", "xxx.split(\",\")") {
    def valForSqlType(s: String) = s match {
      case _ if s.startsWith("_int") => "Seq[Long]"
      case _ if s.startsWith("_uuid") => "Seq[UUID]"
      case _ => "Seq[String]"
    }
    def typForSqlType(s: String) = s match {
      case _ if s.startsWith("_int") => "IntArrayType"
      case _ if s.startsWith("_uuid") => "UuidArrayType"
      case _ => "StringArrayType"
    }
  }

  case object UnknownType extends ColumnType("unknown", "Any", "xxx")

  override val values = findValues
}
