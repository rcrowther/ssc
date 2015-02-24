package sake.util

import sake.DependantTaskerLike

import java.io.File
import java.nio.file.{Paths, Path, Files}
import sys.process._

import scala.language.postfixOps


/** Provides shell utilities.
*/
trait Shell {

  this: sake.Trace =>

//TODO: Non-blocker? How does that work with Sake?
  /** Invoke a shell(like) command, returning output as a string.
    * 
    * Cuts the remarkably simplified Scala interface down to a basic.
    *
    * Insert commandline options as seperate string items e.g.
    *
    * {{{ 
    *   sh("ls", "-a")
    * }}}
    *
    * If used for Bash-style shells, enter each switch and parameter separately e.g.
    *
    * {{{ 
    *   sh(Seq("cp", "-v", "/home", "/usb"))
    * }}}
    *
    * Can also use the Scala shell-like commands `#>`, `#|` etc.
    *
    * Blocks.
    *
    * Throws an exception on non-zero exit codes.
    *
    * See [[http://www.scala-lang.org/api/current/index.html#scala.sys.process.ProcessBuilder]]
    */
  def sh(xs: Seq[String])
      : String =
  {
    //if (options.verbosely) trace(seq.mkString)
    xs !!
  }

  /** Invoke a shell(like) command, returning output as a string.
    */
  def sh(s: String): String = sh(Seq(s))

  /** Invoke a shell(like) command, printing to out.
    * 
    * Cuts the remarkably simplified Scala interface down to a basic.
    *
    * Insert commandline options as seperate string items e.g.
    *
    * {{{ 
    *   sh("ls", "-a")
    * }}}
    *
    * If used for Bash-style shells, enter each switch and parameter separately e.g.
    *
    * {{{ 
    *   sh(Seq("cp", "-v", "/home", "/usb"))
    * }}}
    *
    * Can also use the Scala shell-like commands `#>`, `#|` etc.
    *
    * Blocks.
    *
    * Throws an exception on non-zero exit codes.
    *
    * See [[http://www.scala-lang.org/api/current/index.html#scala.sys.process.ProcessBuilder]]
    */
  def shPrint(xs: Seq[String])
      : Int =
  {
    //if (options.verbosely) trace(seq.mkString)
    xs !
  }

  /** Invoke a shell(like) command, printing to out.
    */
  def shPrint(s: String): Int = shPrint(Seq(s))

}//Utils
