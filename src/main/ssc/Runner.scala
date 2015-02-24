package ssc


import java.nio.file.Paths
import java.nio.file.Path
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import sake.util.file._



// TODO need startup script to run without Scala
object Runner
    extends sake.Trace
    with sake.support.parser.CLParser
{
  // For starters...
  var noColor: Boolean = false
  var verbose: Boolean = false


  val cwd : Path = Paths.get(".").toAbsolutePath().normalize()

  val ccd: Path = {
    val ccdStr = java.lang.System.getProperty("ssc.runner.home")
    if (ccdStr == null) {
      throw new Exception("The system property 'ssc.runner.home' is not available.")
    }
    else new java.io.File(ccdStr).toPath
  }



  ///////////////////////////
  // Main handled switches //
  ///////////////////////////

  /** Appends to a string general help items caught by the runner.
    *
    * There is no need to pass these items to projects, so their help
    * is added away from the main switch values.
    */
  private def addMainhandledHelp(b: StringBuilder)
  {
    addHelpItem(b, "-mavenStrict", "sets default configuration to Maven conventions (/src/main/scala etc.)")
    addHelpItem(b, "-config", "output the config (the default with file-modifications")
    addHelpItem(b, "-version", "output version information")
    addHelpItem(b, "-help", "output this message")
  }


  /** Outputs help.
    * 
    * Can sub-categorise help using the taskNme.
    * 
    * @param taskName name of a task, causes help to
    *  specialise it's string output.
    */
  def printHelp(taskName: String)
  {
    val b = new StringBuilder

    if (!taskName.isEmpty) {
      val tss = CLSchema.taskSwitchSeq(taskName)
      if(tss.isEmpty) {
        addHelpUsage(b, "ssc", taskName)
      }
      else {
        addHelpUsage(b, "ssc", "<options> " + taskName)
        var first = true
        CLSchema.taskSwitchSeq(taskName).foreach{ sg =>
          if (first) {
            first = false
            addHelp(b, "Options:", sg)
            addHelpNewLine(b)
            addHelpLine(b, "Other Options:")
          }
          else {
            addHelp(b, "", sg)
          }
        }
        addMainhandledHelp(b)
      }
    }
    else {
      addHelpUsage(b, "ssc", "<options> <task>")
      addHelp(b, "Tasks:", CLSchema.tasks)
      addHelpNewLine(b)
      addHelp( b, "Basic Options:", CLSchema.appdataSwitches)
      addMainhandledHelp(b)
      addHelpNewLine(b)
      addHelpLine(b,  "Options are available for each task. Try ssc -help <task>")
    }

    printHelp(b)
  }


  /** Output the configuration of the project at the root of the tree.
    * 
    * This includes overriding build.ssc data, but no command line
    * data.
    */
  def printRootProjectConfig(inputArgs: Seq[String]) {
    val b = new StringBuilder()

    // Build a config. It's no big consumption.
    val defaultConfig =
      if(inputArgs.contains("-mavenStrict")) {
        Configuration.maven
      }
      else Configuration.default

    val projectConfig : ConfigMap = userFileEnhancedConfig(defaultConfig,  loadConfig(cwd))


    defaultConfig.foreach{ case(group, kvs) =>
      b ++= group
      b ++= System.lineSeparator()
      kvs.foreach{ case(k, v) =>
        b ++= k
        b ++= {" " * (16 - k.size)}
        b ++= "= "
        b ++= v.mkString(", ")
        b ++= System.lineSeparator()
      }
      b ++= System.lineSeparator()
    }

    trace(b.result())
  }


  /** Outputs version and license info.
    */
  def printVersion()
  {
    trace("ssc " + Version.version)
    trace(Version.licence)
    trace("\n")
    trace(Version.author)
  }



  //////////////////
  // Main helpers //
  //////////////////

  def loadConfig(launchDirectory: Path)
      : ConfigMap =
  {
    // Need to load any buildfile
    // Check for a build file
    val bfPath = launchDirectory.resolve("build.ssc")
    //println(s"bfPath $bfPath")
    //println(s"cst ${Charset.forName(Default.configuration("cplCharset")(0))}")
    // - do in a Sake?
    val configLines =
      try {
        val ret = Entry.read(bfPath, StandardCharsets.UTF_8)
        traceInfo(s"Using configuraton file found at: $bfPath")
        ret
      }
      catch {
        case e: Exception => traceVerbose(s"No configuraton file found (or readable) in: $launchDirectory\n (ssc will use default configuration)")
          Seq.empty[String]
      }

    // Parse the lines
    // Makes no difference if a file was found or not, we printed warnings
    // Doesn't verify against any data, just cleans and makes into map.
    val p = new ParseIni(configLines, verbose, noColor)
    p.parse()
  }

  /** returns a defaults config merged with user preferences
    *
    * Overwrites defaults with preferences expressed in the map of
    * user data.
    *
    * If verbose is on, prints warnings about unmatchable preferences.
    */
  def userFileEnhancedConfig(
    default: ConfigMap,
    user: ConfigMap
  )
      : ConfigMap =
  {
    if (verbose) {
      user.keys.foreach { gk =>
        if (!default.contains(gk)) {
          traceWarning(s"Ignored group in config file: value '$gk' not recognized")
        }
        else {
          user(gk).keys.foreach { k =>
            if (!default(gk).contains(k)) {
              traceWarning(s"Ignored line in config file: key value '$k' not recognized")
            }
          }
        }
      }
    }

    default.map{ case(gk, gv) =>
      val newGV =
        if (user.contains(gk)) {
          gv.map{ case(k, v) =>
            val  newV =
              if (user(gk).contains(k)) user(gk)(k)
              else v
            (k -> newV)
          }
        }
        else gv
      (gk -> newGV)
    }
  }

  /** Builds a tree of configurations.
    *
    * Each node of the tree contains an [[ssc.Project]], carrying the
    * resolved default configuration (and quick links to the path
    * location of the dependant project and projects it may itself be
    * dependant upon).
    */
  def buildProjectTree(
    defaultConfig: ConfigMap,
    cwd: Path
  )
      : Project =
  {
    val fileConfig : ConfigMap = loadConfig(cwd)
    val projectConfig = userFileEnhancedConfig(defaultConfig, fileConfig)
    val dependancies : Seq[Project]  =
      if (
        fileConfig.contains("compile") &&
          fileConfig("compile").contains("dependancies")
      )
      {
        fileConfig("compile")("dependancies").map{ pathStr =>
          buildProjectTree(defaultConfig, pathStr.toPath)
        }
      }
      else Seq.empty[Project]

    Project(
      cwd,
      projectConfig,
      dependancies
    )
  }

  def runProject(
    project: Project,
    task: String,
    clConfigured:ConfigGroup
  )
  {
    // dependancies
    project.dependancies.foreach{ dependancy =>
      runProject(
        dependancy,
        task,
        clConfigured
      )
    }
    // Make up a new task config, by overlaying one with another.
    // Both are, at this point, fully tested.
    val taskConfig = project.projectConfig(task) ++ clConfigured

    //Hi-ho, off we go
    val taskConfigSwitchless = taskConfig.map{case(k, v) => (k.drop(1) -> v)}
    val runTask = new Action(task, cwd, Config(taskConfigSwitchless))
    runTask.run()
  }




  /** Oh, you asked a question?
    */
  def main(inputArgs: Array[String]){
    val SAKE_HOME = "/home/rob/Code/sake/target/scala-2.11/ssc_2.11-0.1.0-SNAPSHOT.jar"
    val SCALA_HOME = "/home/rob/Deployed/scala-2.11.4/"
    //println(s"args:")
    //args.foreach(println)

    // Filter show options
    val mainHandledOption: Option[String] = inputArgs.find{ arg =>
      Seq("-help","-config", "-version").contains(arg)
    }

    if (mainHandledOption != None) {
      mainHandledOption.get match{
        case "-help" => {
          val last = inputArgs.last
          val task =
            if (CLSchema.tasks.contains(last)) last
            else ""
          printHelp(taskName = task)
        }
        case "-config" => printRootProjectConfig(inputArgs)
        case "-version" => printVersion()
      }

    }
    else {

      // Test for, and if necessary apply, these options,
      // ...they apply to the runner too.
      // They should be in every task config.
      verbose = inputArgs.contains("-verbose")
      noColor = inputArgs.contains("-noColor")

      val defaultConfig =
        if(inputArgs.contains("-mavenStrict")) {
          Configuration.maven
        }
        else Configuration.default

      // Build a configuration tree.
      val projectTree = buildProjectTree(
        defaultConfig,
        cwd
      )

      //println("project tree:\n" +projectTree.toStringTree())



      // Right, we got a config.
      // TODO: here, we want all the file-enhanced configs, all the way down.
      // val defaultConfigc = defaultConfig("compile")
      //println(s"defaultConfig(compile):\n $defaultConfigc")
      // Parse the input args.
      // The result is validated against the commandline options structures.
      //TODO: This would only be done once, at top, but maybe passed through.
      val clParser = new ParseCommandLine(verbose, noColor)
      val argsO: Option[(String, ConfigGroup)] = clParser.parse(inputArgs)
      //println(s"argsO:\n $argsO")

      // Conmmand line format fails always end in failure.
      //TODO: do per installation. However, its the same arg result over every base
      // default

      if (argsO != None) {
        val (task, clConfigured) = argsO.get

        runProject(
          projectTree,
          task,
          clConfigured
        )
      }

    }
  }

}//Runner

