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
// TODO: Ought to return Either, not inherit Trace
class ParseIni(
  data: Traversable[String],
  val verbose: Boolean,
  val noColor: Boolean
)
    extends script.io.Trace
{

  /** Tidy the data
    *
    * Skips empty and commented lines, trims data then splits into
    * key -> vals.
    *
    * Note: very irregular, this adds the switch symbol back to
    * recognised switches.
    */
  def parse()
      : ConfigMap =
  {
    val b = Map.newBuilder[String, Map[String, Seq[String]]]
    val gb = Map.newBuilder[String, Seq[String]]
    var currentGroup: String = ""
    var groupFound = false

    data.foreach{ line =>
      val trimmed = line.trim

      // Only try if not empty and not a comment
      // NB: for some reason this didn't short ciruit, and cost me time.
      if (!trimmed.isEmpty && !(trimmed.charAt(0) == '#')) {
        if (trimmed.charAt(0) == '[') {
          // is potentially a group
          if (!groupFound) groupFound = true
          else {
            b += (currentGroup -> gb.result())
            gb.clear()
          }
          //TODO: Should test the end...
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

    // As the last group data is added,
    // check a group title was found at all
    if(groupFound) {
      b += (currentGroup -> gb.result())
    }
    b.result
  }

}//ParseIni
