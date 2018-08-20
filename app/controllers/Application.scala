package controllers

import javax.inject._
import play.api._
import play.api.mvc._

@Singleton
class Application @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def newTask = TODO

  def updateTask(id: Long) = TODO

  def deleteTask(id: Long) = TODO
}