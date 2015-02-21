
/** A document builder for Scala.
  *
  *
  * == Other ==
  *  - RunnerSake* - helpers for sakes to be used with runners. Can do the work for handling commandlines and external variables.
  * - PackageSake - an implemented Sake. If an executable Sake is available, this can package the compiled version into jars, or an installale folder.
  */
package object ssc {

  /** Maps groups to keys to values.
    *
    * For config.
    */
  type ConfigMap = Map[String, Map[String, Seq[String]]]
val ConfigMapEmpty = Map.empty[String, Map[String, Seq[String]]]

  /** Maps keys to values.
    *
    * For a  group in a config.
    */
  type ConfigGroup = Map[String, Seq[String]]

val ConfigGroupEmpty = Map.empty[String, Seq[String]]
}//ssc
