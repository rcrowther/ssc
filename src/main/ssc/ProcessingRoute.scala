package ssc

import java.nio.file.Path
import script.io.file._


/** Holds route data for compiling.
  *
  * @param buildPath path to the build sub-directory for this type of source
  * @param srcPath a path to source, resolved from config
  * @param srcExtension expected file extensions of source files
  * @param srcConfig the original source config selection (useful for error output).
  */
class ProcessingRoute(
  val buildPath: Option[Path],
  val srcPath: Option[Path],
  val srcExtension: String,
  val srcConfig: Seq[String]
)
{
  override def toString()
      : String =
  {
    val b = new StringBuilder()
    b ++= "ProcessingRoutes("
    b append buildPath
    b ++= ", "
    b append srcPath
    b ++= ", "
    b ++= srcExtension
    b ++= ", "
    b append srcConfig
    b += ')'
    b.result()
  }

}//ProcessingRoute



object ProcessingRoute {

  def apply(
    buildPath: Option[Path],
    srcPath: Option[Path],
    srcExtension: String,
    srcConfig: Seq[String]
  )
      : ProcessingRoute =
  {
    new ProcessingRoute(
      buildPath,
      srcPath,
      srcExtension,
      srcConfig
    )
  }

}//ProcessingRoute
