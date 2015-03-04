package sake.helper.file

import sake.DependantTaskerLike

import java.nio.file.Path
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.BasicFileAttributes
import java.nio.charset.Charset
import sake.support.file._



/** Operations for (non-directory) files.
  *
  * These utilites are not atomic, and not to be used for secure
  * running. They assume uninterfered access.
  *
  * These utilities throw errors freely. For the purposes of Sake,
  * this is assumed (Sake traps errors).
  *
  * The methods are placed on a static object. Usage:
  *
  * {{{ Dir.create("/home/bill/lock")}}}
  *
  * This representaion is preferred due to the now extensive use of
  * prefix notation in Java.
  *
  * @define obj directory
  */
object Dir
    extends GenNonSymbolicFile
{

  this: DependantTaskerLike =>



  //TODO: Should be an attribute version
  def create(path: Path)
      : Boolean =
  {
    if (Files.exists(path)) false
    else {
      Files.createDirectories(path)
      true
    }
  }

  /** Reads regular paths from a $obj.
    * 
    * Reads every path, recursively.
    *
    * @param charset Something like `StandardCharsets.UTF_8` is fine.
    */
  def read(path: Path)
      : Traversable[Path] =
  {
    // Protect against files
    if (!Files.isDirectory(path)) TraverseEntryPath.empty
    else {
      TraverseEntryPath(path)
    }
  }

  /** Reads regular file paths from a $obj.
    * 
    * Reads following symlinks.
    *
    */
  def readEntry(path: Path, depth: Int)
      : TraverseEntryPath =
  {

    // Protect against files
    if (!Files.isDirectory(path)) TraverseEntryPath.empty
    else {
      TraverseEntryPath(path, true, depth)
    }
  }

  /** Reads regular file paths from a $obj.
    * 
    * Reads recursively, following symlinks.
    *
    */
  def readEntry(path: Path)
      : TraverseEntryPath =
  {
    readEntry(path, Integer.MAX_VALUE)
  }

  def readEntryAndAttributes(path: Path, depth: Int)
      : Traversable[(Path, BasicFileAttributes)] =
  {

    // Protect against files
    if (!Files.isDirectory(path)) TraverseEntryPathAttributes.empty
    else {
      TraverseEntryPathAttributes(path, true, depth)
    }
  }

  /** Reads regular file paths and attributes from a $obj.
    * 
    * Reads every path, recursively, following symlinks.
    *
    */
  def readEntryAndAttributes(path: Path)
      : Traversable[(Path, BasicFileAttributes)] =
  {
    // Protect against files
    if (!Files.isDirectory(path)) TraverseEntryPathAttributes.empty
    else {
      TraverseEntryPathAttributes(path)
    }
  }

  /** Reads symlink file paths from a $obj.
    * 
    * Reads recursively, following symlinks.
    *
    */
  def readLinks(path: Path, depth:Int)
      : Traversable[Path] =
  {

    // Protect against files
    if (!Files.isDirectory(path)) TraverseLinkPath.empty
    else {
      TraverseLinkPath(path, true, depth)
    }
  }

  /** Reads symlink file paths from a $obj.
    * 
    * Reads recursively, following symlinks.
    *
    */
  def readLinks(path: Path)
      : Traversable[Path] =
  {

    // Protect against files
    if (!Files.isDirectory(path)) TraverseLinkPath.empty
    else {
      TraverseLinkPath(path)
    }
  }

  /** Reads directory file paths from a $obj.
    * 
    * Reads recursively, following symlinks.
    *
    */
  def readDirs(path: Path, depth: Int)
      : Traversable[Path] =
  {

    // Protect against files
    if (!Files.isDirectory(path)) TraverseDirPath.empty
    else {
      TraverseDirPath(path, true, depth)
    }
  }

  /** Reads directory file paths from a $obj.
    * 
    * Reads recursively, following symlinks.
    *
    */
  def readDirs(path: Path)
      : Traversable[Path] =
  {

    // Protect against files
    if (!Files.isDirectory(path)) TraverseDirPath.empty
    else {
      TraverseDirPath(path)
    }
  }

  // works
  // TODO: Dir.clear
  def delete(path: Path)
      : Boolean =
  {

    // Protect against files
    if (Files.exists(path) && !Files.isDirectory(path)) false
    else {
      if (Files.exists(path) ) {
        new TraverseAfterPath(path, true, Integer.MAX_VALUE).foreach { p =>
          Files.delete(p)
        }
      }
      !exists(path)
    }
  }

  def clear(path: Path)
      : Boolean =
  {

    // Protect against files
    if (Files.exists(path) && !Files.isDirectory(path)) false
    else {
      if (Files.exists(path) ) {
        TraverseAfterPath(path).foreach { p =>
          Files.delete(p)
        }
      }
      true
    }
  }



  def exists(path:Path, followLinks: Boolean): Boolean = {
    if (followLinks) {
      Files.isDirectory(path)
    }
    else {
      Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)
    }
  }


  def exists(path:Path): Boolean = exists(path, false)



  private class CopyRec(
    val fromBase: Path,
    val toBase: Path
  )
  {

    val prune = fromBase.getNameCount() - 1
    //println(s"prune $prune")
    def run()
    {
      //println(s"cp.run working from:")
      //new TraversePath(fromBase, true, Integer.MAX_VALUE).foreach(println)
      new TraversePath(fromBase, true, Integer.MAX_VALUE).foreach{ from =>
        //println(s"from $from")

        val targetStub = from.subpath(prune, from.getNameCount())
        //println(s"targetStub $targetStub")
        val targetPath = toBase.resolve(targetStub)
        ///println(s"targetPath $targetPath")
        // make the new whatever
        Files.copy(
          from,
          targetPath,
          java.nio.file.StandardCopyOption.COPY_ATTRIBUTES
        )

      }
    }
  }


  // Bollocks. Java.
  def copy(from: Path, to: Path) : Boolean =
  {
    // Protect against dirs
    if (
      !Files.isDirectory(from) ||
        !Files.isDirectory(to)
    )
    {
      false
    }
    else {
      val cp = new CopyRec(from, to)
      cp.run()
      //TODO: Rubbish test...
      !exists(to)
    }
  }

  def move(from: Path, to: Path) : Boolean =
  {
    if (copy(from, to))
    {
      delete(from)
      //TODO: Rubbish test...
      !exists(to)
    }
    else false
  }

}//Dir
