package controllers

// TODO: разобраться с понятием side-effect и добавить пустые круглые скобки там, где они необоснованно убраны, по соглашению

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

  // TODO: всё-таки разобраться с тем, нужно ли и id делать по умолчанию None, и если да, то в каком месте (здесь, routes, etc...)
  // TODO: детально разобрать, что здесь происходит
  def index(id: Option[Int]) = Action.async { implicit request: MessagesRequest[AnyContent] =>
    taskService.getTaskMatrixForTemplate.map { matrix =>
      Ok(views.html.index(matrix, taskForm,
        id match {
          // TODO: найти способ получать Option[Task] из Future[Option[Task]] получше, чем Await.result
          // (может, передавать футур в шаблон и раскрывать его там?)
          case Some(wantedId) => Await.result(taskService.getTask(wantedId), 10 second)
          case None => None
        }
      ))
    }
  }

  // TODO: добавить уведомление о том, что задача была создана (или изменена)
  def createTask = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val errorFunction = { formWithErrors: Form[TaskData] =>
      taskService.getTaskMatrixForTemplate.map { matrix =>
        BadRequest(views.html.index(matrix, formWithErrors))
      }
    }

    val successFunction = { data: TaskData =>
      taskService.createTask(data).map { _ =>
        searcher.setFutureIndex()
        Redirect(routes.Application.index(None))/*.flashing("info" -> "Task added!")*/
      }
    }

    val formValidationResult = taskForm.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }

  // TODO: проверить на дублирующийся код
  def updateTask(id: Int) = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val errorFunction = { formWithErrors: Form[TaskData] =>
      taskService.getTaskMatrixForTemplate.map { matrix =>
        BadRequest(views.html.index(matrix, formWithErrors))
      }
    }

    val successFunction = { data: TaskData =>
      taskService.updateTask(id, data).map { _ =>
        searcher.setFutureIndex()
        Redirect(routes.Application.index(None))/*.flashing("info" -> "Task added!")*/
      }
    }

    val formValidationResult = taskForm.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }

  def deleteTask(id: Int) = Action.async { implicit request: MessagesRequest[AnyContent] =>
    taskService.deleteTask(id).map { _ =>
      searcher.setFutureIndex()
      Redirect(routes.Application.index(None))
    }
  }
}