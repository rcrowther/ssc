package sake.support.file

import java.nio.file.{Path, Files, SimpleFileVisitor, FileVisitOption}
import java.nio.file.attribute.BasicFileAttributes


/** A trait for a collection of paths in a directory.
  *
  */
trait Traverse[+A]
    extends Traversable[A]
{

  /** A visitor for the traversable.
    *
    * Will be tailored to requested elements.
    */
  def newVisitor[U](f: (A) => U) : SimpleFileVisitor[Path]

  /** The path this traversable works from.
    */
  def path: Path

  /** Wether to follow links when recursing through directories.
    *
    */
  def followLinks: Boolean

  /** Depth of directories to work to.
    *
    */
  def depth: Int

  // Make foreach receive a function from Path to Unit
  def foreach[U](f: (A) => U) {


    // heck. R.C.
    val o = new java.util.HashSet[FileVisitOption]()
    if (followLinks) {
      o.add(FileVisitOption.FOLLOW_LINKS)
      Files.walkFileTree(
        path,
        o,
        depth,
        newVisitor(f)
      )
    }
    else {
      Files.walkFileTree(
        path,
        o,
        depth,
        newVisitor(f)
      )
    }
  }

}//Traverse
