package sake.support.file

import java.io.File
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

/** A template for companions of the path traversable classes.
  *  
  * @define elem paths
  */
trait TraversePathCompanion[+CC]
extends TraverseFileCompanion[CC, Path]

