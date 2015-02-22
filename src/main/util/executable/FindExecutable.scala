package sake.util.executable

import java.nio.file.Path
import sake.util.file._
import java.lang.System


trait FindExecutable
    extends sake.util.noThrow.Shell
{
  /** Returns the version of the found executable.
    */
  def version : Option[Version]

  /**
    *@param customPath user can override the executable path
    *@param verbose if true called tools are made verbose
    *@param required
    *@param versionRequired
    */
  def find(
    customPath: Path,
    verbose: Boolean,
    required: Boolean,
    versionRequired: Version
  )
      : Option[ExecuteData]


  def find(
    verbose: Boolean,
    required: Boolean,
    versionRequired: Version
  )
      : Option[ExecuteData] =
  {
    find(
      PathEmpty,
      verbose,
      required,
      versionRequired
    )
  }

  /** Tells if the system is a Windows OS
    */
  // Something more sophisticated sometime, maybe,
  // or maybe this will do?
  def isWindows
      : Boolean =
  {
    System.getProperty("os.name").startsWith("Windows")
  }

}//FindExecutable



object FindExecutable {

  def apply(appName: String)
      : Generic =
  {
    new Generic(appName)
  }

}//FindExecutable
