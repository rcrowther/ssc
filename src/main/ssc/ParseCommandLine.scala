package ssc

import sake.util.parser.ParameteredOption


class ParseCommandLine(
  val verbose: Boolean,
  val noColor: Boolean
)
    extends sake.util.io.Trace
    with sake.util.parser.Parser
{

  private def processSwitches(
    task: String,
    schema: Map[String, ParameteredOption],
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


  /** Search for a task in input args, and split the args.
    *
    * @return tuple of (presumedSwitches, task, presumedParameters)
    */
  def parseForTask(inputArgs: Array[String])
      : Option[(Array[String], String, Array[String])] =
  {

    val idx = inputArgs.indexWhere{ arg =>
      (arg(0) != '-')
    }

    if (idx == -1) {
      traceWarning(s"no task found?")
      None
    }
    else {
      val task = inputArgs(idx)
      if(!Configuration.tasks.contains(task)) {
        traceWarning(s"task not recognised '$task'")
        None
      }
      else {
        Some((inputArgs.slice(0, idx), task, inputArgs.slice(idx + 1, inputArgs.size)))
      }
    }
  }

  /** Parse a commandline to ssc.
    *
    * The format is <options> <task>
    *
    * @param inputArgs the args themselves
    * @param defaultConfig The config. In context of ssc, the
    *  config group associated with the task.
    * @return if Some, a tuple of task and it's config, if
    *  None, commandline parsing failed.
    */
  def parse(
    inputArgs: Array[String]
  )
      : Option[(String, ConfigGroup)] =
  {
    // Parse the switches
    val rO = parseForTask(inputArgs)

    if(rO == None) None
    else {
      val (switches, task, parameters) = rO.get
      //which schema? By task.
      processSwitches(task, CLSchema.taskSwitches(task), switches)
    }

  }

}//ParseCommandLine


