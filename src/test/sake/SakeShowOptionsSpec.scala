package sake

import org.scalatest._


/** Tests the options which display sake info.
*/
class SakeShowOptionsSpec
 extends FunSpec
with Fixtures
 {

  describe("A Sake"){

      it("should display info but not execute if --showPrereqs is on") {
        val o = vokeOptions(
          
          verbose = true,
             showPrereqs = true
        )
        val sake = new TestSake()
        sake.invoke(o, Route3Lunch)
      }

    it("should invoke but not execute if --tasks is on") {
        val o = vokeOptions(
          
          tasks = true,
          verbose = true
        )
      val sake = new TestSake()
      sake.invoke(o, Route3Lunch)
    }

    it("should invoke but not execute if --where is on, matching tasks") {
        val o = vokeOptions(
          
          where = Some("elevensies|lunch"),
          verbose = true
        )
      val sake = new TestSake()
      sake.invoke(o, Route3Lunch)
    }

  }

}//SakeShowOptionsSpec
