package services.notification

import util.FutureUtils.defaultContext
import play.api.libs.mailer.{Email, MailerClient}
import util.Logging

import scala.concurrent.Future

@javax.inject.Singleton
class EmailService @javax.inject.Inject() (mailerClient: MailerClient) extends Logging {
  def sendAdminMessage(subject: String, htmlBody: String) = {
    sendMessage(util.Config.projectName + " Admin", "kyle@databaseflow.com", subject, htmlBody)
  }

  def sendMessage(name: String, address: String, subject: String, htmlBody: String) = {
    val f = Future {
      val email = Email(
        subject,
        "The Database Flow Team <databaseflow@databaseflow.com>",
        Seq(s"$name <$address>"),
        bodyHtml = Some(htmlBody)
      )
      mailerClient.send(email)
    }
    f.foreach(_ => log.info(s"Successfully sent email to [$address]."))
    f.failed.foreach(x => log.warn(s"Unable to send email to [$address].", x))
  }
}
