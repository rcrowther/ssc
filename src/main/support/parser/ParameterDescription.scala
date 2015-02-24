package sake.support.parser



/** Describes a parameter.
  */ 
class ParameterDescription(val v: String)
{
  override def toString()
      : String =
  {
    if (!v.isEmpty) '<' + v + '>'
    else ""
  }
}

object ParameterDescription
{
  protected val emptyThing : ParameterDescription = new  ParameterDescription("")
  def empty: ParameterDescription = emptyThing


  /*
   implicit def stringfromParameterDescription(pd: ParameterDescription)
   : ParameterDescription = 
   {
   v
   }
   */


  val path = new ParameterDescription("path")
  val paths = new ParameterDescription("path...")
  val file = new ParameterDescription("file")
  val files = new ParameterDescription("file...")
  val int = new ParameterDescription("int")
  val regex = new ParameterDescription("regex")
  val version = new ParameterDescription("version")
  val lang = new ParameterDescription("lang")
  val strCode = new ParameterDescription("code")
  val encoding = new ParameterDescription("encoding")
  val name = new ParameterDescription("name")
  val className = new ParameterDescription("class")
  val classNames = new ParameterDescription("class...")
  val text = new ParameterDescription("text")


}//ParameterDescription
