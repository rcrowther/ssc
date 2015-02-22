package ssc

import sake.util.file._
import java.io.File
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.Path


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
  *  - understand which task is intended
  *  - selected the appropriate config from a projectConfig map, by task
  *  - if appropriate, overlaid the taskConfig with commandline options (some auto-actioning may not require this)
  *  - will invoke the class with the taskName.
  *
  * Instances of the class handle their own output of report
  * information.
  *
  * @param cwd the directory to work actions from. All
  *  actions are relative from this value.
  * @param config a set of values to modify the actions
  *  according to expressed preferences.  
  */
class Action(
  cwd: Path,
  config: Config
)
    extends sake.Trace
    with sake.util.noThrow.Shell
{
  protected val inColor: Boolean = config.asBoolean("inColor")
  protected val verbose: Boolean = config.asBoolean("verbose")

  ///////////
  // Utils //
  ///////////

  private def scalaSrcPath : Option[Path] = dirFind("scalaSrcDir", config.asSeq("scalaSrcDir"))
  private def buildPath : Path = cwd.resolve(config("buildDir"))
  private def docPath : Path = cwd.resolve(config("docDir"))
  private def libPath : Option[Path] = dirFind("libDir", config.asSeq("libDir"))

  private def dirEntryPaths(path: Path, filter: String)
      : Traversable[Path] =
  {
    val paths = Dir.readEntry(path)
    paths.filter{p =>p.getFileName().toString.endsWith(filter)}
  }

  /**
    * This takes an optional path, and returns an empty traversable
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


  /** Finds source files modified after classfiles.
    */ 
  // TODO: This seems to pick up stuff scalac doesn't compile, e.g.
  // orphans and configs. Bothered?
  private def incrementalSrcs(
    srcPath: Path
  )
      : Traversable[String] =
  {
    traceInfo("using incremental compile")
    val allCompiledPaths : Traversable[(Path, BasicFileAttributes)] = dirEntryPathsAndAttributes(cwd.resolve(config("buildDir")), ".class")

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
    val allCompiledPathsMap : Map[String, java.nio.file.attribute.FileTime]  = b.result()
    //println(s"allCompiledPathsMap: $allCompiledPathsMap")
    //println
    val allSrcPaths = dirEntryPathsAndAttributes(srcPath, ".scala")

    allSrcPaths.filter{ pa =>
      // Convert "path/X.scala" to "path/X.class"
      val fileName = pa._1.getFileName().toString
      val className = fileName.substring(0, fileName.lastIndexOf('.'))
      if(!allCompiledPathsMap.contains(className)) true
      else {

        // Compare class creation time to file modified time.
        // if file modified later, include.
        val classCreationTime: java.nio.file.attribute.FileTime = allCompiledPathsMap(className)
        val srcModificationTime: java.nio.file.attribute.FileTime = pa._2.lastModifiedTime()
        // if (srcModificationTime > classCreationTime) {
        //  println(s"$className\n srcModificationTime: $srcModificationTime classCreationTime: $classCreationTime, ")
        // }

        srcModificationTime > classCreationTime
      }
    }.map(_._1.toString)
  }


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

  private def dirIsPopulated(buildPath: Path, extension: String)
      : Boolean =
  {
    !Dir.read(buildPath).filter(_.toString.endsWith(extension)).isEmpty
  }
  
  private def dirFind(requester: String, pathStrs: Seq[String])
      : Option[Path] =
  {
    val pathO = pathStrs.find{ pathStr =>
      Dir.exists(pathStr.toPath)
    }
    if (pathO == None){
      val ps = pathStrs.mkString(", ")
      traceInfo(s"$requester could not find an existing directory in the sequence: $ps")
      None
    }
    else {
      val pathStr = pathO.get
      traceInfo(s"$requester is $pathStr")
      Some(pathStr.toPath)
    }
  }


  private def quote(b: StringBuilder, str: String)
      : StringBuilder =
  {
    b += '"'
    b ++= str
    b += '"'
    b
  }

  private def quote(str: String)
      : String =
  {
    '"' + str + '"'
  }



  /////////////////////
  // Option builders //
  /////////////////////


  /** Builds standard scala commandline options.
    * 
    * These are generated from configuration data.
    *
    * @param executable the name of the tool to  be invoked via the shell.
    * @param destination the destination for material generated
    *  by the action. May be documentation, compiled files, etc.
    * @param usePrecompiled adds the current build path as a "-classpath"
    */
  private def buildScalaStandardOptions(
    executable : String,
    destination : Option[Path],
    usePrecompiled : Boolean
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
    // Needs ignoring for soft scaladocing?

    if (usePrecompiled) {
      b += "-classpath"
      b += cwd.resolve(config("buildDir")).toString
    }

    b
  }

  /** Builds a commandline for source files.
    *
    * Will only append source files updated compared to the classfile,
    * unless forced with `strict`.
    *
    * @param useAllSrcFiles forces all source files onto the builder,
    *  avoiding incremental compilation.
    * @return true if options were added, else false.
    */
  private def buildScalaSrcOptions(
    srcPath: Path,
    b: scala.collection.mutable.Builder[String, Seq[String]],
    incrementalCompile : Boolean
  )
      : Boolean =
  {
    // Gather srcpaths
    val allSrcPaths = dirEntryPaths(scalaSrcPath, ".scala").map{ p =>
      p.toString
    }
    // TODO: Make non-recent, or complete...
    val targetSrcPaths =
      if (incrementalCompile) {
        incrementalSrcs(srcPath)
      }
      else  allSrcPaths
    
    b ++= targetSrcPaths
    !targetSrcPaths.isEmpty
  }

  //*** Where to test? ***
  def createDir() {
    val p = cwd.resolve("doc")
    Dir.create(p)
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

    /*
     if (!config("diagrams").isEmpty) {
     b +=  "-diagrams"
     b += "-diagrams-dot-path"
     b += config("executable")
     b += "-diagrams-dot-timeout"
     b += config("diagramTimeout")
     b += "-diagrams-max-classes"
     b += config("diagramMaxClasses")
     }
     */
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
  def doCompile()
      : Boolean =
  {

    // Empty the build directory, if not incrementally compiling
    if(!config.asBoolean("incremental") && dirIsPopulated(buildPath, ".class")) {
      Dir.clear(buildPath)
    }


    // Build some compile options
    val b = buildScalaStandardOptions(
      "scalac",
      Some(cwd.resolve(config("buildDir"))),
      true
    )

    appendCompileOptions(b)
    //println("scalac line:" + b.result())

    // Maybe incremental compile, maybe full
    val needsCompiling = buildScalaSrcOptions(
      scalaSrcPath.get,
      b,
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

  def assertCompile(requester: String)
      : Boolean =
  {
    if(dirIsPopulated(buildPath, ".class")) {
      traceInfo(s"$requester is working from classes in ${buildPath.toString}")
      true
    }
    else {
      if (scalaSrcPath == None) {
        val configPaths = config.asSeq("scalaSrcDir").mkString(", ")
        traceInfo(s"$requester is requesting a compile, but no source directories can be found in the configuration options: ${configPaths}")
        false
      }
      else {
        traceInfo(s"$requester has no classes to work from, is forcing compile...")
        doCompile()
      }
    }
  }



  ////////////////
  // Root tasks //
  ////////////////

  /** Produces documentation in docDir.
    */
  def doc() {
    if (scalaSrcPath == None) {
      val configPaths = config.asSeq("scalaSrcDir").mkString(", ")
      traceWarning(s"A 'doc' task has been requested, but no source directories can be found in the configuration options: ${configPaths}")
    }
    else {

      Dir.create(docPath)

      val b = buildScalaStandardOptions(
        "scaladoc",
        Some(cwd.resolve(config("docDir"))),
        false
      )

      appendDocOptions(b)

      // Add paths for source files all sources
      buildScalaSrcOptions(
        scalaSrcPath.get,
        b,
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
    traceInfo(s"emptying build directory $buildPath...")
    Dir.clear(buildPath)
  }


  /** Deletes all ssc-generated material.
    * 
    * Skips '.jar' files (we don't know their name) and build.ssc
    * definitions.
    */
  def clean() {
    try {
      traceInfo(s"deleting documentation directory $docPath...")
      Dir.delete(docPath)
    } catch {
      case e: Exception => println("Was:" + e.getMessage)
    }
    traceInfo(s"deleting build directory $buildPath...")
    Dir.delete(buildPath)
  }


  /** Produces compiled classes.
    */
  def compile() {
    if (scalaSrcPath == None) {
      val configPaths = config.asSeq("scalaSrcDir").mkString(", ")
      traceWarning(s"A 'compile' task has been requested, but no source directories can be found in the configuration options: ${configPaths}")
    }
    else {
      doCompile()
    }
  }

  def runK()
  {
    if(!dirIsPopulated(buildPath, ".class")) {
      traceWarning(s"A 'run' task has been requested, but no class files can be found in the build directory: ${buildPath.toString}")

    }
    else {
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

  /** Produces a library jar file.
    */
  def jar()
  {
    // ensure compiled classes exist
    if(assertCompile("'jar' request")) {

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
      b += "-C"
      b += config("buildDir")
      b += "."

      traceInfo("building jar...")
      sh (b.result())

      // Cleanup
      Entry.delete(cwd.resolve("MANIFEST.MF"))
    }
  }


  /** Produces introspective output.
    */
  def introspect()
  {

    // ensure compiled classes exist
    if (assertCompile("'introspect' request")) {

      val cps =  config.asSeq("classnames")

      if (cps.isEmpty) {
        traceError("Please add classnames to an 'introspect' task. Use switch -classnames <list of class names>")

      }
      else {

        val b = Seq.newBuilder[String]
        b += "scalap"

        if (config.asBoolean("private")) {
          b += "-private"
        }
        // Scalap verbose adds little beyond the titles CLASSPATH
        // and FILENAME, so use it anyway.
        b += "-verbose"

        b += "-classpath"
        b += cwd.resolve(config("buildDir")).toString

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
  def bytecode()
  {

    // ensure compiled classes exist
    if (assertCompile("'bytecode' request")) {

      val cps =  config.asSeq("classnames")

      if (cps.isEmpty) {
        traceError("Please add classnames to a 'bytecode' task. Use switch -classnames <list of class names>")

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
        b += "-c"

        // libs
        if (libPath != None) {
          b += "-extdirs"
          b += libPath.toString
        }


        b += "-classpath"
        b += cwd.resolve(config("buildDir")).toString

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

  def run(taskName:String) {
    //trace(s"action route...$taskName")
    taskName match {
      case "clear" => clear()
      case "clean" => clean()
      case "compile" => compile()
      case "doc" => doc()
      case "run" => runK()
      case "jar" => jar()
      case "introspect" => introspect()
      case "bytecode" => bytecode()

      case  _ => traceError(s"Unrecognised task? $taskName")
    }
  }

}//Action

