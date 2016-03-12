package controllers

import java.util.UUID

import akka.actor.ActorRef
import models.{ RequestMessage, ResponseMessage }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{ AnyContentAsEmpty, Request, WebSocket }
import services.connection.ConnectionService
import services.database.MasterDatabase
import services.schema.MetadataService
import utils.ApplicationContext
import utils.web.MessageFrameFormatter

import scala.concurrent.Future

@javax.inject.Singleton
class QueryController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def main(connectionId: UUID) = withSession(s"connection-$connectionId") { implicit request =>
    Future.successful(Ok(views.html.query.main(request.identity, ctx.config.debug)))
  }

  val mff = new MessageFrameFormatter(ctx.config.debug)
  import mff.{ requestFormatter, responseFormatter }
  import play.api.Play.current

  def connect(connectionId: UUID) = WebSocket.tryAcceptWithActor[RequestMessage, ResponseMessage] { request =>
    implicit val req = Request(request, AnyContentAsEmpty)
    SecuredRequestHandler { securedRequest =>
      Future.successful(HandlerResult(Ok, Some(securedRequest.identity)))
    }.map {
      case HandlerResult(r, Some(user)) => Right(ConnectionService.props(None, ctx.supervisor, user, _: ActorRef, request.remoteAddress))
      case HandlerResult(r, None) => Left(r)
    }
  }

  def view(connectionId: UUID) = withSession(s"connection-$connectionId") { implicit request =>
    val database = MasterDatabase.databaseFor(connectionId)
    val tables = MetadataService.getMetadata(database.source)
    Future.successful(Ok(views.html.query.view(request.identity, tables)))
  }
}
