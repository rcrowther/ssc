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
  * @define target file
  *
  * @param path the path to start at (usually a directory file).
  * @param followLinks will follow symlinks if true, if false, will not.
  * @param depth of recursion.
  */
class TraverseAfterPath(
  val path: Path,
  val followLinks: Boolean,
  val depth: Int
)
    extends Traverse[Path]
{

  def newVisitor[U](f: (Path) => U) = new AfterVisitorPath(f)
}



object TraverseAfterPath
extends TraversePathCompanion[TraverseAfterPath]
 {

  protected val emptyThing = new TraverseAfterPath(
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
    //new TraverseAfterPath(path, followLinks, depth)
    // + 1 because the Java interface is consistent and reads only the 
    // target path file unless going down + 1.
    // take(1) because the read then includes the target file
    // (at the end, this is After traversal).
    // Protect agains adding one to Int.max...
    val d =  if(depth == Integer.MAX_VALUE) depth else (depth -1)
    val tp = new TraverseAfterPath(path, followLinks, depth)
    tp.take(tp.size - 1)
  }

}//TraverseAfterPath
