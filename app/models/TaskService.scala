package models

object TaskState extends Enumeration {
  type TaskState = Value
  // столбцы будут рендериться в шаблоне согласно порядку перечисления:
  val TODO        = Value("TODO")
  val IN_PROGRESS = Value("In Progress")
  val DONE        = Value("Done")
}

import controllers.TaskForm.TaskData
import javax.inject._
import models.TaskState._

import scala.concurrent.{ExecutionContext, Future}

// case class Task(id: Int, title: String, description: String, state: TaskState)
// Временное решение с mutable полями:
case class Task(var id: Int, var title: String, var description: String, var state: TaskState)

@Singleton
class TaskService @Inject() (taskRepository: TaskRepository) {
  def getTaskSequence: Future[Seq[Task]] = {
    taskRepository.getTaskList
  }

  // TODO: подумать над вариантом поиска в TaskRepository, а не здесь
  def getTask(id: Int)(implicit ec: ExecutionContext): Future[Option[Task]] = {
    getTaskSequence.map{ taskSeq =>
      taskSeq.find(task => task.id == id)
    }
  }

  // Генерирует из списка задач матрицу, полностью совпадающую со структурой таблицы в шаблоне:
  // TODO: исправить баг смещения столбцов, когда задачи одного и более типа полностью отсутствуют
  def getTaskMatrixForTemplate()(implicit ec: ExecutionContext): Future[List[List[Task]]] = {
    getTaskSequence.map{ taskSeq =>
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
    }
  }

  def createTask(data: TaskData): Future[String] = {
    taskRepository.create(data.title, data.description, data.state)
  }

  def deleteTask(id: Int): Future[Int] = {
    taskRepository.deleteTask(id)
  }

  def updateTask(id: Int, newData: TaskData): Future[String] = {
    // TODO: доделать изменение задачи
    taskRepository.updateTask(id, newData.title, newData.description, newData.state)
  }
}