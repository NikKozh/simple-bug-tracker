package controllers

// TODO: вычистить до конца код: убрать комментарии, посмотреть импорты и т.д.

import javax.inject._
import play.api.mvc._
import play.api.data.Form

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import models._

@Singleton
class Application @Inject()(taskService: TaskService, cc: MessagesControllerComponents, searcher: Search)
                           (implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {
  import TaskForm._

  // TODO: детально разобрать, что здесь происходит
  def index(id: Option[Int]): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    taskService.getTaskMatrixForTemplate.map { matrix =>
      Ok(views.html.index(matrix, taskForm,
        id match {
          case Some(wantedId) => Await.result(taskService.getTask(wantedId), 10 second)
          case None => None
        }
      ))
    }
  }

  def errorFormFunction(implicit request: MessagesRequest[AnyContent]) = { formWithErrors: Form[TaskData] =>
    taskService.getTaskMatrixForTemplate.map { matrix =>
      BadRequest(views.html.index(matrix, formWithErrors))
    }
  }

  def successFormFunction()

  def createTask(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    /*val errorFunction = { formWithErrors: Form[TaskData] =>
      taskService.getTaskMatrixForTemplate.map { matrix =>
        BadRequest(views.html.index(matrix, formWithErrors))
      }
    }*/

    val successFunction = { data: TaskData =>
      taskService.createTask(data).map { _ =>
        searcher.setFutureIndex()
        Redirect(routes.Application.index())
      }
    }

    val formValidationResult = taskForm.bindFromRequest
    formValidationResult.fold(errorFormFunction, successFunction)
  }

  // TODO: проверить на дублирующийся код
  def updateTask(id: Int): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    /*val errorFunction = { formWithErrors: Form[TaskData] =>
      taskService.getTaskMatrixForTemplate.map { matrix =>
        BadRequest(views.html.index(matrix, formWithErrors))
      }
    }*/

    val successFunction = { data: TaskData =>
      taskService.updateTask(id, data).map { _ =>
        searcher.setFutureIndex()
        Redirect(routes.Application.index())
      }
    }

    val formValidationResult = taskForm.bindFromRequest
    formValidationResult.fold(errorFormFunction, successFunction)
  }

  def deleteTask(id: Int): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    taskService.deleteTask(id).map { _ =>
      searcher.setFutureIndex()
      Redirect(routes.Application.index())
    }
  }
}