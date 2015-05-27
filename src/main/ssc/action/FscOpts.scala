package ssc.action


/** Collects options for FSC actions.
*/
class FscOpts(
  val maxIdle: Int
)
{
  def addOpts(b: scala.collection.mutable.Builder[String, Seq[String]])
  {
    b += "-max-idle"
    b += maxIdle.toString
    //b += "-ipv4"
  }
}
