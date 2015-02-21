
/** An executable collection with dependancies linking the elements.
  *
  * Each element must be of a type derived from [[sake.Task]].
  * 
  * Each task can carry a closure.
  * 
  * Elements can have dependancies defined, which force the elements
  * to execute in a given order.
  *
  * == Other ==
  *  - RunnerSake* - helpers for sakes to be used with runners. Can do the work for handling commandlines and external variables.
  * - PackageSake - an implemented Sake. If an executable Sake is available, this can package the compiled version into jars, or an installale folder.
  */
package object sake {

  //type InvocationChain = scala.collection.mutable.Seq[Task]
  //val InvocationChainEmpty = scala.collection.mutable.Seq.empty[Task]

  /** Maps names to Any.
    *
    * Used to carry parameters into tasks.
    */
  type MultiTypeOptionMap = Map[String, Option[Any]]

  /** An empty multi-type map.
    *
    * Useful for Tasks which take no parameters (the tasks may still
    * be active through dependancies and returns)
    */
  val MultiTypeMapOptionEmpty =  Map.empty[String, Option[Any]]

  /** Maps names to Any.
    *
    * Used to carry class parameters into tasks.
    */
  type MultiTypeMap = Map[String, Any]

  /** An empty multi-type map.
    *
    * Useful for Tasks which take no class parameters (the tasks may still
    * be active through dependancies and returns)
    */
  val MultiTypeMapEmpty =  Map.empty[String, Any]

  /** Defines an action in a task.
    *
    */
  type Action = () => Any

  /** An action which does nothing.
    *
    * For tasks which do nothing (the tasks may still be active as
    * placeholders or in chains).
    */
  //TODO: is this dead Function optimal?
  //val ActionEmpty: Action =  Function.const[(Option[Any], MultiTypeMap), Any] _
  val ActionEmpty: Action =  () => {None}
}//shared
