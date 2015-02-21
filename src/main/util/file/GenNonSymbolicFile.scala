package sake.util.file

import java.nio.file.Path


/** Templates a non-symbolic file utility object.
  *
  */
trait GenNonSymbolicFile
    extends GenFile
{


  /** Creates a $obj.
    * 
    * Will not follow symlinks. Will not overwrite existing files, of
    * any type.
    *
    * If writing to the $obj, use a task. This method is rarely
    * useful.
    *
    * @return true if a $obj is created by this method, otherwise
    *  false (including if a file exists).
    */
  //TODO: Should be an attribute version
  def create(path:Path) : Boolean

  /** Tests if the $obj exists.
    *
    * If you're doing something to a $obj, don't use this. Do the
    * action and see if it fails. This method is rarely useful.
    *
    * @return true if a $obj exists, otherwise false (including
    *  if no file exists).
    */
  def exists(path:Path, followLinks: Boolean): Boolean

}//GenNonSymbolicFile
