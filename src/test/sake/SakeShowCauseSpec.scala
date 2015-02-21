package sake

import org.scalatest._



class SakeErrorReportSpec
 extends FunSpec
with Fixtures
 {

  describe("A Sake using the --showCause option"){

    it("should show messages and 'show cause' help if --showCause is off") {
        val o = vokeOptions(
          
          threadStats = true
        )
      val sake = new TestSakeWithExceptionError()
      sake.evoke(o, Route3Lunch)
    }

    it("should show a cause if --showCause is on") {
        val o = vokeOptions(
          
          showCause = true,
          threadStats = true
        )
      val sake = new TestSakeWithExceptionError()
      sake.evoke(o, Route3Lunch)
    }

    it("should show a cause and backtrace if --showCause and --backtrace are on") {
        val o = vokeOptions(
          
          showCause = true,
          backtrace = true,
          threadStats = true
        )
      val sake = new TestSakeWithExceptionError()
      sake.evoke(o, Route3Lunch)
    }
  }

}//SakeErrorReportSpec
