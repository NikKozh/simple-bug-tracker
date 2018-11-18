package models

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import models.TaskState.TaskState

@Singleton
case class TaskRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  // Для преобразования типа TaskState в обычный String для БД и обратно:
  private implicit val taskStateMapper = MappedColumnType.base[TaskState, String](
    e => e.toString,
    s => TaskState.withName(s)
  )

  private class TaskTable(tag: Tag) extends Table[Task](tag, "tasks") {
    def id          = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def title       = column[String]("title")
    def description = column[String]("description")
    def state       = column[TaskState]("state")
    def *           = (id, title, description, state) <> ((Task.apply _).tupled, Task.unapply)
  }
  private val tasks = TableQuery[TaskTable]

  def getTaskList: Future[Seq[Task]] = db.run {
    tasks.result
  }

  def getTask(id: Int): Future[Option[Task]] =
    getTaskList.map(_.find(_.id == id))

  def createTask(title: String, description: String, state: TaskState): Future[Int] = db.run {
    // Благодаря атрибутам столбца id, указанным в файле "1.sql", а также O.AutoInc,
    // БД проигнорирует 0 и сама проставит значение:
    ((tasks.map(t => (t.title, t.description, t.state))
      returning tasks.map(_.id)
      into ((restData, id) => Task(id, restData._1, restData._2, restData._3))
     ) += (title, description, state)).map(_.id)
  }

  def deleteTask(id: Int): Future[Int] = db.run {
    tasks.filter(_.id === id).delete.map(_ => id)
  }

  def updateTask(task: Task): Future[Int] = db.run {
    tasks.filter(_.id === task.id).update(task).map(_ => task.id)
  }
}