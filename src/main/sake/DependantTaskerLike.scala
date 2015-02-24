package sake

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Callable

import scala.util.matching.Regex


/** Runs a collection of tasks with dependancies expressed between them.
  * 
  * The class can be loaded with instances of
  * [[sake.Task]]. Dependancies can be expressed between
  * tasks. This seemingly trivial idea (why not build the code
  * needed?), has found two purposes. First, it is convenient for
  * on-the-fly scripting. Second, if tasks have dependancies
  * resolved, this can avoid overwriting previous
  * material, and repetition of long-running tasks.
  * 
  * Tasks can be run by the calling thread `invoke`, or by generated
  * threads using `evoke`. Both methods will cause dependant tasks to
  * run first.
  * 
  * The Dependancy Tasker has a simple concept of classwide data. This
  * is inserted using the class parameter `envDatamap`. This
  * data is available in the action of every
  * task. Supporting code will set the name of the parameter.
  * 
  * The classwide data type is Map[String, Any]. The key represents a
  * name, which can be anything. The value is data, and must be cast
  * back (params(<name>).isInstanceOf[<sometype>]) on retrieval.
  *
  * Perhaps in compensation for the above (similar code often filters
  * environment data), the dependant tasker has a feature where the
  * results of a task are passed to any task dependant on it. This
  * data passing is intended to make resulting code layout more like
  * conventional classes and methods. It also keeps data in close
  * scope. For example, if a task creates a file, it can pass the
  * filepath forward to a task which is dependant on the file
  * existing.
  *
  * Returns from tasks are typed by a Map[String, Option[Any]]. The
  * string key is the name of the task. The optional value is the
  * return or, if the task did nothing, None.
  *  
  * This class uses a simple string to store and find tasks for
  * invokation. This is called the task route. Note that the task
  * route is separate from the name given to a task. Implementations
  * of this class derive the name from the route. The simplistic
  * method is to use the last colon as a separator i.e. a route
  * "compile:doc" refers to a task named "doc" (with a namespace of
  * "compile"). The technique for deriving names from routes can be
  * changed by overriding the method `routeToName`.
  *
  * Note that the running of tasks is soft, in other words, it
  * attempts to finish tasks. Even if errors are thrown, it will catch
  * them and attempt to continue (this is part practicality. As an
  * often multithreaded program, stopping is risky). An excepting task
  * returns `None`, so code that uses returns is defended naturally,
  * and needs no special handling.
  */
trait DependantTaskerLike
    extends Trace
    with DynamicScope
{
  //Get ExecutorService from Executors utility class, thread pool size is 10
  private var executor: ExecutorService = null

  // The output stream handler.
  protected var opts = VokeOptions.empty
  
  protected var verbose: Boolean = true

  protected var noColor: Boolean = false


  // Thread history
  private var threadsUsed = 0

  // Registry //
  protected val registry = TaskRegistry()


  /** Returns the number of threads used in runs.
    *
    * Mainly used for testing and diagnostics. The return is unreliable 
    * when a sake is running.
    */
  def getThreadsUsed() : Int = threadsUsed

  /** Resets a sake for invokation.
    *
    * Doesn't do much aside from resetting the thread count.
    */
  def clear() = {
    threadsUsed = 0
  }

  /** Retrieves values associated with the given keys.
    *
    * This method should be used for retrieving prerequisites.
    * It's here so it can throw a nice error if prerequisites are missing,
    * which they may well be.
    */
  protected def prerequisiteTasks(task: Task)
      : Seq[Task] =
  {
    val r = task.prerequisiteRoutes
    val b = Seq.newBuilder[Task]
    r.foreach{route =>
      if(registry.contains(route)) b += registry(route)
      else {
        throw new SakeRuntimeException(s"Dependant task not in registry: task '${task.route}', dependancy: '$route'")
      }
    }
    b.result()
    /*
     try {
     r.map(registry(_))
     }
     catch {
     case e: NoSuchElementException =>
     throw new Exception(s"Dependant task not found: route: ${task.route} *** Missing ${e.getMessage} ***")
     }
     */
  }


  /** Converts a task route to a name.
    *
    * Can be overridden.
    */
  def routeToName(route: String)
      : String =
  {
    val i = route.lastIndexOf(':')
    if (i != -1) {
      route.slice(i + 1, route.size)
    }
    else route
  }


  // Threading //
  /** Composes a callable
    */
  private class TaskThreadable(
    val task: Task,
    val invocationChain: InvocationChain
  )
      extends Callable[(String, Any)]
  {

    def call() : (String, Any) =
    {
      (task.route, invokeWithCallChain(task, invocationChain))
    }

  }//TaskThreadable


  // Execution //

  /** Set a default task name, to be used if no names supplied.
    *
    * The default is "exec", but this can be overriden in subclasses.
    */
  val defaultTaskRoute: String = "exec"


  /** Execute the action in a task.
    *
    * Possibly in supplementary thread.
    */
  private def execute(
    task: Task,
    prevReturns: MultiTypeMap,
    invocationChain: InvocationChain
  )
      : Any =
  {

    if (opts.dryrun) {
      trace(s"** Execute (dry run, no action) ${task.name}")
      None
    }
    else {
      if (opts.verbosely) {
        trace(s"** Execute ${task.name}")
      }
      if (task.action == ActionEmpty) {
        Unit
      }
      else {
        //println (s"prevReturns: $prevReturns")
        //println (s"opts: $opts ")
        // We know something will return from the task.
        // (even if coders try to get tricky and return none)
        // Also, we have to invoke  in scope,
        // to make the params available.
        val ret =
          invokeWithParams(prevReturns) {
            task.action()
          }

        task.returnValue = ret
        ret
      }

    }
  }



  /** Invokes prerequisites of this task.
    *
    * Also checks if invoking should simply be a trace,
    * and builds an invokation chain.
    *
    * Possibly in supplementary thread.
    */
  private def invokePrerequisites(
    task: Task,
    invocationChain: InvocationChain
  )
      : MultiTypeMap =
  {
    if (opts.alwaysMultiTask || task.multitaskDependancies) {
      // TODO: Reenable
      //invoke_prerequisites_concurrently(tArgs, invocationChain)
      // A return map is formed.
      //println(s"multitask ${task.name}")
      val tasks = prerequisiteTasks(task)
      val futures = tasks.map{ dpnt =>
        threadsUsed += 1
        val threadable = new TaskThreadable(
          dpnt,
          invocationChain
        )
        executor.submit(threadable)
      }

      // Catch future returns
      try {
        futures.map{ f =>
          //println("  evoke return: " + f.get());
          f.get()
        }.toMap
      }
      catch  {
        case ex: Exception =>
          ex.printStackTrace()
          MultiTypeMapEmpty
      }
    }
    else {
      // A return map is formed.
      prerequisiteTasks(task).map{ dpnt =>
        (dpnt.route, invokeWithCallChain(
          dpnt,
          invocationChain
        )
        )
      }.toMap
    }
  }

  /** Invokes this task, checking for prerequisites first.
    *
    * Also checks if invoking should simply be a trace,
    * and builds an invokation chain.
    *
    * Possibly in supplementary thread.
    * Synchronized.
    */
  // Contains the major catch block for *vokation
  // errors
  private def invokeWithCallChain(
    task: Task,
    invocationChain: InvocationChain
  )
      : Any =
  {

    val newChain = invocationChain.append(task.route)

    task.invokedLock.synchronized {
      try {

        if (opts.verbosely) {
          trace(s"** Running ${task.name} ${task.mkStringTrace}")
        }
        //println(s"  not invoking? $invoked")
        if (task.getInvoked) {
          task.returnValue
        }
        else {
          task.setInvoked(true)

          val prevReturns: MultiTypeMap = invokePrerequisites(
            task,
            newChain
          )
          execute(task, prevReturns, newChain)
        }
      }
      catch {
        case e: Exception => {
          displayErrorMessageWithAbort(
            //options,
            newChain,
            new Exception("Sake runtime exception", e)
          )
          // Send a dead return to *vokers
          Unit
        }
      }
    }
  }

  /** Routes *voke commands.
    *
    * Mostly, it catches various display options to avoid calling invokes
    * if display is all that is requested.
    */ 
  private def handleShowOptionsElse(
    taskRoutes: Seq[String]
  )(vokeBlock: => MultiTypeMap)
      : MultiTypeMap =
  {
    if (opts.showPrereqs){
      displayPrerequisites
      MultiTypeMapEmpty
    }
    else {
      if (opts.where != None) {
        val re = opts.where.get.r
        registry.tasks.foreach{ case(name, task) =>
          if(re.findFirstIn(name) != None) {
            trace(name)
            trace(task.description)
          }
        }
        MultiTypeMapEmpty
      }
      else {
        if (opts.tasks){
          registry.tasks.foreach{ case(name, task) =>
            trace(name)
            trace(task.description)
          }
          MultiTypeMapEmpty
        }
        else {
          //        if (opts.version){
          //        if (opts.verifyTasking){
          vokeBlock
        }
        //}
        //}
      }
    }

  }

  /** Tests if routes are in the registry.
    *
    * Sake throws an exception to get out of the task tree, but that will
    * cover initial tasks. There less information too. So this method.
    * Throws an exception, as invokes may also be internal, and it's more consistent.
    */
  private def verifyTasksInRegistry(routes: Seq[String]) = {
    routes.foreach{ route =>
      if (registry.contains(route)) registry(route)
      else throw new SakeRuntimeException(s"Route '$route' requested in a *voke but not in the registry")
    }
  }

  /** Invokes tasks in the dependant tasker.
    *
    * This method uses the calling thread to run tasks. It can be used
    * inside other tasks, or for one-off invokations that do not
    * benefit from threading.
    *
    * If the tasks have dependancies, they will be invoked also.
    *
    * If no routes are supplied, the default task is run. The default
    * task can be set using `defaultTaskRoute`.
    *
    * @param options a set of options for supplying data, altering
    *  performance and output, etc. See [[sake.VokeOptions]].
    * @param taskRoutes a list of task names to run. The
    *  default taskRoute is used, if the parameter seq is empty.
    */
  // TODO: make multiple.
  def invoke(
    options: VokeOptions,
    taskRoutes: String*
  )
      : MultiTypeMap =
  {
    // Start the threadpool.
    // Still do this for an invoke, in case multitasking
    // requested on task dependancies.

    opts = options

    handleShowOptionsElse(taskRoutes)
    {
      verifyTasksInRegistry(taskRoutes)

      executor = Executors.newCachedThreadPool()

      //TODO: What if the names do not exist?
      val rets = taskRoutes.map{ taskRoute =>
        val task = registry(taskRoute)
        (task.route, invokeWithCallChain(task, InvocationChain.empty))
      }

      //      taskRoutes.foreach{ taskRoute =>
      //      val task = registry(taskRoute)
      //      invokeWithCallChain(task,InvocationChain.empty)
      //   }
      //  }

      //shut down the executor service
      executor.shutdown()
      if(opts.threadStats) {
        traceAdvice(s"Total threads: $threadsUsed")
      }
      rets.toMap

    }
  }


  /** Invokes tasks with default options.
    *
    * This method uses the calling thread to run tasks. It can be used
    * inside other tasks, or for one-off invokations that do not
    * benefit from threading.
    *
    * If the tasks have dependancies, they will be invoked also.
    *
    * If no routes are supplied, the default task is run. The default
    * task can be set using `defaultTaskRoute`.
    *
    * @param options a set of options for supplying data, altering
    *  performance and output, etc. See [[sake.VokeOptions]].
    * @param taskRoutes a list of task names to run. The
    *  default taskRoute is used, if the parameter seq is empty.
    */
  def invoke(
    taskRoutes: String*
  )
      : MultiTypeMap =
  {
    invoke(VokeOptions.empty, taskRoutes: _*)
  }

  /** Invokes tasks with code debugging options.
    *
    * This method uses switches `backtrace` and `showCause`. It is
    * intended for investigating errors in code, not in `sake`.
    *
    * This method uses the calling thread to run tasks. It can be used
    * inside other tasks, or for one-off invokations that do not
    * benefit from threading.
    *
    * If the tasks have dependancies, they will be invoked also.
    *
    * If no routes are supplied, the default task is run. The default
    * task can be set using `defaultTaskRoute`.
    *
    * @param options a set of options for supplying data, altering
    *  performance and output, etc. See [[sake.VokeOptions]].
    * @param taskRoutes a list of task names to run. The
    *  default taskRoute is used, if the parameter seq is empty.
    */
  def invokeDebug(
    taskRoutes: String*
  )
      : MultiTypeMap =
  {
    invoke(VokeOptions.codeDebug(), taskRoutes: _*)
  }

  /** Evokes tasks in the dependant tasker.
    *
    * This method uses threads to run the targeted tasks. It can be
    * used for heavy duty tasks.
    *
    * If the tasks have dependancies, they will be invoked also.
    *
    * If no routes are supplied, the default task is run. The default
    * task can be set using `defaultTaskRoute`.
    *
    * @param options a set of options for supplying data, altering
    *  performance and output, etc. See [[sake.VokeOptions]].
    * @param taskRoutes a list of task names to run. The
    *  default taskRoute is used, if the parameter seq is empty.
    */
  def evoke(
    options: VokeOptions,
    taskRoutes: String*
  )
      :  MultiTypeMap =
  {
    opts = options

    handleShowOptionsElse(taskRoutes)
    {

      verifyTasksInRegistry(taskRoutes)

      // Ok, running
      val trs =
        if (taskRoutes.isEmpty) Seq(defaultTaskRoute)
        else taskRoutes

      //TODO: What if the names do not exist?
      /////////////////////////////////////
      // Enable and run with threads
      executor = Executors.newCachedThreadPool()

      val futures = trs.map{ taskRoute =>
        threadsUsed += 1
        val threadable = new TaskThreadable(
          registry(taskRoute),
          InvocationChain.empty
        )
        executor.submit(threadable)
      }

      // Iterate future returns
      val ret = futures.map{ f =>
        try {
          //println("  evoke return: " + f.get())
          f.get()
        }
        catch  {
          case ex: Exception =>
            ex.printStackTrace()
            //TODO: What we need here is the task, to give a proper name?
            ("naff", None)
        }
      }.toMap

      //shut down the executor service
      executor.shutdown()


      if(opts.threadStats) {
        traceAdvice(s"Total threads: $threadsUsed")
      }
      ret
    }

  }

  /** Evokes tasks with default options.
    *
    * This method uses threads to run the targeted tasks. It can be
    * used for heavy duty tasks.
    *
    * If the tasks have dependancies, they will be invoked also.
    *
    * If no routes are supplied, the default task is run. The default
    * task can be set using `defaultTaskRoute`.
    *
    * @param options a set of options for supplying data, altering
    *  performance and output, etc. See [[sake.VokeOptions]].
    * @param taskRoutes a list of task names to run. The
    *  default taskRoute is used, if the parameter seq is empty.
    */
  def evoke(
    taskRoutes: String*
  )
      :  MultiTypeMap =
  {
    evoke(VokeOptions.empty, taskRoutes: _*)
  }

  /** Evokes tasks with code debugging options.
    *
    * This method uses switches `backtrace` and `showCause`. It is
    * intended for investigating errors in code, not in `sake`.
    *
    * This method uses threads to run the targeted tasks. It can be
    * used for heavy duty tasks.
    *
    * If the tasks have dependancies, they will be invoked also.
    *
    * If no routes are supplied, the default task is run. The default
    * task can be set using `defaultTaskRoute`.
    *
    * @param options a set of options for supplying data, altering
    *  performance and output, etc. See [[sake.VokeOptions]].
    * @param taskRoutes a list of task names to run. The
    *  default taskRoute is used, if the parameter seq is empty.
    */
  def evokeDebug(
    taskRoutes: String
  )
      :  MultiTypeMap =
  {
    evoke(VokeOptions.codeDebug(), taskRoutes)
  }


  // Tracing and error messages //
  /** Display the tasks and prerequisites
    */
  private def displayPrerequisites()
  {
    // TODO: I think this is the list of every task
    registry.foreach{ case(route, task) =>
      print(route)
      println(":")
      prerequisiteTasks(task).foreach {p =>
        print("    ")
        print(p.route)
      }
      println
    }
  }
  
  // Stock error reporting
  private def displayExceptionBacktrace(
    t: Throwable
  )
  {
    // Humm. This is a trait, so must be in the method?
    val stackTraceStripRE = """\nsake[^\n]+""".r

    if (opts.backtrace) {
      val st = t.getStackTraceString
      trace(stackTraceStripRE.replaceAllIn(st, ""))
    }
    else {
      traceAdvice("(See a full backtrace by running task with the 'backtrace' option)")
    }
  }

  // Stock error reporting, no callchain
  // TODO: Maybe should use callchain?
  private def displayException(
    ex: Exception
  )
  {
    traceError(ex.getMessage())
    displayExceptionBacktrace(ex)
  }

  /** Writes the error message from an exception.
    */
  protected def displayErrorMessage(
    ex: Exception
  )
  {
    displayException(ex)
  }

  // Stock error reporting with callchain
  /** Writes the error message from an exception.
    * 
    * Includes an invokation chain.
    */
  protected def displayErrorMessage(
    //options: VokeOptions,
    invocationChain: InvocationChain,
    ex: Exception
  )
  {
    displayException(invocationChain, ex)
  }



  /** Writes the error message from an exception.
    *
    * Includes an invokation chain.
    *
    * Adds a line to the error saying execution was aborted.
    */
  private def displayErrorMessageWithAbort(
    //options: VokeOptions,
    invocationChain: InvocationChain,
    ex: Exception
  )
  {
    trace ("task aborted!")
    displayErrorMessage(invocationChain, ex)
  }

  /** Prints exceptions to streams.
    *
    *
    */
  private def displayException(
    //options: VokeOptions,
    invocationChain: InvocationChain,
    ex: Exception
  )
  {
    displayExceptionMessageDetails(invocationChain, ex)

    if (ex.getCause() != null && !opts.showCause) {
      traceAdvice("(To see the cause of this exception, run with the 'showCause' option)")
    }

    if (ex.getCause() != null && opts.showCause) {
      val cause = ex.getCause
      // Dont display causes with invokation chains.
      traceError("Cause: " + cause.getMessage())
      displayExceptionBacktrace(cause)
    }
    else displayExceptionBacktrace( ex)
  }

  
  private def displayExceptionMessageDetails(
    invocationChain: InvocationChain,
    t: Throwable
  )
  {

    // TODO: In original, if not runtime, adds the class where exception thrown
    /*
     ex match {
     case e: RuntimeException => {
     trace(e.toString())
     }
     case e: Exception => trace(e.getMessage() + exceptionChainStr)
     }
     */
    val exceptionChainStr = "\n" + invocationChain.toString
    traceError(t.getMessage() + exceptionChainStr)
  }

}//DependantTaskerLike
