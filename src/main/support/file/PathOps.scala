package sake.support.file

import java.nio.file.Path
import java.util.NoSuchElementException


/** Wraps Paths with several Scala-like operations.
  *
  * Where needed, instances of Paths are implicitly converted into this class.
  *
  * General approach and nomenclature,
  * {{{
  *  val p: Path = "/home/sam/tax.return.xml"
  *  p.dir = "/home/sam/"
  *  p.base = "tax.return"
  *  p.entry = "tax.return.xml"
  *  p.extension = "xml"
  * }}}
  *
  * A big hand for [[http://php.net/manual/en/function.pathinfo.php `PHP pathinfo`]]
  */
//TODO: and im sure we could iterate elements, or have a proper orderied.
// lets see what is useful?
class PathOps(val v: Path) extends AnyVal
{

  /** Tests whether this path is empty
    */
  // Only reliable way I found? R.C.
  def isEmpty
      : Boolean =
  {
    (v.toString == "")
  }


  /** Retrieves a directory from the path.
    *
    * @throws NoSuchElementException
    */
  def dir
      : Path =
  {
    val p = v.getParent
    if (p == null || (p.toString == "")) throw new NoSuchElementException("path has no parent")
    else p
  }

  /** Retrieves a directory from the path.
    *
    */
  def getDir
      : Option[Path] =
  {
    val p = v.getParent
    if (p == null || (p.toString == "")) None
    else Some(p)
  }

  /** Retrieves a base (file name, no extension) from the path.
    *
    * @throws NoSuchElementException
    */
  def base
      : String =
  {
    val p = getBase
    if (p == None) throw new NoSuchElementException("path has no base")
    else p.get
  }

  /** Retrieves a base (file name, no extension) from the path.
    *
    */
  def getBase
      : Option[String] =
  {
    val p = v.getFileName()
    if (p == null) None
    else {
      val pStr = p.toString
      if(pStr.isEmpty) None
      else {
        val endPos = pStr.lastIndexOf('.')
        if (endPos != -1) Some(pStr.toString.slice(0, endPos))
        else Some(pStr)
      }
    }
  }

  /** Retrieves an entry (file name) from the path.
    *
    * @throws NoSuchElementException
    */
  def entry
      : Path =
  {
    val p = v.getFileName()
    if (p == null || (p.toString == "")) throw new NoSuchElementException("path has no entry")
    else p
  }

  /** Retrieves an entry (file name) from the path.
    *
    */
  def getEntry
      : Option[Path] =
  {
    val p = v.getFileName()
    if (p == null || (p.toString == "")) None
    else Some(p)
  }

  /** Retrieves an extension from the path.
    *
    * @throws NoSuchElementException
    */
  def extension
      : String =
  {
    val p = getExtension
    if (p == None) throw new NoSuchElementException("path has no extension")
    else p.get
  }

  /** Retrieves an extension from the path.
    *
    */
  def getExtension
      : Option[String] =
  {
    val p = v.getFileName()
    if (p == null) None
    else {
      val pStr = p.toString
      if(pStr.isEmpty) None
      else {
        val endPos = pStr.lastIndexOf('.')
        //N.B. slice() protects against exceeding starts, etc.
        if (endPos != -1) Some(pStr.toString.slice(endPos + 1, pStr.size))
        else None
      }
    }
  }

}//PathOps



