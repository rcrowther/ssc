package sake.util.io



/** Print to stdout, formatted and consistent.
  * 
  * Probably best to keep them central, too. We don't know the stdout
  * implementation.
  */
// It should be a trait, but it's causing instanciation grief
// ..and it's got data in it.
abstract class Trace
    extends scala.io.AnsiColor
{


  /** Define if verbose prints are to go to the output.
    */
  def verbose: Boolean

  /** Print output in colour
    */
  def noColor: Boolean


  private val newLine = System.lineSeparator()

  private val out = java.lang.System.out
  private val err = java.lang.System.err



  /** Write information output to standard output stream.
   *
   * Only if verbose is on.
   */
  def traceInfo(line: String)
  {
    if (verbose) {
      if (noColor) out.println(line)
      else out.println(GREEN + line + RESET)
    }
  }


  /** Write information output to standard output stream.
   *
   * No newline. May need a `flush` when complete.
   */
  def traceInfoPrint(str: String)
  {
    if (noColor) out.print(str)
    else out.print(GREEN + str + RESET)
  }


  /** Flush output
    *
    * Sometimes necessary when simply printing, or print may not
    * become visible.
    */
  def flush() { out.flush() }


  /** Write information output to standard output stream.
   *
   * No newline. *Always outputs*. Intended for commandlines and
   * similar activity.
   */
  def traceTerminalPrompt(prompt: String)
  {
    if (noColor) out.print(prompt)
    else out.print(BOLD + BLUE + prompt + RESET)
    flush()
  }


  /** Write advice output to standard output stream.
   *
   * Only if verbose is on.
   */
  def traceAdvice(line: String)
  {
    if (verbose) {
      if (noColor) out.println(line)
      else out.println(CYAN + line + RESET)
    }
  }


  /** Write warning output to standard error stream.
   *
   * Always prints.
   */
  def traceWarning(line: String)
  {
      if (noColor) err.println("** Warning ** " + line)
      else err.println(YELLOW + line + WHITE)
  }


  /** Write error output to standard error stream.
   *
   * Always prints.
   */
  def traceError(line: String)
  {
    if (noColor) err.println("** Error ** " + line)
    else err.println(RED + "** Error ** " + WHITE + line)
  }


  /** Write plain output to standard output stream
   * 
   * This method always prints, in the stock terminal font/color settings.
   * Used for output of the results of actions.
   */
  def trace(line: String)
  {
    out.println(line)
  }

}//Trace
