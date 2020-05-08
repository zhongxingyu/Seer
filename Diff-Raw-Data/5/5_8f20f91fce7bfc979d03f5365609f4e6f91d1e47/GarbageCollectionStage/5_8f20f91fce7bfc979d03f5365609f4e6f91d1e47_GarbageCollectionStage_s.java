 package org.radargun.stages;
 
 import org.radargun.DistStageAck;
 import org.radargun.utils.Utils;
 
 /**
  * @author Diego Didona, didona@gsd.inesc-id.pt
  *         Date: 25/12/12
  */
 
 /*
 This is needed because invoking System.gc() on a node while another one is populating may induce
 timeouts on that node, avoiding the completion of the warmup phase.
 This happens when the gc takes a long time wrt to the timeout
  */
 public class GarbageCollectionStage extends AbstractDistStage {
 
    @Override
    public DistStageAck executeOnSlave() {
       DefaultDistStageAck defaultDistStageAck = newDefaultStageAck();
       long start = System.currentTimeMillis();
       log.info("Going to invoke the garbage collection");
      Utils.printMemoryFootprint(true);
       System.gc();
       log.info("Garbage collection ended");
      Utils.printMemoryFootprint(false);
       long duration = System.currentTimeMillis() - start;
       defaultDistStageAck.setDuration(duration);
       return defaultDistStageAck;
    }
 }
