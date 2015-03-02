package sake

/** Print to stdout, formatted and consistent.
  * 
  * Probably best to keep them central, too. We don't know the stdout implementation.
  */
trait Trace
{
  private val sep = System.lineSeparator()

  private val GREEN = "\u001b[32m"
  private val MAGENTA = "\u001b[35m"
  //private val BLUE = "\u001b[34m"
  private val CYAN = "\u001b[36m"
  private val YELLOW = "\u001b[33m"
  private val RED = "\u001b[31m"
  private val WHITE = "\u001b[37m"
  // Well, it's not this...
  private val DIMWHITE = "\u001b[37;2m"
  private val BOLDWHITE = "\u001b[1m"
  private val RESET = "\u001b[0m"
  //private val BOLD = "\u001b[1m"

  /** Define if verbose prints are to go to the output.
    */
  protected def verbose: Boolean

  /** Print output in colour
    */
  // TODO: Do ANSI codes make a mess in some terminals?
  protected def noColor: Boolean

  /* Write output to standard output stream, only if verbose is on.
   *
   */
  // deprecated
  def traceVerbose(line: String)
  {
    if (verbose) trace(line)
  }

  /* Write info output to standard output stream.
   *
   * Only if verbose is on.
   */
  def traceInfo(line: String)
  {
    if (verbose) {
      if (noColor) trace(line)
      else trace(GREEN + line + WHITE)
    }
  }



  /* Write info output to standard output stream.
   *
   * No newline.
   */
  def traceInfoPrint(str: String)
  {
    if (noColor) print(str)
    else print(GREEN + str + WHITE)
  }

  /* Write info output to standard output stream.
   *
   * No newline. *Always outputs*. Intended for commandlines and
   * similar activity.
   */
  def traceTerminalPrompt(str: String)
  {
      if (noColor) print(str)
      else print("\u001b[1m\u001b[34m" + str + RESET)
  }

  /* Write advice output to standard output stream.
   *
   * Only if verbose is on.
   */
  def traceAdvice(line: String)
  {
    if (verbose) {
      if (noColor) trace(line)
      else trace(CYAN + line + WHITE)
    }
  }

  /* Write warning output to standard output stream.
   *
   * Only if verbose is on.
   */
  def traceWarning(line: String)
  {
    if (verbose) {
      if (noColor) trace("** Warning ** " + line)
      else trace(YELLOW + line + WHITE)
    }
  }

  /* Write error output to standard output stream.
   *
   * Always prints.
   */
  def traceError(line: String)
  {
    if (noColor) trace("** Error ** " + line)
    else trace(RED + "** Error ** " + WHITE + line)
  }

  /* Write output to standard output stream
   * 
   *  The write is done as a single IO call (to print) to lessen the
   *  chance that the trace output is interrupted by other tasks also
   *  producing output.
   */
  //TODO: Originally took an output stream?
  // NB not deprecated, good for white...
  def trace(lines: String*)
  {
    lines.foreach{ line =>
      if (!line.isEmpty) {

        val l =
          if (line.charAt(line.length - 1) != '\n') line + sep
          else line
        print(l)
        
      }
    }
  }

}//Trace
