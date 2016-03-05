package models.codegen

import enumeratum._

sealed abstract class Engine(val id: String, val driverClass: String, val exampleUrl: String) extends EnumEntry {
  override def toString = id
}

object Engine extends Enum[Engine] {
  case object H2 extends Engine("h2", "org.h2.Driver", "jdbc:h2:~/database.h2db")
  case object MySQL extends Engine("mysql", "com.mysql.jdbc.Driver", "jdbc:mysql://localhost/test")
  case object PostgreSQL extends Engine("postgres", "org.postgresql.Driver", "jdbc:postgresql://hostname:port/dbname")

  override val values = findValues
}