package ssc.util


import scala.language.implicitConversions


/** A simple commandline parser.
  * 
  * The parser works from schemas. These are a map of strings (the
  * task or switch to be identified), to an [[UnParameteredOption]] or a
  * [[ParameteredOption]]
  * 
  */
package object parser {

  implicit def stringToParameterDescription(s: String)
      : ParameterDescription =
    new ParameterDescription(s)

  /** Defines an arg configuration
    *
    * 
    */
  type UnParameteredConfig = Map[String, UnParameteredOption]


  /** Defines a switch configuration
    *
    * 
    */
  type ParameteredConfig = Map[String, ParameteredOption]


  /** Defines a configuration underlying switches and args
    *
    * 
    */
  type OptionConfig = Map[String, CLOption]

}//support
