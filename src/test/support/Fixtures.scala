package sake.support.parser



trait Fixtures
{

  val CLSwitchSchema  = Map[String, CLSwitchOption](
    "-featureWarnings" -> "Turn features on",
    "-srcDir" -> ("List of files to be processed", "", ParameterDescription.files, true, 64),
    "-docTitle" -> ("Title of the document", "Title",  ParameterDescription.text, false, 1)
  )

  val CLSwitchesOk = Seq[String](
    "-featureWarnings",
    "-srcDir", "/someplace", "/home/anyplace",
    "-docTitle", "testTitle"
  )

  val CLSwitchBadSwitch = Seq[String](
    "-freaktureWarnings",
    "-srcDir", "/someplace", "/home/anyplace",
    "-docTitle", "testTitle"
  )

  val CLSwitchBadArgCount = Seq[String](
    "-featureWarnings", "blue",
    "-srcDir", "/someplace", "/home/anyplace",
    "-docTitle", "testTitle"
  )

  val CLSwitchesExcessArgs = Seq[String](
    "-sub", "more",
    "-featureWarnings",
    "-srcDir", "/someplace", "/home/anyplace",
    "-docTitle", "testTitle",
    "-super", "more"
  )

  val CLArgsSchema  = Map[String, CLArgOption](
    "compile" -> "Make bytecode objects from the code",
    "run" -> "Run a main class",
    "doc" -> "Generate documentation"
  )


  val CLArgs = Seq("compile", "doc")
  val CLArgsUnrecognizedArg = Seq("compile", "collapse")

}//Fixtures
