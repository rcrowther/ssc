package sake.helper.file

import sake.DependantTaskerLike

import java.nio.file.Path
import java.nio.file.Files


/** Operations for file system soft links.
  *
  * These utilites are not atomic, and not to be used for secure
  * running. They assume uninterfered access.
  *
  * These utilities all throw errors freely. For the purposes of Sake,
  * this is assumed (Sake traps errors).
  *
  * The methods are placed on a static object. Usage:
  *
  * {{{ Dir.create("/home/bill/lock")}}}
  *
  * This representaion is preferred due to the now extensive use
  * of prefix notation in Java.
  *
  * @define obj link
  */
object Link
    extends GenFile
{

  this: DependantTaskerLike =>


  /** Creates a $obj.
    * 
    * Will not follow symlinks. Will not overwrite existing files, of any type.
    *
    * If writing to the file, use a task. Should be rarely used.
    *
    * @return true if a $obj is created by this method, otherwise false (including if a file exists).
    */
  //TODO: Should be an attribute version
  def create(path:Path, linkPath: Path)
      : Boolean =
  {
    if(Files.exists(path)) false
    else {
      Files.createSymbolicLink(path, linkPath)
      true
    }
  }


  def delete(path: Path)
      : Boolean =
  {
    // Protect against dirs
    if (Files.exists(path) && !Files.isSymbolicLink(path)) false
    else {
      Files.deleteIfExists(path)
      !exists(path)
    }
  }


  def exists(path: Path) : Boolean = {
    Files.isSymbolicLink(path)
  }


  def copy(from: Path, to: Path) : Boolean =
  {
    // Protect against dirs
    if (
      !Files.isSymbolicLink(from) || !Files.isDirectory(to)
    )
    {
      false
    }
    else {
      val targetPath = to.resolve(from.getFileName())
      Files.copy(
        from,
        targetPath
      )
      !exists(targetPath)
    }
  }

  def move(from: Path, to: Path)
      : Boolean =
  {
    // Protect against dirs
    if (
      !Files.isSymbolicLink(from) ||
        !Files.isDirectory(to)
    )
    {
      false
    }
    else {
      val targetPath = to.resolve(from.getFileName())
      Files.move(
        from,
        targetPath
      )
      !exists(targetPath)
}
}
}//Link
