package sake.support.file

import java.io.File
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

/** A template for companions of the attributes traversable classes.
  *  
  * @define elem attributes
  */
trait TraverseAttributesCompanion[+CC]
extends TraverseFileCompanion[CC, BasicFileAttributes]
