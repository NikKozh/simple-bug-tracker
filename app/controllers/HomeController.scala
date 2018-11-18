package controllers

import javax.inject._

import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc._
import play.api.data.Form
import models._
import TaskForm._

@Singleton
class HomeController @Inject()(taskService: TaskService, cc: MessagesControllerComponents, searcher: Search)
                              (implicit ec: ExecutionContext, assetsFinder: AssetsFinder)
                              extends MessagesAbstractController(cc) {

  def index(id: Option[Int]): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    for {
      matrix    <- taskService.getTaskMatrixForTemplate
      maybeTask <- id match {
        case Some(wantedId) => taskService.getTask(wantedId)
        case None           => Future(None)
      }
    } yield Ok(views.html.index(matrix, maybeTask))
  }

  private def indexAndRedirect(taskId: Int): Result = {
    searcher.setIndex(taskId) // переиндексируем только ту задачу, которая была изменена
    Redirect(routes.HomeController.index())
  }

  private def handleErrorForm(implicit request: MessagesRequest[AnyContent]) = { _: Form[TaskData] =>
    taskService.getTaskMatrixForTemplate.map { matrix =>
      BadRequest(views.html.index(matrix))
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