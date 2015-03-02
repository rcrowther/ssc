package sake.action

/*
 import sun.jvmstat.monitor.HostIdentifier
 import sun.jvmstat.monitor.MonitorException
 import sun.jvmstat.monitor.MonitoredHost
 import sun.jvmstat.monitor.MonitoredVm
 import sun.jvmstat.monitor.MonitoredVmUtil
 import sun.jvmstat.monitor.VmIdentifier
 */
import sun.jvmstat.monitor._
import scala.collection.JavaConverters._
/*
Working scalac...
scalac -toolcp /home/rob/Deployed/jdk1.7.0_71/lib/tools.jar -d /home/rob/Code/sake/build/main/scala/ssc /home/rob/Code/sake/src/main/ssc/action/IntrospectVM.scala
java -Xmx256M -Xms32M -Xbootclasspath/a:/home/rob/Deployed/scala-2.11.4/lib/akka-actor_2.11-2.3.4.jar:/home/rob/Deployed/scala-2.11.4/lib/config-1.2.1.jar:/home/rob/Deployed/scala-2.11.4/lib/jline-2.12.jar:/home/rob/Deployed/scala-2.11.4/lib/scala-actors-2.11.0.jar:/home/rob/Deployed/scala-2.11.4/lib/scala-actors-migration_2.11-1.1.0.jar:/home/rob/Deployed/scala-2.11.4/lib/scala-compiler.jar:/home/rob/Deployed/scala-2.11.4/lib/scala-continuations-library_2.11-1.0.2.jar:/home/rob/Deployed/scala-2.11.4/lib/scala-continuations-plugin_2.11.4-1.0.2.jar:/home/rob/Deployed/scala-2.11.4/lib/scala-library.jar:/home/rob/Deployed/scala-2.11.4/lib/scalap-2.11.4.jar:/home/rob/Deployed/scala-2.11.4/lib/scala-parser-combinators_2.11-1.0.2.jar:/home/rob/Deployed/scala-2.11.4/lib/scala-reflect.jar:/home/rob/Deployed/scala-2.11.4/lib/scala-swing_2.11-1.0.1.jar:/home/rob/Deployed/scala-2.11.4/lib/scala-xml_2.11-1.0.2.jar:/home/rob/Deployed/jdk1.7.0_71/lib/tools.jar -classpath "" -Dscala.home=/home/rob/Deployed/scala-2.11.4 -Dscala.usejavacp=true -Denv.emacs= scala.tools.nsc.Main -d /home/rob/Code/sake/build/main/scala/ssc /home/rob/Code/sake/src/main/ssc/action/IntrospectVM.scala

*/
/*
 /home/rob/Deployed/jdk1.7.0_71/lib
 /home/rob/Deployed/jdk1.7.0_71/bin/java -Xmx256M -Xms32M -Xbootclasspath/a:/home/rob/Deployed/jdk1.7.0_71/lib/jconsole.jar:/home/rob/Deployed/jdk1.7.0_71/lib/missioncontrol/mc.jar:/home/rob/Deployed/scala-2.11.4/lib/akka-actor_2.11-2.3.4.jar:/home/rob/Deployed/scala-2.11.4/lib/config-1.2.1.jar:/home/rob/Deployed/scala-2.11.4/lib/jline-2.12.jar:/home/rob/Deployed/scala-2.11.4/lib/scala-actors-2.11.0.jar:/home/rob/Deployed/scala-2.11.4/lib/scala-actors-migration_2.11-1.1.0.jar:/home/rob/Deployed/scala-2.11.4/lib/scala-compiler.jar:/home/rob/Deployed/scala-2.11.4/lib/scala-continuations-library_2.11-1.0.2.jar:/home/rob/Deployed/scala-2.11.4/lib/scala-continuations-plugin_2.11.4-1.0.2.jar:/home/rob/Deployed/scala-2.11.4/lib/scala-library.jar:/home/rob/Deployed/scala-2.11.4/lib/scalap-2.11.4.jar:/home/rob/Deployed/scala-2.11.4/lib/scala-parser-combinators_2.11-1.0.2.jar:/home/rob/Deployed/scala-2.11.4/lib/scala-reflect.jar:/home/rob/Deployed/scala-2.11.4/lib/scala-swing_2.11-1.0.1.jar:/home/rob/Deployed/scala-2.11.4/lib/scala-xml_2.11-1.0.2.jar:/home/rob/Code/sake/myApp.jar -classpath /home/rob/Deployed/jdk1.7.0_71/lib/tools.jar -Dssc.isjdk=true -Dscala.usejavacp=true -Denv.emacs= ssc.Runner -verbose compile

 scalac -toolcp /home/rob/Deployed/jdk1.7.0_71/lib/tools.jar -d /home/rob/Code/sake/build/main/scala/ssc /home/rob/Code/sake/src/main/ssc/action/IntrospectVM.scala
 */


class IntrospectVM {

  def run() {
    System.out.println(getPid(this.getClass.getName))
  }

  //mainClass.getName()
  def getPid(mainClassName: String)
      : Option[Integer] =
  {
    try {
      val monitoredHost: MonitoredHost = MonitoredHost.getMonitoredHost(new HostIdentifier(""))
      val activeVmPids = monitoredHost.activeVms()

      //java.util.Set[Integer]
      activeVmPids.asScala.find{ vmPid =>
        var mvm: MonitoredVm = null
        try {
          mvm = monitoredHost.getMonitoredVm(new VmIdentifier(vmPid.toString))
          val mvmMainClassStr = MonitoredVmUtil.mainClass(mvm, true)
          (mainClassName == mvmMainClassStr)
        }
        finally {
          if (mvm != null) {
            mvm.detach()
          }
        }
      }
    }
    catch {
      case e: java.net.URISyntaxException =>
        throw new InternalError(e.getMessage())
      case e: MonitorException =>
        throw new InternalError(e.getMessage())
    }
  }

}//IntrospectVM
