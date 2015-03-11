package ssc

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Callable
import java.nio.file.Path




/** Runs tasks
  * 
  */
// First step towards threadding.
// Currently unimplemented.
class TaskRunner(
  isJDK: Boolean,
  javaPaths: Map[String, Path],
  scalaPaths: Map[String, Path]
)
{
  //Get ExecutorService from Executors utility class, thread pool size is 10
  private val executor = Executors.newCachedThreadPool()
  private var threadCount = 0

  def executeTaskDependancies(
    threaded: Boolean,
    project: Project,
    commandlineConfig: ConfigGroup,
    task: String
  )
  {

    val dependants = project.dependancies


    if (!threaded) {
      dependants.map{ dpnt =>
        // Make up a task config, by overlaying one with another.
        // Both are, at this point, fully tested.
        val taskConfig = dpnt.projectConfig(task) ++ commandlineConfig

        val action = new Action(
          task,
          dpnt.cwd,
          Config(taskConfig),
          isJDK,
          javaPaths,
          scalaPaths
        )
        action.run()
      }
    }
    else {
      val futures = dependants.map{ dpnt =>
        threadCount += 1

        // Make up a task config, by overlaying one with another.
        // Both are, at this point, fully tested.
        val taskConfig = dpnt.projectConfig(task) ++ commandlineConfig

        val threadable = new Action(
          task,
          dpnt.cwd,
          Config(taskConfig),
          isJDK,
          javaPaths,
          scalaPaths
        )
        executor.submit(threadable)
      }

      // Catch future returns

      futures.map{ f =>
        //println("  evoke return: " + f.get());
        f.get()
      }
    }
  }

  private def invoke(
    project: Project,
    commandlineConfig: ConfigGroup,
    task: String
  )
  {
  }

  private def evoke(
    project: Project,
    commandlineConfig: ConfigGroup,
    task: String
  )
  {
  }

  def run(
    project: Project,
    commandlineConfig: ConfigGroup,
    task: String
  )
  {
    val runner =
      task match{
        case "find" => invoke _
        case "findfile" => invoke _
        case "tree" => invoke _
        case _ => evoke _
      }
    runner(project, commandlineConfig, task)
  }

  //shut down the executor service
  def stop() = executor.shutdown()

}//TaskRunner



object TaskRunner {

}//TaskRunner
