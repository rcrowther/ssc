package sake.util

import java.nio.file.Path
import java.io.File
import java.nio.file.attribute.FileTime
import sake.support.file.{StringOps, PathOps, FileTimeOps}

import scala.language.implicitConversions


/** Helper code for filehandling.
  *
  * These helpers are split into three categories, Dir/Entry (meaning
  * a regular file) and Link (a soft symbolic link).
  *
  * They offer basic CRUD operations, where appropriate. They are not
  * very flexible, being intended for quick scripting. They offer no
  * guarantees about copying attributes, as Java (currently) will not,
  * and to do so reduces their scope of operation substancially.
  *
  * Importing the complete helper package i.e.
  *
  *{{{
  * import sake.util.file._
  *}}}
  *
  * imports the package object, which has useful implicits and
  * definitions, listed below.
  */
package object file {

  implicit def stringToPath(v: String) : StringOps = new StringOps(v)

  implicit def fromPath(p: Path) = new PathOps(p)
  implicit def toPath(op: PathOps): Path = op.v

  implicit def augmentFileTime(x: FileTime): FileTimeOps = new FileTimeOps(x)
  implicit def unaugmentFileTime(x: FileTimeOps): FileTime = x.v

  val PathEmpty = new File("").toPath

}//support
