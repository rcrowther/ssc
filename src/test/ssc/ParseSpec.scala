package ssc

import org.scalatest._

//import ssc._


class ParseSpec extends FunSpec {

  val data =  Seq(
    " blurt  =   libby",
    "libFolders =   libby   , hole, jars ",
    "zonked =   bleak   , hus, "
  )

  val defaults =  Map(
    "libFolders" -> "lib"
  )

  val p =  new ParseInit( data, true, true)

  describe("A Parser"){

    it("should output a message for unknown keys") {
      p.parse(defaults.keys.toSeq)
    }

    it("should return a Map[String, Seq[String]]") {
      assert(p.parse(defaults.keys.toSeq).getClass === Map("" -> Seq("")).getClass)
    }

    it("should lack refused keys") {
      assert(p.parse(defaults.keys.toSeq).contains("blurt") === false)
    }

    it("should have one key") {
      assert(p.parse(defaults.keys.toSeq).size === 1)
    }

    it("should clean commas") {
val data = p.parse(defaults.keys.toSeq)
info(data("libFolders").toString)
      assert(data("libFolders") === Seq("libby","hole","jars"))
    }
  }

}//ParseSpec
