package controllers

import javax.inject._
import org.apache.lucene.document.Document
import org.apache.lucene.search.ScoreDoc
import play.api.mvc._
import views.html.helper.form

@Singleton
class SearchController @Inject()(searcher: Search, cc: ControllerComponents) extends AbstractController(cc) {
  // TODO: возможно, заменить на Option?
  def search(query: String) = Action {
    if (query == "") {
      Ok(views.html.search())
    } else {
      val searchResult: Array[Document] = searcher.search(query)
      Ok(views.html.search(Some(searchResult), query))
    }
  }
}