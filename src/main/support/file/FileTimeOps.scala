package sake.support.file

import java.nio.file.attribute.FileTime

/** Lightweight wrap of file time
*
* Gives Scala-consistent equality, wrap and string operations.
* {{{
* import sake.FileTimeOps._
* }}}
*/
final class FileTimeOps(val v: FileTime) extends AnyVal
{
  def <(other: FileTime) = v.compareTo(other) < 0
  def >(other: FileTime) = v.compareTo(other) > 0
  def ==(other: FileTime) = v.compareTo(other) == 0
  def ===(other: FileTime) = v.equals(other)

  override def toString() = "FileTime(" + v + ')'

}//FileTimeOps

