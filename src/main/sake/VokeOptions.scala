package sake

/** Carries options for invoking and evoking.
  *
  * @param verbose print what is being done.
  * @param dryrun run but don't execute - forces 'verbose'.
  * @param showCause show the cause of an exception (another exception).
  *  If showCause and backtrace are both true, the backtrace of the
  *  cause is shown (default: false).
  * @param backtrace enable full backtraces from errors (default: False).
  * @param tasks display the tasks and dependencies, then exit.
  * @param where display the tasks and dependencies matching the pattern, then exit.
  * @param showPrereqs display the tasks and dependencies, then exit.
  * @param alwaysMultiTask all tasks are threaded (default: separate
  *  threads for user provided start tasks, but not every task).
  * @param threadStats on exit, print thread stats.
  */
final class VokeOptions(
  verbose: Boolean,
  val dryrun : Boolean,
  val showCause: Boolean,
  val backtrace: Boolean,
  val tasks: Boolean,
  val where: Option[String],
  val showPrereqs: Boolean,
  val alwaysMultiTask : Boolean,
  val threadStats : Boolean
)
{
  /** Turn on invoke/execute tracing
    *
    * Final value of the verbose/dryrun options.
    */
  val verbosely: Boolean =
    if (dryrun) true
    else verbose

  def addString(b: StringBuilder)
      : StringBuilder = {
    b ++= "verbosely:"
    b append verbosely
    b ++= ", dryrun:"
    b append dryrun
    b ++= ", showCause:"
    b append showCause
    b ++= ", backtrace:"
    b append tasks
    b ++= ", tasks:"
    b append backtrace
    b ++= ", where:"
    b append where
    b ++= ", showPrereqs:"
    b append showPrereqs
    b ++= ", alwaysMultiTask:"
    b append alwaysMultiTask
    b ++= ", threadStats:"
    b append threadStats
  }

  override def toString()
      : String =
  {
    val b = new StringBuilder()
    b ++= "VokeOptions("
    addString(b)
    b += ')'
    b.toString
  }

}//VokeOptions



object VokeOptions {
  protected val emptyThing : VokeOptions = new VokeOptions (
    false,
    false,
    false,
    false,
    false,
    None,
    false,
    false,
    false
  )

  def empty: VokeOptions = emptyThing

  def apply(
    verbose: Boolean,
    dryrun: Boolean,
    showCause: Boolean,
    backtrace: Boolean,
    tasks: Boolean,
    where: Option[String],
    showPrereqs: Boolean,
    alwaysMultiTask : Boolean,
    threadStats : Boolean
  )
      : VokeOptions =
  {
    new VokeOptions(
      verbose,
      dryrun,
      showCause,
      backtrace,
      tasks,
      where,
      showPrereqs,
      alwaysMultiTask,
      threadStats
    )
  }

  /** Voke settings for showing errors in code.
   *
   * Switches on `backtrace` and `showCause`.
   */
  def codeDebug()
      : VokeOptions =
  {
    new VokeOptions (
      false,
      false,
      true,
      true,
      false,
      None,
      false,
      false,
      false
    )
  }

}//VokeOptions
