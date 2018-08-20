package models

object TaskState extends Enumeration {
  type TaskState = Value
  val  TODO, IN_PROGRESS, DONE = Value
}

import models.TaskState._

case class Task(id: Int, title: String, description: String, state: TaskState)

object Task {
  def getTasks: List[Task] = Nil // TODO: временное решение до реализации доступа к БД
  def create(title: String, description: String, state: TaskState) {}
  def delete(id: Int) {}
  def update(id: Int) {}
}