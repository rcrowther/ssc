package sake

import java.nio.file.{Paths, Path, Files}
import sake.support.parser.CLParser

/** A sake for the runner which recovers useful params.
  *
  * This is the base Sake for a classes to be used with a runner.
  * It does very little, and needs a `main` method to work without
  * errors.
  *
  * This class is appropriate when full control is needed over
  * commandline parsing and initialization. See subclasses for
  * more conveniece options.
  *
  * This must be used with a Runner which supplies the appropriate
  * Java properties.
  */
trait RunnerSake
    extends SakeLike
    with CLParser
{

  /** Path from where the application was launched.
    */
  val cwd : Path = Paths.get(".").toAbsolutePath().normalize()

  /** Path to the script invoked.
    */
  val ccd: Path = {
    val ccdStr = java.lang.System.getProperty("sake.runner.home")
    if (ccdStr == null) {
      throw new Exception("The system property 'sake.runner.home' is not available.")
    }
    else new java.io.File(ccdStr).toPath
  }

  
}//RunnerSake
