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
  * The factory objects make this adjustment and process contents
  * only.
  *
  * @define target directory
  *
  * @param path the path to start at (usually a directory file).
  * @param followLinks will follow symlinks if true, if false, will not.
  * @param depth of recursion.
  */
class TraverseDirAttributes(
  val path: Path,
  val followLinks: Boolean,
  val depth: Int
)
    extends Traverse[BasicFileAttributes]
{

  def newVisitor[U](f: (BasicFileAttributes) => U) = new DirVisitorAttributes(f)
}



object TraverseDirAttributes
extends TraverseAttributesCompanion[TraverseDirAttributes]
 {

  protected val emptyThing = new TraverseDirAttributes(
    emptyPath,
    false,
    0
  )
  {
    override def foreach[U](f: (BasicFileAttributes) => U): Unit = {}
    override def toString: String = "empty traversable entrypath"
  }

  def apply(
    path: Path,
    followLinks: Boolean,
    depth: Int
  )
      : Traversable[BasicFileAttributes] =
  {
    // + 1 beccause the Java interface is consistent and reads only the 
    // target path file unless going down + 1.
    // drop(1) because the read then includes the target file.
    new TraverseDirAttributes(path, followLinks, depth + 1).drop(1)
  }

}//TraverseDirAttributes
