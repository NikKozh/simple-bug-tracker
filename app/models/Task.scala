package models

object TaskState extends Enumeration {
  type TaskState = Value
  // перечислять состояния нужно именно в том порядке, в котором столбцы таблицы будут рендериться в шаблоне:
  val  TODO, IN_PROGRESS, DONE = Value

  // TODO: переопределить toString() у Value
  def toStr(value: Value): String = {
    value.toString.head + value.toString.replace("_", " ").tail.toLowerCase
  }
}

import models.TaskState._

case class Task(id: Int, title: String, description: String, state: TaskState)

object Task {
  // TODO: сделать запрос к БД, запросить все элементы
  def getTasks: List[Task] = Task(1, "title1", "desc1", TODO) :: Task(2, "title2", "desc2", DONE) ::
                             Task(3, "title3", "desc3", TODO) :: Task(4, "title3", "desc3", TODO) ::
                             Task(5, "title5", "desc5", IN_PROGRESS) :: Task(6, "title6", "desc6", IN_PROGRESS) ::
                             Task(7, "title1", "desc1", TODO) :: Nil

  // TODO: сделать запрос к БД, запросить элемент по ID
  def getTask(id: Int): Option[Task] = {
    getTasks.find(task => task.id == id)
  }

  // Генерирует из списка задач матрицу, полностью совпадающую со структурой таблицы в шаблоне:
  // TODO: не забыть проверить на "крайние" случаи
  def getTasksMatrixForTemplate: List[List[Task]] = {
    // TODO: если будет время, замерить время выполнения с view и без
    val sortedTasksMatrix = getTasks.view.groupBy(_.state).values.toList.reverse
    val maxRowLength = sortedTasksMatrix.view.map(_.size).max
    // TODO: заменить null на Option(None)
    sortedTasksMatrix.map(_.padTo(maxRowLength, null)).transpose
  }

  def create(title: String, description: String, state: TaskState) {}
  def delete(id: Int) {}
  def update(id: Int) {}
}