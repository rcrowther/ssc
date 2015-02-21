package sake

/** Main class of sake.
  *
  * Declare a class and inherit this class. Then declare tasks,
  * 
  * {{{
  *    task("elevensies", after("morning", "dawn")) {
  *      println(s"printAction2Elevensies! returns: \$returns")
  *      // Making a return value available,
  *      // perhaps for a task "lunch"
  *      "in need of a walk"
  *    }
  * }}}
  *
  * Note how the variables `returns` and `params` are available.
  *
  * Sake is Scala. Scala code should work ok in the blocks, provided
  * keywords such as `task*` are avoided.
  *
  * See the documentation of [[sake.DependantTaskerLike]] for more detail.
  */
// This class defines the DSL. DSL gear only in here please.
// If you can do better with the DSL, great.
// task Route2Elevensies after(Route1Morning) {}
// would be top.
trait SakeLike
    extends DependantTaskerLike
{

  /** Add a task
    */ 
  def task(taskRoute: String)(block: => Any)
      : Unit =
    task(taskRoute, "", false, Seq.empty[String])(block)

  def task(taskRoute: String, dependencies: Seq[String])(block: => Any)
      : Unit =
    task(taskRoute, "", false, dependencies)(block)

  def task(taskRoute: String, description: String)(block: => Any)
      : Unit =
    task(taskRoute, description, false, Seq.empty[String])(block)

  def task(
    taskRoute: String,
    multitaskDependancies: Boolean,
    dependencies: Seq[String]
  )(block: => Any)
      : Unit =
    task(taskRoute, "", multitaskDependancies, dependencies)(block)

  def task(
    taskRoute: String,
    description: String,
    dependencies: Seq[String]
  )(block: => Any)
      : Unit =
    task(taskRoute, description, false, dependencies)(block)

  /** Adds a task to the registry.
    */
  def task(
    taskRoute: String,
    description: String,
    multitaskDependancies: Boolean,
    dependencies: Seq[String]
  )(block: => Any)
      : Unit =
  {
    // Convert the block to a scoped block, so it will take the parameters

    val task = GenericTask(
      taskRoute,
      dependencies,
      routeToName(taskRoute),
      description,
      multitaskDependancies,
      block
    )
    registry.addTask(task)
  }

  def stubTask(
    taskRoute: String,
    description: String,
    multitaskDependancies: Boolean,
    dependencies: Seq[String]
  )
      : Unit =
  {
    val task = StubTask(
      taskRoute,
      dependencies,
      routeToName(taskRoute),
description,
      multitaskDependancies
)
    registry.addTask(task)
}

  def stubTask(
    taskRoute: String,
    multitaskDependancies: Boolean,
    dependencies: Seq[String]
  )
      : Unit =
{
  stubTask(
    taskRoute,
    "",
    multitaskDependancies,
    dependencies
  )
}

  def stubTask(
    taskRoute: String,
    dependencies: Seq[String]
  )
      : Unit =
{
  stubTask(
    taskRoute,
    "",
    false,
    dependencies
  )
}

  protected def after(dependancies: String*)
      : Seq[String] = dependancies

}//Sake

