package sake.support.parser


/** Template for a value definition for a commandline schema.
  *
  *
  * @define obj option
  */
trait CLOption
{
  def description: String
  def parameterDescription: ParameterDescription
}

object CLOption {
}//CLOption

