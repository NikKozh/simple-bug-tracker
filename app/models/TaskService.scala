package models

import javax.inject._

import scala.concurrent.{ExecutionContext, Future}

import controllers.TaskForm.TaskData
import models.TaskState._

@Singleton
class TaskService @Inject()(taskRepository: TaskRepository) {
  def getTasks: Future[Seq[Task]] = {
    taskRepository.getTaskList
  }

  def getTask(id: Int)(implicit ec: ExecutionContext): Future[Option[Task]] = {
    taskRepository.getTask(id)
  }

  // Генерирует из списка задач матрицу, полностью совпадающую со структурой таблицы в шаблоне:
  def getTaskMatrixForTemplate()(implicit ec: ExecutionContext): Future[List[List[Task]]] = {
    getTasks.map{ taskSeq =>
      if (taskSeq.nonEmpty) {

        /*
          В процессе разработки появилась следующая задача: преобразовать исходный смешанный список в структуру,
          которую будет удобно сразу рендерить как таблицу в HTML со столбцами в виде состояний задач.
          Пример такой структуры:

          List(
                List(  todoTask 1,   in progress task 1,   done task 1  ),
                List(  todoTask 2,   null              ,   done task 2  ),
                List(  null      ,   null              ,   done task 3  )
          )

          Обычно подробно комментировать каждую строчку кода - плохая практика, но в данном случае метод содержит
          много последовательных операций, которые я посчитал нужным объяснить для лучшего понимания "что происходит".
          К сожалению, я не нашёл более простого и при этом характерного для Scala решения данной задачи.
        */

        // Из исходного списка задач формируется Map, где ключи - это порядковые номера состояний задач
        // (view - для оптимизации, чтобы не создавались промежуточные коллекции):
        val tasksGroupedByStates = taskSeq.view.groupBy(_.state.id)
        // Если задачи какого-либо состояния отсутствуют, то их ключи всё равно нужно внести для корректного отображения
        // html-таблицы. Поэтому вставляются недостающие ключи, а в качестве их значений - пустая последовательность:
        val sortedTaskMatrix = TaskState.values.flatMap(state =>
          if (tasksGroupedByStates.filterKeys(key => key == state.id).isEmpty)
            tasksGroupedByStates.updated(state.id, Nil)
          else
            tasksGroupedByStates
        // Затем происходит сортировка по ключам, чтобы столбцы конечной матрицы были в том же порядке,
        // что и перечисление состояний, после чего ключи отбрасываются и структура приводится к списку:
        ).toSeq.sortBy(_._1).toMap.values.toList
        // Для успешного поворота матрицы нужно, чтобы все списки были одной длины, вычисляется длина самого большого:
        val maxRowLength = sortedTaskMatrix.map(_.size).max
        // Каждый список дополняется null и матрица транспонируется:
        sortedTaskMatrix.map(_.padTo(maxRowLength, null)).transpose

        /*
          В Scala использование null - также плохая практика, но для введения более правильного Option(None)
          пришлось бы обернуть каждый элемент списка также в Option, что потребовало бы дополнительных
          операций и ещё больше увеличило метод. К тому же, код не будет никем сопровождаться, что ещё больше
          снижает "вредность" null в данном случае.
        */

      } else {
        Nil
      }
    }
  }

  def createTask(data: TaskData): Future[Int] =
    taskRepository.createTask(data.title, data.description, data.state)

  def deleteTask(id: Int): Future[Int] = {
    taskRepository.deleteTask(id)
  }

  def updateTask(id: Int, newData: TaskData): Future[Int] = {
    taskRepository.updateTask(id, newData.title, newData.description, newData.state)
  }
}
