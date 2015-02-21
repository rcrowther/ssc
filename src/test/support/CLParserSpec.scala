package sake.support.parser

import org.scalatest._

import java.io.File
import java.nio.file.Path


// TODO: This test doesnt cover a symlink to a file
class CLParserSpec
    extends FunSpec
    with Fixtures
{

  describe("A CLParser"){

    class TestCLParser
        extends CLParser
        with sake.Trace
    {
      protected def inColor: Boolean = true
      protected def verbose: Boolean = true
    }

    it("should print help from an args schema") {
      val schema = CLArgsSchema
      val testParser = new TestCLParser
      testParser.printHelp("testapp", "<switch options>", schema)
    }

    it("should print help from a switch schema") {
      val schema = CLSwitchSchema
      val testParser = new TestCLParser
      testParser.printHelp("testapp", "<switch options>", schema)
    }

    describe("Args parsing"){
      it("should return Some(Seq.empty) with no args") {
        val testParser = new TestCLParser

        val res = testParser.parseArgs(
          appName = "testapp",
          schema = CLArgsSchema,
          args= Seq.empty[String]
        )
        assert(res.get.isEmpty === true)
      }

      it("should read switches from command line args") {
        val testParser = new TestCLParser

        val res = testParser.parseArgs(
          appName = "testapp",
          schema = CLArgsSchema,
          args = CLArgs
        )

        assert((res.get == CLArgs) === true)
      }

      it("should return None with warnings from unrecognized args") {
        val testParser = new TestCLParser

        val res = testParser.parseArgs(
          appName = "testapp",
          schema = CLArgsSchema,
          args= CLArgsUnrecognizedArg
        )
        assert(res === None)
      }
    }



    describe("Switch parsing"){

    describe("parseSwitches()"){
      it("should default with no args") {
        val clArgs = Seq.empty[String]
        val testParser = new TestCLParser

        val res = testParser.parseSwitches(
          appName = "testapp",
          schema = CLSwitchSchema,
          args = clArgs
        )
        //info("res" + res)
        val defaultAsMap = CLSwitchSchema.map{case(name, data) => (name, data.default)}
        //info("defaultAsMap " + defaultAsMap)
        assert((res.get == defaultAsMap) === true)
      }

      it("should read switches from command line args") {
        val testParser = new TestCLParser

        val res = testParser.parseSwitches(
          appName = "testapp",
          schema = CLSwitchSchema,
          args= CLSwitchesOk
        )
val goodArgs = Map(
"-featureWarnings" -> List(),
"-srcDir" -> List("/someplace", "/home/anyplace"),
"-docTitle" -> List("testTitle")
)
        assert(res.get === goodArgs)
      }

      it("should return None with warnings from unrecognized switches") {
        val testParser = new TestCLParser

        val res = testParser.parseSwitches(
          appName = "testapp",
          schema = CLSwitchSchema,
          args= CLSwitchBadSwitch
        )
        assert(res === None)
      }

      it("should return None with warnings from wrong number of switch arguments") {
        val testParser = new TestCLParser

        val res = testParser.parseSwitches(
          appName = "testapp",
          schema = CLSwitchSchema,
          args= CLSwitchBadArgCount
        )
        assert(res === None)
      }
}

    describe("parseAllSwitches()") {
      it("should default with no args") {
        val clArgs = Seq.empty[String]
        val testParser = new TestCLParser

        val res = testParser.parseAllSwitches(
          appName = "testapp",
          schema = CLSwitchSchema,
          args = clArgs
        )

        val defaultAsMap = CLSwitchSchema.map{case(name, data) => (name, data.default)}
        assert(res.get._2 === defaultAsMap)
      }

      it("should return valid switches") {
        val testParser = new TestCLParser

        val res = testParser.parseAllSwitches(
          appName = "testapp",
          schema = CLSwitchSchema,
          args= CLSwitchesExcessArgs
        )

val goodArgs = Map(
"-featureWarnings" -> List(),
 "-srcDir" -> List("/someplace", "/home/anyplace"),
 "-docTitle" -> List("testTitle")
)
        assert(res.get._2 === goodArgs)
      }

      it("should return excess args") {
        val testParser = new TestCLParser

        val res = testParser.parseAllSwitches(
          appName = "testapp",
          schema = CLSwitchSchema,
          args= CLSwitchesExcessArgs
        )
        assert(res.get._1 === Seq("-sub", "more", "-super", "more"))
      }
}
    }
  }

}//CLParserSpec
