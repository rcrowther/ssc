package sake.util.file

import sake.DependantTaskerLike

import java.nio.file.Path
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.attribute.FileAttribute
import java.nio.charset.Charset
import scala.collection.JavaConversions._

/** Operations for regular files.
  *
  *
  * These utilites are not atomic, and not to be used for secure running.
  * They assume uninterfered access.
  *
  * These utilities all throw errors freely. For the purposes of Sake,
  * this is assumed (Sake traps errors).
  *
  * The methods are placed on a static object. Usage:
  *
  * {{{ Entry.create("/home/bill/lock.java")}}}
  *
  * This representaion is preferred due to the now extensive use
  * of prefix notation in Java.
  *
  * @define obj entry
  */
object Entry
    extends GenNonSymbolicFile
{

  this: DependantTaskerLike =>

  
  //TODO: Should be an attribute version
  def create(path: Path)
      : Boolean =
  {
    if(Files.exists(path)) false
    else {
      Files.createFile(path)
      true
    }
  }

  /** Reads text from a $obj.
    * 
    * Not a good method for big $obj, but ok for utilities.
    *
    * @param charset Something like `StandardCharsets.UTF_8` is fine.
    */
  def read(path: Path, charset: Charset)
      : Traversable[String] =
  {
    Files.readAllLines(path, charset)
  }

  /** Write text to an $obj.
    * 
    * Creates a $obj if the entry file doesn't exist. Throws an
    * exception if a file exists.
    *
    * If it fails, may leave the file mangled.
    *
    * @param charset Something like `StandardCharsets.UTF_8` is fine.
    */
  def write(
    path: Path,
    charset: Charset,
    lines: Seq[String]
  )
      : Boolean =
  {
    Files.write(
      path,
      lines,
      charset,
      java.nio.file.StandardOpenOption.CREATE_NEW
    )
    true
  }

  /** Write text to an $obj.
    * 
    * Creates a $obj if the entry file doesn't exist. Throws an
    * exception if a file exists.
    *
    * If it fails, may leave the file mangled.
    *
    * The charset is assumed to be UTF8
    */
  def write(
    path: Path,
    lines: Seq[String]
  )
  {
    write(
      path,
      java.nio.charset.StandardCharsets.UTF_8,
      lines
    )
  }

  def delete(path: Path)
      : Boolean =
  {
    // Protect against dirs
    if (Files.exists(path) && !Files.isRegularFile(path)) false
    else {
      Files.deleteIfExists(path)
      !exists(path)
    }
  }


  def exists(path:Path, followLinks: Boolean): Boolean = {
    if (followLinks) {
      Files.isRegularFile(path)
    }
    else {
      Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)
    }
  }


  def exists(path:Path): Boolean = exists(path, false)

  /** Tests if a $obj exists and is executable.
    *
    * Will not follow symlinks.
    */
  def isExecutable(path:Path): Boolean = Files.isExecutable(path)


  def copy(from: Path, to: Path)
      : Boolean =
  {
    // Protect against dirs
    if (
      !Files.isRegularFile(from) ||
        !Files.isDirectory(to)
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
      !Files.isRegularFile(from) ||
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
}//Entry
