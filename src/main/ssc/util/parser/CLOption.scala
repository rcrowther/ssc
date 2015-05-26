package ssc.util.parser


/** Template for a value definition for a commandline schema.
  *
  *
  * @define obj commandline option
  */
trait CLOption
{
  def description: String
  def parameterDescription: ParameterDescription
}


