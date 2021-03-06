package ssc.util.parser

import script.io.Trace



/** Simple commandline parser.
  *
  * Must be mixed into a class mixing 'Trace'.
  *
  * To verify the commandline, and restructure in a more convenient
  * format, the class methods work from a suppiled schema. To make a
  * schema, see [[ssc.util.parser.UnParameteredOption]] and
  * [[ssc.util.parser.ParameteredOption]].
  *
  * The class has no features for short args, switch callbacks, arg
  * typing (or argument type checks), controlled error handling, or
  * output formatting.
  *
  * It will reject several non-specified formatting errors, and return
  * auto-formatted help. For a simple commandline app, it should be
  * enough.
  *
  * Note that the prescence of a switch with no parameters is returned
  * as Seq("true"). The class is assuming `Boolean`.
  */
trait Parser
{
  this : Trace =>


  private def verifySwitch(
    appName: String,
    switch: String,
    schema: Map[String, ParameteredOption],
    parameters: Seq[String]
  )
      : Boolean =
  {
    var ok = true
    if (schema.contains(switch)) {
      val argData: ParameteredOption = schema(switch)
      val argCount = argData.parameterCount
      if(argData.parameterCountIsMax && parameters.size > argCount) {
        trace(s"$appName: too many parameters for switch $switch\n$appName -help  gives more information")
        ok = false
      }
      if(!argData.parameterCountIsMax && argCount != parameters.size) {
        trace(s"$appName: switch $switch takes parameter count $argCount (has ${parameters.size})\n$appName -help  gives more information")
        ok = false
      }
    }
    else {
      // An unrecognised switch
      trace(s"$appName: unrecognised switch $switch\n$appName -help  gives more information")
      ok = false
    }
    ok
  }

  /** Parses for switches, printing error messages.
    *
    * If arguments all pass, the return is the commandline switches,
    * assembled as a map.
    *
    * Unrecognised switches and wrong parameter lengths cause a fail.
    * 
    * @param appName the name of this app. Used in error messages.
    * @param schema a map of data about the switches, including
    *  defaults values. 
    * @param args the arguments fron the commandline, as a string seq.
    * @return optional map of all switches as keys with parameters as
    *  string sequences. None, if the parse fails. 
    */
  def parseSwitchesMinimal(
    appName: String,
    schema: Map[String, ParameteredOption],
    args: Seq[String]
  )
      : Option[Map[String, Seq[String]]] =
  {

    val recognized = Map.newBuilder[String, Seq[String]]
    var currentSwitch = ""
    var parametersB = Seq.newBuilder[String]

    var ok = true
    var first = true

    val limit = args.size
    var i = 0
    while(i < limit && ok) {

      val arg = args(i)
      if (arg(0) == '-') {
        if (first) first = false
        else {
          // load the last switch
          val parameters = parametersB.result()
          ok = verifySwitch(
            appName,
            currentSwitch,
            schema,
            parameters
          )

          if (ok) {
            if(schema(currentSwitch).parameterCount == 0) {
              recognized += (currentSwitch -> Seq("true"))
            }
            else recognized += (currentSwitch -> parameters)
          }
        }
        // Set for new switch
        currentSwitch = arg
        parametersB.clear()
      }
      else {
        // A parameter, or something
        parametersB += arg
      }
      i += 1
    }

    // add the last switch
    // If it failed so far, don't try (or disrupt 'ok' status)
    if (ok && !currentSwitch.isEmpty) {
      var parameters = parametersB.result()
      ok = verifySwitch(
        appName,
        currentSwitch,
        schema,
        parameters
      )
      if (ok) {
        if(schema(currentSwitch).parameterCount == 0) {
          recognized += (currentSwitch -> Seq("true"))
        }
        else recognized += (currentSwitch -> parameters)
      }
    }

    if (!ok) None
    else {
      Some(recognized.result())
    }
  }


  /** Parses for switches, printing error messages.
    *
    * If arguments all pass, the return is the defaults from the
    * schema, overwritten by any found on the commadline.
    *
    * Unrecognised switches and wrong parameter lengths cause a fail.
    * 
    * @param appName the name of this app. Used in error messages.
    * @param schema a map of data about the switches, including
    *  defaults values. 
    * @param args the arguments fron the commandline, as a string seq.
    * @return optional map of all switches as keys with parameters as
    *  string sequences. Missing keys are filled with values from
    *  `spec` parameter data.
    */
  def parseSwitches(
    appName: String,
    schema: Map[String, ParameteredOption],
    args: Seq[String]
  )
      : Option[Map[String, Seq[String]]] =
  {

    val res : Option[Map[String, Seq[String]]] = parseSwitchesMinimal(
      appName: String,
      schema: Map[String, ParameteredOption],
      args: Seq[String]
    )
    if (res == None) None
    else {
      Some(schema.map{case(k, v) => (k, v.default)} ++ res.get)
    }
  }


  /** Parses for switches, partitioning by the given schema.
    *
    * Only parses as far as it reconizes switches. An unrecogised
    * switch and subsequent switches (which may include recognised
    * switches) are returned through the unrecognised return seq.
    *
    * If arguments all pass, the return is the defaults from the
    * schema, overwritten by any found on the commadline.
    *
    * Unrecognised switches and wrong parameter lengths cause a fail.
    * Note that the method can fail on recognised switches
    * e.g. incorrect parameter counts, so has an optional return.
    * 
    * @param appName the name of this app. Used in error messages.
    * @param schema a map of data about the switches, including
    *  defaults values. 
    * @param args the arguments fron the commandline, as a string seq.
    * @return optional tuple of unrecognised args, and a map of
    *  recognised switches with parameters as string sequences.
    *  Missing keys are filled with values from `spec` parameter
    *  data.
    */
  def parseAllSwitches(
    appName: String,
    schema: Map[String, ParameteredOption],
    args: Seq[String]
  )
      : Option[(Seq[String], Map[String, Seq[String]])] =
  {

    val recognized = Map.newBuilder[String, Seq[String]]
    val unrecognisedArgs = Seq.newBuilder[String]

    var unrecognisedSwitch = true
    var currentSwitch = ""
    var parametersB = Seq.newBuilder[String]

    var ok = true
    var first = true


    val limit = args.size
    var i = 0

    while(i < limit && ok) {
      val arg = args(i)

      if (arg(0) == '-') {
        //println(s"is switch: $arg")
        if (!schema.contains(arg)) {
          unrecognisedArgs += arg
          unrecognisedSwitch = true
        }
        else {
          unrecognisedSwitch = false
          if (first) first = false
          else {
            // process the previous switch
            val parameters = parametersB.result()
            ok = verifySwitch(
              appName,
              currentSwitch,
              schema,
              parameters
            )
            if (ok) {
              if(schema(currentSwitch).parameterCount == 0) {
                recognized += (currentSwitch -> Seq("true"))
              }
              else recognized += (currentSwitch -> parameters)
            }
          }
          // Set for new switch
          currentSwitch = arg
          parametersB.clear()
        }
      }
      else {
        // A parameter, or something
        if (unrecognisedSwitch) unrecognisedArgs += arg
        else    parametersB += arg
      }
      i += 1
    }
    //println(s"ok $ok")
    //add the last switch
    // If it failed so far, don't try (or disrupt 'ok' status)

    if (ok && !currentSwitch.isEmpty) {
      var parameters = parametersB.result()
      // Find the parameter count of this last switch
      val argData: ParameteredOption = schema(currentSwitch)
      val argCount = argData.parameterCount

      //println(s"argCount $argCount")
      //println(s"parameters.size ${parameters.size}")
      if (parameters.size > argCount) {

        // More than needed, return the excess
        unrecognisedArgs ++= parameters.drop(argCount)
        parameters = parameters.take(argCount)
      }

      ok = verifySwitch(
        appName,
        currentSwitch,
        schema,
        parameters
      )
      if (ok) {
        if(schema(currentSwitch).parameterCount == 0) {
          recognized += (currentSwitch -> Seq("true"))
        }
        else recognized += (currentSwitch -> parameters)
      }
    }

    if (!ok) None
    else {
      Some(
        unrecognisedArgs.result(),
        schema.map{case(k, v) => (k, v.default)} ++ recognized.result()
      )
    }
  }

  /** Parses command line arguments, printing error messages.
    *
    * If arguments all pass, the return is the arguments supplied.
    *
    * Unrecognised arguments cause a fail.
    *
    * @param appName the name of this app. Used in error messages.
    * @param schema a map of data about the switches, including
    *  defaults values. 
    * @param args the arguments fron the commandline, as a string seq.
    * @return optional seq of args. Missing keys are filled with values from
    *  `spec` parameter data.
    */
  def parseArgs(
    appName: String,
    schema: Map[String, UnParameteredOption],
    args: Seq[String]
  )
      : Option[Seq[String]] =
  {

    val recognized = Seq.newBuilder[String]

    var ok = true
    val limit = args.size
    var i = 0

    while(i < limit && ok) {

      val arg = args(i)
      if (schema.contains(arg)) {
        recognized += arg
      }
      else {
        // An urecognised arg
        trace(s"$appName: unrecognised arg '$arg'\n$appName -help  gives more information")
        ok = false
      }
      i += 1
    }
    //println(s"ok $ok")

    if (!ok) None
    else {
      Some(recognized.result())
    }
  }



  /** Parses command line arguments, partitioning by the given schema.
    *
    * This only parses arguments as far as it recognises the
    * arg. After that it stops (see `parseAllSwitches`), even if later
    * args would be recognised.
    *
    * @param appName the name of this app. Used in error messages.
    * @param schema a map of data about the switches, including
    *  defaults values. 
    * @param args the arguments fron the commandline, as a string seq.
    * @return tuple of seq of args. Unrecognised switches are to the
    *  left, recognized switches to the right.
    */
  def parseAllArgs(
    schema: Map[String, UnParameteredOption],
    args: Seq[String]
  )
      : (Seq[String], Seq[String]) =
  {
    val recognised = Seq.newBuilder[String]
    val unrecognised = Seq.newBuilder[String]
    val limit = args.size
    var i = 0
    while(i < limit && schema.contains(args(i))) {
      recognised += args(i)
      i += 1
    }
    while(i < limit) {
      unrecognised += args(i)
      i += 1
    }
    (unrecognised.result(), recognised.result())
  }

}//Parser
