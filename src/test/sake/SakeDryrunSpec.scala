package sake

import org.scalatest._



class SakeDryrunSpec
 extends FunSpec
with Fixtures
 {

  describe("A Sake using the --dryrun option"){

    it("should invoke but not execute if --dryrun is on") {
        val o = vokeOptions(
          
          dryrun = true,
          threadStats = true
        )
      val sake = new TestSake()
      sake.invoke(o, Route3Lunch)
    }

    it("should evoke but not execute if --dryrun is on") {
        val o = vokeOptions(
          
          dryrun = true,
          threadStats = true
        )
      val sake = new TestSake()
      sake.evoke(o, Route3Lunch)
    }

  }

}//SakeDryrunSpec
