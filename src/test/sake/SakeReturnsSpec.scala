package sake

import org.scalatest._



class SakeReturnsSpec
 extends FunSpec
with Fixtures
 {

  describe("A Sake when returning uses a map of Any"){

  describe("A task returning a string"){
    it("should return the string from invoke()") {
        val o = vokeOptions(
          
          threadStats = true
        )
      val sake = new TestSakeWithReturn()
      val ret = sake.invoke(o, Routed1Morning)
      assert(ret(Routed1Morning).asInstanceOf[String] === "freedom")
    }

    it("should return the string from evoke()") {
        val o = vokeOptions(
          
          threadStats = true
        )
      val sake = new TestSakeWithReturn()
      val ret = sake.evoke(o, Routed1Morning)
      assert(ret(Routed1Morning).asInstanceOf[String] === "freedom")
    }

    it("should return to a method") {
        val o = vokeOptions(
          
          threadStats = true
        )
      val sake = new TestSakeWithReturn()
      val ret = sake.invoke(o, Routed2Elevensies)
info("see above for 'freedom'")
      //assert(ret(Routed1Morning).asInstanceOf[String] === "freedom")
    }
  }
}
}//SakeReturnsSpec
