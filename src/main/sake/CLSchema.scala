package sake

import support.parser.{CLSwitchOption, CLArgOption}


object CLSchema {

  val RunnerSchema = Map[String, CLArgOption](
    "-cacheClear" -> "empty the cache",
    //"-cacheCompile" -> "compile found files, don't run",
    "-cacheList" -> "list the classes in the cache"
  )

  val VokeSwitchSchema = Map[String, CLSwitchOption](
    "-verbose" -> ("explain what is being done", "false", false, 0),
    "-dryrun" -> ("run but don't execute - forces 'verbose'", "false", false, 0),
    "-showCause" -> ("show the cause of an exception (another exception). If showCause and backtrace are both true, the backtrace of the cause is shown", "false", false, 0),
    "-backtrace" -> ("enable full backtraces from errors (default: False)", "false", false, 0),
    "-tasks" -> ("display the tasks and dependencies, then exit", "false", false, 0),
    "-where" -> ("display the tasks and dependencies matching the pattern, then exit", "", false, 1),
    "-showPrereqs" -> ("display the tasks and dependencies, then exit", "false", false, 0),
    "-alwaysMultiTask" -> ("all tasks are threaded (default: separate threads for user provided start tasks, but not every task)", "false", false, 0),
    "-threadStats" -> ("on exit, print thread stats", "false", false, 0)
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
* This schema is of no functional use, as scala tools handle and even filter these options in their runners. However, they are available for any Runner sake, so can be added to to their help file, perhaps if they would be of use to the sake users.
*/
  val ScalacSchema = Map[String, CLSwitchOption](
    "-verbose" -> ("explain what is being done", "false", false, 0),
  "-Dproperty=value" -> ("Pass -Dproperty=value directly to the runtime system.", "false", false, 0),
  "-J<flag" -> ("Pass <flag> directly to the runtime system.", "false", false, 1),
  "-P:<plugin>:<opt" -> ("Pass an option to a plugin", "false", false, 1),
  "-X" -> ("Print a synopsis of advanced options.", "false", false, 0),
  "-bootclasspath <path" -> ("Override location of bootstrap class files.", "false", false, 1),
  "-classpath <path" -> ("Specify where to find user class files.", "false", false, 1),
  "-d <directory|jar" -> ("destination for generated classfiles.", "false", false, 1),
  "-dependencyfile <file" -> ("Set dependency tracking file.", "false", false, 1),
  "-deprecation" -> ("Emit warning and location for usages of deprecated APIs.", "false", false, 0),
  "-doc-external-doc:<external-doc" -> ("comma-separated list of classpath_entry_path#doc_URL pairs describing external dependencies.", "false", true, 128),
  "-encoding <encoding" -> ("Specify character encoding used by source files.", "false", false, 1),
  "-explaintypes" -> ("Explain type errors in more detail.", "false", false, 0),
  "-extdirs <path" -> ("Override location of installed extensions.", "false", false, 1),
  "-feature" -> ("Emit warning and location for usages of features that should be imported explicitly.", "false", false, 0),
  "-g:<level" -> ("Set level of generated debugging info. (none,source,line,vars,notailcalls) default:vars", "false", false, 1),
  "-help" -> ("Print a synopsis of standard options", "false", false, 0),
  "-implicits-sound-shadowing" -> ("Use a sound implicit shadowing calculation. Note: this interacts badly with usecases, so only use it if you haven't defined usecase for implicitly inherited members.", "false", false, 0),
  "-javabootclasspath <path" -> ("Override java boot classpath.", "false", false, 1),
  "-javaextdirs <path" -> ("Override java extdirs classpath.", "false", false, 1),
  "-language:<_,feature,-feature" -> ("Enable or disable language features: `_' for all, `-language:help' to list", "false", false, 1),
  "-no-specialization" -> ("Ignore @specialize annotations.", "false", false, 0),
  "-nobootcp" -> ("Do not use the boot classpath for the scala jars.", "false", false, 0),
  "-nowarn" -> ("Generate no warnings.", "false", false, 0),
  "-optimise" -> ("Generates faster bytecode by applying optimisations to the program", "false", false, 0),
  "-print" -> ("Print program with Scala-specific features removed.", "false", false, 0),
  "-sourcepath <path" -> ("Specify location(s) of source files.", "false", false, 1),
  "-target:<target" -> ("Target platform for object files. All JVM 1.5 targets are deprecated. (jvm-1.5,jvm-1.6,jvm-1.7,jvm-1.8) default:jvm-1.6", "false", false, 1),
  "-toolcp <path" -> ("Add to the runner classpath.", "false", false, 1),
  "-unchecked" -> ("Enable additional warnings where generated code depends on assumptions.", "false", false, 0),
  "-uniqid" -> ("Uniquely tag all identifiers in debugging output.", "false", false, 0),
  "-usejavacp" -> ("Utilize the java.class.path in classpath resolution.", "false", false, 0),
  "-usemanifestcp" -> ("Utilize the manifest in classpath resolution.", "false", false, 0),
  "-verbose" -> ("Output messages about what the compiler is doing.", "false", false, 0),
  "-version" -> ("Print product version and exit.", "false", false, 0)
)
}//CLSchema
