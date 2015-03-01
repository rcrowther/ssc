package ssc

import java.io.File
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.Path

import sake.util.file._
import sake.util.executable.FindExecutable

import java.util.regex.Pattern


/** Carries actions ssc can make.
  *
  * This class contains every external action `ssc` can make. It is
  * the same class for any [[ssc.Project]] action.
  *
  * The configuration parameter is an instance of [[ssc.Config]], a
  * wrap of a Map[String, Seq[String]]. This is not the same as a
  * default config, which is keyed by 'task' names. It is expected
  * that the code which creates and calls this class will,
  *
  *  - understand which task is intended.
  *  - select the appropriate config from a projectConfig map, by task.
  *  - if appropriate, overlaid the taskConfig with commandline options (some auto-actioning may not require this).
  *  - supply the data.
  *
  * Instances of the class handle their own output of report
  * information.
  *
  * @param taskName the name of the task which can be invoked using `run`.
  * @param cwd the directory to work actions from. All
  *  actions are relative from this value.
  * @param config a set of values to modify the actions
  *  according to expressed preferences.  
  */
final class Action(
  val taskName: String,
  val cwd: Path,
  val config: Config
)
    extends sake.Trace
    with sake.util.noThrow.Shell
    with Runnable
{
  // construct Trace abstracts from the incoming config
  protected val noColor: Boolean = config.asBoolean("noColor")
  protected val verbose: Boolean = config.asBoolean("verbose")



  ///////////
  // Utils //
  ///////////
  
  // NB: Sometimes the build paths are not
  // present, because the action does not require it.
  private def buildPathBase : Option[Path] =
    if (config.contains("buildDir")) Some(cwd.resolve(config("buildDir")))
    else None

  private def buildPathMain : Option[Path] =
    if (buildPathBase != None) Some(buildPathBase.get.resolve("main/"))
    else None

  // A lot of Java gear will not work without a correct level of
  // directory (for packaging). Classpaths to Main will fail.  Hence
  // this...
  private def buildPathMainSections : Seq[Path] =
    if (buildPathBase == None) Seq.empty[Path]
    else {
      Seq(
        buildPathMain.get.resolve("scala"),
        buildPathMain.get.resolve("java")
      )
    }

  // Tests are separated first, so jar and other collective runners
  // can include java/scala compiled gear by referencing
  // mainSections...
  private lazy val scalaRoute: ProcessingRoute = ProcessingRoute(
    if (buildPathBase == None) None else Some(buildPathBase.get.resolve("main/scala")),
    dirFind("scalaSrcDir", config.asSeq("scalaSrcDir")),
    ".scala",
    config.asSeq("scalaSrcDir")
  )

  private lazy val scalaTestRoute: ProcessingRoute = ProcessingRoute(
    if (buildPathBase == None) None else Some(buildPathBase.get.resolve("test/scala")),
    dirFind("scalaTestRoute", config.asSeq("scalaTestDir")),
    ".scala",
    config.asSeq("scalaTestDir")
  )

  private def docPath : Path = cwd.resolve(config("docDir"))
  private def libPath : Option[Path] = dirFind("libDir", config.asSeq("libDir"))



  /** Returns an iterable of all entry files in a directory.
    *
    * @param path a path to a directory
    * @param filter the paths must end in this string
    * @return entries found in the dir
    */
  private def dirEntryPaths(path: Path, filter: String)
      : Traversable[Path] =
  {
    val paths = Dir.readEntry(path)
    paths.filter{p =>p.getFileName().toString.endsWith(filter)}
  }


  /** Returns an iterable of all entry files in a directory.
    *
    * This method returns an empty seq if the path is None, or
    * attempts to read the seq of paths in the directory, if the path
    * is Some.
    *
    * @param path a path to a directory
    * @param filter the paths must end in this string
    * @return entries found in the dir
    */
  private def dirEntryPaths(path: Option[Path], filter: String)
      : Traversable[Path] =
  {
    if (path != None) dirEntryPaths(path.get, filter)
    else Seq.empty[Path]
  }

  private def dirEntryPathsAndAttributes(path: Path, filter: String)
      : Traversable[(Path, BasicFileAttributes)] =
  {
    val paths = Dir.readEntryAndAttributes(path)
    paths.filter{p => p._1.getFileName().toString.endsWith(filter)}
  }


  /** Tests if a directory is populated by files of the given extension.
    */
  private def dirIsPopulated(buildPath: Path, extension: String)
      : Boolean =
  {
    !Dir.read(buildPath).filter(_.toString.endsWith(extension)).isEmpty
  }
  

  /** Selects an existing directory from a seq of possibilities.
    *
    * @param requester a name to be used in trace output
    * @param pathStrs the seq of possible paths
    */
  private def dirFind(
    requester: String,
    pathStrs: Seq[String]
  )
      : Option[Path] =
  {
    val pathO = pathStrs.find{ pathStr =>
      Dir.exists(pathStr.toPath)
    }
    if (pathO == None){
      val ps = pathStrs.mkString(", ")
      //NB: If we can't find directories it's not woth noting...
      // they may not be used.
      //traceInfo(s"$requester could not find an existing directory in the sequence: $ps")
      None
    }
    else {
      val pathStr = pathO.get
      traceInfo(s"$requester is $pathStr")
      Some(pathStr.toPath)
    }
  }



  /////////////////////
  // Option builders //
  /////////////////////


  /** Builds standard scala commandline options.
    * 
    * These are generated from configuration data.
    *
    * Some tasks need the build directory, and precompiled files
    * ('compile', 'run'), some don't ('doc')
    *
    * @param executable the name of the tool to be invoked via
    *  the shell.
    * @param destination the destination for material generated
    *  by the action. May be documentation, compiled files, etc.
    * @param addPreCompiledClassPaths if true, the 
    * compiled/build paths are added as "-classpath"s.
    */
  private def buildScalaStandardOptions(
    executable : String,
    destination : Option[Path],
    addPreCompiledClassPaths: Boolean
  )
      : scala.collection.mutable.Builder[String, Seq[String]] =
  {
    val b = Seq.newBuilder[String]

    b += executable

    if (config.asBoolean("verboseTools")) {
      b += "-verbose"
    }

    if (!config("charset").isEmpty) {
      b += "-encoding"
      b += config("charset")
    }

    //set destination
    if(destination != None) {
      b += "-d"
      b += destination.get.toString
    }

    // Add libs
    dirEntryPaths(libPath, ".jar").foreach{ p =>
      b += "-toolcp"
      b += p.toString
    }

    // TODO: now, how does this work?
    // Add precompiled paths.
    if (addPreCompiledClassPaths) {
      // NB: classpath probe down for Scala and Java files.
      compiledClasspaths.foreach { p =>
        b += "-classpath"
        b += p.toString
      }
    }

    b
  }


  private def appendDocOptions(
    b: scala.collection.mutable.Builder[String, Seq[String]]
  )
      : scala.collection.mutable.Builder[String, Seq[String]] =
  {
    // compilation
    if (config.asBoolean("noTypePrefixes")) {
      b += "-no-prefixes"
    }

    //TODO: Not right, building from empty.
    val pkgs = config.asSeq("skipPackages")
    if(!pkgs.isEmpty){
      b += "-skip-packages"
      b += pkgs.mkString("", ":", "")
    }


    // formatting
    if (!config("title").isEmpty) {
      b += "-doc-title"
      b += config("title")
    }

    b += "-author"

    val rd = entryExists("rootdoc")
    if (rd != None) {
      b += "-doc-root-content"
      b += rd.get.toString
    }

    if (!config("footer").isEmpty) {
      b += "-doc-footer"
      b += config("footer")
    }



    // "Basically, sourcepath allows you to compile the standard library without anything
    // on the classpath. The compiler will create class symbols for source files based on their path and name."
    // Bollocks.
    //println("scaladoc line:" + b.result())

    // Dot //
    if (config.asBoolean("diagrams")) {

      val exec : Option[Path] =
        if (!config("dotPath").isEmpty) {
          Some(config("dotPath").toPath)
        }
        else {
          val e = FindExecutable("dot").find(
            false,
            true,
            sake.util.executable.Version.empty
          )

          if(e != None) {
            traceInfo("dot executable found!")
            Some(e.get.path)
          }
          else {
            traceWarning("diagram construction requested, but the program 'dot' could not be found. Either install the program to the host computer, or provide a path using the switch -dotPath")
            None
          }
        }

      if (exec != None) {
        b +=  "-diagrams"
        b += "-diagrams-dot-path"
        b += exec.get.toString
        b += "-diagrams-dot-restart"
        b += config("dotRestarts")
        b += "-diagrams-dot-timeout"
        b += config("dotTimeout")
        b += "-diagrams-max-classes"
        b += config("dotMaxClasses")
        b += "-diagrams-max-implicits"
        b += config("dotMaxImplicits")
      }

    }

    b
  }


  private def appendCompileOptions(
    b: scala.collection.mutable.Builder[String, Seq[String]]
  )
      : scala.collection.mutable.Builder[String, Seq[String]] =
  {
    if (config.asBoolean("optimise")) {
      b += "-optimise"
    }
    if (config.asBoolean("feature")) {
      b += "-feature"
    }
    if (config.asBoolean("deprecation")) {
      b += "-deprecation"
    }
    b
  }



  ///////////////////////
  // Build environment //
  ///////////////////////


  /** Verifies source paths do not lie within each others path.
    *
    * Prints appropriate messages if not.
    *
    * @return true if the paths pass, false if they fail.
    */
  def sourcePathVerify()
      : Boolean =
  {

    /** Tests wether a string is contained within another, or visa versa.
      *
      * @return true if either string is wholy contained in the other
      */
    def unionContains(
      str1: String,
      str2: String
    )
        : Boolean =
    {
      var similar = true
      var limit = Math.min(str1.size, str2.size) - 1
      var i = 0
      while(i < limit && similar) {
        similar = str1(i) == str2(i)
        i += 1
      }
      similar
    }

    val existingSourcesB = Seq.newBuilder[Path]
    if (scalaRoute.srcPath != None) existingSourcesB += scalaRoute.srcPath.get
    if (scalaTestRoute.srcPath != None) existingSourcesB += scalaTestRoute.srcPath.get

    val existingSources = existingSourcesB.result()

    if (existingSources.size < 2) true
    else {
      val aScala = scalaRoute.srcPath.get.toAbsolutePath().toString
      val aScalaTest = scalaTestRoute.srcPath.get.toAbsolutePath().toString
      if(!unionContains(aScala, aScalaTest)) true
      else {
        traceError(s"A source path lies within the path of another path. The paths are:\n$aScala, $aScalaTest")
        traceAdvice("SSC will not progress. Please reorganise the folder structure.")
        false
      }
    }
  }


  /** Return the paths of existing groups in the build paths.
    *
    * i.e. "/main/scala", "main/java".
    * @return build classpaths which exist
    */
  def compiledClasspaths
      : Seq[Path] =
  {
    buildPathMainSections.filter{p =>
      Dir.exists(p)
    }
  }


  /** Builds commandline data for source files.
    *
    * Will only append source files updated compared to the classfile,
    * unless forced with `strict`.
    *
    * @param useAllSrcFiles forces all source files onto the builder,
    *  avoiding incremental compilation.
    * @return true if options were added, else false.
    */
  private def addSrcPathStrings(
    b: scala.collection.mutable.Builder[String, Seq[String]],
    ProcessingRoute : ProcessingRoute,
    incrementalCompile : Boolean
  )
      : Boolean =
  {
    // Gather srcpaths
    val targetSrcPaths =
      if (incrementalCompile) {
        incrementalSrcs(ProcessingRoute)
      }
      else {
        dirEntryPaths(ProcessingRoute.srcPath, ProcessingRoute.srcExtension).map{ p =>
          p.toString
        }
      }
    b ++= targetSrcPaths
    !targetSrcPaths.isEmpty
  }


  /** Finds source files modified after classfiles.
    *
    * A raw method, needs protection against missing source/build
    * directories.
    *
    * @param ProcessingRoute a collection of paths to source and
    *  target directories.
    * @return traversable of all paths, as strings.
    */ 
  // TODO: This seems to pick up stuff scalac doesn't compile, e.g.
  // orphans and configs. Bothered?
  private def incrementalSrcs(
    processingRoute: ProcessingRoute
  )
      : Traversable[String] =
  {
    //traceInfoPrint("incremental compile, file count:")
    val allCompiledPaths : Traversable[(Path, BasicFileAttributes)] =
      dirEntryPathsAndAttributes(processingRoute.buildPath.get, ".class")

    // Map the creation time to the filename, minus extension
    //TODO: This includes stacks of $ stuff which can be dropped?
    val b = Map.newBuilder[String, java.nio.file.attribute.FileTime]
    allCompiledPaths.foreach{ pa =>
      val fileName = pa._1.getFileName().toString
      val className = fileName.substring(0, fileName.lastIndexOf('.'))

      if(!className.contains("$")) {
        val creationTime = pa._2.creationTime()
        b += (className -> creationTime)
      }
    }
    val allCompiledPathsMap : Map[String, java.nio.file.attribute.FileTime] = b.result()
    //println(s"allCompiledPathsMap: $allCompiledPathsMap")
    //println
    val allSrcPaths = dirEntryPathsAndAttributes(
      processingRoute.srcPath.get,
      processingRoute.srcExtension
    )

    val ret = allSrcPaths.filter{ pa =>
      // Convert "path/X.scala" (or some other extension) to "path/X.class"
      val fileName = pa._1.getFileName().toString
      val className = fileName.substring(0, fileName.lastIndexOf('.'))
      if(!allCompiledPathsMap.contains(className)) true
      else {

        // Compare class creation time to file modified time.
        // if file modified later, include.
        val classCreationTime: java.nio.file.attribute.FileTime =
          allCompiledPathsMap(className)
        val srcModificationTime: java.nio.file.attribute.FileTime =
          pa._2.lastModifiedTime()
        // if (srcModificationTime > classCreationTime) {
        //  println(s"$className\n srcModificationTime: $srcModificationTime classCreationTime: $classCreationTime, ")
        // }

        srcModificationTime > classCreationTime
      }
    }.map(_._1.toString)

    //traceInfo(ret.size.toString)
    traceInfo("incremental compile, file count:" + ret.size.toString)
    ret
  }

  /** From a config key, tests if an entry file exists.
    */
  private def entryExists(configKey: String)
      : Option[Path] =
  {
    if (!configKey.isEmpty) {
      val p = cwd.resolve(config(configKey))
      if (Entry.exists(p)) Some(p)
      else None
    }
    else None
  }



  ///////////////////
  // Inner Compile //
  ///////////////////


  /** Compiles source files to the buildDir.
    *
    * This is the raw action, and should be protected against missing
    * sources, etc.
    *
    * @return true if a compile succeeded, else false.
    */
  // introspect/run/bytecode need close classpaths
  // jar does not
  // scalatest does not?
  def doScalaCompile(
    processingRoute: ProcessingRoute
  )
      : Boolean =
  {
    val buildPath = processingRoute.buildPath.get
    // Assert the build directory?
    Dir.create(buildPath)

    // Empty the build directory, if not incrementally compiling
    if(
      !config.asBoolean("incremental") &&
        dirIsPopulated(buildPath, ".class")
    )
    {
      Dir.clear(buildPath)
    }


    // Build some compile options
    val b = buildScalaStandardOptions(
      "scalac",
      Some(buildPath),
      true
    )

    appendCompileOptions(b)
    //println("scalac line:" + b.result())

    // Maybe incremental compile, maybe full
    val needsCompiling = addSrcPathStrings(
      b,
      processingRoute,
      config.asBoolean("incremental")
    )


    if(!needsCompiling) {
      traceInfo("No files need to be compiled")
      true
    }
    else {

      val pb = new ProgressNotifier(
        config("meter"),
        traceInfoPrint _,
        verbose,
        "compiling",
        22
      )

      val (retCode, stdErr, stdOut) = shCatch (b.result())

      pb.stop()
      trace(stdErr)
      trace(stdOut)

      if (retCode != 0) {
        traceWarning("compilation errors")
        false
      }
      else true
    }
  }


  /////////////
  // Control //
  /////////////

  /** Tests if sourcepaths exist on this compile route.
    */
  def sourcePathExists(
    requester: String,
    processingRoute: ProcessingRoute
  )
      : Boolean =
  {
    if (processingRoute.srcPath == None) {
      val configPaths = processingRoute.srcConfig.mkString(", ")
      traceInfo(s"$requester is requesting an operation from source files, but no directories can be found in the configured list: $configPaths")
      false
    }
    else true
  }

  /** Tests is a -subpath switch is a valid dir.
    *
    * Outputs it's own error messages.
    */
  private def validateDirSubpath(
    requester: String,
    processingRoute: ProcessingRoute
  )
      : Option[Path] =
  {
    if (!sourcePathExists(requester, processingRoute)) None
    else {
      val srcP = processingRoute.srcPath.get
      if (config("subpath").isEmpty) {
        Some(srcP)
      }
      else {
        val maybeP = srcP.resolve(config("subpath"))
        // Need to test if a subdir exists (the root )
        if (java.nio.file.Files.isDirectory(maybeP)) Some(maybeP)
        else {
          traceWarning(s"A path created from the '-subpath' switch does not lead to a valid directory: $maybeP")
          None
        }
      }
    }
  }

  /** Compiles if no classes exist
    *
    * Has a test before `assertCompile`, to see if compiling is
    * necessary. Useful for commands where an up-to-date compile is
    * not strictly necessary, and may be unintended and tedious
    * e.g. 'introspect', 'bytecode'.
    */
  def compileIfNotPopulated(
    requester: String,
    processingRoute: ProcessingRoute
  )
      : Boolean =
  {
    if(dirIsPopulated(processingRoute.buildPath.get, ".class")) {
      traceInfo(s"$requester is working from classes in ${processingRoute.buildPath.get.toString}")
      true
    }
    else {
      assertCompile(requester, processingRoute)
    }
  }

  /** Ensures an up-to-date compile.
    *
    * Compiles in the way configuration requests. Used where compiles
    * must match source e.g. 'jar', 'scalaTest'.
    */
  def assertCompile(
    requester: String,
    processingRoute: ProcessingRoute
  )
      : Boolean =
  {
    if (sourcePathExists(requester, processingRoute)) {
      traceInfo(s"$requester is forcing compile...")
      doScalaCompile(processingRoute)
    }
    else false
  }



  ////////////////
  // Root tasks //
  ////////////////

  /** Produces documentation in docDir.
    */
  def doc() {
    // Get the basic scala routes
    val r = scalaRoute

    if (r.srcPath == None) {
      val configPaths = r.srcConfig.mkString(", ")
      traceWarning(s"A 'doc' task has been requested, but no source directories can be found in the configuration options: ${configPaths}")
    }
    else {

      Dir.create(docPath)

      //TODO: I have no idea if scaladoc can use compiled files?
      val b = buildScalaStandardOptions(
        "scaladoc",
        Some(docPath),
        false
      )

      appendDocOptions(b)

      // Add paths for source files all sources
      addSrcPathStrings(
        b,
        r,
        false
      )


      val pb = new ProgressNotifier(
        config("meter"),
        traceInfoPrint _,
        verbose,
        "documenting",
        22
      )

      val (retCode, stdErr, stdOut) = shCatch (b.result())

      pb.stop()
      trace(stdErr)
      trace(stdOut)

      if (retCode != 0) {
        traceWarning("documentation errors")
      }
    }
  }


  /** Empties the build dir (so all compiled material).
    */
  def clear() {
    val bpb = buildPathBase.get
    traceInfo(s"clearing build directory $bpb...")
    Dir.clear(bpb)
  }


  /** Deletes ssc-generated material.
    * 
    * Skips '.jar' files (we don't know their name) and build.ssc
    * definitions.
    */
  def clean() {
    val bpb = buildPathBase.get

    // Delete documentation...
    try {
      traceInfo(s"deleting documentation directory $docPath...")
      Dir.delete(docPath)
    } catch {
      case e: Exception => println("Was:" + e.getMessage)
    }

    // Stray manifest files...
    val manifestPath = cwd.resolve("MANIFEST.MF")
    if(Entry.exists(manifestPath)) {
      traceInfo(s"deleting stray MANIFEST.MF file...")
      Entry.delete(manifestPath)
    }

    //..and the build directory
    traceInfo(s"deleting build directory $bpb...")
    Dir.delete(bpb)
  }


  /** Searches for text in the source.
    */
  def find() {

    val e = FindExecutable("grep").find(
      false,
      true,
      sake.util.executable.Version.empty
    )


    if(e == None) {
      traceWarning("'find' requested, but the program 'grep' could not be found. If 'find' is desired, please install 'grep' to the host computer.")
    }
    else {
      // Get the basic scala routes
      val r = scalaRoute

      val root = validateDirSubpath("'find' request", r)
      if (root != None) {

        val b = Seq.newBuilder[String]

        b += "grep"

        // recursive (simple)
        b += "-r"

        // Add line numbering
        b += "-n"

        // -case via grep
        if (!config.asBoolean("case")) {
          b += "-i"
        }

        val re =
          if(!config("get").isEmpty) {
            //easy, the default.
            b += config("get")
          }
          else {
            // crazy as it is,
            // empty works, and is API consistent
            // End anchoring is spec.
            // "-x" = match whole line
            b += "-x"
            b += config("match")
          }
        if(!config.asBoolean("hidden")) {
          // Double glob.
          b += "--exclude"
          b += ".*"
          b += "--exclude"
          b += "*~"
        }
        // No way to do a directory
        // change in grep. See the results formatting.
        b += root.get.toString

        traceInfo(s"line:  ${b.result()}")

        traceInfo("search result:")

        // Go!
        val (retCode, stdErr, stdOut) = shCatch (b.result())

        if (retCode != 0) {
          // Exit results are always a lottery
          // Until someone digs in source, assuming '1' is 'nothing found'
          retCode match {
            case 1 => {
              traceInfo("  ** no results **")
            }
            case _ => {
              traceWarning("run errors")
            }
          }
          trace(stdErr)
        }
        else {

          // A major formatting job...

          // Split the 'grep' lines
          val lines = stdOut.split("\n")

          // We couldn't/didn't use a subprocess/switch directory
          // search.
          // So lets remove items above the search dir
          val srcPathAsStr = r.srcPath.get.toString + '/'

          val origLines = lines.filter {
            _.startsWith(srcPathAsStr)
          }

          // Split the lines
          // Split at the first and second colons
          // Return is (path, (linenumber, linedetail))
          val splitLines: Seq[(String, (String, String))] =
            origLines.map{ line =>
              val twinLine = line.splitAt(line.indexOf(':'))
              // NB: dropping the colon before the number
              val detailLine = twinLine._2.drop(1)
              val twinLine2 = detailLine.splitAt(detailLine.indexOf(':'))

              // NB: dropping the colon after the number
              (twinLine._1, (twinLine2._1, twinLine2._2.drop(1)))
            }

          // and categorise

          //linesIn.foreach{p => println(p.toString)}
          val b = scala.collection.mutable.Map[String, Seq[(String, String)]]()


          splitLines.foreach { case(pathStr, detail) =>
            //println("oh: " + p.toString)
            if (!b.contains(pathStr)) {
              b += (pathStr -> Seq.empty[(String, String)])
            }

            val newV: Seq[(String, String)] = b(pathStr) :+ detail
            b += (pathStr -> newV)

          }
          val categorised = b.toSeq

          // Trim keys (directory path strings) to subpath
          //println(s"$")
          val subpathed =
            if (config("subpath").isEmpty) categorised
            else {
              val subPathLen = config("subpath").size + 1
              categorised.map { case(k, v) =>
                (k.drop(subPathLen), v)
              }
            }


          // Add color back, if requested
          val linesColored =
            if(!noColor) {
              subpathed.map { case(pathStr, detail) =>
                // CYAN
                val pathStrC = "\u001b[36m" + pathStr
                val gseq = detail.map{ case(num, lineDetail) =>
                  // GREEN and following is MAGENTA
                  val lineNumC = "\u001b[32m" + num + "\u001b[35m"
                  // RESET
                  val lineC = "\u001b[0m" + lineDetail
                  (lineNumC, lineC)
                }
                (pathStrC, gseq)
              }
            }
            else subpathed



          // Gather everything into a string blob
          // ... and spacially format.
          val lb = new StringBuilder
          linesColored.foreach{ case(pathStr, details) =>
            lb ++= pathStr
            lb ++= "\n"

            details.foreach {case(num, line) =>
              lb ++= "  "
              lb ++= num
              lb ++= ": "
              lb ++= line
              lb ++= "\n"
            }
            lb ++= "\n"
          }


          if (subpathed.isEmpty) {
            traceInfo("  ** no results **")
          }
          else trace(lb.result())
        }
      }
    }
  }


  def categorise(
    linesIn : Traversable[Path]
  )
      : Seq[(String, Seq[String])] =
  {
    //linesIn.foreach{p => println(p.toString)}
    val b = scala.collection.mutable.Map[String, Seq[String]]()

    linesIn.foreach { p: Path =>
      //println("oh: " + p.toString)
      val parentStr = p.getParent().toString
      ////println("prnt: " + parentStr.toString)
      if (!b.contains(parentStr)) {
        b += (parentStr -> Seq.empty[String])
      }

      val newV: Seq[String] = b(parentStr) :+ p.getFileName().toString
      b += (parentStr -> newV)

    }
    b.toSeq
  }



  /** Filters a traversable using configured match values.
    *
    * 
    */
  private def filterRe[A](trav: Traversable[A], getMatchRegion: (A) => String)
      : Traversable[A] =
  {
    if(!config("get").isEmpty) {

      val re =
        if (config.asBoolean("case"))  Pattern.compile(config("get"))
        else {
          Pattern.compile(config("get"), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
        }
      val m = re.matcher("")
      println (s" mchr: $m")
      trav.filter(r => m.reset(getMatchRegion(r)).find())
    }
    else {
      if (!config("match").isEmpty) {
        val re =
          if (config.asBoolean("case"))  Pattern.compile(config("match"))
          else {
            Pattern.compile(config("match"), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
          }

        val m = re.matcher("")
        //traceWarning(s"re $re")
        trav.filter(r => m.reset(getMatchRegion(r)).matches())
      }
      else trav
    }
  }



  /** Searches for filenames in the source.
    */
  //TODO: hidden
  // TODO: trim to subpath
  def findFile() {

    // Get the basic scala routes
    val r = scalaRoute

    val root = validateDirSubpath("'findfile' request", r)
    if (root != None) {

      val res = Dir.read(root.get)



      // Do matches
      // (the Traversable tool is generous)
      // Only match against file names
      val reLines =
        filterRe[Path](res, (p: Path) => {p.base.toString})

      // Categorize results
      val categorised = categorise(reLines)

      // Trim keys (directory path strings) to subpath
      //println(s"$")
      val subpathed =
        if (config("subpath").isEmpty) categorised
        else {
          val subPathLen = config("subpath").size + 1
          categorised.map { case(k, v) =>
            (k.drop(subPathLen), v)
          }
        }

      // Add color back, if requested
      val linesColored =
        if(!noColor) {
          subpathed.map{ kv =>
            val gl = kv._2.map("\u001b[0m" + _)
            ("\u001b[36m" + kv._1, gl)
          }
        }
        else categorised

      // Gather everything into a string blob
      // ... and spacially format.
      val lb = new StringBuilder
      linesColored.foreach{ sl =>
        lb ++= sl._1
        lb ++= "\n"

        sl._2.foreach {gsl =>
          lb ++= "  "
          lb ++= gsl
          lb ++= "\n"
        }
        lb ++= "\n"
      }


      if (subpathed.isEmpty) {
        traceInfo("  ** no results **")
      }
      else trace(lb.result())
      
    }
  }



  /** Outputs a tree representation of source.
    */
  def tree() {


    val e = FindExecutable("tree").find(
      false,
      true,
      sake.util.executable.Version.empty
    )

    if(e == None) {
      traceWarning("'tree' requested, but the program 'tree' could not be found. If 'tree' is desired, please install 'tree' to the host computer.")
    }
    else {
      // Get the basic scala routes
      val r = scalaRoute

      val root = validateDirSubpath("'tree' request", r)
      if (root != None) {

        val b = Seq.newBuilder[String]

        b += "tree"

        if (config.asBoolean("dir")) {
          b += "-d"
        }

        if (!config("get").isEmpty) {
          // Use a glob. Spec allows it (though we ought to strip glob-significant chars like wildcards).
          // However, we must allow content-free globbing
          b += "-P"
          b += "*" + config("get") + "*"
        }

        if (!config.asBoolean("hidden")) {
          b += "-I"
          b += "*~"
          // NB: Tree is unable to find hidden Linux files.
          //b += "-I"
          //b += ".*"
        }

        if (noColor) b += "-n"
        else b += "-C"

        b += root.get.toString

        //println(s"line: ${b.result()}")
        shPrint(b.result())
      }
    }
  }



  /** Produces compiled class files.
    */
  def compile() {
    // Get the basic scala routes
    val r = scalaRoute

    if (sourcePathExists("compile", r)) {
      doScalaCompile(r)
    }
  }






  def runK()
  {
    //NB. This only can use scala, so simpler.
    //TODO: Will it run java classes?

    if(assertCompile("'run' request", scalaRoute)) {
      val c = config("class")

      if (c.isEmpty) {
        traceError("Please add classnames to a 'run' task. Use the switch -class <classname>")
      }
      else {

        val b = buildScalaStandardOptions(
          "scala",
          None,
          true
        )
        b += "-howtorun:object"
        b += "-nc"
        b += c
        //println(s"run : ${b.result()}")
        //val r = java.lang.Runtime.getRuntime()
        //r.exec(b.result.toArray)
        //r.exec("ls -l &")
        val (retCode, stdErr, stdOut) = shCatch (b.result())
        trace(stdErr)
        trace(stdOut)

        if (retCode != 0) {
          traceWarning("run errors")
        }
      }
    }
  }


  /** Produces introspective output.
    */
  //TODO:: Make work for java, too?
  def introspect()
  {
    // Get the basic scala routes
    //NB. This only can use scala, so simpler.
    val r = scalaRoute

    // ensure compiled classes exist
    if (compileIfNotPopulated("'introspect' request", r)) {

      val cps = config.asSeq("classes")

      if (cps.isEmpty) {
        traceError("Please add classnames to an 'introspect' task. Use switch -classes <list of class names>")
      }
      else {

        val b = Seq.newBuilder[String]
        b += "scalap"

        if (config.asBoolean("private")) {
          b += "-private"
        }

        if (config.asBoolean("verboseTools")) {
          // Scalap verbose adds little beyond the titles CLASSPATH
          // and FILENAME, but be consistent...
          b += "-verbose"
        }

        // NB: classpath probe down for Scala and Java files.
        compiledClasspaths.foreach { p =>
          b += "-classpath"
          b += p.toString
        }

        cps.foreach{ cp =>
          b  += cp
        }

        //println(s"cps $cps")
        //println(b.result())
        traceInfo("scalap out:")
        shPrint(b.result())
      }
    }
  }


  /** Produces bytecode output.
    */
  //TODO: Could work for java too, unlike 'introspect'
  def bytecode()
  {
    // Get the basic scala routes
    val r = scalaRoute

    // ensure compiled classes exist
    if (compileIfNotPopulated("'bytecode' request", r)) {

      val cps = config.asSeq("classes")

      if (cps.isEmpty) {
        traceError("Please add classnames to a 'bytecode' task. Use switch -classes <list of class names>")

      }
      else {

        val b = Seq.newBuilder[String]
        b += "javap"

        if (config.asBoolean("methodfields")) {
          b += "-l"
        }
        if (config.asBoolean("public")) {
          b += "-public"
        }
        if (config.asBoolean("protected")) {
          b += "-protected"
        }
        if (config.asBoolean("private")) {
          b += "-private"
        }
        if (config.asBoolean("systemInfo")) {
          b += "-sysinfo"
        }
        if (config.asBoolean("classInfo")) {
          b += "-verbose"
        }

        // All-important -c option
        // If not, this is a rubbish version of scalap
        b += "-c"

        // libs
        if (libPath != None) {
          b += "-extdirs"
          b += libPath.toString
        }

        // NB: classpath probe down for Scala and Java files.
        compiledClasspaths.foreach { p =>
          b += "-classpath"
          b += p.toString
        }

        // Classes to be disassembled
        cps.foreach{ cp =>
          b  += cp
        }

        //println(s"cps $cps")
        //println(b.result())
        traceInfo("javap out:")
        shPrint(b.result())
      }
    }
  }


/*


  def repl()
  {


    println(s"settings:  ${settings.bootclasspath}, ${settings.classpath}, ${settings.Yreplsync}, ${settings.Xnojline}")

  }
*/

import java.io.{ File }
import scala.tools.nsc.util.{ ClassPath, ScalaClassLoader }
import scala.tools.nsc.Properties.{ versionString, copyrightString }
import scala.tools.nsc.GenericRunnerCommand._
import scala.tools.nsc.GenericRunnerCommand
import scala.tools.nsc.interpreter.ILoop
import scala.tools.nsc._

  def errorFn(str: String): Boolean = {
    Console.err println str
    false
  }

  def errorFn(ex: Throwable): Boolean = {
    ex.printStackTrace()
    false
  }

  def repl()
: Boolean =
  {
println("home:" + System.getProperty("java.home"))
val args = Array[String](
""
)

    val command = new GenericRunnerCommand(args.toList, (x: String) => errorFn(x))
val settings = command.settings
    def sampleCompiler = new Global(settings) 

    if (!command.ok) return errorFn("\n" + command.shortUsageMsg)
    else if (settings.version) return errorFn("Scala code runner %s -- %s".format(versionString, copyrightString))
    else if (command.shouldStopWithInfo)  return errorFn(command getInfoMessage sampleCompiler)

//settings.loadfiles.isDefault = false
//settings.loadfiles.value = Nil
//settings.Xnojline = true
println(s"settings.loadfiles.isDefault: ${settings.loadfiles.isDefault}")
    println(s"settings:  ${settings.bootclasspath}, ${settings.classpath}, ${settings.Yreplsync}, ${settings.Xnojline}")
      //settings.Yreplsync.value = true
val i : Boolean = new ILoop process settings
i 
  }


  def scalaTest()
  {
    // ensure compiled src classes exist
    if(assertCompile("'scalaTest' 'compile sources' request", scalaRoute)) {


      // ensure compiled test classes exist
      val r = scalaTestRoute
      if(assertCompile("'scalaTest' 'compile test' request", r)) {

        // Find the executable
        val exec: String =
          if (!config("scalaTestExe").isEmpty) config("scalaTestExe")
          else {
            if (libPath != None) {
              val inLibJar = Dir.read(libPath.get).find{ p =>
                p.getFileName.toString.startsWith("scalatest")
              }

              if (inLibJar != None)  inLibJar.get.toString
              else ""
            }
            else ""
          }

        if (exec.isEmpty) {
          traceWarning("'test' running requested, but a library 'scalaTest' could not be found. Either place a jar in the project library, or provide a path using the switch -scalaTestDir")
        }
        else {
          traceInfo("scalaTest executable found!")

          val b = Seq.newBuilder[String]
          b += "scala"
          b += "-classpath"
          b += exec

          // Hokay, add the classpaths to compiled code
          // NB: classpath probe down for Scala and Java files.
          //NB. NB. It's important, because scalatest gets confused, that these are -toolcp.
          compiledClasspaths.foreach { p =>
            b += "-toolcp"
            b += p.toString
          }

          // Add the runner
          b += "org.scalatest.tools.Runner"

          // ...and the -R thing
          b += "-R"


          // ...and the directory with the tests in
          b += r.buildPath.get.toString

          //...and the output reporter
          // decide where output goes
          // Argument to Runner, must go afterwards.
          var reporter =
            config("to") match {
              case "gui" => "-g"
              case "out" => "-o"
              case "err" => "-e"
              case _ => "-o"
            }

          // Quietly ignore the gui issue
          // ...let people have the commandline history.
          if(noColor && config("to") != "gui") {
            reporter = reporter + "W"
          }

          b += reporter

          // ...and finally the  target options.
          if (!config("suite").isEmpty) {
            b+= "-s"
            b += config("suite")
          }
          if (!config("suiteFrag").isEmpty) {
            b += "-w"
            b += config("suiteFrag")
          }
          if (!config("name").isEmpty) {
            b += "-t"
            b += config("name")
          }
          if (!config("nameFrag").isEmpty) {
            b += "-z"
            b += config("nameFrag")
          }
          /*
           scala -toolcp /home/rob/Code/sake/build/main/scala/ -classpath lib/scalatest_2.11-2.2.4.jar org.scalatest.tools.Runner -o -R "/home/rob/Code/sake/build/test/scala"
           List(scala, -classpath, lib/scalatest_2.11-2.2.4.jar, org.scalatest.tools.Runner, -R, /home/rob/Code/sake/build)
           */


          val (retCode, stdErr, stdOut) = shCatch (b.result())
          trace(stdErr)
          trace(stdOut)
          //println(s"scalatest line: ${b.result()}")
          if (retCode != 0) {
            traceError("errors from ScalaTest?")
          }
        }
      }
    }
  }


  /** Produces a library jar file.
    */
  def jar()
  {
    // Get the basic scala routes
    val r = scalaRoute

    // ensure compiled classes exist
    if(assertCompile("'jar' request", r)) {

      // Create a manifest file
      val b = Seq.newBuilder[String]

      b += "Manifest-Version: 1.0"
      b += "Implementation-Version: " + config("appVersion")
      b += "Specification-Title: " + config("appName")
      b += "Specification-Version: " + config("appVersion")

      if (!config("classpaths").isEmpty) {
        b += "Class-Path: " + config.asSeq("classpaths").mkString(" ")
      }

      if (!config("mainClass").isEmpty) {
        b += "Main-Class: " + config("mainClass")
      }


      Entry.write(
        cwd.resolve("MANIFEST.MF"),
        b.result()
      )


      // TODO: permissions?


      // Now make a jar file
      // NB: the 'jar' tool silenty replaces existing
      // jars, which is what we would like.
      b.clear
      b += "jar"

      var switches = "cfm"
      if (config.asBoolean("verboseTools")) {
        switches = switches + "v"
      }
      if (config.asBoolean("uncompressed")) {
        switches = switches + "0"
      }
      b += switches

      // TODO: Scala version in title?
      val jarFileName =
        if (config.asBoolean("noVersionTitle")) {
          config("appName") + "_" + config("appVersion") + ".jar"
        }
        else config("appName") + ".jar"
      b += jarFileName

      b += "MANIFEST.MF"



      // NB: 'relative path'  probe down to include Scala and Java files.
      compiledClasspaths.foreach { p =>
        // Also, the all-important '.'
        b += "-C"
        b += p.toString
        b += "."
      }

      traceInfo("building jar...")
      sh (b.result())

      // Cleanup
      Entry.delete(cwd.resolve("MANIFEST.MF"))
    }
  }

  def run() {
    //trace(s"action route...$taskName")

    taskName match {
      case "clear" => clear()
      case "clean" => clean()
      case "introspect" => introspect()
      case "bytecode" => bytecode()
      case "repl" => repl()

      case  _ => {

        // Test sourcepaths for these tasks
        if(sourcePathVerify()) {
          taskName match {
            case "find" => find()
            case "findfile" => findFile()
            case "tree" => tree()
            case "compile" => compile()
            case "test" => scalaTest()
            case "doc" => doc()
            case "run" => runK()
            case "jar" => jar()

            case  _ => traceError(s"Unrecognised task? $taskName")
          }
        }
      }
    }
//sys.exit(1)
  }

}//Action

