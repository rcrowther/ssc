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
class Sake
    extends SakeLike
{
}//Sake


object Sake {
  val emptyThing = new Sake()
  def empty: Sake = emptyThing
}
