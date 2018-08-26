package models

import models.TaskState.TaskState

case class Task(id: Int, title: String, description: String, state: TaskState)