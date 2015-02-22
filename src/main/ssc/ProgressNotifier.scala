package ssc

import java.util.Timer
import java.util.concurrent.TimeUnit
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.Random


/** Outputs messages for progress.
  *
  * Has a small commandline progress meter. This is only switched on
  * when `verbose` (and meterType != "none"). Otherwise, instances of
  * the class print appropriate messages.
  * 
  * @param meterType currently 'none', 'bounce', 'buzz', 'progress'.
  * @verbose if true, show progress indications, if false, the class
  *  does nothing.
  * @param initialMessage custom message shown by some meters, and
  *  the 'none' meter.
  * @param maxLength space used as number of characters. Applies to
  *  meters which trail across the screen.
  */
class ProgressNotifier(
  val meterType: String,
  val printer: (String) => Unit,
  val verbose: Boolean,
  val initialMessage: String,
  val maxLength: Int
)
{

  class PrintBar extends Runnable {
    val random = new Random()
    val messages = Seq(
      "pot empty",
      "onwards",
      "so it goes",
      "industrious",
      "the rain",
      "endless",
      "enough!",
      "waiting",
      "poetry",
      "dark",
      "candles",
      "reeds"
    )
    var barLen = initialMessage.size
    var currentMessage = initialMessage

    // print the initial message...
    printer(currentMessage)

    def run()
    {
      if(barLen < maxLength) {
        printer(".")
        barLen += 1
      }
      else {
        // erase backwards then reset cursor to start (erase is
        // supposed to move the cursor on some terminals, but not my
        // GNU Mint...)
        printer("\033[1K \r")

        currentMessage = messages(random.nextInt(7))
        barLen = currentMessage.size
        printer(currentMessage)
      }
    }
  }


  class Buzzer extends Runnable {
    val random = new Random()
    var moveDelay = 0
    var pos = 0

    def run()
    {



      if(moveDelay > 0) {
        val nextChar = random.nextInt(7) match {
          case 0 => """\"""
          case 1 => """\"""
          case 2 => "/"
          case 3 => "/"
          case 4 => "+"
          case 5 => "-"
          case 6 => "-"
          case 7 => "-"
        }
        printer("\b" + nextChar)
        moveDelay -= 1
      }
      else {
        // erase backwards then reset cursor to start (erase is supposed
        // to move the cursor on some terminals, but not my GNU Mint...)
        printer("\033[1K \r")

        moveDelay = random.nextInt(9)
        pos = random.nextInt(maxLength)
        printer(" " * pos)
      }
    }
  }


  class Bounce extends Runnable {
    val img = Seq(
      "O               ",
      "OO              ",
      " OOOO           ",
      "    OOOOOOOO    ",
      "           OOOO ",
      "              OO",
      "               O"
    )
    var i = 0
    var direction = 1
    var limit = img.size - 1

    def run()
    {

      if (i >= limit) {
        direction = -1
      }
      if (i <= 0) {
        direction = 1
      }

      //reset lines are the same length for overwriting
      printer("\r")
      printer(img(i))
      i += direction
    }
  }



  private val exec = new ScheduledThreadPoolExecutor(1)


  // Autostart on construction.
  // Fixed delay is nicer, less likely to stutter.
  if (verbose) {

    // Don't hide the cursor for simple printouts
    if(meterType == "none") {
      printer(initialMessage + "\n")
      printer("...\n")
    }
    else {

      // hide the cursor
      printer("\033[?25l")

      meterType match {

        case "progress" =>
          exec.scheduleWithFixedDelay(
            new PrintBar,
            10L,
            700L,
            TimeUnit.MILLISECONDS
          )
        case "bounce" =>
          exec.scheduleWithFixedDelay(
            new Bounce,
            10L,
            300L,
            TimeUnit.MILLISECONDS
          )

        case "buzz" =>
          exec.scheduleWithFixedDelay(
            new Buzzer,
            100L,
            100L,
            TimeUnit.MILLISECONDS
          )

        case x => throw new RuntimeException(s"Unknown meter type $x")
      }
    }
  }

  def stop ()
  {
    // NB: For none, we do nothing
    if (verbose && meterType != "none") {

      // Stop the thread
      exec.shutdownNow()

      meterType match {
        // progress, leave the remnamt and mewline
        case "progress" => printer("\n")
        // bounce and buzz, clear and newline
        case "bounce" => printer("\033[1K \n")

        case "buzz" => printer("\033[1K \n")

        case x => throw new RuntimeException(s"Unknown meter type $x")
      }

      // reshow the cursor
      printer("\033[?25h")

    }

  }

}//ProgressNotifier
