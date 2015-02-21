package sake.support

import java.nio.file.Path
import java.nio.file.attribute.FileTime



/** Handlers for files.
  *
  * == Intro ==
  * Interesting items include,
  *
  *  - [[sake.support.file.PathOps]] - very useful, takes a Path and returns the entry (fileName), entry extensions, etc.
  *  - [[sake.support.file.FileTimeOps]] - wraps [[java.nio.file.attribute.FileTime]] to make it ordered i.e. will work with '<', '==' etc.
  *  - [[sake.support.file.StringOps]] - very simple, but reduces much code - wraps a String so it has a `toPath` method.
  *
  * Also included are a set of classes which return a traversable of
  * paths and/or attributes from a given directory.
  */
package object file {



}//support
