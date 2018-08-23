package controllers

// TODO: разобраться с импортами, свести всё воедино

import models.TaskState
import models.TaskState.TaskState
import play.api.data.{FormError, Forms}
import play.api.data.format.Formatter

object TaskForm {
  import play.api.data.Forms._
  import play.api.data.Form

  case class TaskData(title: String, description: String, state: TaskState)

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

  val taskForm: Form[TaskData] = Form {
    mapping(
      "title" -> nonEmptyText,
      "description" -> text,
      "state" -> Forms.of[TaskState]
    )(TaskData.apply)(TaskData.unapply)
  }
}