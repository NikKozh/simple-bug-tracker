package controllers

import javax.inject._
import play.api.mvc._
import models.Task
import play.api.data.Form

import scala.concurrent.{ExecutionContext, Future}
import models._

@Singleton
class Application @Inject()(repo: TaskRepository, cc: MessagesControllerComponents)
                           (implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {
  import TaskForm._

  private def getTasks = {
    Task.getTasksMatrixForTemplate(repo.tasksList)
  }

  // TODO: всё-таки разобраться с тем, нужно ли и id делать по умолчанию None, и если да, то в каком месте (здесь, routes, etc...)
  def index(id: Option[Int], futureTasksMatrix: Future[List[List[Task]]] = getTasks) = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.index(futureTasksMatrix, taskForm, id))
  }

  def createTask = Action { implicit request: MessagesRequest[AnyContent] =>
    val errorFunction = { formWithErrors: Form[Data] =>
      BadRequest(views.html.index(Task.getTasksMatrixForTemplate(repo.tasksList), formWithErrors))
    }

    val successFunction = { data: Data =>
      repo.create(data.title, data.description, data.state).map { _ =>
        Task.create(data)
      }
      Redirect(routes.Application.index(None)/*.flashing("info" -> "Task added!")*/
    }

    val formValidationResult = taskForm.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }

  // TODO: проверить на дублирующийся код
  def updateTask(id: Int) = Action { implicit request: MessagesRequest[AnyContent] =>
    /*Task.update(id)
    Ok(views.html.index(Task.getTasksMatrixForTemplate, taskForm))*/

    val errorFunction = { formWithErrors: Form[Data] =>
      BadRequest(views.html.index(Task.getTasksMatrixForTemplate(repo.tasksList), formWithErrors))
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