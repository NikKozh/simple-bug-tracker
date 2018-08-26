package models

object TaskState extends Enumeration {
  type TaskState = Value
  // столбцы будут рендериться в шаблоне строго согласно порядку перечисления:
  val TODO        = Value("TODO")
  val IN_PROGRESS = Value("In Progress")
  val DONE        = Value("Done")
}