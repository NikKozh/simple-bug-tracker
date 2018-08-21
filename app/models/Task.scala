package models

object TaskState extends Enumeration {
  type TaskState = Value
  // перечислять состояния нужно именно в том порядке, в котором столбцы таблицы будут рендериться в шаблоне:
  val  TODO, IN_PROGRESS, DONE = Value
}

import models.TaskState._

case class Task(id: Int, title: String, description: String, state: TaskState)

object Task {
  def getTasks: List[Task] = Task(1, "title", "desc", TODO) :: Nil // TODO: временное решение до реализации доступа к БД

  // Генерирует из списка задач матрицу, полностью совпадающую со структурой таблицы в шаблоне:
  def getTasksMatrixForTemplate: List[List[Task]] = {
    // TODO: если будет время, замерить время выполнения с view и без
    val sortedTasksMatrix = getTasks.view.groupBy(_.state).values.toList.reverse
    val maxRowLength = sortedTasksMatrix.view.map(_.size).max
    sortedTasksMatrix.map(_.padTo(maxRowLength, null)).transpose
  }

  def create(title: String, description: String, state: TaskState) {}
  def delete(id: Int) {}
  def update(id: Int) {}
}