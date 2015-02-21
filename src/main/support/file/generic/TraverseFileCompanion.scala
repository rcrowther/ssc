package sake.support.file

import java.io.File
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

/** A template for companions of the file traversable classes.
  *  
  * @define target file
  * @define elem data
  */
trait TraverseFileCompanion[+CC, +A]
{
  protected def emptyPath : Path = new File("").toPath
  protected def emptyThing : CC

  /** An empty traversable.
    */
  def empty : CC = emptyThing

  /** Creates a traversable of $target $elem in a directory.
    */
  def apply(path: Path, followLinks: Boolean, depth: Int) : Traversable[A]

  /** Creates a traversable of $target $elem in a directory.
    *
    * This traversable is recursive.
    *
    */
  def apply(path: Path, followLinks: Boolean) : Traversable[A] =
  {
    apply(path, followLinks, Integer.MAX_VALUE)
  }

  /** Creates a traversable of $target $elem in a directory.
    *
    * This traversable is recursive and will follow symlinks.
    *
    */
  def apply(path: Path) : Traversable[A] =
  {
    apply(path, true, Integer.MAX_VALUE)
  }

}//TraverseFileCompanion
