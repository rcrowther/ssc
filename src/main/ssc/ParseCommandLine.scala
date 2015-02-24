package ssc

import sake.support.parser.CLSwitchOption


class ParseCommandLine(val verbose: Boolean, val noColor: Boolean)
    extends sake.Trace
    with sake.support.parser.CLParser
{


  private def processSwitches(
    task: String,
    schema: Map[String, CLSwitchOption],
    inputArgs: Array[String]
  )
      : Option[(String, ConfigGroup)] =
  {
    val argConfig : Option[Map[String, Seq[String]]] = parseSwitchesMinimal(
      "ssc",
      schema,
      inputArgs
    )
    if (argConfig == None) None
    else Some((task, argConfig.get))
  }

  /** Parse a commandline to ssc.
    *
    * The format is <options> <task>
    *
    * @param inputArgs the args themselves
    * @param defaultConfig The config. In context of ssc, the config group associated with the task.
    * @return if Some, a tuple of task and it's config, if None, commandline parsing failed.
    */
  def parse(
    inputArgs: Array[String]
  )
      : Option[(String, ConfigGroup)] =
  {
    // Parse the switches


    val task = inputArgs.last
    val switches = inputArgs.take(inputArgs.size - 1)

    if(!CLSchema.tasks.contains(task)) {
      traceWarning(s"task not recognised '$task'")
      None
    }
    else {
      //which schema? By task.
      processSwitches(task, CLSchema.taskSwitches(task), switches)
    }
  }

}//ParseCommandLine
