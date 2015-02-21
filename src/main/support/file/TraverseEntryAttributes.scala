package sake.support.file

import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.FileVisitOption


/** A collection of paths in a directory.
  * 
  * Visits regular files recursivly. Note that this includes the
  * target file. To walk the contents of a directory, do not process
  * the first return e.g.
  *
  * {{{
  * val tp = new TraversePath(path)
  * tp.toIterator.drop(1)
  * }}}
  *
  * @define target entry
  *
  * @param path the path to start at (usually a directory file).
  * @param followLinks will follow symlinks if true, if false, will not.
  * @param depth of recursion.
  */
class TraverseEntryAttributes(
  val path: Path,
  val followLinks: Boolean,
  val depth: Int
)
    extends Traverse[BasicFileAttributes]
{

  def newVisitor[U](f: (BasicFileAttributes) => U) = new EntryVisitorAttributes(f)
}



object TraverseEntryAttributes
extends TraverseAttributesCompanion[TraverseEntryAttributes]
 {

  protected val emptyThing = new TraverseEntryAttributes(
    emptyPath,
    false,
    0
  )
  {
    override def foreach[U](f: BasicFileAttributes => U): Unit = {}
    override def toString: String = "empty traversable entrypath"
  }

  def apply(
    path: Path,
    followLinks: Boolean,
    depth: Int
  )
      : TraverseEntryAttributes =
  {
    new TraverseEntryAttributes(path, followLinks, depth)
  }

}//TraverseEntryAttributes
