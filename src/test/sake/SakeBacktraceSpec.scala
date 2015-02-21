package sake

import org.scalatest._



class SakeBacktraceSpec
 extends FunSpec
with Fixtures
 {

  describe("A Sake when using the --backtrace option"){

    it("should show messages and 'show backtrace' help if --backtrace is off") {
        val o = vokeOptions(
          
          threadStats = true
        )
      val sake = new TestSakeWithExceptionError()
      sake.evoke(o, Route3Lunch)
    }
 
   it("should show message and a backtrace if --backtrace is on") {
        val o = vokeOptions(
          
          backtrace = true,
          threadStats = true
        )
      val sake = new TestSakeWithExceptionError()
      sake.evoke(o, Route3Lunch)
    }

  }

}//SakeBacktraceSpec
