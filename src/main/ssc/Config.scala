package ssc

import scala.collection.GenTraversableOnce


/** Wraps a group config so the interface is uncluttered.
*/
class Config(val repr: Map[String, Seq[String]])
{
/*
  def ++ (xs: GenTraversableOnce[(String, String)])
      : Config =
  {
    new Config(repr ++ xs)
  }
*/

  def apply(k: String)
      : String =
  {
    repr(k)(0)
  }

  def asSeq(k: String)
      : Seq[String] =
  {
    repr(k)
  }

  def asBoolean(k: String)
      : Boolean =
  {
    if (!repr.contains(k)) {
println(s"Warning** Non-existent config: key $k")
true
}
else {
    if (repr(k).isEmpty) {
println(s"Warning** Empty config: key $k")
true
}
    else {
    val v = repr(k)(0)
    if (v == "true") true
    else {
      if(v == "false") false
      else throw new Exception(s"Key $k with value $v can not be decoded as Boolean")
    }
}
  }
}

  def asInt(k: String)
      : Int =
  {
    try {
      Integer.parseInt(repr(k)(0))
    }
    catch {
      case e: Exception => throw new Exception(s"Key $k with value ${repr(k)} can not be decoded as Int")
    }
  }

  def keys
      : Iterable[String] =
  {
    repr.keys
  }

  override def toString()
      : String =
  {
    repr.toString()
  }

}//Config


object Config {

  protected def emptyThing = new Config(Map.empty[String, Seq[String]])

  def empty: Config = emptyThing


  /** Build a map from elements.
    *
    * Here mainly to type the default config.
    */
/*
  def apply(elems: (String, String)*)
      : Config =
  {
    new Config(elems.toMap)
  }
*/
  def apply(elems: Map[String, Seq[String]])
      : Config =
  {
    new Config(elems)
  }
}//Config
