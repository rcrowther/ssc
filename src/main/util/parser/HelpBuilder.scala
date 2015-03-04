package sake.util.parser



/** Build help items.
  *
  * @param newLine separator used for newlines
  */
class HelpBuilder
{

  private val newLine = System.lineSeparator()

  private val b = new StringBuilder()

  /** Appends help data.
    *
    * Help is generated from the option name and description.
    * The string is formatted for use in terminals.
    */
  def add(
    prefix: String,
    schema: OptionConfig,
    suffix: String
  )
      : StringBuilder =
  {
    if(!prefix.isEmpty) {
      b ++= prefix
      b ++= newLine
    }
    schema.map{ case(name, data) =>
      val label: String = "  " + name + " " + data.parameterDescription
      val labelPad = " " * (34 - label.size)
      b ++= label
      b ++= labelPad
      b ++= data.description
      b ++= newLine
    }
    if(!suffix.isEmpty) {
      b ++= suffix
      b ++= newLine
    }
    b
  }

  /** Appends help data.
    *
    * Help is generated from the option name and description.
    * The string is formatted for use in terminals.
    */
  def add(
    prefix: String,
    schema: OptionConfig
  )
      : StringBuilder =
  {
    add(prefix, schema, "")
  }

  /** Appends help data from a schema.
    *
    * Help is generated from the option name and description.
    * The string is formatted for use in terminals.
    */
  def add(
    schema: OptionConfig
  )
      : StringBuilder =
  {
    add("", schema, "")
  }

  /** Appends help data in coloums.
    *
    * Help is generated from the option name and description.
    * The string is formatted for use in terminals.
    * 
    * This method is useful for placing items like "-help"
    * and "-version", which conventionally finish lists of
    * switches.  
    */
  def addItem(
    name: String,
    description: String
  )
      : StringBuilder =
  {
    val label: String = "  " + name
    val labelPad = " " * (34 - label.size)
    b ++= label
    b ++= labelPad
    b ++= description
    b ++= newLine
  }


  /** Appends a line of string data.
    * 
    */
  def addLine(
    line: String
  )
      : StringBuilder =
  {
    b ++= line
    b ++= newLine
  }

  /** Appends a spacing newline.
    * 
    */
  def addNewLine()
      : StringBuilder =
  {
    b ++= newLine
  }


  /** Appends a line of usage data.
    */
  def addUsage(
    appName: String,
    usage: String
  )
      : StringBuilder =
  {
    b ++= "Usage: "
    b ++= appName
    b ++= " "
    b ++= usage
    b ++= newLine
    b
  }

  def result() = b.result()

}//HelpBuilder



object HelpBuilder {

  def apply()
      : HelpBuilder =
  {
    new HelpBuilder()
  }

  /** Creates a new help builder with usage data.
    */
  def apply(
    appName: String,
    usage: String
  )
      : HelpBuilder =
  {
    val b = new HelpBuilder()
    b.addUsage(appName, usage)
    b
  }


}//HelpBuilder
