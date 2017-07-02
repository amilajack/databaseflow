package models.query

import java.util.UUID

import models.schema.ColumnType
import models.schema.ColumnType.StringType

object QueryResult {
  case class Col(
    name: String,
    t: ColumnType,
    precision: Option[Int] = None,
    scale: Option[Int] = None,
    primaryKey: Boolean = false,
    relationTable: Option[String] = None,
    relationColumn: Option[String] = None
  )

  case class Source(
      t: String,
      name: String,
      sortable: Boolean = true,
      sortedColumn: Option[String] = None,
      sortedAscending: Option[Boolean] = None,
      filters: Seq[QueryFilter] = Nil,
      dataOffset: Int = 0
  ) {
    def filterOpt = filters.headOption

    def asRowDataOptions(limit: Option[Int]) = RowDataOptions(
      orderByCol = sortedColumn,
      orderByAsc = sortedAscending,
      filters = filters,
      limit = limit,
      offset = Some(dataOffset)
    )
  }

  def error(queryId: UUID, sql: String, t: Throwable) = {
    val data = Seq(Seq(Some(t.getClass.getSimpleName)), Seq(Some(t.getMessage)))
    QueryResult(queryId, sql, columns = Seq(Col("error", StringType)), data = data, elapsedMs = 0)
  }
}

case class QueryResult(
  queryId: UUID,
  sql: String,
  isStatement: Boolean = false,
  columns: Seq[QueryResult.Col] = Nil,
  data: Seq[Seq[Option[String]]] = Nil,
  rowsAffected: Int = 0,
  moreRowsAvailable: Boolean = false,
  source: Option[QueryResult.Source] = None,
  elapsedMs: Int,
  occurred: Long = System.currentTimeMillis
)
