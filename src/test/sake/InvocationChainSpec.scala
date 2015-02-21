//package sake

import org.scalatest._

import sake.InvocationChain


class InvocationChainSpec extends FunSpec {

  val c = InvocationChain.empty

  describe("An InvocationChain"){

    it("should return size 0 when empty") {
      assert(c.size === 0)
    }

    it("should be size 1 when item is added") {
      val res = c.append("task1")
      assert(res.size === 1)
    }

    it("should print itself (two items)") {
      val res1 = c.append("task1")
      val res2 = res1.append("task2")
      info(res2.toString)
    }

  }

}//InvocationChainSpec
