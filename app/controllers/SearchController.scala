package controllers

import javax.inject._
import org.apache.lucene.document.Document
import play.api.mvc._

@Singleton
class SearchController @Inject()(searcher: Search, cc: ControllerComponents)
                                (implicit assetsFinder: AssetsFinder) extends AbstractController(cc) {
  def search(query: String) = Action {
    if (query == "") {
      Ok(views.html.search())
    } else {
      val searchResult: Array[Document] = searcher.search(query)
      Ok(views.html.search(Some(searchResult), query))
    }
  }
}