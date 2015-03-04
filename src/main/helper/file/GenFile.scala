package sake.helper.file

import java.nio.file.Path


/** Templates a file utility object.
  *
  *
  * @define obj file
  */
trait GenFile {


  /** Deletes the $obj.
    *
    * Will delete any $obj present, but will not delete a file of
    * another type.
    *
    * @return true if a $obj is deleted by this method or does
    *  not exist, otherwise false.
    */
  def delete(path: Path) : Boolean

  /** Tests if the $obj exists.
    *
    * If you're doing something to a $obj, don't use this. Do the
    * action and see if it fails. This should be rarely used.
    *
    * @return true if a $obj exists, otherwise false (including
    *  if no file exists).
    */
  def exists(path: Path) : Boolean

  /** Copies the $obj to another directory
    *
    * Will not copy if a file exists, of any type. Silently refuses to
    * copy to the same directory. Offers no guarantees that attributes
    * are copied.
    *
    * @param from the path to find data at.
    * @param to the directory into which this object will be placed
    */ 
  def copy(from: Path, to: Path) : Boolean

  /** Moves the $obj to another directory
    *
    * Will not copy if a file exists, of any type. Silently refuses to
    * copy to the same directory. Offers no guarantees that attributes
    * are copied.
    *
    * @param from the path to find data at.
    * @param to the directory into which this object will be placed
    */ 
  def move(from: Path, to: Path) : Boolean

}//GenFile
