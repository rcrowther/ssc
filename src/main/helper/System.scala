package sake.helper


import java.io.File
import java.nio.file.{Paths, Path, Files}
import sys.process._

/** Provides commands for inquiring about the system external to code.
  */
object System {


  /** Returns the directory from which this code was launched.
    * 
    * Note that the site of code launch is not the same as the site of
    * the code itself. If a terminal is used for launching, the
    * returned path is the current site of the terminal, often called
    * the `current working directory`.
    */
  //  Hopefully. Not tested outside Unix.
  def launchDirectory()
      : Path =
  {
    Paths.get(".").toAbsolutePath().normalize()
  }

  /** Returns the /bin of the directory above where this code is sited.
    * 
    * This code assumes the code has been compiled and packaged to a
    * .jar file sited in a /bin directory.
    *
    * First, it seeks the URL of where this object is sited, then
    * backs up to /bin, and strips any hosts. End result should be the
    * parent folder, granted that this code is finally compiled to a
    * /bin folder.
    *
    * The code is fairly reliable, but not guaranteed.
    *
    * @return the path of a /bin file above the code.
    */
  // http://stackoverflow.com/questions/320542/how-to-get-the-path-of-a-running-jar-file
  def codeDirectory()
      : Path =
  {
    // Get code location
    val url = this.getClass.getResource(this.getClass.getSimpleName() + ".class").toString()

    // Strip anything up to and after '/bin'
    val parents = url.lastIndexOfSlice("bin")
    val parentUrl =
      if (parents != -1) {
        url.take(parents)
      }
      else throw new Exception("unable to find /bin")

    //println(s"parentUrl $parentUrl")

    // Optionally strip hosts
    val hosts = parentUrl.toString().lastIndexOf(':') + 1
    val urlStr =
      if (hosts != -1) {
        parentUrl.drop(hosts)
      }
      else parentUrl

    //println(url.toString().drop(hosts))

    val ccd2 = new File(urlStr)
    //println(ccd2)
    ccd2.toPath()
  }

}//System

