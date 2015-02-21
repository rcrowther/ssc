package sake.support.file

import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes



/** A visitor for link files.
  */
class LinkVisitorAttributes[U](f: (BasicFileAttributes) => U) extends SimpleFileVisitor[Path] {

      override def visitFile(
        path: Path,
        attrs: BasicFileAttributes
      ) : FileVisitResult = try {
        // The visitor will pick up symlinks
        if(Files.isSymbolicLink(path)) f(attrs)
        FileVisitResult.CONTINUE
      } catch {
        case _ : Throwable => FileVisitResult.TERMINATE
      }
}
