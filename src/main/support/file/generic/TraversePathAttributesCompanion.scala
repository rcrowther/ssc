package sake.support.file

import java.io.File
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

/** A template for companions of the path and attribute traversable classes.
  *
  * @define elem paths and attributes
  */
trait TraversePathAttributesCompanion[CC]
extends TraverseFileCompanion[CC, (Path, BasicFileAttributes)]

