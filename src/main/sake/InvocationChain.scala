package sake



/** Carries data on tasks invoked.
  * 
  * Note that invocation trees are immutable, and each path down the tree
  * is abandoned as the sake backtracks up through dependancies. 
* So final paths are a direct path to any error.
  */
class InvocationChain(
  val underlying : Seq[String]
)
{

  def append(route: String)
      : InvocationChain =
  {
    if (underlying.contains(route)) {
      // TODO: The original prints the parent of the dependancy
      throw new Exception(s"Circular dependency detected: => ${route}")
    }
    else new InvocationChain(underlying :+ route)
  }

  def size : Int = underlying.size

  override def toString()
      : String =
  {
    underlying.mkString("Invokation chain: ", ", ", "")
  }

}//InvocationChain



object InvocationChain {
  protected val emptyThing = new InvocationChain(Seq.empty[String])
  def empty: InvocationChain = emptyThing

  def apply(underlying: Seq[String])
      : InvocationChain =
  {
    InvocationChain(underlying)
  }

}//InvocationChain
