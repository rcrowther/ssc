package sake

import java.nio.file.Path



/** Task which does nothing.
  * 
  * May still be useful to have a dummy task, though.
  *
  * It must have a valid route and name, and they must follow the
  * rules, i.e. the route must be unique.
  */
//TODO: This should have a route, (and a name)
// Rename...?
class StubTask(
  val route: String,
  val prerequisiteRoutes: Seq[String],
  val name: String,
  val description: String,
  val multitaskDependancies: Boolean
)
    extends Task
{
  // An unused value, make neutral
  val action = ActionEmpty
  
  def getInvoked: Boolean = true
  def setInvoked(state: Boolean) = {}

}//StubTask



object StubTask {

  /** Returns an empty task
    */
  def apply(
    route: Route,
    prerequisiteRoutes: Seq[String],
    name: String,
    description: String,
  multitaskDependancies: Boolean
  )
      : StubTask =
  {
    new StubTask(
      route,
prerequisiteRoutes,
      name,
      description,
multitaskDependancies
    )
  }

}//StubTask
