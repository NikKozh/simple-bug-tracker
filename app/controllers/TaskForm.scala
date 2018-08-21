package controllers

object TaskForm {
  import play.api.data.Forms._
  import play.api.data.Form

  // TODO: добавить в атрибуты состояние и разобраться, как его обрабатывать
  case class Data(title: String, description: String/*, state: TaskState*/)

  val taskForm: Form[Data] = Form {
    mapping(
      "title" -> nonEmptyText,
      "description" -> text
    )(Data.apply)(Data.unapply)
  }
}
