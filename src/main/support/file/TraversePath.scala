package sake.support.file

import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.FileVisitOption


/** A collection of paths in a directory.
  * 
  * Visits all files recursivly. Note that this includes the
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
class TraversePath(
  val path: Path,
  val followLinks: Boolean,
  val depth: Int
)
    extends Traverse[Path]
{

  def newVisitor[U](f: (Path) => U) = new FileVisitorPath(f)
}



object TraversePath
extends TraversePathCompanion[TraversePath]
 {

  protected val emptyThing = new TraversePath(
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
      : Traversable[Path] =
  {
    // + 1 because the Java interface is consistent and reads only the 
    // target path file unless going down + 1.
    // drop(1) because the read then includes the target file.
    // Protect agains adding one to Int.max...
    val d =  if(depth == Integer.MAX_VALUE) depth else (depth -1)
    new TraversePath(path, followLinks, d).drop(1)
  }

}//TraversePath
