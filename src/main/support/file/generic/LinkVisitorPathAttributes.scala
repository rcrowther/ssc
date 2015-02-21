package sake.support.file

import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes



/** A visitor for link files.
  */
class LinkVisitorPathAttributes[U](f: ((Path, BasicFileAttributes)) => U) extends SimpleFileVisitor[Path] {

      override def visitFile(
        path: Path,
        attrs: BasicFileAttributes
      ) : FileVisitResult = try {
        // The visitor will pick up symlinks
        if(Files.isSymbolicLink(path)) f(path, attrs)
        FileVisitResult.CONTINUE
      } catch {
        case _ : Throwable => FileVisitResult.TERMINATE
      }
}
