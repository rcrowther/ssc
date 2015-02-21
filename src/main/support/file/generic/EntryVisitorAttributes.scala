package sake.support.file

import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes



/** A visitor for entry files.
*
* Note this icludes symlinks to regular files, even if not following
* directory links.
  */
class EntryVisitorAttributes[U](f: (BasicFileAttributes) => U) extends SimpleFileVisitor[Path] {

  override def visitFile(
    path: Path,
    attrs: BasicFileAttributes
  ) : FileVisitResult = try {
    // The visitor will pick up every file
    if(Files.isRegularFile(path)) f(attrs)
    FileVisitResult.CONTINUE
  } catch {
    case _ : Throwable => FileVisitResult.TERMINATE
  }
}
