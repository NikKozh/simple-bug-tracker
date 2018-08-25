package controllers

import models._
import java.nio.file.FileSystems

import org.apache.lucene.document._
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index._
import org.apache.lucene.search._
import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer
import javax.inject._
import org.apache.lucene.analysis.core.KeywordAnalyzer
import org.apache.lucene.index.IndexWriterConfig.OpenMode
import org.apache.lucene.queryparser.classic.{MultiFieldQueryParser, QueryParser}
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.{Document, Field}
import org.apache.lucene.index.{IndexWriter, Term}
import org.apache.lucene.store.RAMDirectory
import org.apache.lucene.analysis.ru._
import play.api.inject.ApplicationLifecycle

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

@Singleton
case class Search @Inject()(taskService: TaskService, lifecycle: ApplicationLifecycle)
                           (implicit ec: ExecutionContext) {
  private val directory = new RAMDirectory
  private val analyzer = new RussianAnalyzer()
  private val writer = new IndexWriter(directory, new IndexWriterConfig(analyzer))

  // Когда сервер завершит работу, writer закроется
  lifecycle.addStopHook(() => Future.successful(writer.close()))

  // Перехват с помощью JVM runtime нужен для случаев, когда сервер не завершается нормальным путём
  // (например, в случае ошибки, вылета и т.п.)
  // Если writer уже закрыт, ничего не произойдёт
  Runtime.getRuntime.addShutdownHook(new Thread() {
    override def run(): Unit = writer.close()
  })

  setFutureIndex()

  def search(keyword: String): Array[Document] = {
    val searcher = new IndexSearcher(DirectoryReader.open(directory))
    val queryParser = new QueryParser("description", analyzer)
    val query = queryParser.parse(keyword)

    val hits = searcher.search(query, Int.MaxValue)
    val scoreDoc = hits.scoreDocs
    println("RESULTS FOUND: " + hits.totalHits)
    println("SEARCH RESULTS")

    val searchResults = scoreDoc.map( docs => {
      val doc = searcher.doc(docs.doc)
      println("Task found:")
      println("Id: " + doc.get("id"))
      println("Title: " + doc.get("title"))
      println("Description: " + doc.get("description"))
      println("State: " + doc.get("state"))
      doc
    })
    searchResults
  }

  def setFutureIndex(): Unit = {
    println("*************START INDEXING")
    writer.deleteAll()
    val tasks: Future[Seq[Task]] = taskService.getTasks

    def indexingFuture = {
      val list = Seq {
        tasks.map(_.foreach( task =>
          writeToDoc(task)
        ))
      }
      Future.sequence(list)
    }

    Await.result(indexingFuture, 10 seconds)
    println("*************END INDEXING")
  }

  private def writeToDoc(task: Task) = Future {
    println("INDEXING: " + task.id)
    // TODO: если всё заработает, сделать doc полем класса и просто перезаписывать значение,
    // чтобы не открывать-закрывать при каждом изменении БД
    val doc = new Document()

    val fields: Array[Field] = Array(
      new StoredField("id", task.id),
      new StoredField("title", task.title),
      new TextField("description", task.description, Field.Store.YES),
      new StoredField("state", task.state.toString)
    )

    fields.foreach(field => doc.add(field))

    writer.addDocument(doc)

    writer.commit()
    // writer.close()
    println("COMPLETED INDEXING: " + task.id)
  }
}
