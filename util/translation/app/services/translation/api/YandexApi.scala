package services.translation.api

import util.FutureUtils.defaultContext
import play.api.libs.ws.WSClient

import io.circe._
import io.circe.parser._

@javax.inject.Singleton
class YandexApi @javax.inject.Inject() (ws: WSClient) extends ApiProvider("Yandex") {
  val apiKey = "trnsl.1.1.20160807T203728Z.3db5b35613fb236a.f1895c3d286efe675f44143d84dfdcf1ace08eb6"
  def url(lang: String) = {
    val newLang = lang match {
      case "iw" => "he"
      case x => x
    }
    s"https://translate.yandex.net/api/v1.5/tr.json/translate?key=$apiKey&lang=en-$newLang"
  }

  private[this] def body(text: String) = {
    Map("text" -> Seq(text))
  }

  override def translate(lang: String, key: String, text: String) = {
    ws.url(url(lang)).withHttpHeaders("Accept" -> "application/json").post(body(text)).map { response =>
      val json = decode[JsonObject](response.body).right.get.toMap
      val t = json("code").as[Int].right.get match {
        case 200 => json("text").as[Seq[String]].right.get.head
        case _ => json("message").as[String].right.get
      }
      Some(key -> t)
    }
  }
}
