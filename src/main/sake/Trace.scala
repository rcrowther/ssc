package sake

/** Print to stdout, formatted and consistent.
* 
* Probably best to keep them central, too. We don't know the stdout implementation.
  */
trait Trace
{
  private val sep = System.lineSeparator()

  private val GREEN = "\033[32m"
  private val CYAN = "\033[36m"
  private val YELLOW = "\033[33m"
  private val RED = "\033[31m"
  private val WHITE = "\033[37m"
  private val DIMWHITE = "\033[2m" 
  private val BOLDWHITE = "\033[1m"
  private val RESET = "\033[0m"


  /** Define if verbose prints are to go to the output.
    */
  protected def verbose: Boolean

  /** Print output in colour
    */
  // TODO: Do ANSI codes make a mess in some terminals?
  protected def inColor: Boolean

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
    if (verbose) trace(GREEN + line + WHITE)
  }

  /* Write info output to standard output stream.
   *
   * No newline.
   */
  def traceInfoPrint(line: String)
  {
    print(GREEN + line + WHITE)
  }

  /* Write advice output to standard output stream.
   *
   * Only if verbose is on.
   */
  def traceAdvice(line: String)
  {
    if (verbose) trace(CYAN + line + WHITE)
  }

  /* Write warning output to standard output stream.
   *
   * Only if verbose is on.
   */
  def traceWarning(line: String)
  {
    if (verbose) trace(YELLOW + line + WHITE)
  }

  /* Write error output to standard output stream.
   *
   * Always prints.
   */
  def traceError(line: String)
  {
    trace(RED + "** " + line + WHITE)
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
