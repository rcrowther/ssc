package sake.helper

import java.nio.file.Path
import java.io.File
import java.nio.file.attribute.FileTime
import sake.support.file.{StringOps, PathOps, FileTimeOps}

import scala.language.implicitConversions



/** Helper code for finding and using executables
  *
  * This package is intended for helping with shell invocation. The
  * code has little purpose if libraries have been imported and/or
  * placed on the classpath.
  *
  * [[sake.helper.FindExecutable]] is at the base, templating more
  * refined classes targetting various known executables. If the
  * executable has no specialized class
  * [[sake.helper.executable.Generic]] can be used.
  *
  * The methods may return a [[sake.helper.executable.ExecuteData]],
  * which returns a Path and [[sake.helper.executable.Version]]. How
  * specialized classes handle this is their affair (they may return
  * `None`, to indicate they can not find anything).
  *
  */
package object executable {


}//support
