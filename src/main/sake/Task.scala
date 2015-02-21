package sake


/** Base of classes representing work to do.
  * 
  * The Task is an empty shell with a few printers. Everything is
  * accessible, and instances are only intended for internal use. Do
  * not do anything mutable unless `invokedLock`is synchronized, or
  * the instance is known not to be in an in/evoke.
  *
  * For consistency, new tasks should be extended from
  * [[sake.GenericTask]], or derivatives, not this class.
  */
trait Task
{

  var returnValue: Any = Unit


  /** Used to locate tasks.
    *
    * See [[sake.TaskRegistry]]
    */
  def route: String

  /** Tasks which run before this task.
    * 
    * Part of the dependancy system.
    */
  // NB. Routes are not resolved to Tasks here, but on the fly.
//
 // This is unlikely to make much difference to performance
// But makes a big difference to general running. If routes
//were resolved immediately, Sakes with missing prerequisites
//would crash on construction --- likely irritating.
// And, even if the intention to provide the task was there,
// they wouldn't build if prerequisites tasks were not pre-declared.
  def prerequisiteRoutes: Seq[String]

  /** A human-intended name for this task.
    */
  def name: String

  /** A human-intended description of the purpose of this task.
    *
    * Currently only used if the '-trace' option is used when *voking.
    */
  // NB Not using the complex comment idea in the original.
  def description: String

/** States if the task should have dependancies multitasked.
*
*/
  def multitaskDependancies: Boolean

  /** The action this task can perform.
    *
    * This can be empty, see `ActionEmpty`, in the package object.
    */
  def action: Action


  // Locking and state //

  /** Records if this task has been invoked.
    *
    * A task can be warned, and perhaps invoked again, by calling `reenable`.
    */
  val invokedLock = new java.lang.Object
  def getInvoked: Boolean // = false
  def setInvoked(state: Boolean)



  /** Sets the task so it can be invoked.
    *
    *
    */
  def reenable() =
    invokedLock.synchronized {
      setInvoked(false)
    }



  /** Format task data for a trace.
    *
    */
  def mkStringTrace
      : String =
  {
    var b = Seq.empty[String]
    if (!getInvoked) {
      b = b :+ "first_time"
    }
    if (b.isEmpty) ""
    else b.mkString("(", ", ", ")")
  }

  def addString(b: StringBuilder)
      : StringBuilder = {
    b ++= "route:"
    b ++= route
    //b ++= ", actionArgNames:"
    // b append actionArgNames
    b ++= ", prerequisiteRoutes:"
    b append prerequisiteRoutes
    b ++= ", name:"
    b append name
    b ++= ", description:"
    b append description
  }

  override def toString()
      : String =
  {
    val b = new StringBuilder()
    b ++= "Task("
    addString(b)
    b += ')'
    b.toString
  }


}//Task

