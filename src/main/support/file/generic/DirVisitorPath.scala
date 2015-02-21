package sake.support.file

import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes



/** A visitor for directory files.
  */
class DirVisitorPath[U](f: (Path) => U)
 extends SimpleFileVisitor[Path]
 {

      override def preVisitDirectory(
        path: Path,
        attrs: BasicFileAttributes
      ) : FileVisitResult = try {
        f(path)
        FileVisitResult.CONTINUE
      } catch {
        case _ : Throwable => FileVisitResult.TERMINATE
      }
  

}//DirVisitorPath
