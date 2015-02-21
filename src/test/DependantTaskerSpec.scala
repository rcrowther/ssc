import org.scalatest._

import sake._

class DependantTaskerSpec extends FunSpec {
/*
  //def printAction1(ret: MultiTypeOptionMap, param: MultiTypeMap)
/*
  def printAction1
      : Any =
  {
    println(s"printAction1! ret: $returns, params: $params")
    "headbutt the wall"
  }

  //def printAction2(ret: MultiTypeOptionMap, params: MultiTypeMap)
  def printAction2
      : Any =
  {
    println(s"printAction2! ret: $ret, params: $params")
    "chocolate bar"
  }

  //def printAction3(ret: MultiTypeOptionMap, params: MultiTypeMap)
  def printAction3
      : Any =
  {
    println(s"printAction3! ret: $ret, params: $params")
    "crisps"
  }
*/
  val Route1Morning = "morning"
  val Route2Elevensies = "elevensies"
  val Route3Lunch = "lunch"



  
  def testObj()
      : DependantTasker =
  {
    // Some Tasks
    val emptyTask = new EmptyTask(
     "dawn"
    )

    val depTask1 = new GenericTask(
      Route1Morning,
      //Seq.empty[String],
      Seq(emptyTask),
      "morning",
      "after dawn, but before the car jam..."
      //printAction1 _
)
{
   setAction({
    println(s"printAction1! returns: $returns, params: $params")
    "headbutt the wall"
})
}


/*
    val depTask2 = GenericTask(
      Route2Elevensies,
      //Seq.empty[String],
      Seq(emptyTask, depTask1),
      "elevensies",
      "in need of a walk",
      //printAction2 _
{
    println(s"printAction2! ret: $returns, params: $params")
    "chocolate bar"
}
    )


    val depTask3 = GenericTask(
      Route3Lunch,
      //Seq("provisions"),
      Seq(emptyTask, depTask1, depTask2),
      "lunch",
      "where to eat?",
      //printAction3 _
{
    println(s"printAction2! ret: $returns, params: $params")
    "crisps"
}
    )
*/
    // Construct an active DepenancyTasker
    val dt = DependantTasker()
    dt += emptyTask
    dt += depTask1
    //dt += depTask2
    //dt += depTask3
    dt
  }

  /** Create a *voke option.
    * Simplify switching using defaults
    */
  def vokeOptions(
    envDatamap: MultiTypeMap = MultiTypeMapEmpty,
    verbose: Boolean = false,
    dryRun: Boolean = false,
    backtrace: Boolean = false,
    showPrereqs: Boolean = false,
    alwaysMultiTask : Boolean = false,
    threadStats : Boolean = false
  )
      : VokeOptions =
  {
    new VokeOptions(
      envDatamap,
      verbose,
      dryRun,
      backtrace,
      showPrereqs,
      alwaysMultiTask,
      threadStats
    )
  }




  describe("A DependantTasker"){
    describe("when displaying") {
      it("should print tasks and dependancies with showPrereqs setting on evoke()") {
        val o = vokeOptions(showPrereqs = true, threadStats = true)
        val dt = testObj()
        alert("Look above for stdout!")
        println("\n\nDependantTasker showPrereqs setting (should not use threads):")
        //dt.evoke(o, Route3Lunch)
        dt.evoke(o, Route1Morning)

      }

      it("should print tasks and dependancies with dryrun setting on evoke()") {
        val o = vokeOptions(dryRun = true, threadStats = true)
        val dt = testObj()
        alert("Look above for stdout!")
        println("\n\nDependantTasker dryrun setting (should use threads, should be verbose):")
        //val res: Any = dt.evoke(o, Route3Lunch)
        val res: Any = dt.evoke(o, Route1Morning)
      }

    }


    describe("when running") {

      it("should invoke tasks individually with invoke()") {
        val o = VokeOptions.empty
        val dt = testObj()
        println("\n\nDependantTasker invokeTask():")
        //val res: Any = dt.invoke(o, Route2Elevensies)
        val res: Any = dt.invoke(o, Route1Morning)

        println("res:" + res)
        //assert("chocolate bar" === res.asInstanceOf[Some[String]].get)
      }
      /*
       it("should be invokable after a run") {
       //assert(true === dt(Route2Elevensies).needed)
       }
       */  

      it("should invoke tasks and dependancies from evoke()") {
        val o = vokeOptions(threadStats = true)
        val dt = testObj()
        println("\n\nDependantTasker run() (should use 1 thread):")
        //val res: Any = dt.evoke(o, Route3Lunch)
        val res: Any = dt.evoke(o, Route1Morning)

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
*/
    // Remove and add routes
    // test output vars
  }//A DependantTaskerSpec
*/
}//DependantTaskerSpec
