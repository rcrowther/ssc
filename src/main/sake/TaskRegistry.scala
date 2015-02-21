package sake


/** A collection of tasks.
  * 
  * Inherited into [[sake.DependantTaskerLike]], the anchor and API for
  * this package.
  */
class TaskRegistry(
  val tasks: scala.collection.mutable.Map[String, Task]
)
{
  // A map of routes to tasks ()


  /** Adds a task to the registry.
    */
  def addTask(task: Task)
      : Unit =
    tasks += ((task.route, task))

  /** Removes a task from the registry.
    */
  def removeTask(task: Task)
      : Unit =
    tasks -= (task.route)

  /** Retrieves the task associated with the given route.
    */
  def apply(route: Route)
      : Task =
  {
    tasks(route)
  }

  /** Clears the contents of this task registry.
    * After use the registry will contain no tasks.
    */
  def clear()
  {
    tasks.clear()
  }

  def contains(route: String) : Boolean = tasks.contains(route)

  /** Applies a function f to all elements of this task registry.
    */
  def foreach(f: ((String, Task)) => Unit): Unit = {
    tasks.foreach(f)
  }

  def addString(b: StringBuilder)
      : StringBuilder =
  {
    foreach { kv =>
      b += '('
      b append kv._1
      b ++= ", "
      b append kv._2.name
      b += ')'
    }
    b
  }

  override def toString()
      : String =
  {
    val b = new StringBuilder()
    b ++= "TaskRegistry("
    addString(b)
    b += ')'
    b.toString
  }

}//TaskRegistry



object TaskRegistry {

  def apply()
      : TaskRegistry =
  {
    new TaskRegistry(scala.collection.mutable.Map.empty[String, Task])
  }

}//TaskRegistry
