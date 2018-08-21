package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import models.Task

@Singleton
class Application @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {
  import TaskForm._

  def index(implicit id: Option[Int]) = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.index(Task.getTasksMatrixForTemplate, taskForm))
  }

  def createTask = TODO

  def updateTask(id: Int) = TODO

  def deleteTask(id: Int) = TODO
}