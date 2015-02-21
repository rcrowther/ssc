package sake

import sake.support.parser.CLSwitchOption


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
trait RunnerSakeRun
    extends RunnerSake
{

  /** Override this value to add extra switches to the commandline.
    */
  protected val customSwitches = Map.empty[String, CLSwitchOption]

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
      val b = helpBuilder(appName, "<options>")

      if (!customSwitches.isEmpty) {
        addHelp( b, "", customSwitches)
      }

      addHelp(b, "Standard options for running a sake:", CLSchema.VokeSwitchSchema)
      addHelp(b, "Standard options for the sake runner:", CLSchema.RunnerSchema)
      printHelp(b)

    }
    else {
      // Parse the args
      val config = parseSwitches(
        appName = "sake",
        schema = CLSchema.VokeSwitchSchema ++ customSwitches,
        args = inputArgs
      )

      if (config != None) {
        val opts = CLSchema.vokeOptions(config.get)
        //println(s"opts: $opts")
        // Act with the default taskroute
        invoke(opts, defaultTaskRoute)
      }
    }
  }

}//RunnerSakeRun
