package sake


trait Fixtures {

  /** Create a *voke option.
    * Simplify switching using defaults
    */
  def vokeOptions(
    verbose: Boolean = false,
    dryrun: Boolean = false,
    showCause: Boolean = false,
    backtrace: Boolean = false,
    tasks: Boolean = false,
    where: Option[String] = None,
    showPrereqs: Boolean = false,
    alwaysMultiTask : Boolean = false,
    threadStats : Boolean = false
  )
      : VokeOptions =
  {
    new VokeOptions(
      verbose,
      dryrun,
      showCause,
      backtrace,
      tasks,
      where,
      showPrereqs,
      alwaysMultiTask,
      threadStats
    )
  }


  val Route1Morning = "morning"
  val Route2Elevensies = "elevensies"
  val Route3Lunch = "lunch"

/** Fully loaded sake with routes, descriptions, dependancies and actions.
*/
  class TestSake
      extends Sake
  {
    task(Route1Morning, "Something to do in the morning") {
      println(s"printAction1Morning! returns: $returns")
      "headbutt the wall"
    }

    task(Route2Elevensies, "Job that must be done at elevensies", after(Route1Morning)) {
      println(s"printAction2Elevensies! returns: $returns")
      "in need of a walk"
    }

    task(Route3Lunch, "Necessary action at lunch", after(Route1Morning, Route2Elevensies))  {
      println(s"printAction3Lunch! returns: $returns")
      "crisps"
    }
    // task(taskName: String, dependencies: Seq[Task])(block: => Any)
  }

/** Sake with routes, descriptions, dependancies but no actions.
*/
  class TestSakeNoOutput
      extends Sake
  {
    task(Route1Morning, "Something to do in the morning") {
    }

    task(Route2Elevensies, "Job that must be done at elevensies", after(Route1Morning)) {
    }

    task(Route3Lunch, "Necessary action at lunch",  after(Route1Morning, Route2Elevensies))  {
    }
  }

  class TestSakeNoOutputMultiThreadedTask
      extends Sake
  {
    task(Route1Morning) {
    }

    task(Route2Elevensies, after(Route1Morning)) {
    }

    task(Route3Lunch, true, after(Route1Morning, Route2Elevensies))  {
    }
  }

  class TestSakeWithExceptionError
      extends Sake
  {
    task(Route1Morning) {
    }

    task(Route2Elevensies, after(Route1Morning)) {
      throw new Exception("** Vile and deliberate error from testing")
      val err = 1/0
    }

    task(Route3Lunch, after(Route2Elevensies))  {
    }
  }

  // Task2 refers back to Task3
  class SakeWithCircularDependancy
      extends Sake
  {
    task(Route1Morning) {
    }

    task(Route2Elevensies, after(Route3Lunch)) {
    }

    task(Route3Lunch, after(Route1Morning, Route2Elevensies))  {
    }
    // task(taskName: String, dependencies: Seq[Task])(block: => Any)
  }

  val Routed1Morning = "sunny:morning"
  val Routed2Elevensies = "sunny:elevensies"
  val Routed3Lunch = "gloomy:lunch"

/** Sake with extended routes and simple returns from actions.
*/
  class TestSakeWithReturn
      extends Sake
  {
    task( Routed1Morning, "Something to do in the morning") {
     "freedom"
    }

    task(Routed2Elevensies, after(Routed1Morning)) {
     println("ret: " + returns(Routed1Morning))
    }
  }

  // A Sake with some extended routing
  class RoutingSake
      extends Sake
  {
    task(Routed1Morning) {
    }

    task(Routed2Elevensies, after(Routed1Morning)) {
    }

    task(Routed3Lunch, after(Routed1Morning, Routed2Elevensies))  {
    }
    // task(taskName: String, dependencies: Seq[Task])(block: => Any)
  }

}//Fixtures
