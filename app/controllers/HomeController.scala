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
  private def indexAndRedirect(taskId: Int): Result = {
    searcher.setIndex(taskId) // переиндексируем только ту задачу, которая была изменена
    Redirect(routes.HomeController.index())
  }

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
    // handleSuccessForm вынесен в отдельный метод только частично, т.к. внутри него для создания и обновления задачи
    // требуются два разных метода с разными возвращаемыми типами, и такой рефакторинг неоправданно усложнил бы код
    val handleSuccessForm = { data: TaskData =>
      taskService.createTask(data).map(indexAndRedirect)
    }
    taskForm.bindFromRequest.fold(handleErrorForm, handleSuccessForm)
  }

  def updateTask(id: Int): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val handleSuccessForm = { data: TaskData =>
      taskService.updateTask(id, data).map(indexAndRedirect)
    }
    taskForm.bindFromRequest.fold(handleErrorForm, handleSuccessForm)
  }

  def deleteTask(id: Int): Action[AnyContent] = Action.async {
    taskService.deleteTask(id).map(indexAndRedirect)
  }
}