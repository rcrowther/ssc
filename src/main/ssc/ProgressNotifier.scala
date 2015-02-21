package ssc

import java.util.Timer
import java.util.concurrent.TimeUnit
import java.util.concurrent.ScheduledThreadPoolExecutor



/** Outputs messages for progress.
  *
  * Has a small commandline progress meter. This is only switched on
  * when `verbose` && !`disabled`. Otherwise, instances of the class
  * print appropriate messages.
  */
class ProgressNotifier(
  val printer: (String) => Unit,
  val verbose: Boolean,
  val disable: Boolean,
  val initialMessage: String,
  val maxLength: Int
)
{
  class PrintBar extends Runnable {

    def run()
    {
      if(barLen > maxLength) {
        // back...
        printer("\r")
        // erase
        printer(" " * {maxLength + currentMessage.size + 1})
        // back...
        printer("\r")
        barLen = 0
        currentMessage = "Taking a while"
        printer(currentMessage)
      }

      printer(".")
      barLen += 1
    }
  }



  private val exec = new ScheduledThreadPoolExecutor(1)
  private var barLen = 0
  private var currentMessage = initialMessage

  // Autostart on construction.
  // Fixed delay is nicer, less likely to stutter.
  if (verbose) {
    if(disable) {
      printer(initialMessage + "\n")
      printer("...\n")
    }
    else {
      printer(currentMessage)
      exec.scheduleWithFixedDelay(
        new PrintBar,
        2L,
        1L,
        TimeUnit.SECONDS
      )
    }
  }

  def stop () = {
    if (verbose && !disable) {
      printer("\n")
    }
    exec.shutdownNow()
  }

}//ProgressNotifier
