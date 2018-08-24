package controllers

import javax.inject._
import play.api.mvc._

@Singleton
class SearchController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  def search = Action {
    Ok(views.html.search())
  }
}
