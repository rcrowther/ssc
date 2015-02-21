package sake.support.file

import org.scalatest._

import java.io.File
import java.nio.file.Path


// TODO: This test doesnt cover a symlink to a file
class FileUtilSpec
    extends FunSpec
//with Fixtures
{

  describe("The file.support"){
    val testPath = new File("/home/rob/Code/sake/src/test/scala/support/TestFolder").toPath
/*
    describe("A TraversePath (non-symlink)") {
      it("should read from a directory recursively, including directories") {
        val t = TraversePath(testPath)
        t.foreach{p => println("TraversePath: " + p.getFileName())}
      }

      it("should be size 6 (inc. target dir, subdir, four items)") {
        val t = TraversePath(testPath)
        assert(t.size === 6)
      }
    }

    describe("A TraversePath following links") {
      it("should read from a directory recursively, including directories") {
        val t = TraversePath(testPath, true)
        t.foreach{p => println("TraversePath (followLinks): " + p.getFileName())}
      }

      it("should be size 7 (inc. three subdir dive, then four items") {
        val t = TraversePath(testPath, true)
        assert(t.size === 7)
      }

    }
*/
    describe("A TraverseEntryPath (non-symlink)") {
      it("should read from a directory recursively, only regular files") {
        val t = TraverseEntryPath(testPath)
        t.foreach{p => println("TraverseEntryPath: " +p.getFileName())}
      }

      it("should be size 3") {
        val t = TraverseEntryPath(testPath)
        assert(t.size === 3)
      }
    }

    describe("A TraverseEntryPath following links") {
      it("should read from a directory recursively, only regular files") {
        val t = TraverseEntryPath(testPath, true, Integer.MAX_VALUE)
        t.foreach{p => println("TraverseEntryPath (followLinks): " + p.getFileName())}
      }

      it("should be size 4") {
        val t = TraverseEntryPath(testPath, true, Integer.MAX_VALUE)
        assert(t.size === 4)
      }
    }
/*
    describe("A TraversePathAttributes (non-symlink)") {
      it("should read from a directory recursively, including directories") {
        val t = TraversePathAttributes(testPath)
        t.foreach{pa => println("TraversePathAttributes: " + pa._1.getFileName())}
      }

      it("should be size 6 (inc. target dir, subdir, four items)") {
        val t = TraversePathAttributes(testPath)
        assert(t.size === 6)
      }
    }

    describe("A TraversePathAttributes following links") {
      it("should read from a directory recursively, including directories") {
        val t = TraversePathAttributes(testPath, true)
        t.foreach{pa => println("TraversePathAttributes (followLinks): " + pa._1.getFileName())}
      }

      it("should be size 7 (inc. three subdir dive, then four items") {
        val t = TraversePathAttributes(testPath, true)
        assert(t.size === 7)
      }
    }
*/
   describe("A TraverseDirPath (non-symlink)") {
      it("should read from a directory recursively, only regular files") {
        val t = TraverseDirPath(testPath)
        t.foreach{p => println("TraverseDirPath: " + p.getFileName())}
      }

      it("should be size 2") {
        val t = TraverseDirPath(testPath)
        assert(t.size === 2)
      }
    }

    describe("A TraverseDirPath following links") {
      it("should read from a directory recursively, only regular files") {
        val t = TraverseDirPath(testPath, true)
        t.foreach{p => println("TraverseDirPath (followLinks): " + p.getFileName())}
      }

      it("should be size 2") {
        val t = TraverseDirPath(testPath, true)
        assert(t.size === 2)
      }
    }

    describe("A TraverseLinkPath (non-symlink)") {
      it("should read from a directory recursively, only symbolic link files") {
        val t = TraverseLinkPath(testPath)
        t.foreach{p => println("TraverseLinkPath: " + p.getFileName())}
      }

      it("should be size 1") {
        val t = TraverseLinkPath(testPath)
        assert(t.size === 1)
      }
    }

    describe("A TraverseLinkPath following links") {
      it("should read from a directory recursively, only regular files") {
        val t = TraverseLinkPath(testPath, true)
        t.foreach{p => println("TraverseLinkPath (followLinks): " + p.getFileName())}
      }

      // Poor test, with current Fixture structure
      it("should be size 1") {
        val t = TraverseLinkPath(testPath, true)
        assert(t.size === 1)
      }
    }

    describe("A TraverseAfterPath (non-symlink)") {
      it("should read from a directory recursively, including directories") {
        val t = TraverseAfterPath(testPath)
        t.foreach{p => println("TraverseAfterPath: " + p.getFileName())}
      }

      it("should be size 6") {
        val t = TraverseAfterPath(testPath)
        assert(t.size === 6)
      }
    }


  }

}//FileUtilSpec
