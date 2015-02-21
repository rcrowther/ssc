package sake.support.file

import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes



/** A visitor for files, visiting directories after their contents.
  */
class AfterVisitorPath[U](f: (Path) => U) extends SimpleFileVisitor[Path] {

      override def postVisitDirectory(
        path: Path,
        ex:  java.io.IOException 
      ) : FileVisitResult =
      {
        // if not null, something went wrong with deletion
        // and for all our purpose, we may as well quit.
        if (ex != null) FileVisitResult.TERMINATE
        else {
          try {
            f(path)
            FileVisitResult.CONTINUE
          } catch {
            case _ : Throwable => FileVisitResult.TERMINATE
          }
        }
      }

      override def visitFile(
        path: Path,
        attrs: BasicFileAttributes
      ) : FileVisitResult = try {
        f(path)
        FileVisitResult.CONTINUE
      } catch {
        case _ : Throwable => FileVisitResult.TERMINATE
      }

}//DirVisitor
