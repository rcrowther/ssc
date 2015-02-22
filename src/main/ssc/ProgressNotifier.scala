package ssc

import java.util.Timer
import java.util.concurrent.TimeUnit
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.Random


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

        // back.  Only solid way I found is carridge returns.
        // And thaat doesn't work?
        //printer("\r" + {" " * {maxLength + 1}})
        //printer("\r" + currentMessage)
        printer("\033[1K")
        currentMessage = messages(random.nextInt(7))
        barLen = currentMessage.size
        printer(currentMessage)
      }


    }
  }

  class Buzzer extends Runnable {
    val random = new Random()
    var moveDelay = random.nextInt(9)
    var pos = random.nextInt(maxLength)

    def run()
    {
      // back and erase
      printer("\r")
      printer(" " * {pos})
      
      if(moveDelay > 0) {
        val nextChar = random.nextInt(3) match {
          case 0 => """\"""
          case 1 => "/"
          case 2 => "-"
        }
        printer(nextChar)
        moveDelay -= 1
      }
      else {
        printer(" ")
        moveDelay = random.nextInt(9)
        pos = random.nextInt(maxLength)
      }
    }
  }

  class Bounce extends Runnable {
    val img = Seq(
      "O               ",
      "OOOO            ",
      "    OOOOOOOO    ",
      "            OOOO",
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
        //printer("\033[1K")
      printer("\r")
      printer(img(i))
      i += direction
    }
  }


  private val exec = new ScheduledThreadPoolExecutor(1)


  // Autostart on construction.
  // Fixed delay is nicer, less likely to stutter.
  if (verbose) {
    if(disable) {
      printer(initialMessage + "\n")
      printer("...\n")
    }
    else {
      //hide cursor
      printer("\033[?25l")
      /*
       exec.scheduleWithFixedDelay(
       new PrintBar,
       10L,
       700L,
       TimeUnit.MILLISECONDS
       )
       */
      exec.scheduleWithFixedDelay(
        new Bounce,
        10L,
        700L,
        TimeUnit.MILLISECONDS
      )

      /*
       
       exec.scheduleWithFixedDelay(
       new Buzzer,
       100L,
       100L,
       TimeUnit.MILLISECONDS 
       )
       */

    }
  }

  def stop ()
  {
    if (verbose && !disable) {
      printer("\n")
    }
    exec.shutdownNow()

    //show cursor
    printer("\033[?25h")
  }

}//ProgressNotifier
