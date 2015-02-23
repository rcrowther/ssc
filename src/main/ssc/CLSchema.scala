package ssc

import sake.support.parser.{CLSwitchOption, CLArgOption}



/** Contains definitions for the commandline activity of ssc.
  *
  * Builds groups of switches which may be used for tasks. Note that
  * the switches are duplicated across tasks; for example,
  * 'scalaSrcDir' should appear under every task configuration which
  * may need to compile. See `taskSwitchSeq`.
  *
  * Please note the options here are those which make their way to the
  * projects. A couple of options are caught by [[ssc.Runner]]. See
  * the private definition `addMainhandledHelp`.
  */
object CLSchema {

  /** Switch configuration covering all project activity.
    *
    * Is left public as we reference it in general help.
    */
  val appdataSwitches = Map[String, CLSwitchOption](
    // Platform dependant (yes, they are)
    "-appName" ->  ("name of this application. Used for example, to label .jar files.", "myApp", false, 1),
    "-appVersion" -> ("version of this application", "0.1.SNAPSHOT", false, 1),
    "-dependancies" -> ("paths to dependant applications", "", true, 32),
    "-javaPath" -> ("path to Java executable", "", false, 1),
    "-scalaPath" -> ("path to Scala executable", "", false, 1)
  )

  private val outputFormatSwitch = Map[String, CLSwitchOption](
    "-verbose" -> ("Output messages about what the application is doing", "false"),
    "-verboseTools" -> ("Output messages from tools such as 'scalac' or 'jar'", "false"),
    "-inColor" -> ("Output in color", "false")
  )


  // clean needs docpath and dirpath, so this is seperate
  private val buildDirSwitch = Map[String, CLSwitchOption](
    "-buildDir" -> ("path of the build directory", "build", false, 1)
  )

  private val compileSwitches = Map[String, CLSwitchOption](
    /** The encoding used by source files
      *
      * Note: Nothing to do with document output.
      */
    "-charset" -> ("charset of source documents", "UTF-8", false, 1),


    // Building //
    "-libDir" -> CLSwitchOption("paths of the library directories", Seq("lib","Lib"), true, 32),
    "-scalaSrcDir" -> CLSwitchOption("path to Scala source files", Seq("src/main/scala","src/scala","src/main","src","."), true, 32),
    "-javaSrcDir" -> CLSwitchOption("path to Java source files", Seq("src/main/java","src/java"), true, 32),
    "-scalaTestDir" -> CLSwitchOption("path to Scala test files", Seq("src/test/scala", "src/test", "test"), true, 32),
    "-javaTestDir" -> CLSwitchOption("path to Java test files", Seq("src/test/java"), true, 32),

    // scalac (compile) options //
    "-optimise" -> ("faster bytecode by code analysis", "false"),
    "-incremental" -> ("compile only changed files", "true"),
    "-feature" -> ("turn on Scala's feature warnings (needs new compile to report)", "false"),
    "-deprecation" -> ("turn on Scala's deprecation warnings (needs new compile to report)", "false"),
    "-meter"  -> ("show a progressbar, one of 'none', 'progress' (default), 'bounce', 'buzz', ", "progress", false, 1)
  )


  private val docDirSwitch = Map[String, CLSwitchOption](
    // scaladoc options //
    "-docDir" -> ("path to documentation directory", "Doc", false, 1)
  )


  private val docSwitches = Map[String, CLSwitchOption](

    // doc compilation
    /** No prefixes for types.
      *
      * The lack of prefixes will "significantly speed up scaladoc",
      * presumably because it must not hunt down types through trees.
      *
      * There is also a warning this may produce ambiguities, like two
      * types with the same name in different packages.
      *
      * The main visual effect of prefixes is to place the package
      * chain, in little HTML anchors, above a class/object name.
      */ 
    "-noTypePrefixes" -> ("don't find packaging prefixes (makes scaladoc faster).", "false"),

    /** Skip these packages when making documentation.
      *
      * The list must be of fully-qualified names
      */
    "-skipPackages" -> ("skip these packages", "", true, 32),

    /** Assemble documentation from compiled files.
      *
      * If false will do a soft compile, reporting errors but
      * producing documentation.  If true and files are not compiled,
      * will request compilation. Compilation is performed according
      * to compilation keys, including `incremental`.
      */
    // "-strict" -> ("always assemble from source files (if false, will do it's own compile which always succeeds and produces documentation)", "false"),

    // doc formatting
    /** A title to scaladoc, will appear in tabs and meta-data elements.
      *
      * A line of text. Avoid HTML?
      */
    "-title" -> ("main title of the documentation", "Scala project", false, 1),

    /** A root document for the documentation.
      *
      * The problem addressed by the root document is that no "root"
      * package object exists to carry documentation. Hence this. See
      * Scala's root document at
      * [[http://www.scala-lang.org/api/current/index.html#package]].
      *
      * So distrustful are people of code documentation that a root
      * document seems to be rarely viewed. Try putting something
      * silly there and see if anyone notices.
      */
    "-rootdoc" -> CLSwitchOption("point at a text document describing the application's root package", Seq("src/main/scala/rootdoc.txt,src/main/rootdoc.txt","src/rootdoc.txt","rootdoc.txt"), false, 1),

    /** A footer comment to scaladoc, usually an overall credit.
      *
      * A line of text. Can be HTML, so HTML anchors, etc.
      */
    "-footer" -> ("a text footer to each page", "By SSC"),



    // Dot/diagram production //
    "-diagrams" -> ("Create inheritance diagrams for classes, traits and packages", "false"),
    "-dotPath" -> ("path to the dot executable, which produces the diagrams (ssc will guess, but the path can be entered explicitly here)", "", false, 1),
    "-dotRestarts" -> ("number of times to restart a malfunctioning dot process before abandoning diagram production (default: 5)", "5", false, 1),
    "-dotTimeout" -> ("the timeout before the dot util is forcefully closed, in seconds (default: 10)", "10", false, 1),
    "-dotMaxClasses" -> ("The maximum number of superclasses or subclasses to show in a diagram (default: 8)", "8", false, 1),
    "-dotMaxImplicits" -> ("The maximum number of implicitly converted classes to show in a diagram (default: 2)", "2", false, 1)
  )


  private val runSwitches = Map[String, CLSwitchOption](
    "-class" -> ("specify a class to run", "", false, 1)
      //"-noGuess" -> ("do not try to guess packaging", "false")
  )

  private val scalaTestSwitches = Map[String, CLSwitchOption](
    "-scalaTestExe" -> ("path to a scalaTest jar (ssc will try the library, but the path can be entered explicitly here)", "", false, 1),
    "-to" -> ("where output goes. One of 'gui', 'out', 'err' (default: out)", "", false, 1),
    "-suite" -> ("specify a suite to run", "", false, 1),
    "-suiteFrag" -> ("a fragment of path used to find a set of suites", "", false, 1),
    "-name" -> (" specify a test to run", "", false, 1),
    "-nameFrag" -> ("a fragment of name used to find a set of tests", "", false, 1)

  )

  private val jarSwitches = Map[String, CLSwitchOption](
    "-classpaths" -> ("add classpaths to the manifest", "", true, 64),
    "-mainClass" -> ("specify a main class for an executable file", "", false, 1),
    "-noVersionTitle" -> ("the jar name will be the name of the app only", "false"),
    "-uncompressed" -> ("the resulting jar will not be compressed", "false")
  )


  private val introspectSwitches = Map[String, CLSwitchOption](
    "-private" -> ("print private definitions", "false"),
    "-classnames" -> CLSwitchOption("classnames to introspect", Seq.empty[String], true, 64)
  )


  private val bytecodeSwitches = Map[String, CLSwitchOption](
    "-methodfields" -> ("print field (variables) tables in methods", "false"),
    "-public" -> ("print public definitions", "false"),
    "-protected" -> ("print protected definitions", "false"),
    "-private" -> ("print private definitions", "false"),
    "-systemInfo" -> ("print paths, dates, sizes, hashcodes", "false"),
    "-classInfo" -> ("print stacksize and arg counts", "false"),
    "-classnames" -> CLSwitchOption("classnames to introspect", Seq.empty[String], true, 64)
  )


  /** All switches for a task in a seq of switch groups.
    *
    * This value gathers relevant switch groups together, keyed by
    * task. It is the base data from which default config is
    * generated, and also help.
    */
  val taskSwitchSeq = Map[String, Seq[Map[String, CLSwitchOption]]](
    "bytecode" -> Seq(bytecodeSwitches, compileSwitches, buildDirSwitch, appdataSwitches, outputFormatSwitch),
    "introspect" -> Seq(introspectSwitches, compileSwitches, buildDirSwitch, appdataSwitches, outputFormatSwitch),
    "repl" ->  Seq.empty,
    // Empty compiles
    "clear" ->  Seq(buildDirSwitch, docDirSwitch, outputFormatSwitch),
    // Full cleanup
    "clean" ->  Seq(buildDirSwitch, docDirSwitch, outputFormatSwitch),
    "compile" -> Seq(compileSwitches, buildDirSwitch, appdataSwitches, outputFormatSwitch),
    "test" -> Seq(scalaTestSwitches, compileSwitches, buildDirSwitch, appdataSwitches, outputFormatSwitch),
    "run" -> Seq(runSwitches, compileSwitches, buildDirSwitch, appdataSwitches, outputFormatSwitch),
    "doc" -> Seq(docSwitches, docDirSwitch, compileSwitches, buildDirSwitch, appdataSwitches, outputFormatSwitch),
    "jar" -> Seq(jarSwitches, compileSwitches, buildDirSwitch, appdataSwitches, outputFormatSwitch),
    "reload" -> Seq(outputFormatSwitch)
  )


  /** All switches for a task.
    *
    * Generated from `taskSwitchSeq`, *not* `tasks`
    */
  val taskSwitches :  Map[String, Map[String, CLSwitchOption]] =
    taskSwitchSeq.map{ case(k, v) =>
      val vt = (Map.empty[String, CLSwitchOption] /: v) (_ ++ _)
      (k -> vt)
    }


  /** All tasks and a description.
    *
    * This value exists to provide help in a way the commandline
    * parser can recognise. Do not use for commandline parsing. Or
    * risk mind-warp.
    */
  val tasks = Map[String, CLArgOption](
    "bytecode" -> "output bytecode from classes (can be hard to trace from Scala code. Can be pointed at Scala '$' class fragments)",
    "introspect" -> "output information on classes",
    "repl" -> "start the repl",
    // Empty compiles
    "clear" -> "empty the build directory (deletes compiled files)",
    // Full cleanup
    "clean" -> "remove SSC material, except build.ssc and .jar files",
    "compile" -> "run the scala compiler, scalac",
    "test" -> "run tests (scalatest)",
    "run" -> "run a main class in compiled files",
    "doc" -> "run the scala documentation tool, scaladoc",
    "jar" -> "make a jar from compiled files (use -mainClass to create an executable)",
    "reload" -> "reload the build definition - changes the default config."
  )

}//CLSchema
