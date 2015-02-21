package sake.util.executable

import java.nio.file.Path
import sake.util.file._

trait FindExecutable
extends sake.util.noThrow.Shell
{
  /** Returns the version of the found executable.
   */
  def version : Version

  /**
    *@param customPath user can override the executable path
    */
  def find(
    customPath: Path,
    verbose: Boolean,
    required: Boolean,
    versionRequired: Version
  )
      : ExecuteData


  def find(
    verbose: Boolean,
    required: Boolean,
    versionRequired: Version
  )
      : ExecuteData =
  {
    find(
      PathEmpty,
      verbose,
      required,
      versionRequired
    )
  }

}//FindExecutable

