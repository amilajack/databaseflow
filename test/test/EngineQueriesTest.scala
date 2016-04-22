package test

import models.engine.EngineQueries
import models.engine.rdbms.{ MySQL, PostgreSQL, SqlServer }
import models.query.RowDataOptions
import org.scalatest.{ FlatSpec, Matchers }

class EngineQueriesTest extends FlatSpec with Matchers {
  val rdoLimit = RowDataOptions(
    limit = Some(1000)
  )

  val rdoCol = RowDataOptions(
    filterCol = Some("y"),
    filterOp = Some("="),
    filterVal = Some("z")
  )

  val rdoOrder = RowDataOptions(
    orderBy = Some("y")
  )

  "Engine Queries" should "output SQL Server limit syntax" in {
    val sql = EngineQueries.selectFrom("X", rdoLimit)(SqlServer)
    sql should be("select * from [X] fetch next 1000 rows only")
  }

  it should "output PostgreSQL limit syntax" in {
    val sql = EngineQueries.selectFrom("X", rdoLimit)(PostgreSQL)
    sql should be("select * from \"X\" limit 1000")
  }

  it should "output MySQL limit syntax" in {
    val sql = EngineQueries.selectFrom("X", rdoLimit)(MySQL)
    sql should be("select * from `X` limit 1000")
  }

  it should "output SQL Server where clause syntax" in {
    val sql = EngineQueries.selectFrom("X", rdoCol)(SqlServer)
    sql should be("select * from [X] where y = 'z'")
  }

  it should "output PostgreSQL where clause syntax" in {
    val sql = EngineQueries.selectFrom("X", rdoCol)(PostgreSQL)
    sql should be("select * from \"X\" where y = 'z'")
  }

  it should "output a proper order by clause" in {
    val sql = EngineQueries.selectFrom("X", rdoOrder)(PostgreSQL)
    sql should be("select * from \"X\" order by \"y\"")
  }
}