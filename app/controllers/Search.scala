package controllers

import javax.inject._
import play.api.inject.ApplicationLifecycle

import scala.concurrent.{ExecutionContext, Future}
import org.apache.lucene.document._
import org.apache.lucene.index._
import org.apache.lucene.search._
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.document.{Document, Field}
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.store.RAMDirectory
import org.apache.lucene.analysis.ru._
import models._

@Singleton
case class Search @Inject()(taskService: TaskService, lifecycle: ApplicationLifecycle)
                           (implicit ec: ExecutionContext) {
  private val directory = new RAMDirectory()
  private val analyzer  = new RussianAnalyzer()
  private val writer    = new IndexWriter(directory, new IndexWriterConfig(analyzer))

  // Когда сервер завершит работу, writer закроется:
  lifecycle.addStopHook(() => Future.successful(writer.close()))

  // Перехват с помощью JVM runtime нужен для случаев, когда сервер не завершается нормальным путём
  // (например, в случае ошибки, вылета и т.п.). Если writer уже закрыт, ничего не произойдёт:
  Runtime.getRuntime.addShutdownHook(new Thread() {
    override def run(): Unit = writer.close()
  })

  setIndexes()

  def search(keyword: String): Array[Document] = {
    val searcher    = new IndexSearcher(DirectoryReader.open(directory))
    val queryParser = new QueryParser("description", analyzer)

    val query    = queryParser.parse(keyword)
    val hits     = searcher.search(query, Int.MaxValue)
    val scoreDoc = hits.scoreDocs

    val searchResults = scoreDoc.map(docs => searcher.doc(docs.doc))
    searchResults
  }

  def setIndexes(): Unit = {
    writer.deleteAll()

    for {
      taskSeq <- taskService.getTasks
      task  <- taskSeq
    } writeToDoc(task)
  }

   def setIndex(taskId: Int): Unit = {
     writer.deleteDocuments(new Term("id", taskId.toString))

     for {
       maybeTask <- taskService.getTask(taskId)
       task      <- maybeTask
     } writeToDoc(task)
   }

  private def writeToDoc(task: Task) = Future {
    val doc = new Document()

    val fields: Array[Field] = Array(
      new StoredField("id", task.id),
      new StoredField("title", task.title),
      new TextField  ("description", task.description, Field.Store.YES),
      new StoredField("state", task.state.toString)
    )

    fields.foreach(field => doc.add(field))
    writer.addDocument(doc)
    writer.commit()
  }
}
