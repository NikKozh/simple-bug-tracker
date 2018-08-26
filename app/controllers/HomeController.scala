package controllers

import javax.inject._
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

import play.api.mvc._
import play.api.data.Form

import models._
import TaskForm._

@Singleton
class HomeController @Inject()(taskService: TaskService, cc: MessagesControllerComponents, searcher: Search)
                              (implicit ec: ExecutionContext, assetsFinder: AssetsFinder)
                              extends MessagesAbstractController(cc) {

  def index(id: Option[Int]): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    taskService.getTaskMatrixForTemplate.map { matrix =>
      Ok(views.html.index(matrix, taskForm,
        id match {
          case Some(wantedId) => Await.result(taskService.getTask(wantedId), 10 seconds)
          case None => None
        }
      ))
    }
  }

  def createTask(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    // handleSuccessForm не вынесен в отдельный метод, т.к. внутри него для создания и обновления задачи
    // требуются два разных метода с разными возвращаемыми типами, и такой рефакторинг неоправданно усложнил бы код
    val handleSuccessForm = { data: TaskData =>
      taskService.createTask(data).map { _ =>
        searcher.setIndexes() // индексируем все задачи заново для полнотекстового поиска
        Redirect(routes.HomeController.index())
      }
    }

    taskForm.bindFromRequest.fold(handleErrorForm, handleSuccessForm)
  }

  def updateTask(id: Int): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val handleSuccessForm = { data: TaskData =>
      taskService.updateTask(id, data).map { _ =>
        searcher.setIndexes()
        Redirect(routes.HomeController.index())
      }
    }

    taskForm.bindFromRequest.fold(handleErrorForm, handleSuccessForm)
  }

  private def handleErrorForm(implicit request: MessagesRequest[AnyContent]) = { formWithErrors: Form[TaskData] =>
    taskService.getTaskMatrixForTemplate.map { matrix =>
      BadRequest(views.html.index(matrix, formWithErrors))
    }
  }

  def deleteTask(id: Int): Action[AnyContent] = Action.async {
    taskService.deleteTask(id).map { _ =>
      searcher.setIndexes()
      Redirect(routes.HomeController.index())
    }
  }
}