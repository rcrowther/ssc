package sake

import scala.util.DynamicVariable


/** Trait which can supplys return parameters to an anomymous block.
  *
  * This is inherited into a [[sake.Task]]. It enables DSLs to
  * define task actions as an anonymous block (thunk), yet the
  * underlying code can provide the action code with parameters (maybe
  * better described not as 'parameters', but 'passed-in and
  * accessible' values). If the values were provided as parameters to
  * the actions, then they would need to be visible in DSLs.
  *
  * This is as near as can be called in code a 'trick'. It was derived
  * from Scalatra, and the background to the Scalatra implementation
  * (crutial to keeping the DSL clean) is explained in detail in
  * Gabriele Renzi's blog post about Step, out of which Scalatra grew:
  *
  * To use, inherit the trait, then underlying code must call
  * `invokeWithReturn` with the value wished to become available, and
  * the block to be invoked.
  *
  * http://www.riffraff.info/2009/4/11/step-a-scala-web-picoframework
  */
trait DynamicScope {

  /** Makes the accessible value available using the symbol `returns`.
    * 
    * Valid only inside the `invokeWithParams` method.
    */
  implicit def returns: MultiTypeMap = dynamicReturns.value

  private[this] val dynamicReturns = new DynamicVariable[MultiTypeMap](null)

  /** Makes the accessible value available using the symbol `params`.
    * 
    * Valid only inside the `invokeWithParams` method.
    */
  //implicit def params: MultiTypeMap = dynamicParams.value

  //private[this] val dynamicParams = new DynamicVariable[MultiTypeMap](null)


  /** Executes the given anonymous block with the supplied value scoped in.
    *
    * @param m a multi-type option map to be made available to block code.
    * @param block an anonymous code block, such as provided by non-executing
    *  parameter `:=>` operations.  
    */
  protected def invokeWithParams(
    returns: MultiTypeMap
  )(block: => Any): Any =
    //dynamicParams.withValue(params) {
      dynamicReturns.withValue(returns) {
        block
      }
   // }

}//DynamicScope
