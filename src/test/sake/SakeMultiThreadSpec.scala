package sake

import org.scalatest._

class SakeMultiThreadSpec
    extends FunSpec
    with Fixtures
{

  describe("when running a task with multitask option") {
    it("should invoke() tasks and dependancies with 3 threads") {
      val o = vokeOptions(
        alwaysMultiTask = true
      )
      val sake = new TestSakeNoOutput()
      sake.invoke(o, Route3Lunch)
      assert(sake.getThreadsUsed === 3)
    }

    it("should evoke() tasks and dependancies with 4 threads") {
      val o = vokeOptions(
        alwaysMultiTask = true
      )
      val sake = new TestSakeNoOutput()
      sake.evoke(o, Route3Lunch)
      assert(sake.getThreadsUsed === 4)
    }

  }

  describe("when running a task with dependancies with a multitasked task") {
    it("should invoke() tasks and dependancies with 2 threads") {
      val o = vokeOptions()
      val sake = new TestSakeNoOutputMultiThreadedTask()
      sake.invoke(o, Route3Lunch)
      assert(sake.getThreadsUsed === 2)
    }



    it("should evoke() tasks and dependancies with 3 threads") {
      val o = vokeOptions()
      val sake = new TestSakeNoOutputMultiThreadedTask()
      sake.evoke(o, Route3Lunch)
      assert(sake.getThreadsUsed === 3)
    }
  }


  describe("when running a task with dependancies --multitasked option and a multitasked task") {
    it("should invoke() tasks and dependancies with 3 threads") {
      val o = vokeOptions(
        alwaysMultiTask = true
      )
      val sake = new TestSakeNoOutputMultiThreadedTask()
      sake.invoke(o, Route3Lunch)
      assert(sake.getThreadsUsed === 3)
    }


    it("should evoke() tasks and dependancies with 4 threads") {
      val o = vokeOptions(
        alwaysMultiTask = true
      )
      val sake = new TestSakeNoOutputMultiThreadedTask()
      sake.evoke(o, Route3Lunch)
      assert(sake.getThreadsUsed === 4)
    }

  }

}//SakeMultiThreadSpec
