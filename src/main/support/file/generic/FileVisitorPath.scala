package sake.support.file

import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes



/** A visitor for entry files.
*
* Note this icludes symlinks to regular files, even if not following
* directory links.
  */
class FileVisitorPath[U](f: (Path) => U) extends SimpleFileVisitor[Path] {

      override def preVisitDirectory(
        path: Path,
        attrs: BasicFileAttributes
      ) : FileVisitResult = try {
        f(path)
        FileVisitResult.CONTINUE
      } catch {
        case _ : Throwable => FileVisitResult.TERMINATE
      }

  override def visitFile(
    path: Path,
    attrs: BasicFileAttributes
  ) : FileVisitResult = try {
    // The visitor will pick up every file
    f(path)
    FileVisitResult.CONTINUE
  } catch {
    case _ : Throwable => FileVisitResult.TERMINATE
  }
}
