package ssc

import sake.util.parser.ParameteredOption



/** Configurations for SSC.
  *
  * Look at this file in source to see what settings can be altered by
  * commandline and configuration file. There are detailed notes,
  * synchronized with those in the sample configuration file.
  */
object Configuration {

  /** Override a default configuration with some altered values.
    *
    * The default configuration duplicates values across task groups.
    * While this is nicely namespaced, and no big effort?, it means
    * similar values appear in different groups. For example,
    * 'scalaSrcDir' appears in any task configuration which may
    * provoke compiling.
    * 
    * Modifying a frequently-occuring value by duplicating into every
    * task group is tedious and error-prone. So this method wipes
    * across all task groups (but double check that key names are not
    * duplicated, and thus will cause unexpected modifications in some
    * groups)
    */
  private def overrideDefaults(
    default: ConfigMap,
    overrides: Map[String, Seq[String]]
  )
      : ConfigMap =
  {
    default.map{ case(gk, gv) =>
      val newGV =
        gv.map{ case(k, v) =>
          //println(s"overrides.contains($k) ${overrides.contains(k)}")
          val  newV =
            if (overrides.contains(k)) overrides(k)
            else v
          (k -> newV)
        }
      (gk -> newGV)
    }
  }

  private def mavenOverrides = Map[String, Seq[String]](
    "-scalaSrcDir" -> Seq("/src/main/scala"),
    "-javaSrcDir" -> Seq("/src/main/java"),
    "-testDir" -> Seq("src/test/scala"),
    "-charset" -> Seq("UTF8"),

    // Building //
    "-buildDir" -> Seq("target"),
    "-libDir" -> Seq("lib")
  )

  /** Returns a default configuration enforcing Maven conventions.
    * 
    * The config somewhat cripples Scala and ssc, of course.
    */
  def maven : ConfigMap = overrideDefaults(default, mavenOverrides)

  /** Generates a default configuration for ssc.
    * 
    * Generated from commandline switch data. See [[ssc.CLSchema]] for
    * the source material.
    */
  def default: ConfigMap =
    CLSchema.taskSwitches.map{ case(task, optionMap) =>
      val defaultMap = optionMap.map{ case(k, v) => (k, v.default)}
      (task -> defaultMap)
    }

  /** Returns a seq of the tasks available.
    * 
    * Generated from commandline switch data. See [[ssc.CLSchema]] for
    * the source material.
    */
  val tasks : Seq[String] = CLSchema.taskSwitchSeq.keys.toSeq

}//Configuration
