package sake.helper.executable


sealed abstract class Fail
case object Unparsable extends Fail
case object NotExists extends Fail


/** Data on a version.
*
* The current comparators are miserablist, refusing to deal with comparison
* bar equality if the numbers are unparsable. This could be changed, depends
* on the wild scenarios.
*/
class Version(val str: String)
    extends Ordered[Version]
{

// TODO: Rubbish
  override def hashCode: Int = {
    41 * (
      41 * (
        41 + majorAsInt.right.get
      ) + minorAsInt.right.get
    ) + patchAsInt.right.get
  }

  override def equals(that: Any) : Boolean =
    that match {
      case that: Version => str == that.str
      case _ => false
    }

  private def cmpElems(thisElem : Option[Int], thatElem: Option[Int])
      : Int =
  {
    if (thisElem == None && thatElem == None) 0
    else {
      if (thisElem == None || thatElem == None) -1
      else (thisElem.get - thatElem.get)
    }
  }

  private def cmpElems(thisElem : Either[Fail, Int], thatElem: Either[Fail, Int])
      : Int =
  {
    thisElem match {
      case Left(fail) => {
        if (fail == NotExists) {
          //equivalence is equivalence, else anything on that is bigger
          if (thatElem.isLeft && thatElem.left.get == NotExists) 0
          else -1
        }
        else {
          // this unparsable
          // if that not exists, this is bigger, else that is bigger
          if (thatElem.isLeft && thatElem.left.get == NotExists) 1
          else -1
        }
      }
      case Right(v) => {
        // if that exists, calculate, else that is bigger
        if (thatElem.isRight) v - thatElem.right.get
        else -1
      }
    }
  }

  def compare(that: Version)
      : Int =
  {
    val thisMajor = this.majorAsInt
    val thatMajor = that.majorAsInt

    val majorCmp = cmpElems(thisMajor, thatMajor)

    if (majorCmp != 0) majorCmp
    else {
      println("minor...")
      val thisMinor = this.minorAsInt
      val thatMinor = that.minorAsInt
      val minorCmp = cmpElems(thisMinor, thatMinor)

      if (minorCmp != 0) minorCmp
      else {
        println("patch...")
        val thisPatch = this.patchAsInt
        val thatPatch = that.patchAsInt
        cmpElems(thisPatch, thatPatch)
      }
    }
  }

  def majorAsInt
      : Either[Fail, Int] =
  {
    val a = str.split('.')
    if(a.size >= 1) {
      try{
        Right(a(0).toInt)
      }
      catch {
        case e: Exception => Left(Unparsable)
      }
    }
    else Left(NotExists)
  }

  def minorAsInt
      : Either[Fail, Int] =
  {
    val a = str.split('.')
    if(a.size >= 2) {
      try{
        Right(a(1).toInt)
      }
      catch {
        case e: Exception => Left(Unparsable)
      }
    }
    else Left(NotExists)
  }

  def patchAsInt
      : Either[Fail, Int] =
  {
    val a = str.split('.')
    if(a.size >= 3) {
      try{
        Right(a(2).toInt)
      }
      catch {
        case e: Exception => Left(Unparsable)
      }
    }
    else Left(NotExists)
  }

  def major
      : Option[String] =
  {
    val a = str.split('.')
    if(a.size >= 1) {
      Some(a(0))
    }
    else None
  }

  def minor
      : Option[String] =
  {
    val a = str.split('.')
    if(a.size >= 2) {
      Some(a(1))
    }
    else None
  }

  def patch
      : Option[String] =
  {
    val a = str.split('.')
    if(a.size >= 3) {
      Some(a(2))
    }
    else None
  }

  override def toString: String = {
    "Version(" + str + ')'
  }

}//Version



object Version {

  protected val emptyThing = new Version("") {
    override def toString: String = "empty version"
  }

  def empty : Version = emptyThing

  def apply(str: String)
      : Version =
  {
    new Version(str)
  }

}//Version
