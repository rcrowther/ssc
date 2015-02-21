
/** A document builder for Scala.
  *
  * Most control is in the [[ssc.Runner]], most of the action in [[ssc.Action]]
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
