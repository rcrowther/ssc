package ssc

import scala.util.matching.Regex


/** Parse an init file.
  *
  * Very simple. However, will handle uneven indentation, empty lines,
  * and comments.
  *
  * Handles it's own message output.
  *
  * @param data lines from a file with an `=` char as separater.
  */
class ParseIni(
  data: Traversable[String],
  val verbose: Boolean,
  val noColor: Boolean
)
    extends sake.Trace
{

  /** Tidy the data
    *
    * Skips empty and commented lines, trims data then splits into
    * key -> vals.
    *
    * Note: very irregular, this adds the switch symbol back to
    * recognised switches.
    */
  def clean()
      : ConfigMap =
  {
    val b = Map.newBuilder[String, Map[String, Seq[String]]]
    val gb = Map.newBuilder[String, Seq[String]]
    var currentGroup: String = ""
    var first = true
    data.foreach{line =>
      val trimmed = line.trim

      // Only try if not empty and not a comment
      // NB, for some reason this didn't short ciruit, and cost me time.
      if (!trimmed.isEmpty && !(trimmed.charAt(0) == '#')) {
        if (trimmed.charAt(0) == '[') {
          // is a group
          if (first) first = false
          else {
            b += (currentGroup -> gb.result())
            gb.clear()
          }
          currentGroup = trimmed.slice(1, trimmed.size - 1)
        }
        else {
          // is a keyvalue
          val e: Array[String] = trimmed.split('=')
          if (e.size != 2) traceWarning(s"Ignored line in config file: line would not split with '=': $trimmed")
          else {
            //gb += (e(0).trim -> e(1).trim)
            gb += ({'-' + e(0).trim} -> e(1).split(',').map(_.trim))
          }
        }
      }
    }

    b += (currentGroup -> gb.result())
    b.result
  }


  def parse() //(verifyKeys: Seq[String])
      : ConfigMap =
  {
    clean()
  }

}//ParseIni
