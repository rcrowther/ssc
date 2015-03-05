package ssc


import java.nio.file.Paths
import java.nio.file.Path
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import sake.helper.file._
import sake.util.parser.HelpBuilder


// TODO need startup script to run without Scala
object Runner
    extends sake.util.io.Trace
{

  // For starters...
  var noColor: Boolean = false
  var verbose: Boolean = false


  val cwd : Path = Paths.get(".").toAbsolutePath().normalize()

  // This is only usable if isJDK is also true
  val javaDistPath: Path = {
    val v = java.lang.System.getProperty("ssc.java.home")
    //println(s"javaDistPath $v")
    if (v == null) {
      throw new Exception("The system property 'ssc.java.home' is not available.")
    }
    else v.toPath
  }

  val isJDK: Boolean = {
    val v = java.lang.System.getProperty("ssc.java.isjdk")
    //println(s"isJDK $v")
    if (v == null) {
      throw new Exception("The system property 'ssc.java.isjdk' is not available.")
    }
    else (v == "true")
  }

  val isInstalled: Boolean = {
    val v = java.lang.System.getProperty("ssc.java.installed")
    //println(s"isInstalled $v")
    if (v == null) {
      throw new Exception("The system property 'ssc.java.installed' is not available.")
    }
    else (v == "true")
  }

  private val javaPaths = {
    val b = Map.newBuilder[String, Path]
    if(isInstalled || isJDK) {
      val root =
        if (isInstalled) "/usr".toPath
        else javaDistPath
      b += ("java" -> root.resolve("bin/java"))
      b += ("jar" -> root.resolve("bin/jar"))
      b += ("javap" -> root.resolve("bin/javap"))
      b += ("jps" -> root.resolve("bin/jps"))
    }
    //println(s"jpaths: ${b.result}")
    b.result()
  }



  val scalaDistPath: Path = {
    val v = java.lang.System.getProperty("ssc.scala.home")
    //println(s"sscJavaDir $v")
    if (v == null) {
      throw new Exception("The system property 'ssc.scala.home' is not available.")
    }
    else v.toPath
  }

  private val scalaPaths = {
    val b = Map.newBuilder[String, Path]
    val root = scalaDistPath
    b += ("scala" -> root.resolve("bin/scala"))
    b += ("scalap" -> root.resolve("bin/scalap"))
    b += ("fsc" -> root.resolve("bin/fsc"))
    b += ("scalac" -> root.resolve("bin/scalac"))
    b += ("scaladoc" -> root.resolve("bin/scaladoc"))

    //println(s"spaths: ${b.result}")
    b.result()
  }



  ///////////////////////////
  // Main handled switches //
  ///////////////////////////

  /** Appends to a string general help items caught by the runner.
    *
    * There is no need to pass these items to projects, so their help
    * is added away from the main switch values.
    */
  private def addMainhandledHelp(b: HelpBuilder)
  {
    b.addItem( "-maven", "sets default configuration to Maven conventions (/src/main/scala etc.)")
    b.addItem( "-config", "output the config (the default with file-modifications")
    b.addItem( "-version", "output version information")
    b.addItem( "-help", "output this message")
  }


  /** Outputs help.
    * 
    * Can sub-categorise help using the task name.
    * 
    * @param taskName name of a task, causes help to
    *  specialise it's string output.
    */
  def printHelp(taskName: String)
  {
    val b = HelpBuilder()

    if (!taskName.isEmpty) {
      val tss = CLSchema.taskSwitchSeq(taskName)
      if(tss.isEmpty) {
        b.addUsage("ssc", taskName)
      }
      else {
        b.addUsage("ssc", "<options> " + taskName)
        var first = true
        CLSchema.taskSwitchSeq(taskName).foreach{ sg =>
          if (first) {
            first = false
            b.add("Options:", sg)
            b.addNewLine
            b.addLine("Other Options:")
          }
          else {
            b.add("", sg)
          }
        }
        addMainhandledHelp(b)
      }
    }
    else {
      b.addUsage("ssc", "<options> <task>")
      b.add("Tasks:", CLSchema.tasks)
      b.addNewLine
      b.add("Basic Options:", CLSchema.appdataSwitches)
      addMainhandledHelp(b)
      b.addNewLine
      b.addLine("Options are available for each task. Try ssc -help <task>")
    }

    trace(b.result())
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
      if(inputArgs.contains("-maven")) {
        Configuration.maven
      }
      else Configuration.default

    val projectConfig : ConfigMap = userFileEnhancedConfig(defaultConfig,  localEntryConfig(cwd))


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

  /** Load a build.ssc file, parse, and return the configuration map.
    *
    * @param launchDirectory the directory to try load a file from
    */
  def localEntryConfig(launchDirectory: Path)
      : ConfigMap =
  {
    // Need to load any build.ssc file
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
        case e: Exception => traceInfo(s"No configuraton file found (or readable) in: $launchDirectory\n  (using default configuration)")
          Seq.empty[String]
      }

    // Parse the lines
    // Makes no difference if a file was found or not, we printed warnings
    // Doesn't verify against any data, just cleans and makes into map.
    val p = new ParseIni(configLines, verbose, noColor)
    p.parse()
  }

  /** Returns a defaults config merged with user preferences
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
    val fileConfig : ConfigMap = localEntryConfig(cwd)
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
    // Drop the switches from the map
    val taskConfigSwitchless = taskConfig.map{case(k, v) => (k.drop(1) -> v)}
    //...then form a Config, in an Action
    val runTask = new Action(
      task,
      cwd,
      Config(taskConfigSwitchless),
      isJDK,
      javaPaths,
      scalaPaths
    )
    //...and run
    runTask.run()
  }




  /** Oh, you asked a question?
    */
  def main(inputArgs: Array[String]){
    //println(s"args:")
    //args.foreach(println)

    if(inputArgs.size == 0) {
      // Special case, no message, goto help
      printHelp("")
    }
    else {
      // Filter simple show options
      val mainHandledOption: Option[String] = inputArgs.find{ arg =>
        Seq("-help", "-config", "-version").contains(arg)
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
        // ...they apply to the runner/parsing too.
        // They should be in every task config.
        verbose = inputArgs.contains("-verbose")
        noColor = inputArgs.contains("-noColor")

        // Get the default config
        val (defaultConfig, taskArgs) =
          if(inputArgs.contains("-maven")) {
            // This arg needs to be stripped, as the main commandline
            // parser will not recognise it.
            val argsNoMaven = inputArgs.filter(_ != "-maven")
            (Configuration.maven, argsNoMaven)
          }
          else (Configuration.default, inputArgs)

        // Build a configuration tree.
        // (also checks for overriding build.ssc files)
        val projectTree = buildProjectTree(
          defaultConfig,
          cwd
        )

        //println("project tree:\n" +projectTree.toStringTree())



        // Parse the input args.
        val clParser = new ParseCommandLine(verbose, noColor)

        // NB: The return is tuple(task, targeted configuration)
        val argsO: Option[(String, ConfigGroup)] = clParser.parse(taskArgs)
        //println(s"argsO:\n $argsO")



        // Conmmand line format fails always end in failure.
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
  }

}//Runner

