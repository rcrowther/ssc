package ssc.util.parser

import scala.language.implicitConversions


/** Value definition for a commandline schema.
  *
  * If there is more than one parametered option on a commandline,
  * then some sort of identifying mark is necessary in the option,
  * otherwise options are hard to distinguished from their
  * parameters. Conventionally, this would be a '-' prefix,
  * defining a switch. 
  * 
  * Identifying marks such as '-' must be placed in the keys to a
  * configuration. This is not clever code.
  *
  * Implicit definitions make tuples ok,
  *
  * {{{
  * val CLSwitchDefaultTest = Map[String, ParameteredOption](
  *    "-docTitle" -> ("Title of the document", "Title", false, 1)
  *)
  * }}}
  *
  * Multiple defaults need explicit definition,
  *
  * {{{
  * val CLSwitchDefaultTest = Map[String, ParameteredOption](
  *    "-srcDir" -> ParameteredOption("List of files to be processed", Seq("/home/forthe/heart", "/home/alone", "/home"), true, 64),
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
class ParameteredOption (
  val description: String,
  val default: Seq[String],
  val parameterDescription: ParameterDescription,
  val parameterCountIsMax: Boolean,
  val parameterCount: Int
)
    extends CLOption
{
  override def toString() : String = {
    val b = new StringBuilder("ParameteredOption(")
    b append description
    b ++= ", "
    b append default
    b ++= ", "
    b append parameterDescription
    b ++= ", "
    b append parameterCountIsMax
    b ++= ", "
    b append parameterCount
    b += ')'
    b.result()
  }
}

object ParameteredOption {

  def apply(
    description: String,
    default: Seq[String],
    parameterDescription: ParameterDescription,
    parameterCountIsMax: Boolean,
    parameterCount: Int
  )
      : ParameteredOption =
  {
    new ParameteredOption(
      description,
      default,
      parameterDescription,
      parameterCountIsMax,
      parameterCount
    )
  }

  def apply(
    description: String,
    default: Seq[String],
    parameterDescription: ParameterDescription
  )
      : ParameteredOption =
  {
    new ParameteredOption(
      description,
      default,
      parameterDescription,
      false,
      0
    )
  }

  /** Describes a switch using a tuple.
    *
    * Allows a sequence of default values to be defined.
    *
    * @param args tuple of (description, Seq(default), paramDesc, limit, paramCount) 
    */
  implicit def tuple5ToFreeParameteredOption(args: Tuple5[String, Seq[String], ParameterDescription, Boolean, Int])
      : ParameteredOption =
    new ParameteredOption(args._1, args._2, args._3, args._4, args._5)


  /** Describes a switch using a tuple.
    *
    * Allows only one default value, as a string.
    *
    * @param args tuple of (description, default, paramDesc, limit, paramCount) 
    */
  implicit def tuple5ToParameteredOption(args: Tuple5[String, String, ParameterDescription, Boolean, Int])
      : ParameteredOption =
    new ParameteredOption(args._1,  Seq(args._2), args._3, args._4, args._5)


  /** Describes a one parameter switch using a tuple.
    *
    * Allows a sequence of default values to be defined.
    *
    * @param args tuple of (description, default, paramDesc) 
    */
  implicit def tuple3ToParameteredOption(args: Tuple3[String, Seq[String], ParameterDescription])
      : ParameteredOption =
    new ParameteredOption(args._1, args._2, args._3, false, 1)

/*
  /** Describes a one parameter switch using a tuple.
    *
    * Allows only one default argument, as a string.
    *
    * @param args tuple of (description, default, paramDesc) 
    */
  implicit def tuple3ToParameteredOption(args: Tuple3[String, String, ParameterDescription])
      : ParameteredOption =
    new ParameteredOption(args._1,  Seq(args._2), args._3, false, 1)
*/

  /** Describes a no-parameter switch using a tuple.
    *
    * @param args tuple of (description, default) 
    */
  implicit def tuple2ToParameteredOption(args: Tuple2[String, String])
      : ParameteredOption =
    new ParameteredOption(args._1, Seq(args._2), ParameterDescription.empty, false, 0)


  /** Describes a no-parameter switch defaulting to 'false' using a string.
    *
    * @param args string of description 
    */
  implicit def stringToParameteredOption(args: String)
      : ParameteredOption =
    new ParameteredOption(args, Seq("false"), ParameterDescription.empty, false, 0)

}//ParameteredOption

