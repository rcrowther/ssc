package sake.util.executable

import java.nio.file.{Path, Files}
import sake.util.file._
import sake.support.file.TraverseEntryPath

/** A generic executable finder.
  */
// Not ssorted for Windows at all.
class Generic(appName: String)
    extends FindExecutable
{

  def version : Option[Version] = throw new Exception("Generic can not supply version data")


  def find(
    customPath: Path,
    verbose: Boolean,
    required: Boolean,
    versionRequired: Version
  )
      : Option[ExecuteData] =
  {

    val pO: Option[Path] =
      if(!customPath.isEmpty) Some(customPath)
      else {

        if(isWindows) {
          throw new Exception("Windows not currently supported!")
        }
        else {
          // Try pre-installs
          val res = TraverseEntryPath("/bin".toPath, false, 1).find(_.getFileName.startsWith(appName))
          if (res != None) res
          else {
            //Ok, preinstalls failed. How about post-installs?
            TraverseEntryPath("/usr/bin".toPath, false, 1).find(_.getFileName.startsWith(appName))
          }
        }
      }

    if (pO != None && Files.isExecutable(pO.get)) {
      Some(ExecuteData(pO.get, None))
    }
    else None

  }

}//Generic
