package sake.helper.executable

import java.nio.file.{Path, Files}
import sake.helper.file._

object Java
    extends FindExecutable
{

  def version : Option[Version] =
  {
    // Yup, java version goes to stdErr, on Linux...
    shCatch (Seq("java", "-version"), false, true) match {
      case Left(e) => None
      case Right(str) => {
        val openB = str.indexOf('"')
        val closeB = str.indexOf('"', openB + 1)
        // Little check so we are ok
        if (openB < closeB) {
          Some(Version(str.slice(openB + 1, closeB)))
          }
        else None
      }
    }
  }


  def find(
    customPath: Path,
    verbose: Boolean,
    required: Boolean,
    versionRequired: Version
  )
      : Option[ExecuteData] =
  {
    val p =
      if(!customPath.isEmpty) customPath
      else {
        // Try for JAVA_HOME
        val env : String =
          try {
            val v = System.getenv("JAVA_HOME")
            if (v == null) ""
            else v
          }
          catch {
            case e: Exception => ""
          }

        (env + "/bin/java").toPath
      }
    if (Files.isExecutable(p)) {
    Some(ExecuteData(p, version))
    }
    else None
  }

  /** Produce a library jar file.
    *
    * The jar is placed in cwd. Could be anyplace, but untested.
    *
    * @param cwd currentWorkingDirectory
    * @param manifestData a seq of strings for the manifest, one per line
    * @param compiledClassDirectory where to find class files 
    * @param uncompressed dont compress the jar
    */
  def libraryJar(
    cwd: Path,
    fileName: String,
    manifestData :Seq[String],
    compiledClassDirectory : Path,
    uncompressed: Boolean
  )
  {

    // Write the manifest file

    Entry.write(
      cwd.resolve("MANIFEST.MF"),
      manifestData
    )

    // TODO: permissions?

    //Now make a jar file
    val b = Seq.newBuilder[String]
    b += "jar"
    var switches = "cfm"
    if (uncompressed) switches = switches + "0"
    b += switches
    b += fileName
    b += "MANIFEST.MF"
    b += "-C"
    b += compiledClassDirectory.toString
    b += "."
    sh (b.result())

    // Cleanup
    Entry.delete(cwd.resolve("MANIFEST.MF"))
  }

}//FindJava
