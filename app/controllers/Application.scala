package controllers

import javax.inject._
import play.api.mvc._
import models.Task
import play.api.data.Form

import scala.concurrent.ExecutionContext
import models._

@Singleton
class Application @Inject()(taskRepository: TaskRepository, cc: MessagesControllerComponents)
                           (implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {
  import TaskForm._

  // TODO: всё-таки разобраться с тем, нужно ли и id делать по умолчанию None, и если да, то в каком месте (здесь, routes, etc...)
  // TODO: детально разобрать, что здесь происходит
  def index(id: Option[Int]) = Action.async { implicit request: MessagesRequest[AnyContent] =>
    taskRepository.getTasksList.map { tasks =>
      val matrixForTemplate = Task.getTasksMatrixForTemplate(tasks)
      val editableTask = id match {
        case Some(wantedId) => tasks.find(task => task.id == wantedId)
        case None => None
      }
      Ok(views.html.index(matrixForTemplate, taskForm, editableTask))
    }

    /* То, что не сработало:
    Task.getTasksMatrixForTemplate(taskRepository.getTasksList).map { tasks =>
      // val futureTasksMatrix = Task.getTasksMatrixForTemplate(taskRepository.getTasksList)
      Ok(views.html.index(tasks, taskForm, id))
    }*/
  }

  def createTask = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val errorFunction = { formWithErrors: Form[TaskData] =>
      taskRepository.getTasksList.map { tasks =>
        BadRequest(views.html.index(Task.getTasksMatrixForTemplate(tasks), formWithErrors))
      }
      /*Future.successful{
          BadRequest(views.html.index(Task.getTasksMatrixForTemplate(Await.result(taskRepository.getTasksList, Duration.Inf)), formWithErrors))
        //}
      }*/
    }

    val successFunction = { data: TaskData =>
      taskRepository.create(data.title, data.description, data.state).map { _ =>
        Task.create(data)
        Redirect(routes.Application.index(None)) /*.flashing("info" -> "Task added!")*/
      }
    }

    val formValidationResult = taskForm.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }

  // TODO: проверить на дублирующийся код
  def updateTask(id: Int) = Action.async { implicit request: MessagesRequest[AnyContent] =>
    //Task.update(id)
    //Ok(views.html.index(Task.getTasksMatrixForTemplate, taskForm))

    val errorFunction = { formWithErrors: Form[TaskData] =>
      taskRepository.getTasksList.map { tasks =>
        BadRequest(views.html.index(Task.getTasksMatrixForTemplate(tasks), formWithErrors))
      }
    }

    val successFunction = { data: TaskData =>
      taskRepository.create(data.title, data.description, data.state).map { _ =>
        Task.update(id, data)
        Redirect(routes.Application.index(None)).flashing("info" -> "Task added!")
      }
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