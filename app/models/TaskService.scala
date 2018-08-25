package models

// TODO: продолжить рефакторинг, отделить сервис от всего остального

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
  def getTasks: Future[Seq[Task]] = {
    taskRepository.getTaskList
  }

  // TODO: подумать над вариантом поиска в TaskRepository, а не здесь
  def getTask(id: Int)(implicit ec: ExecutionContext): Future[Option[Task]] = {
    getTasks.map{ taskSeq =>
      taskSeq.find(task => task.id == id)
    }
  }

  // Генерирует из списка задач матрицу, полностью совпадающую со структурой таблицы в шаблоне:
  def getTaskMatrixForTemplate()(implicit ec: ExecutionContext): Future[List[List[Task]]] = {
    getTasks.map{ taskSeq =>
      if (taskSeq.nonEmpty) {

        /*
          Передо мной встала следующая задача: преобразовать исходный смешанный список в структуру, которую будет
          удобно рендерить как таблицу в HTML со столбцами в виде состояний задач. Пример такой структуры:

          List(
                List(  todoTask 1,   in progress task 1,   done task 1  ),
                List(  todoTask 2,   null              ,   done task 2  ),
                List(  null      ,   null              ,   done task 3  )
          )

          Обычно подробно комментировать каждую строчку кода - плохая практика, но в данном случае метод содержит
          много последовательных операций, которые я посчитал нужным объяснить для лучшего понимания "что происходит".
          К сожалению, я не нашёл более простого и характерного для Scala решения данной задачи.
        */

        // Из исходного списка задач формируем Map, где ключи - это порядковые номера состояний задач:
        // (view для оптимизации, чтобы не создавались промежуточные коллекции)
        val tasksGroupedByStates = taskSeq.view.groupBy(_.state.id)
        // Если задачи какого-либо состояния отсутствуют, то их ключи всё равно нужно внести для корректного отображения
        // html-таблицы. Поэтому вставляем недостающие ключи, в качестве их значений - пустая последовательность:
        val sortedTaskMatrix = TaskState.values.flatMap(state =>
          if (tasksGroupedByStates.filterKeys(key => key == state.id).isEmpty)
            tasksGroupedByStates.updated(state.id, Nil)
          else
            tasksGroupedByStates
        // Затем сортируем по ключам, чтобы столбцы конечной матрицы были в том же порядке,
        // что и перечисление состояний, после чего отбрасываем ключи и приводим к списку:
        ).toSeq.sortBy(_._1).toMap.values.toList
        // Для успешного поворота матрицы нужно, чтобы все списки были одной длины, вычисляем длину самого большого:
        val maxRowLength = sortedTaskMatrix.map(_.size).max
        // Дополняем каждый список с помощью null и транспонируем матрицу:
        sortedTaskMatrix.map(_.padTo(maxRowLength, null)).transpose

        /*
          В Scala использование null - также плохая практика, но для введения более правильного Option(None)
          мне пришлось бы обернуть каждый элемент списка также в Option, что потребовало бы дополнительных
          операций и ещё больше увеличило метод. К тому же, код не будет никем сопровождаться, что ещё больше
          снижает "вредность" null.
        */

      } else {
        Nil
      }
    }
  }

  def createTask(data: TaskData): Future[String] = {
    taskRepository.create(data.title, data.description, data.state)
  }

  def deleteTask(id: Int): Future[Int] = {
    println("TASK DELETED")
    taskRepository.deleteTask(id)
  }

  def updateTask(id: Int, newData: TaskData): Future[Int] = {
    taskRepository.updateTask(id, newData.title, newData.description, newData.state)
  }
}