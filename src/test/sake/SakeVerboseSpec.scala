package sake

import org.scalatest._



class SakeVerboseSpec
 extends FunSpec
with Fixtures
 {

  describe("A Sake when using the --verbose option"){

    it("should show nothing if --verbose is off (N.B. threadStats is on)") {
        val o = vokeOptions(
          
          threadStats = true
        )
      val sake = new TestSakeNoOutput()
      sake.evoke(o, Route3Lunch)
    }

   it("should show a trace if --verbose is on") {
        val o = vokeOptions(
          
          verbose = true,
          threadStats = true
        )
      val sake = new TestSakeNoOutput()
      sake.evoke(o, Route3Lunch)
    }

  }

}//SakeVerboseSpec
