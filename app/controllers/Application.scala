package controllers

import javax.inject._
import models.Task
import play.api._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._

import models.TaskState.TaskState

@Singleton
class Application @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {

  val taskForm: Form[TaskForm] = Form {
    mapping(
      "title" -> nonEmptyText,
      "description" -> text
    )(TaskForm.apply)(TaskForm.unapply)
  }

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index(Task.getTasksMatrixForTemplate, taskForm))
  }

  def newTask = TODO

  def updateTask(id: Int) = TODO

  def deleteTask(id: Int) = TODO
}

// TODO: добавить в атрибуты состояние и разобраться, как его обрабатывать
case class TaskForm(title: String, description: String/*, state: TaskState*/)