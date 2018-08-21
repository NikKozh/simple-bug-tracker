package controllers

import javax.inject._
import play.api.mvc._
import models.Task
import play.api.data.Form

@Singleton
class Application @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {
  import TaskForm._

  def index(implicit id: Option[Int]) = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.index(Task.getTasksMatrixForTemplate, taskForm, id))
  }

  def createTask = Action { implicit request: MessagesRequest[AnyContent] =>
    val errorFunction = { formWithErrors: Form[Data] =>
      BadRequest(views.html.index(Task.getTasksMatrixForTemplate, formWithErrors))
    }

    val successFunction = { data: Data =>
      Task.create(data)
      Redirect(routes.Application.index(None))/*.flashing("info" -> "Task added!")*/
    }

    val formValidationResult = taskForm.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }

  // TODO: проверить на дублирующийся код
  def updateTask(id: Int) = Action { implicit request: MessagesRequest[AnyContent] =>
    /*Task.update(id)
    Ok(views.html.index(Task.getTasksMatrixForTemplate, taskForm))*/

    val errorFunction = { formWithErrors: Form[Data] =>
      BadRequest(views.html.index(Task.getTasksMatrixForTemplate, formWithErrors))
    }

    val successFunction = { data: Data =>
      Task.update(id, data)
      Redirect(routes.Application.index(None))/*.flashing("info" -> "Task added!")*/
    }

    val formValidationResult = taskForm.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }

  def deleteTask(id: Int) = Action { implicit request: MessagesRequest[AnyContent] =>
    Task.delete(id)
    // Ok(views.html.index(Task.getTasksMatrixForTemplate, taskForm))
    Redirect(routes.Application.index(None))
  }
}