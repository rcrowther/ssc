package sake.support


import scala.language.implicitConversions


/** Various code used in sake.
  *
  * == Intro ==
  * a simple commandline parser.
  */
package object parser {
  implicit def stringToParameterDescription(s: String)
      : ParameterDescription =
    new ParameterDescription(s)

}//support
