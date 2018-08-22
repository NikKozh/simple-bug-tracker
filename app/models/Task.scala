package models

object TaskState extends Enumeration {
  type TaskState = Value
  // столбцы будут рендериться в шаблоне согласно порядку перечисления:
  val TODO        = Value("TODO")
  val IN_PROGRESS = Value("In Progress")
  val DONE        = Value("Done")
}

import controllers.TaskForm.Data
import models.TaskState._

import scala.concurrent.{ExecutionContext, Future}

// case class Task(id: Int, title: String, description: String, state: TaskState)
// Временное решение с mutable полями:
case class Task(var id: Int, var title: String, var description: String, var state: TaskState)

object Task {
  import scala.collection.mutable._
  import slick.jdbc.JdbcBackend.Database
  import slick.jdbc.PostgresProfile.api._

  // Временное решение, автоинкремент будет в БД
  private var counter = 7
  private def increment = {
    counter += 1
    counter
  }

  var list = new ListBuffer[Task]()
  list ++= List[Task](Task(1, "title1", "desc1", TODO),        Task(2, "title2", "desc2", DONE),
                      Task(3, "title3", "desc3", TODO),        Task(4, "title4", "desc4", TODO),
                      Task(5, "title5", "desc5", IN_PROGRESS), Task(6, "title6", "desc6", IN_PROGRESS),
                      Task(7, "title7", "desc7", TODO))

  // TODO: сделать запрос к БД, запросить все элементы
  def getTasks: ListBuffer[Task] = {
    list
  }

  // TODO: сделать запрос к БД, запросить элемент по ID
  def getTask(id: Int): Option[Task] = {
    getTasks.find(task => task.id == id)
  }

  // Генерирует из списка задач матрицу, полностью совпадающую со структурой таблицы в шаблоне:
  // TODO: исправить баг смещения столбцов, когда задачи одного и более типа полностью отсутствуют
  // TODO: когда с заглушкой в виде мутабельного списка будет покончено, поменять scala.Seq на просто Seq
  def getTasksMatrixForTemplate(tasks: Future[scala.Seq[Task]])(implicit ec: ExecutionContext): Future[List[List[Task]]] = {
    tasks.map(taskSeq => {
      if (taskSeq.nonEmpty) {
        // TODO: если будет время, замерить время выполнения с view и без
        // TODO: желательно заменить конструкцию toMap -> values -> toList чем-то покороче
        val sortedTasksMatrix = taskSeq.view.groupBy(_.state.id).toSeq.sortBy(_._1).toMap.values.toList
        val maxRowLength = sortedTasksMatrix.view.map(_.size).max
        // TODO: заменить null на Option(None)
        sortedTasksMatrix.map(_.padTo(maxRowLength, null)).transpose
      } else {
        Nil
      }
    })

    /*if (list.nonEmpty) {
      // TODO: если будет время, замерить время выполнения с view и без
      // TODO: желательно заменить конструкцию toMap -> values -> toList чем-то покороче
      val sortedTasksMatrix = getTasks.view.groupBy(_.state.id).toSeq.sortBy(_._1).toMap.values.toList
      val maxRowLength = sortedTasksMatrix.view.map(_.size).max
      // TODO: заменить null на Option(None)
      sortedTasksMatrix.map(_.padTo(maxRowLength, null)).transpose
    } else {
      Nil
    }*/
  }

  def create(data: Data): Unit = {
    list += Task(increment, data.title, data.description, data.state)
  }

  def delete(id: Int):Unit = {
    list = list.filterNot(task => task.id == id)
  }

  def update(id: Int, data: Data):Unit = {
    // Нет смысла писать что-то более красивое\оптимизированное,
    // т.к. в дальнейшем всё равно будет взаимодействие с БД
    for (task <- list) {
      if (task.id == id) {
        task.title = data.title
        task.description = data.description
        task.state = data.state
      }
    }
  }
}