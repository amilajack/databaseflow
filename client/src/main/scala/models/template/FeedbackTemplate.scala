package models.template

import ui.UserManager

import scalatags.Text.all._

object FeedbackTemplate {
  def content() = {
    val content = div(id := "feedback-panel")(
      div(cls := "row")(
        form(cls := "col s12")(
          div(cls := "row")(
            div(cls := "col s12")(
              p("Thanks for your feedback. If you'd like to be notified when we reply, please include an email address.")
            ),
            div(cls := "input-field col s12")(
              input(id := "feedback-email-input", `type` := "email", cls := "feedback-email")(),
              label("for".attr := "feedback-email-input", value := UserManager.email.getOrElse(""))("Email Address")
            ),
            div(cls := "input-field col s12")(
              textarea(id := "feedback-content-input", cls := "feedback-content materialize-textarea")(),
              label("for".attr := "feedback-content-input")("Enter your feedback")
            )
          )
        )
      )
    )

    StaticPanelTemplate.cardRow(
      content = content,
      iconAndTitle = Some(Icons.feedback -> "Feedback"),
      actions = Seq(
        a(href := "", cls := "theme-text right submit-feedback")("Submit Feedback"),
        div(style := "clear: both;")
      )
    )
  }
}
