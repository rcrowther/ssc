package sake

import org.scalatest._



class SakeCircularDependancySpec
 extends FunSpec
with Fixtures
 {

  describe("A Sake when evoked with circular dependancies"){

    it("should refuse tasks which contain a circular dependancy (dependancy 'lunch' should be rejected)") {
        val o = vokeOptions(
          
          //verbose = true,
          showCause = true,
          threadStats = true
        )
      val sake = new SakeWithCircularDependancy()
      sake.evoke(o, Route3Lunch)
    }


  }

}//SakeCircularDependancySpec
