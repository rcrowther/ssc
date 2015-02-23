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

  val p =  new ParseIni( data, true, true)

  describe("A Parser"){

    it("should output a message for unknown keys") {
      p.parse()
    }

    it("should return a  Map[String, Map[String, Seq[String]]] (ConfigMap)") {
      assert(p.parse().getClass === ConfigMap("" -> Map("" -> Seq(""))).getClass)
    }

    it("should lack refused keys") {
      assert(p.parse().contains("blurt") === false)
    }

    it("should have one key") {
      assert(p.parse().size === 1)
    }

    it("should clean commas") {
val data = p.parse()
info(data("libFolders").toString)
      assert(data("libFolders") === Seq("libby","hole","jars"))
    }
  }

}//ParseSpec
