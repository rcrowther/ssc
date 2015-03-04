package sake

import java.nio.file.Paths
import java.nio.file.Path
import java.io.File
import java.nio.charset.Charset

//import sake.helper.noThrow.Entry
//import sake.helper.Dir
import sake.helper.noThrow.Shell

import sake.support.parser.CLParser
import sake.helper.file._


/** Find a SakeFile.txt, and run it.
  *
  */
object SakeFileRunner
    extends Trace
    with CLParser
    with Shell
{
  //TODO: pass in tasks too

  // Set these for Trace, until args are parsed.
  var noColor: Boolean = false
  var verbose: Boolean = true
  
  // This runner needs these
  val cwd : Path = Paths.get(".").toAbsolutePath().normalize()

  val ccd: Path = {
    val prop = java.lang.System.getProperty("sake.runner.home")
    if (prop == null) {
      throw new Exception("The system property 'sake.runner.home' is not available.")
    }
    else new java.io.File(prop).toPath
  }

  val scalaHome: Path = {
    val prop = java.lang.System.getProperty("scala.home")
    if (prop == null) {
      throw new Exception("The system property 'scala.home' is not available.")
    }
    else new java.io.File(prop).toPath
  }

  val sakeHome: Path = {
    val prop = java.lang.System.getProperty("sake.home")
    if (prop == null) {
      throw new Exception("The system property 'sake.home' is not available.")
    }
    else new java.io.File(prop).toPath
  }

  val tmpDir: Path = {
    val prop = java.lang.System.getProperty("sake.runner.tmp")
    if (prop == null) {
      // if unset, set to script.home/tmp
      ccd.resolve("tmp")
    }
    else new java.io.File(prop).toPath
  }


  /** Finds a class file in the cache, and returns the classname and package path it represents.
    *
    * @param className the stated name of the class (with no file extensions)
    */
  private def findPrecompiledClass(
    className: String
  )
      : Option[Path] =
  {
    val sakeClassFileName = className + ".class"
    Dir.readEntry(tmpDir).find(_.toString.endsWith(sakeClassFileName))
  }



  ///////////////////////////////////
  // Contact point for sake runner //
  ///////////////////////////////////

  def main(inputArgs: Array[String]) {
    //val SAKE_HOME = "/home/rob/Code/sake/target/scala-2.11/ssc_2.11-0.1.0-SNAPSHOT.jar"
    //val SCALA_HOME = "/home/rob/Deployed/scala-2.11.4/"
    //println(s"ccd $ccd")
    //println(s"sakeHome $sakeHome")
    //println(s"scalaHome $scalaHome")
    //println(s"tmpDir $tmpDir")

    var compileOnly = false

    // Test for, and if necessary apply, the -verbose option,
    // this applying to the runner too.
    verbose = inputArgs.contains("-verbose")
    noColor = inputArgs.contains("-noColor")

    // Assert the tmpdir
    if(!Dir.exists(tmpDir)){
      traceWarning("tmpdir for sake does not exist. Attempting to create at $tmpDir")
      try {
        Dir.create(tmpDir)
      }
      catch {
        case e: Exception =>
          traceError("tmpdir creation failed. Quitting.")
      }
    }

    if(Dir.exists(tmpDir)){
      // Filter runner switches
      val (otherArgs, runnerSwitches) = parseAllArgs(
        schema = CLSchema.RunnerSchema,
        args = inputArgs
      )

      if (!runnerSwitches.isEmpty) {
        runnerSwitches.foreach { s =>
          s match {
            case "-cacheClear" =>
              traceInfo("cleaning the cache")
              //println(tmpDir)
              Dir.clear(tmpDir)

            case "-cacheList" =>
              traceInfo("cache listing:")
              Dir.readEntry(tmpDir).map(_.getBase).foreach {baseO =>
                if (baseO != None) {
                  val base = baseO.get
                  // Filter Scala autogenerated classes/files using '$'
                  if (!base.contains('$')) trace(base)
                }
              }
          }
        }
      }
      else {

        // read the SakeFile
        val possibleFile: Option[Path] = Dir.readEntry(cwd, 1).find(_.getExtension == Some("sake"))

        //traceInfo(s"possibleFile to run is: ${possibleFile.toString}")
        val srcPath : Path =
          if (possibleFile != None) {
            possibleFile.get
          }
          else {
            throw new Exception(s"No Sake file found (or readable) in: $cwd")
          }

        traceInfo(s"file to run is: ${srcPath.toString}")

        val sakeClassName : String = srcPath.base
        //traceInfo(s"class to run is: ${sakeClassName}")

        // Ok, look for a compiled version
        val preCompiledO : Option[Path] = findPrecompiledClass(sakeClassName)

        val preCompiledAssertedO =
          if(preCompiledO != None) {
            traceInfo("using a preloaded file. If this is wrong, please clean the cache using 'sake clean'")
            preCompiledO
          }
          else {
            traceInfo("compiling...")
            // time for a compile
            val b = Seq.newBuilder[String]

            b += {scalaHome + "/bin/scalac"}
            //b += "-verbose"
            // Destination for compiles
            b += "-d"
            //TODO: why tools?
            b += tmpDir.toString
            // Sake on the classpath
            //TODO: This will be in the lib, but not now
            b += "-toolcp"
            b += sakeHome.toString
            // And a source, the SakeFile
            b += srcPath.toString

            sh (b.result()) match {
              case Left(x) => traceInfo("compilation errors")
              case Right(x) => traceInfo("done")
            }
            /*
             scalac -d /home/rob/Code/sake/src/main/scala/sake/tools/tmp -toolcp /home/rob/Code/sake/target/scala-2.11/ssc_2.11-0.1.0-SNAPSHOT.jar /home/rob/Code/saketest/SakeFile.scala
             */
            //            println(s"compile: ${b.result()}")
            // trying again...
            findPrecompiledClass(sakeClassName)
          }


        if (preCompiledAssertedO == None) {
          // Barring serious malfunction, we have a class file,.
          traceError(s"No compiled classfiles were found for $sakeClassName.\nThis is exceptional, and suggests permission or installation problems, etc.")
        }
        else {
          // Ok, find the packaged path for a scala invokation in the shell.
          val preCompiled = preCompiledAssertedO.get

          // ...which should be relative to the tmp folder
          val preCompiledFromTmp = preCompiled.subpath(tmpDir.getNameCount(), preCompiled.getNameCount())

          // ...and needs slashes replacing with dots
          val packagedClass = preCompiledFromTmp.toString.replace(File.separator, ".")

          // Remove the '.class' extension
          val packagedClassName = packagedClass.take(packagedClass.size - 6)
          //println(s"packagedClassName $packagedClassName")


          if(!compileOnly) {
            // Alright. Running
            traceInfo("running...")
            val b = Seq.newBuilder[String]
            //set destination
            b += {scalaHome + "/bin/scala"}
            // Tell the scala runner the target is classfiles
            b += "-howtorun:object"
            // Sake on the bootclasspath
            //TODO: This will be in the lib, but not now
            b += "-bootclasspath"
            b += sakeHome.toString
            // Tmp dir on the classpath
            b += "-classpath"
            b += tmpDir.toString
            // Add property args
            //b += "-D"
            b += {"-Dsake.runner.home=" + ccd}
            //b += "-D"
            b += "-Dsake.runner.args=niggle|peeps"
            // Target 'script' on the classpath
            //b += SAKE_CLASS
            b += packagedClassName

            // Ah, and the args
            b ++= otherArgs
            /*
             scala -howtorun:object -bootclasspath /home/rob/Code/sake/target/scala-2.11/ssc_2.11-0.1.0-SNAPSHOT.jar -classpath /home/rob/Code/sake/src/main/scala/sake/tools/tmp -Dsake.runner.home=/home/rob/Code/sake/src/main/scala/sake -Dsake.runner.args="niggle|peeps" sake.MySake

             */
            //println(s"run: ${b.result()}")

            val res = shPrint(b.result()) match {
              case Left(x) => traceInfo("compilation errors")
              case Right(x) => traceInfo("done")
            }
          }
        }
      }
    }
  }

}//SakeFileRunner
