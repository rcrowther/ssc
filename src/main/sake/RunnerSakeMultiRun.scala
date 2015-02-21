package sake


import sake.support.parser.{CLSwitchOption, CLArgOption}



/** A sake for the runner which invokes one root task.
  *
  * This sake has a `main` method handling Sake `invoke` arguments,
  * commandline formatting, and help.
  *
  * Extra switches can be added by overriding `customSwitches` with a
  * map of [[sake.support.parser.CLSwitchOption]].
  *
  * Change the name for the default task by overriding
  * `defaultTaskRoute` (this name will show up in situations such as
  * help and debug printouts).
  *
  * This class is appropriate when only one root task is to be run
  * (note that the task can have dependancies).
  *
  * Must be used with a Runner which supplies the appropriate Java
  * properties.
  */
//TODO: Very untested!!!
trait RunnerSakeMultiRun
    extends RunnerSake
{

  /** Schema for the args the commandline can accept.
    */
  protected def customArgs: Map[String, CLArgOption]

  /** Override this value to add extra switches to the commandline.
    */
  protected def customSwitches: Map[String, CLSwitchOption]

  /** Override this value to change the appname in help.
    *
    * The default value is "sake app"
    */
  protected val appName = "sake app"



  // Entry point for sake runner.
  def main(inputArgs: Array[String]) {

    //println(s"args:")
    //inputArgs.foreach(println)

    if (inputArgs.contains("-help")) {
      val b = helpBuilder(appName, "<switches> <tasks>")

      if (!customSwitches.isEmpty) {
        addHelp( b, "Switches:", customSwitches)
      }
      if (!customArgs.isEmpty) {
        addHelp( b, "Tasks:", customArgs)
      }
      addHelp(b, "Standard options for running a sake:", CLSchema.VokeSwitchSchema)
      addHelp(b, "Standard options for the sake runner:", CLSchema.RunnerSchema)
      printHelp(b)
    }
    else {
      // Parse the switches
      val argConfig : Option[(Seq[String], Map[String, Seq[String]])] = parseAllSwitches(
        appName = "sake",
        schema = CLSchema.VokeSwitchSchema ++ customSwitches,
        args = inputArgs
      )

      if (argConfig != None) {
        val (filteredArgs, config) = argConfig.get
        // Now parse those args
        val taskArgs : Option[Seq[String]] = parseArgs(
          appName = "sake",
          schema = customArgs,
          args = filteredArgs
        )
        if (taskArgs != None) {
          val opts = CLSchema.vokeOptions(config)
          //println(s"opts: $opts")
          // Act with the default taskroute
          invoke(opts, taskArgs.get:_*)
        }
      }
    }
  }


}//RunnerSakeMultiRun
