package ssc


import sake.support.parser._


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
    "-appName" ->  ("name of this application. Used for example, to label .jar files.", "myApp", ParameterDescription.name, false, 1),
    "-appVersion" -> ("version of this application", "0.1.SNAPSHOT", ParameterDescription.version, false, 1),
    "-dependancies" -> ("paths to dependant applications", "",ParameterDescription.paths, true, 32),
    "-javaPath" -> ("path to Java executable", "", ParameterDescription.path, false, 1),
    "-scalaPath" -> ("path to Scala executable", "", ParameterDescription.path, false, 1)
  )

  private val outputFormatSwitch = Map[String, CLSwitchOption](
    "-verbose" -> "Output messages about what the application is doing",
    "-verboseTools" -> "Output messages from tools such as 'scalac' or 'jar'. Can be very noisy.",
    "-noColor" -> "Output without color codes"
  )


  // clean needs docpath and dirpath, so this is seperate
  private val buildDirSwitch = Map[String, CLSwitchOption](
    "-buildDir" -> ("path of the build directory", "build", ParameterDescription.path, false, 1)
  )

  ///////////////////
  // Task switches //
  ///////////////////

  private val findSwitches = Map[String, CLSwitchOption](
    "-subpath" -> ("define a subpath of source to build the tree from", "", ParameterDescription.path, false, 1),
    "-get" -> ("partial text match against live text (filename base or a line)", "", ParameterDescription.text, false, 1),
    "-match" -> ("match a regex against live text (filename base or a line). Full match, anchored at text ends", "", ParameterDescription.text, false, 1),
    "-hidden" -> "include hidden files",
    "-case" -> "text must match case"
  )

  private val treeSwitches = Map[String, CLSwitchOption](
    "-subpath" -> ("define a subpath of source to build the tree from", "", ParameterDescription.path, false, 1),
    "-dir" -> "output directories only",
    "-get" -> ("partial text match against live text (filename base or a line). Case-sensitve", "", ParameterDescription.text, false, 1),
    "-hidden" -> "include hidden files ('tree' can not find hidden Unix files - preceeded with a dot - with or without this switch)"
  )

  private val sourceSwitches = Map[String, CLSwitchOption](
    "-scalaSrcDir" -> ("path to Scala source files", Seq("src/main/scala","src/scala","src/main","src", "scala", "."), ParameterDescription.path, false, 1),
    "-javaSrcDir" -> ("path to Java source files", Seq("src/main/java","src/java", "java"), ParameterDescription.path, false, 1),
    "-scalaTestDir" -> ("path to Scala test files", Seq("src/test/scala", "src/test", "test"), ParameterDescription.path, false, 1),
    "-javaTestDir" -> ("path to Java test files", Seq("src/test/java"), ParameterDescription.path, false, 1)
  )

  private val compileSwitches = {
    sourceSwitches ++
    Map[String, CLSwitchOption](
      /** The encoding used by source files
        *
        * Note: Nothing to do with document output.
        */
      "-charset" -> ("charset of source documents", Seq("UTF-8"), ParameterDescription.encoding, false, 1),


      // Building //
      "-libDir" -> ("paths of the library directories", Seq("lib","Lib"), ParameterDescription.paths, false, 1),

      // scalac (compile) options //
      "-optimise" -> "faster bytecode by code analysis",
      "-incremental" -> ("compile only changed files", "true"),
      "-feature" -> ("turn on Scala's feature warnings (needs a new compile to report)"),
      "-deprecation" -> "turn on Scala's deprecation warnings (needs a new compile to report)",
      "-meter"  -> ("show a progressbar, one of 'none', 'progress', 'bounce', 'buzz'. Only visible using the -verbose switch (default: 'progress')", "progress", ParameterDescription.strCode, false, 1)
    )
  }

  private val docDirSwitch = Map[String, CLSwitchOption](
    // scaladoc options //
    "-docDir" -> ("path to documentation directory", "Doc", ParameterDescription.path, false, 1)
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
    "-skipPackages" -> ("skip these packages", "", new ParameterDescription("packageNames"), true, 32),

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
    "-title" -> ("main title of the documentation", "Scala project",ParameterDescription.text, false, 1),

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
    "-rootdoc" -> ("point at a text document describing the application's root package", Seq("src/main/scala/rootdoc.txt,src/main/rootdoc.txt","src/rootdoc.txt","rootdoc.txt"), ParameterDescription.file, false, 1),

    /** A footer comment to scaladoc, usually an overall credit.
      *
      * A line of text. Can be HTML, so HTML anchors, etc.
      */
    "-footer" -> ("a text footer to each page", "By SSC"),



    // Dot/diagram production //
    "-diagrams" -> ("Create inheritance diagrams for classes, traits and packages"),
    "-dotPath" -> ("path to the dot executable, which produces the diagrams (ssc will guess, but the path can be entered explicitly here)", "", ParameterDescription.path, false, 1),
    "-dotRestarts" -> ("number of times to restart a malfunctioning dot process before abandoning diagram production (default: 5)", "5", ParameterDescription.int, false, 1),
    "-dotTimeout" -> ("the timeout before the dot util is forcefully closed, in seconds (default: 10)", "10",ParameterDescription.int, false, 1),
    "-dotMaxClasses" -> ("The maximum number of superclasses or subclasses to show in a diagram (default: 8)", "8", ParameterDescription.int, false, 1),
    "-dotMaxImplicits" -> ("The maximum number of implicitly converted classes to show in a diagram (default: 2)", "2", ParameterDescription.int, false, 1)
  )




  private val scalaTestSwitches = Map[String, CLSwitchOption](
    "-scalaTestExe" -> ("path to a scalaTest jar (ssc will try the library, but the path can be entered explicitly here)", "", ParameterDescription.path, false, 1),
    "-to" -> ("where output goes. One of 'gui', 'out', 'err' (default: out)", "", ParameterDescription.strCode, false, 1),
    "-suite" -> ("specify a suite to run", "", ParameterDescription.text, false, 1),
    "-suiteFrag" -> ("a fragment of path used to find a set of suites", "", ParameterDescription.text, false, 1),
    "-name" -> (" specify a test to run", "", ParameterDescription.className, false, 1),
    "-nameFrag" -> ("a fragment of name used to find a set of tests", "", ParameterDescription.text, false, 1)

  )

  private val jarSwitches = Map[String, CLSwitchOption](
    "-classpaths" -> ("add classpaths to the manifest", "", ParameterDescription.paths, true, 64),
    "-mainClass" -> ("specify a main class for an executable file", "", ParameterDescription.className, false, 1),
    "-noVersionTitle" -> ("the jar name will be the name of the app only"),
    "-uncompressed" -> ("the resulting jar will not be compressed")
  )


  private val introspectSwitches = Map[String, CLSwitchOption](
    "-private" -> "print private definitions",
    "-classes" -> ("classnames to introspect", Seq.empty[String], ParameterDescription.classNames, true, 64)
  )


  private val bytecodeSwitches = Map[String, CLSwitchOption](
    "-methodfields" -> ("print field (variables) tables in methods", "false"),
    "-public" -> ("print public definitions", "false"),
    "-protected" -> ("print protected definitions", "false"),
    "-private" -> ("print private definitions", "false"),
    "-systemInfo" -> ("print paths, dates, sizes, hashcodes", "false"),
    "-classInfo" -> ("print stacksize and arg counts", "false"),
    "-classes" -> ("classnames to introspect", Seq.empty[String], ParameterDescription.paths, true, 64)
  )

  private val runSwitches = Map[String, CLSwitchOption](
    "-class" -> ("specify a class to run", "", ParameterDescription.className, false, 1)
      //"-noGuess" -> ("do not try to guess packaging", "false")
  )

  /** All switches for a task in a seq of switch groups.
    *
    * This value gathers relevant switch groups together, keyed by
    * task. It is the base data from which default config is
    * generated, and also help.
    */

  // NB. 'outputFormatSwitch' should be in every task, as the runner
  // uses some of the values too, from a quick parse.
  val taskSwitchSeq = Map[String, Seq[Map[String, CLSwitchOption]]](
    "bytecode" -> Seq(bytecodeSwitches, compileSwitches, buildDirSwitch, appdataSwitches, outputFormatSwitch),
    "introspect" -> Seq(introspectSwitches, compileSwitches, buildDirSwitch, appdataSwitches, outputFormatSwitch),
    "repl" -> Seq(outputFormatSwitch),
    // Empty compiles
    "clear" ->  Seq(buildDirSwitch, docDirSwitch, outputFormatSwitch),
    // Full cleanup
    "clean" ->  Seq(buildDirSwitch, docDirSwitch, outputFormatSwitch),
    "find" ->  Seq(findSwitches, sourceSwitches, outputFormatSwitch),
    "findfile" ->  Seq(findSwitches, sourceSwitches, outputFormatSwitch),
    "tree" ->  Seq(treeSwitches, sourceSwitches, outputFormatSwitch),
    "compile" -> Seq(compileSwitches, buildDirSwitch, appdataSwitches, outputFormatSwitch),
    "test" -> Seq(scalaTestSwitches, compileSwitches, buildDirSwitch, appdataSwitches, outputFormatSwitch),
    "run" -> Seq(runSwitches, compileSwitches, buildDirSwitch, appdataSwitches, outputFormatSwitch),
    "doc" -> Seq(docSwitches, docDirSwitch, compileSwitches, buildDirSwitch, appdataSwitches, outputFormatSwitch),
    "jar" -> Seq(jarSwitches, compileSwitches, buildDirSwitch, appdataSwitches, outputFormatSwitch)
      // TOCONSIDER: Will be enabled sometime, likely
      //"reload" -> Seq(outputFormatSwitch)
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
    "repl" -> "start a repl (not enabled!)",
    // Empty compiles
    "clear" -> "empty the build directory (deletes compiled files)",
    // Full cleanup
    "clean" -> "remove SSC material, except build.ssc and .jar files",
    "find" -> "search for text in source files",
    "findfile" -> "search for source files in directories",
    "tree" -> "outputs a tree representation of source files",
    "compile" -> "run the scala compiler, scalac",
    "test" -> "run tests (scalatest)",
    "run" -> "run a main class in compiled files",
    "doc" -> "run the scala documentation tool, scaladoc",
    "jar" -> "make a jar from compiled files (use -mainClass to create an executable)"
      // TOCONSIDER: Will be enabled sometime, likely
      //"reload" -> "reload the build definition - changes the default config"
  )

}//CLSchema
