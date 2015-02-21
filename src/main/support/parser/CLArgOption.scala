package sake.support.parser


/** Value definition for a commandline schema.
  *
  * Implicit definitions make simple maps ok,
  *
  * {{{
  * val CLSwitchDefaultTest = Map[String, CLArgOption](
  *    "-docTitle" -> "Title of the document"
  *)
  * }}}
  *
  */
class CLArgOption (
  val description: String
)
extends CLOption
{
  override def toString() : String = {
    val b = new StringBuilder("CLArgOption(")
    b append description
    b += ')'
    b.result()
  }
}

object CLArgOption {

  def apply(
    description: String
  )
      : CLArgOption =
  {
    CLArgOption(
      description
    )
  }


  implicit def stringToCLArgOption(args: String): CLArgOption = new CLArgOption(args)

}//CLArgOption

