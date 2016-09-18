package controllers

import java.util.UUID

import models.audit.AuditType
import models.connection.ConnectionSettings
import models.engine.DatabaseEngine
import models.forms.ConnectionForm
import models.user.Role
import services.audit.AuditRecordService
import services.config.ConfigFileService
import services.connection.ConnectionSettingsService
import services.database.DatabaseRegistry
import services.schema.SchemaService
import services.settings.SettingsService
import utils.{ApplicationContext, PasswordEncryptUtils}

import scala.concurrent.Future

@javax.inject.Singleton
class ConnectionSettingsController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def addNew() = withSession("add-new") { implicit request =>
    val conn = ConnectionSettings(UUID.randomUUID, "", request.identity.id)
    Future.successful(Ok(views.html.connection.form(request.identity, conn, messagesApi("connection.new.title"), isNew = true)))
  }

  def editForm(id: UUID) = withSession("edit-form-" + id) { implicit request =>
    val addConnectionRole = Role.withName(SettingsService(models.settings.SettingKey.AddConnectionRole))
    if (request.identity.role.qualifies(addConnectionRole)) {
      val conn = ConnectionSettingsService.getById(id).getOrElse {
        throw new IllegalArgumentException(s"Invalid connection [$id].")
      }
      Future.successful(Ok(views.html.connection.form(request.identity, conn, conn.name, isNew = false)))
    } else {
      Future.successful(Redirect(routes.HomeController.home()).flashing("error" -> messagesApi("connection.permission.denied")))
    }
  }

  def save(connectionId: UUID) = withSession("save") { implicit request =>
    val addConnectionRole = Role.withName(SettingsService(models.settings.SettingKey.AddConnectionRole))
    if (request.identity.role.qualifies(addConnectionRole)) {
      val connOpt = ConnectionSettingsService.getById(connectionId)
      val conn = connOpt.getOrElse(ConnectionSettings(id = connectionId, name = "", owner = request.identity.id))
      val result = ConnectionForm.form.bindFromRequest.fold(
        formWithErrors => {
          val title = formWithErrors.value.map(_.name).getOrElse(messagesApi("connection.new.title"))
          BadRequest(views.html.connection.form(request.identity, conn, title, isNew = connOpt.isEmpty, formWithErrors.errors))
        },
        cf => {
          val almostUpdated = cf.copyWith(conn)
          val updated = if (cf.password.trim.isEmpty) {
            almostUpdated.copy(password = conn.password)
          } else {
            almostUpdated.copy(password = PasswordEncryptUtils.encrypt(cf.password))
          }
          connOpt match {
            case Some(existing) =>
              ConnectionSettingsService.update(updated)
              DatabaseRegistry.reset(updated) match {
                case Right(c) =>
                  SchemaService.getSchema(c, forceRefresh = true)
                  AuditRecordService.create(AuditType.EditConnection, request.identity.id, None, Some(updated.id.toString))
                  Redirect(routes.QueryController.main(connectionId))
                case Left(x) =>
                  log.warn("Unable to connect to newly saved connection.", x)
                  Redirect(routes.QueryController.main(connectionId))
              }
            case None =>
              ConnectionSettingsService.insert(updated)
              AuditRecordService.create(AuditType.CreateConnection, request.identity.id, None, Some(updated.id.toString))
              Redirect(routes.QueryController.main(connectionId))
          }
        }
      )
      Future.successful(result)
    } else {
      Future.successful(Redirect(routes.HomeController.home()).flashing("error" -> messagesApi("connection.permission.denied")))
    }
  }

  def copyConnection(connectionId: UUID) = withSession("copy") { implicit request =>
    val conn = ConnectionSettingsService.getById(connectionId).getOrElse(throw new IllegalStateException(s"Invalid connection [$connectionId]."))
    val updated = conn.copy(
      id = UUID.randomUUID,
      name = "Copy of " + conn.name,
      password = ""
    )
    Future.successful(Ok(views.html.connection.form(request.identity, updated, updated.name, isNew = true)))
  }

  def delete(connectionId: UUID) = withSession("delete") { implicit request =>
    ConnectionSettingsService.delete(connectionId, request.identity.id)
    Future.successful(Redirect(routes.HomeController.home()))
  }

  def createSample() = withSession("create.sample") { implicit request =>
    Future.successful(Redirect(routes.QueryController.main(ConnectionSettingsService.createSample(request.identity.id))))
  }
}
