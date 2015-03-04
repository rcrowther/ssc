package sake.util.parser


import scala.language.implicitConversions



/** Value definition for a commandline schema.
  *
  * An UnParameteredOption has no parameters.
  *
  * Implicit definitions make simple maps ok,
  *
  * {{{
  * val CLSwitchDefaultTest = Map[String, UnParameteredOption](
  *    "-docTitle" -> "Title of the document"
  *)
  * }}}
  *
  */
class UnParameteredOption (
  val description: String
)
extends CLOption
{
  // make nothing, to print nothing.
  val parameterDescription = ParameterDescription.empty

  override def toString() : String = {
    val b = new StringBuilder("UnParameteredOption(")
    b append description
    b += ')'
    b.result()
  }
}

object UnParameteredOption {

  def apply(
    description: String
  )
      : UnParameteredOption =
  {
    UnParameteredOption(
      description
    )
  }


  implicit def stringToUnParameteredOption(args: String): UnParameteredOption = new UnParameteredOption(args)

}//UnParameteredOption

