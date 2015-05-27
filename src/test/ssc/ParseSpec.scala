package ssc

import org.scalatest._

//import ssc._


class ParseSpec extends FunSpec {

  val data =  Seq(
    "[hooter]",
    " blurt  =   libby",
    "libFolders =   libby   , hole, jars ",
    "zonked =   bleak   , hus, ",
    "[wig]",
    " orientation  =   out"
  )

  val malformedLine =  Seq(
    "[hooter]",
    " blurt  =   libby = prestigious"
  )

  val commentLine =  Seq(
    "#[hooter]",
    "[wig]"
  )

  val defaults =  Map(
    "libFolders" -> "lib"
  )



  describe("A Parser"){

    it("should output a message for unknown keys") {
      val p =  new ParseIni(malformedLine, true, true)

      p.parse()
    }

    it("should be size 2") {
      val p =  new ParseIni(data, true, true)
      assert(p.parse().size === 2)
    }

    it("should contain keyvals") {
      val p =  new ParseIni(data, true, true)
      assert( p.parse()("wig").contains("-orientation") )
    }

    it("should skip comments") {

      val p =  new ParseIni(commentLine, true, true)
      assert(p.parse().size === 1)
    }

    it("should clean commas") {
      val p =  new ParseIni(data, true, true)
      //info(data("hooter")("libFolders").toString)
      assert(p.parse()("hooter")("-libFolders") === Seq("libby","hole","jars"))
    }
  }

}//ParseSpec
