package controllers

import models.TaskState
import models.TaskState.TaskState
import play.api.data.{FormError, Forms}
import play.api.data.format.Formatter

object TaskForm {
  import play.api.data.Forms._
  import play.api.data.Form

  // TODO: не забыть выяснить, как обрабатывать потом state при загрузке из БД
  case class Data(title: String, description: String, state: TaskState)

  // TODO: если останется время - досконально разобрать
  // Адаптированный метод со StackOverflow:
  implicit def taskStateFormat: Formatter[TaskState] = new Formatter[TaskState] {
    override def bind(key: String, data: Map[String, String]) =
      data.get(key)
        .map(TaskState.withName(_))
        .toRight(Seq(FormError(key, "error.required", Nil)))

    override def unbind(key: String, value: TaskState) =
      Map(key -> value.toString)
  }

  val taskForm: Form[Data] = Form {
    mapping(
      "title" -> nonEmptyText,
      "description" -> text,
      "state" -> Forms.of[TaskState]
    )(Data.apply)(Data.unapply)
  }
}