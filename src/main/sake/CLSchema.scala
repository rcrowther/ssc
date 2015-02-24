package sake

import sake.support.parser._


object CLSchema {

  val RunnerSchema = Map[String, CLArgOption](
    "-cacheClear" -> "empty the cache",
    //"-cacheCompile" -> "compile found files, don't run",
    "-cacheList" -> "list the classes in the cache",
    "-verbose" -> "Output messages about what the application is doing",
    "-noColor" -> "Output without color codes"
  )

  val VokeSwitchSchema = Map[String, CLSwitchOption](
    "-verbose" -> "explain what is being done",
    "-dryrun" -> "run but don't execute - forces 'verbose'",
    "-showCause" -> ("show the cause of an exception (another exception). If showCause and backtrace are both true, the backtrace of the cause is shown"),
    "-backtrace" -> ("enable full backtraces from errors (default: False)"),
    "-tasks" -> "display the tasks and dependencies, then exit",
    "-where" -> ("display the tasks and dependencies matching the pattern, then exit", "", ParameterDescription.regex,  false, 1),
    "-showPrereqs" -> "display the tasks and dependencies, then exit",
    "-alwaysMultiTask" -> "all tasks are threaded (default: separate threads for user provided start tasks, but not every task)",
    "-threadStats" -> "on exit, print thread stats"
  )

  /** Returns a voke options set from a parsed option map.
    *
    * Needs a full set of option keys, or will throw an error. So
    * probably best to use output from `parseSwitches` and 
    * `parseSwitches` in [[sake.support.parser.CLParser]],
    * resolved against a full set of options.
    */
  def vokeOptions(o: Map[String, Seq[String]])
      : VokeOptions =
  {

    val whereVal = o("-where")(0)
    val where =
      if(whereVal.isEmpty) None
      else Some(whereVal)

    new VokeOptions(
      verbose = o("-verbose").isEmpty,
      dryrun = o("-dryrun").isEmpty,
      showCause = o("-showCause").isEmpty,
      backtrace = o("-backtrace").isEmpty,
      tasks = o("-tasks").isEmpty,
      where = where,
      showPrereqs = o("-showPrereqs").isEmpty,
      alwaysMultiTask = o("-alwaysMultiTask").isEmpty,
      threadStats = o("-threadStats").isEmpty
    )
  }

  /** Schema for scala standard commandline options.
    * 
    * This schema is of no functional use, as scala tools handle and
    * even filter these options in their runners. However, they are
    * available for any Runner sake, so can be added to to their help
    * file, perhaps if they would be of use to sake users.
    */
  val ScalacSchema = Map[String, CLSwitchOption](
    "-verbose" -> ("explain what is being done"),
    "-D" -> ("Pass -Dproperty=value directly to the runtime system.", "false",  new ParameterDescription("property=value"), false, 0),
    "-J" -> ("Pass <flag> directly to the runtime system.", "false", new ParameterDescription("flag"), false, 1),
    "-P" -> ("Pass an option to a plugin", "false", new ParameterDescription("plugin"), false, 1),
    "-X" -> ("Print a synopsis of advanced options."),
    "-bootclasspath" -> ("Override location of bootstrap class files.", "", ParameterDescription.path, false, 1),
    "-classpath" -> ("Specify where to find user class files.", "false", ParameterDescription.path, false, 1),
    "-d" -> ("destination for generated classfiles.", "", ParameterDescription.file, false, 1),
    "-dependencyfile" -> ("Set dependency tracking file.", "", ParameterDescription.file, false, 1),
    "-deprecation" -> ("Emit warning and location for usages of deprecated APIs."),
    "-doc-external-doc" -> ("comma-separated list of classpath_entry_path#doc_URL pairs describing external dependencies.", "", ParameterDescription.path, true, 128),
    "-encoding " -> ("Specify character encoding used by source files.", "am-br", ParameterDescription.encoding, false, 1),
    "-explaintypes" -> "Explain type errors in more detail.",
    "-extdirs" -> ("Override location of installed extensions.", "", ParameterDescription.path, false, 1),
    "-feature" -> "Emit warning and location for usages of features that should be imported explicitly.",
    "-g" -> ("Set level of generated debugging info. (none,source,line,vars,notailcalls) default:vars",  "vars", ParameterDescription.strCode, false, 1),
    "-help" -> "Print a synopsis of standard options",
    "-implicits-sound-shadowing" -> "Use a sound implicit shadowing calculation. Note: this interacts badly with usecases, so only use it if you haven't defined usecase for implicitly inherited members.",
    "-javabootclasspath" -> ("Override java boot classpath.", "", ParameterDescription.path, false, 1),
    "-javaextdirs" -> ("Override java extdirs classpath.", "", ParameterDescription.paths,  false, 1),
    "-language:<_,feature,-feature" -> ("Enable or disable language features: `_' for all, `-language:help' to list", "gb-am", ParameterDescription.lang, false, 1),
    "-no-specialization" -> "Ignore @specialize annotations.",
    "-nobootcp" -> "Do not use the boot classpath for the scala jars.",
    "-nowarn" -> "Generate no warnings.",
    "-optimise" -> "Generates faster bytecode by applying optimisations to the program",
    "-print" -> "Print program with Scala-specific features removed",
    "-sourcepath" -> ("Specify location(s) of source files.", "", ParameterDescription.paths, true, 1),
    "-target" -> ("Target platform for object files. All JVM 1.5 targets are deprecated. (jvm-1.5,jvm-1.6,jvm-1.7,jvm-1.8) default:jvm-1.6", "", ParameterDescription.version,  false, 1),
    "-toolcp" -> ("Add to the runner classpath.", "", ParameterDescription.paths, true, 64),
    "-unchecked" -> "Enable additional warnings where generated code depends on assumptions.",
    "-uniqid" -> "Uniquely tag all identifiers in debugging output.",
    "-usejavacp" -> "Utilize the java.class.path in classpath resolution.",
    "-usemanifestcp" -> "Utilize the manifest in classpath resolution.",
    "-verbose" -> "Output messages about what the compiler is doing.",
    "-version" -> "Print product version and exit."
  )
}//CLSchema
