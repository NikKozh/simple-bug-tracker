package models

import javax.inject.{Inject, Singleton}
import models.TaskState.TaskState
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaskRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import profile.api._

  private implicit val stateMapper = MappedColumnType.base[TaskState, String](
    e => e.toString,
    s => TaskState.withName(s)
  )

  private class TaskTable(tag: Tag) extends Table[Task](tag, "tasks") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")
    def description = column[String]("description")
    def state = column[TaskState]("state")
    def * = (id, title, description, state) <> ((Task.apply _).tupled, Task.unapply)
  }
  private val tasks = TableQuery[TaskTable]

  def create(title: String, description: String, state: TaskState): Future[String] = db.run {
    // We create a projection of just the name and age columns, since we're not inserting a value for the id column
    ((tasks.map(t => (t.title, t.description, t.state))
      // Now define it to return the id, because we want to know what id was generated for the person
      returning tasks.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the
      // returned id
      into ((restData, id) => Task(id, restData._1, restData._2, restData._3))
      // And finally, insert the person into the database
      ) += (title, description, state)).map(_ => "Task successfully added")
    /*(tasks.map(_ => {})) += Task(1, title, description, state)*/
  }

  def getTaskList: Future[Seq[Task]] = db.run {
    tasks.result
  }

  def getTask(id: Int): Future[Seq[Task]] = db.run {
    tasks.filter(_.id === id).result
  }

  def deleteTask(id: Int): Future[Int] = db.run {
    tasks.filter(_.id === id).delete
  }

  def updateTask(id: Int, newTitle: String, newDescription: String, newState: TaskState): Future[String] = {
    Future("") // TODO: доделать
  }
}