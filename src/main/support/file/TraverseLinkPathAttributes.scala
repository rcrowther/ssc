package sake.support.file

import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.FileVisitOption


/** A collection of paths in a directory.
  * 
  * Visits directory files recursivly. Note that this includes the
  * target file. To walk the contents of a directory, do not process
  * the first return e.g.
  *
  * {{{
  * val tp = new TraversePath(path)
  * tp.toIterator.drop(1)
  * }}}
  *
  * @define target link
  *
  * @param path the path to start at (usually a directory file).
  * @param followLinks will follow symlinks if true, if false, will not.
  */
class TraverseLinkPathAttributes(
  val path: Path,
  val followLinks: Boolean,
  val depth: Int
)
    extends Traverse[(Path, BasicFileAttributes)]
{

  def newVisitor[U](f: ((Path, BasicFileAttributes)) => U) = new LinkVisitorPathAttributes(f)
}



object TraverseLinkPathAttributes
extends TraversePathAttributesCompanion[TraverseLinkPathAttributes]
 {

  protected val emptyThing = new TraverseLinkPathAttributes(
    emptyPath,
    false,
    0
  )
  {
    override def foreach[U](f: ((Path, BasicFileAttributes)) => U): Unit = {}
    override def toString: String = "empty traversable entrypath"
  }

  def apply(
    path: Path,
    followLinks: Boolean,
    depth: Int
  )
      : TraverseLinkPathAttributes =
  {
    new TraverseLinkPathAttributes(path, followLinks, depth)
  }

}//TraverseLinkPathAttributes
