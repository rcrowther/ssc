package sake.util.executable

import java.nio.file.Path
import sake.util.file._


class ExecuteData(
  val path: Path,
  val version: Version
)
{
  override def toString: String = {
    "ExecuteData(" + path.toString + ", " + version.toString + ')'
  }
}


object ExecuteData {
  protected val emptyThing = new ExecuteData("".toPath, Version.empty){
    //override val path: Path = throw new Exception("executable is empty - not found!")
    //override val version: Version = throw new Exception("executable is empty - not found!")
    override def toString: String = "empty executedata"
  }

  def empty : ExecuteData = emptyThing

  def apply(
    path: Path,
    version: Version
  )
      : ExecuteData =
  {
    new ExecuteData(path, version)
  }

}//ExecuteData

