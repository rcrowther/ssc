package sake.support.file

import org.scalatest._

import java.io.File
import java.nio.file.Path

// Importing this imports the implicit conversions too.
// TODO: maybe this should be in file utils then?
import sake.util.file._

// TODO: This test doesnt cover a symlink to a file
class PathOpsSpec
    extends FunSpec
{


  describe("A PathOps"){
  val pathEmpty = new File("").toPath
  val pathOneElement = new File("scrooge").toPath
  val pathWithFile = new File("/home/sam/Documents/Office/tax.return.xml").toPath
  val pathWithoutFile = new File("/home/sam/Documents/Office/").toPath

    it("should test an empty path as empty") {
info(pathEmpty.toString)
      assert(pathEmpty.isEmpty)
    }

    it("should test a populated path as not empty") {
      assert(!pathOneElement.isEmpty)
    }

    it("should find the directory") {
      assert(pathWithFile.dir.toString ==  "/home/sam/Documents/Office")
    }


    it("should find the base") {
      assert(pathWithFile.base.toString ==  "tax.return")
    }

    it("should fail to find the base if none") {
      assert(pathEmpty.getBase == None)
    }

    it("should find the entry") {
      assert(pathWithFile.entry.toString ==  "tax.return.xml")
    }


    it("should find the extension") {
      assert(pathWithFile.extension.toString ==  "xml")
    }


  }//Describe

}//PathOpsSpec
