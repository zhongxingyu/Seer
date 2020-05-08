 package org.kevoree.monitoring.comp.monitor;
 
 import org.kevoree.annotation.*;
 import org.kevoree.framework.AbstractComponentType;
 import org.kevoree.framework.MessagePort;
 import org.resourceaccounting.ResourcePrincipal;
 
 import java.lang.management.ManagementFactory;
 import java.lang.management.ThreadMXBean;
 import java.util.Timer;
 import java.util.TimerTask;
 
 /**
  * Created with IntelliJ IDEA.
  * User: inti
  * Date: 9/5/13
  * Time: 2:22 PM
  * To change this template use File | Settings | File Templates.
  */
 @Requires({
         @RequiredPort(name = "maximumCPU", optional = true, type = PortType.MESSAGE),
         @RequiredPort(name = "maximumMem", optional = true, type = PortType.MESSAGE)
 })
 @ComponentType
 public class MeasuringCPUUsage extends AbstractComponentType {
 
     private final static int nCPUs = Runtime.getRuntime().availableProcessors();
     public static final double ELAPSED_TIME = 1000; // 1 second
 
     private final ThreadMXBean tmxb = ManagementFactory.getThreadMXBean();
     double previousCPU = 0; // in milliseconds
     double cpuUsageCumulative = 0;
     int time = 0;
 
     double maximumCPUUsage = 0;
 
     int count = 0;
 
     GCWatcher gcWatcher = new GCWatcher();
     Timer t = new Timer();
    private double maximumMem;
 
     class Measuring extends TimerTask implements ContractVerificationRequired {
 
         @Override
         public void run() {
             long[] threadIDs = tmxb.getAllThreadIds();
             double sum = 0;
             for (int i = 0; i < threadIDs.length; i++) {
                 long threadId = threadIDs[i];
                 if (threadId != -1) {
                     sum += tmxb.getThreadCpuTime(threadId) / 1000000F;
                 }
             }
 
             boolean firstTime = previousCPU == 0;
             double diff = sum - previousCPU; // FIXME : this has a problem because the set of active threads may differs between
                                              // calls. For now I can put the minimum to 0
             if (diff < 0) diff = 0;
             previousCPU = sum;
             double cpuUsage = Math.min(99, diff/(ELAPSED_TIME*nCPUs)*100);
 
             time ++;
             cpuUsageCumulative += cpuUsage;
 
             // avoid to check when the previous measurement doesn't exist because its use may imply erroneous estimation
             if (!firstTime) {
 //                System.out.printf("Last %f , total %f\n", cpuUsage, cpuUsageCumulative/(100*time)*100);
             }
 
             // skip initial values because the overhead is due deployment
             if (count > 5) {
                 if (cpuUsage > maximumCPUUsage)  {
                     maximumCPUUsage = cpuUsage;
                     MessagePort p = getPortByName("maximumCPU",MessagePort.class);
                     p.process(maximumCPUUsage);
                 }
             }
             count++;
         }
 
         @Override
         public void verifyContract(ResourcePrincipal principal, Object obj) {
         }
 
         @Override
         public void onGCVerifyContract(long used, long max) {
             double u = used;
             u /= max;
             u *= 100;
            if (u > maximumMem) {
                maximumMem = u;
                MessagePort p = getPortByName("maximumMem",MessagePort.class);
                p.process(maximumMem);
            }
         }
     }
 
 
     @Start
     public void start() {
         gcWatcher.register();
         Measuring m = new Measuring();
         gcWatcher.addContractVerificationRequieredListener(m);
         t.schedule(m,(long)ELAPSED_TIME,(long)ELAPSED_TIME);
     }
 
     @Stop
     public void stop() {
         t.cancel();
         t.purge();
         gcWatcher.unregister();
     }
 
     @Update
     public void update() {
         stop();
         start();
     }
 }
