package sake

import org.scalatest._

class SakeEvokeSpec
 extends FunSpec
with Fixtures
 {


  describe("A Sake"){

    describe("when running") {
      it("should evoke() tasks and dependancies with 2 threads") {
        val o = vokeOptions(
          threadStats = true,
          verbose = true
        )
        val sake = new TestSake()
        alert("Look above for stdout!")
        println("\n\nDependantTasker evoke() (use threads):")
        sake.evoke(o, Route1Morning, Route3Lunch)
      }

    }

    /*
     describe("when multithread running") {
     it("should invoke tasks and dependancies on evoke()") {
     val o = vokeOptions(
    ,
     threadStats = true,
     verbose = true
     )
     val dt = testObj()
     println("\n\nDependantTasker multi-thread run() (should use threads, execute each task only once):")
     val res: Any = dt.evoke(o, Route2Elevensies, Route3Lunch, Route1Morning)
     }
     }
     // Remove and add routes
     // test output vars
     */

  }

}//SakeEvokeSpec
