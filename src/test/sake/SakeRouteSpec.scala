package sake

import org.scalatest._



class SakeRouteSpec
 extends FunSpec
with Fixtures
 {

  describe("A Sake when evoked with extended routing"){
      val sake = new RoutingSake()


    it("should show routes when invoked with --showPrereqs") {
        val o = vokeOptions(
          
          showPrereqs = true,
          threadStats = true
        )

      sake.invoke(o, Routed3Lunch)
    }

    it("should invoke with a route, tracing names") {
      val o = vokeOptions(
          
          verbose = true,
          threadStats = true
        )
      sake.invoke(o, Routed3Lunch)
    }
  }

}//SakeRouteSpec
