package util

import java.sql.{SQLSyntaxErrorException, Timestamp}
import java.util.UUID

import com.mysql.jdbc.exceptions.MySQLStatementCancelledException
import models.database.Row
import models.query.QueryError
import models.{QueryErrorResponse, ResponseMessage}
import org.h2.jdbc.JdbcClob
import org.joda.time.LocalDateTime
import org.postgresql.jdbc.PgArray
import org.postgresql.util.PSQLException

import scala.io.Source
import scala.util.control.NonFatal

object JdbcUtils extends Logging {
  def sqlCatch(queryId: UUID, sql: String, startMs: Long, resultId: UUID, index: Int)(f: () => ResponseMessage) = try {
    f()
  } catch {
    case NonFatal(t) =>
      val elapsedMs = (DateUtils.nowMillis - startMs).toInt
      log.warn(s"Encountered query error after [${elapsedMs}ms] running sql [$sql].", t)
      t match {
        case sqlEx: PSQLException =>
          val e = sqlEx.getServerErrorMessage
          val lineIndex = Source.fromString(sql).getLines().take(e.getLine - 1).map(_.length).sum
          val index = lineIndex + e.getPosition
          QueryErrorResponse(resultId, index, QueryError(queryId, sql, e.getSQLState, e.getMessage, Some(index), elapsedMs, startMs))
        case sqlEx: SQLSyntaxErrorException =>
          QueryErrorResponse(resultId, index, QueryError(queryId, sql, sqlEx.getSQLState, sqlEx.getMessage, elapsedMs = elapsedMs, occurred = startMs))
        case sqlEx: MySQLStatementCancelledException =>
          QueryErrorResponse(resultId, index, QueryError(queryId, sql, sqlEx.getSQLState, sqlEx.getMessage, elapsedMs = elapsedMs, occurred = startMs))
        case x =>
          QueryErrorResponse(resultId, index, QueryError(queryId, sql, x.getClass.getSimpleName, x.getMessage, elapsedMs = elapsedMs, occurred = startMs))
      }
  }

  def toLocalDateTime(row: Row, column: String) = {
    val ts = row.as[Timestamp](column)
    new LocalDateTime(ts.getTime)
  }

  @SuppressWarnings(Array("AsInstanceOf"))
  def toSeq[T](row: Row, column: String): Seq[Any] = {
    val a = row.as[PgArray](column)
    a.getArray.asInstanceOf[Array[T]].toSeq
  }

  def trim(s: String) = s.replaceAll("""[\s]+""", " ").trim

  def extractString(o: Any) = o match {
    case s: String => s
    case c: JdbcClob => c.getSubString(1, c.length().toInt)
    case x => throw new IllegalStateException(x.toString)
  }
}
