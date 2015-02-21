package sake.support.parser


/** Value definition for a commandline schema.
  *
  * Switche keys need to be declared with their prefix '-'. This is
  * not clever code.
  *
  * Implicit definitions make tuples ok,
  *
  * {{{
  * val CLSwitchDefaultTest = Map[String, CLSwitchOption](
  *    "-docTitle" -> ("Title of the document", "Title", false, 1)
  *)
  * }}}
  *
  * Multiple defaults need explicit definition,
  *
  * {{{
  * val CLSwitchDefaultTest = Map[String, CLSwitchOption](
  *    "-srcDir" -> CLSwitchOption("List of files to be processed", Seq("/home/forthe/heart", "/home/alone", "/home"), true, 64),
  *    "-docTitle" -> ("Title of the document", "Title", false, 1)
  *)
  * }}}
  *
  * For switches where the switch simply needs to exist or not, 
  * use a default value and `isEmpty` to test if the switch was used.
  *
  * @param description a description of the switch action. Appears
  *  in help.
  * @param default value of the switch, should it not appear in a
  *  commandline, as a string.
  * @param parameterCountIsMax if true, the option accepts any number
  *  of parameters up to `parameterCount`, if false, the number of
  *  parameters must match `parameterCount` exactly. 
  * @param parameterCount either exact mumber of parameters, or the
  *  limit to the number of parameters. See `parameterCountIsMax`.
  */
class CLSwitchOption (
  val description: String,
  val default: Seq[String],
  val parameterCountIsMax: Boolean,
  val parameterCount: Int
)
extends CLOption
{
  override def toString() : String = {
    val b = new StringBuilder("CLSwitchOption(")
    b append description
    b ++= ", "
    b append default
    b ++= ", "
    b append parameterCountIsMax
    b ++= ", "
    b append parameterCount
    b += ')'
    b.result()
  }
}

object CLSwitchOption {

  def apply(
    description: String,
    default: Seq[String],
    parameterCountIsMax: Boolean,
    parameterCount: Int
  )
      : CLSwitchOption =
  {
    new CLSwitchOption(
      description,
      default,
      parameterCountIsMax,
      parameterCount
    )
  }

  def apply(
    description: String,
    default: Seq[String]
  )
      : CLSwitchOption =
  {
    new CLSwitchOption(
      description,
      default,
      false,
      0
    )
  }

  implicit def tuple4ToCLSwitchOption(args: Tuple4[String, String, Boolean, Int]): CLSwitchOption = new CLSwitchOption(args._1, Seq(args._2), args._3, args._4)

  implicit def tuple2ToCLSwitchOption(args: Tuple2[String, String]): CLSwitchOption = new CLSwitchOption(args._1, Seq(args._2), false, 0)

}//CLSwitchOption

