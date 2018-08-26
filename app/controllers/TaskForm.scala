package controllers

import models.TaskState
import models.TaskState.TaskState
import play.api.data.{FormError, Forms}
import play.api.data.format.Formatter

object TaskForm {
  import play.api.data.Forms._
  import play.api.data.Form

  case class TaskData(title: String, description: String, state: TaskState)

  implicit def taskStateFormat: Formatter[TaskState] = new Formatter[TaskState] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], models.TaskState.Value] = {
      data.get(key)
        .map(TaskState.withName(_))
        .toRight(Seq(FormError(key, "error.required", Nil)))
    }

    override def unbind(key: String, value: TaskState): Map[String, String] = {
      Map(key -> value.toString)
    }
  }

  val taskForm: Form[TaskData] = Form {
    mapping(
      "title" -> nonEmptyText,
      "description" -> text,
      "state" -> Forms.of[TaskState]
    )(TaskData.apply)(TaskData.unapply)
  }
}