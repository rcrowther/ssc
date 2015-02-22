package sake.util.executable

import java.nio.file.Path
import sake.util.file._



/** Carries data returned by the 'find' tools.
*/
// Version is optional, as sometimes it is impractical to derive,
// and sometimes developing, and sometimes non-existant (see Generic)
class ExecuteData(
  val path: Path,
  val version: Option[Version]
)
{
  override def toString: String = {
    "ExecuteData(" + path.toString + ", " + version.toString + ')'
  }
}


object ExecuteData {
  protected val emptyThing = new ExecuteData("".toPath, None){
    //override val path: Path = throw new Exception("executable is empty - not found!")
    //override val version: Version = throw new Exception("executable is empty - not found!")
    override def toString: String = "empty executedata"
  }

  def empty : ExecuteData = emptyThing

  def apply(
    path: Path,
    version: Option[Version]
  )
      : ExecuteData =
  {
    new ExecuteData(path, version)
  }

}//ExecuteData

