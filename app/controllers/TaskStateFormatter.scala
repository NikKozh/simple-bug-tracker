package controllers

import models.TaskState
import models.TaskState._
import play.api.data.FormError
import play.api.data.format.Formatter

// TODO: дубль метода из TaskForm, разобраться, надо ли выносить его в отдельный файл и в любом случае удалить дубликат
class TaskStateFormatter {
  implicit def taskStateFormat: Formatter[TaskState] = new Formatter[TaskState] {
    override def bind(key: String, data: Map[String, String]) =
      data.get(key)
        .map(TaskState.withName(_))
        .toRight(Seq(FormError(key, "error.required", Nil)))

    override def unbind(key: String, value: TaskState) =
      Map(key -> value.toString)
  }
}