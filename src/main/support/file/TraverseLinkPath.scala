package sake.support.file

import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.FileVisitOption


/** A collection of paths in a directory.
  * 
  * Visits link files recursivly. Note that this includes the
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
class TraverseLinkPath(
  val path: Path,
  val followLinks: Boolean,
  val depth: Int
)
    extends Traverse[Path]
{

  def newVisitor[U](f: (Path) => U) = new LinkVisitorPath(f)
}



object TraverseLinkPath
extends TraversePathCompanion[TraverseLinkPath]
 {

  protected val emptyThing = new TraverseLinkPath(
    emptyPath,
    false,
    0
  )
  {
    override def foreach[U](f: Path => U): Unit = {}
    override def toString: String = "empty traversable entrypath"
  }

  def apply(
    path: Path,
    followLinks: Boolean,
    depth: Int
  )
      : TraverseLinkPath =
  {
    new TraverseLinkPath(path, followLinks, depth)
  }

}//TraverseLinkPath
