package sake


/** Task which does something always.
  *
  * The Generic task can be given actions,
  * and can accept dependancies on other tasks.  
  */
class GenericTask(
  val route: String,
  val prerequisiteRoutes: Seq[String],
  val name: String,
  val description: String,
  val multitaskDependancies: Boolean,
  val action: Action
)
    extends Task
{
 private var invoked = false
  def getInvoked: Boolean = invoked
  def setInvoked(state: Boolean) = invoked = state
}//GenericTask



object GenericTask {

/** Returns a generic task
*/
  def apply(
    route: Route,
    prerequisiteRoutes: Seq[String],
    name: String,
    description: String,
  multitaskDependancies: Boolean,
action: => Any
  )
      : GenericTask =
  {
    new GenericTask(
      route,
      prerequisiteRoutes,
      name,
      description,
multitaskDependancies,
      () => action
    )
  }

/** Task with no action, only dependancies.
*/
  def apply(
    route: Route,
    prerequisiteRoutes: Seq[String],
    name: String,
    description: String,
  multitaskDependancies: Boolean
  )
      : GenericTask =
  {
    new GenericTask(
      route,
      prerequisiteRoutes,
      name,
      description,
multitaskDependancies,
      ActionEmpty
    )
  }

  def apply(
    route: Route,
    name: String,
    description: String,
action: => Any
  )
      : GenericTask =
  {
    new GenericTask(
      route,
      Seq.empty[String],
      name,
      description,
      false,
      () => action
    )
  }

  def apply(
    route: Route,
    name: String,
    description: String
  )
      : GenericTask =
  {
    new GenericTask(
      route,
      Seq.empty[String],
      name,
      description,
      false,
      ActionEmpty
    )
  }

}//GenericTask
