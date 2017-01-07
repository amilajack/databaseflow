package models.connection

import models.engine.DatabaseEngine
import models.graphql.{CommonSchema, GraphQLContext}
import models.graphql.CommonSchema._
import models.user.Permission
import sangria.macros.derive._
import sangria.schema._
import services.connection.ConnectionSettingsService

import scala.concurrent.Future

object ConnectionGraphQL {
  implicit val permissionEnum = CommonSchema.deriveEnumeratumType(
    name = "Permission",
    description = "The role of the system user.",
    values = Permission.values.map(t => t -> t.entryName).toList
  )

  implicit val databaseEngineEnum = CommonSchema.deriveEnumeratumType(
    name = "DatabaseEngine",
    description = "The database engine used by this connection.",
    values = DatabaseEngine.values.map(t => t -> t.entryName).toList
  )

  implicit val connectionSettingsType = deriveObjectType[GraphQLContext, ConnectionSettings](
    ObjectTypeDescription("Information about the current session."),
    ExcludeFields("password")
  )

  val queryFields = fields[GraphQLContext, Unit](
    Field(
      name = "connection",
      description = Some("Returns information about the available database connections."),
      fieldType = ListType(connectionSettingsType),
      resolve = c => Future.successful(ConnectionSettingsService.getVisible(c.ctx.user))
    )
  )
}
