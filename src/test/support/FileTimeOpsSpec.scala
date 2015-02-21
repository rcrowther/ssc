package sake.support.file

import org.scalatest._

import java.nio.file.attribute.FileTime

// Importing this imports the implicit conversions too.
// TODO: maybe this should be in file utils then?
import sake.util.file._

// TODO: This test doesnt cover a symlink to a file
class FileTimeOpsSpec
    extends FunSpec
{
val a = FileTime.fromMillis(1000L)
val b = FileTime.fromMillis(1000L)
val z = FileTime.fromMillis(2000L)

  describe("An implicit wrap of FileTime with FileTimeOps"){

      it("should compare FileTimes using >") {
        assert(z > b)
      }

      it("should compare FileTimes using <") {
        assert(b < z)
      }

      it("should compare different FileTimes using ==") {
        assert(a == b)
      }

      it("should, when wrapped, print a FileTime with Scala-type string") {
        info(new FileTimeOps(a).toString)
      }
}
}//FileTimeOpsSpec
