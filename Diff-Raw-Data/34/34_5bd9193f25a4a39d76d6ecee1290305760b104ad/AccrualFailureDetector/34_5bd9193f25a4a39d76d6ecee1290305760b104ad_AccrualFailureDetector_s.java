 package org.phicluster.core;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.zookeeper.KeeperException;
 import org.apache.zookeeper.ZooKeeper;
 import org.apache.zookeeper.data.Stat;
 import org.phicluster.core.util.ByteUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 


 public class AccrualFailureDetector implements Runnable {
     protected static final Logger logger = LoggerFactory.getLogger(AccrualFailureDetector.class);
     
     protected final int nodeId;
     protected final ZooKeeper zk;
     protected final int interval; // in milliseconds
     
     protected final Map<Integer, Long> nodeHeartbeats;
     protected final Map<Integer, HeartbeatMiss> heartbeatMisses;
     
     private boolean detectFailures;
     private volatile boolean suspended;
     
     public enum State {INSTANTIATED, RUNNING, SUSPENDED, STOPPED};
    protected State state;
 
     
     public AccrualFailureDetector(int nodeId, 
                                   ZooKeeper zk, 
                                   int interval) {
         this.nodeId = nodeId;
         this.zk = zk;
         this.interval = interval;
         
         this.nodeHeartbeats = new HashMap<Integer, Long>();
         this.heartbeatMisses = new HashMap<Integer, HeartbeatMiss>();
         
         this.detectFailures = true;
         this.suspended = false;
         
         this.state = State.INSTANTIATED;
 
     }
 
     public void run() {
         while (detectFailures) {
             state = State.RUNNING;
             try {
                 if (suspended) {
                     synchronized (this) {
                         while (suspended) {
                             state = State.SUSPENDED;
                             this.wait();
                         }
                     }
                     continue;
                 }
                 
                 // first wait to give nodes a chance to register heartbeats
                 Thread.sleep(interval);
                 
                 // scan nodes for accrual failure detection
                 scanNodes();
                 
                 // scan this node's secondary list to reclaim tasks of detected failed nodes
                 scanSecondaryTaskList();
                 
             } catch (InterruptedException | KeeperException e) {
                 e.printStackTrace();
             }
         }
         state = State.STOPPED;
     }
 
     public State state() {
         return state;
     }
     
     public void stopAccrualFailureDetector() {
         this.detectFailures = false;
     }
     
     public synchronized void suspend() {
         suspended = true;
     }
     
     public synchronized void resume() {
         suspended = false;
         notifyAll();
     }
     
     /**
      * list of other nodes that the failure detector believes they are up.  
      */
     public Map<Integer, String> availableNodes() throws KeeperException, InterruptedException {
         Map<Integer, String> nodes = nodes();
         nodes.remove(nodeId); // remove itself
 
         Iterator<Integer> itr = nodes.keySet().iterator();
         while (itr.hasNext()) {
             Integer nid = itr.next();
             HeartbeatMiss hm = heartbeatMisses.get(nid);
             if (hm != null && hm.misses >= Constants.ACCRUAL_FAILURE_THRESHOLD) {
                 itr.remove();
             }
         }
         
         return nodes;
     }
     
     protected Map<Integer, String> nodes() throws KeeperException, InterruptedException {
         Map<Integer, String> nodes = new HashMap<Integer, String>();
         
         List<String> nodePathNames = zk.getChildren(Constants.ZN_NODES, null);
         for (String nodePathName : nodePathNames) {
             String suffix = nodePathName.substring(Constants.PREFIX_NODE.length());
             Integer nodeId = new Integer(suffix);
             nodes.put(nodeId, nodePathName);
         }
         
         return nodes;
     }
     
     protected void scanNodes() throws KeeperException, InterruptedException {
         Map<Integer, String> nodes = nodes();
         nodes.remove(nodeId); // remove itself
         
         for (Integer nodeId : nodes.keySet()) {
             String heartbeatPath = Constants.ZN_HEARTBEAT + "/" + nodes.get(nodeId);
             byte[] tsbytes = null;
             try {
                 tsbytes = zk.getData(heartbeatPath, false, null);
             } catch (KeeperException.NoNodeException nne) {
                 // no heart-beat znode, consider it as a miss
                 incrementMissCount(nodeId);
                 continue;
             }
             long ts = ByteUtil.readLong(tsbytes, 0);
             Long lastHeartbeat = nodeHeartbeats.get(nodeId);
             if (lastHeartbeat == null) {
                 lastHeartbeat = new Long(-1); // meaning no previous heart-beat
             }
             if (ts > lastHeartbeat) {
                 nodeHeartbeats.put(nodeId, ts);
                 // reset miss count as soon as a recent heartbeat detected
                 resetMissCount(nodeId);
             } else {
                 // no heart-beat since the last known one!
                 // increment the miss count
                 incrementMissCount(nodeId);
             }
         }
         
         // remove any nodes that still have entry in heartbeat map but 
         // not in available nodes list. an alternative approach could be to 
         // increment the miss count for such nodes.
         for (Integer nodeId : heartbeatMisses.keySet()) {
             if (!nodes.containsKey(nodeId)) {
                 heartbeatMisses.remove(nodeId);
             }
         }
     }
     
     protected void scanSecondaryTaskList() throws KeeperException, InterruptedException {
         Map<Integer, String> nodes = nodes(); // get list of nodes from ZK
         
         // get all tasks in the secondary tasks list (replicated tasks) 
         // and scan to identify the ones whose owner currently have 
         // above-the-threshold heartbeat misses (accrual failure detection)
         String secondaryTasksPath = Constants.ZN_TASK_SECONDARY_WITH_PREFIX + this.nodeId;
         List<String> secondaryTaskList = zk.getChildren(secondaryTasksPath, false);
         for (String taskNode : secondaryTaskList) {
             String taskNodePath = secondaryTasksPath + "/" + taskNode;
             byte[] b = zk.getData(taskNodePath, false, null);
             int taskOwnerNodeId = ByteUtil.readInt(b, 0);
             
             if (!nodes.containsKey(taskOwnerNodeId)) {
                 // task's owner node is not part of the cluster; it might have been
                 // removed, try to reclaim and put the task back into the queue
                 logger.debug("task's owner node is not in the list of available " +
                 		"nodes, considering not part of the cluster and releasing " +
                 		"the task, task: {}, node: {}", taskNode, taskOwnerNodeId);
                 reclaimTask(taskNode);
                 continue;
             }
             
             HeartbeatMiss hm = heartbeatMisses.get(taskOwnerNodeId);
             if (hm == null) {
                 // no heartbeat miss registered, the node is healthy
                 logger.debug("no heartbeat miss registered for the task's owner " +
                 		"node: {}, considering the node healthy [task: {}]", 
                 		taskOwnerNodeId, taskNode);
                 continue;
             }
             
             if (hm.misses >= Constants.ACCRUAL_FAILURE_THRESHOLD) {
                 // consider the owning node failed, reclaim the task 
                 logger.debug("accrual failure detected for node: {}, releasing " +
                 		"the task: {} [miss count: {}]", 
                 		new Object[] {taskOwnerNodeId, taskNode, hm.misses});
                 reclaimTask(taskNode);
             }
         }
     }
     
     protected boolean reclaimTask(String taskName) throws InterruptedException, KeeperException {
         // check if the task is in "taken" list        
         String taskTakenPath = Constants.ZN_TASK_TAKEN + "/" + taskName;
         
         Stat stat = zk.exists(taskTakenPath, false);
         
         String secondaryTaskPath = Constants.ZN_TASK_SECONDARY_WITH_PREFIX 
                 + this.nodeId + "/" + taskName;
         if (stat == null) {
             // not in 'taken' list, no need to do anything other than 
             // just removing it from the secondary task list
             logger.debug("{} is not in taken list, only removing from " +
             		"secondary list of node {}", taskName, nodeId);
             zk.delete(secondaryTaskPath, -1);
             return false;
         }
         
         // task is in taken list, so, we need to put it back into task queue. 
         // 1. create the task in the queue
         // 2. remove it from taken list to make it available. 
         // TODO: we can also carry the secondary nodes
         // info with along the task, and remove the task from other secondary
         // list when it's reclaimed here.
         String suffix = taskName.substring(Constants.PREFIX_TASK.length());
         Long taskId = new Long(suffix);
         try {
             DistTaskPool.defaultInstance().createTaskInQueue(taskId);
         } catch (KeeperException.NodeExistsException nee) {
             // TODO: already created by someone else? then, it is being taken
             // care of, so no need to do anything here --just remove the task
             // from the secondary list and return.
             logger.debug("{} is already in task queue --it must have been " +
             		"taken care of by someone else, removing it from " +
             		"my secondary task list.", taskName);
             zk.delete(secondaryTaskPath, -1);
             return false;
         }
         
         // remove the task from taken list
         try {
             zk.delete(taskTakenPath, -1);
         } catch (KeeperException.NoNodeException nne) {
             logger.error("{} was expected to be in taken tasks list but not! " +
             		"removing the task from secondary list anyway...", taskName);
         }
         
         zk.delete(secondaryTaskPath, -1);
         return true;
     }
     
     protected Integer heartbeatMisses(int nodeId) {
         HeartbeatMiss hm = heartbeatMisses.get(nodeId);
         if (hm == null) {
             return null;
         }
         return hm.misses;
     }
     
     protected void incrementMissCount(int nodeId) {
         HeartbeatMiss hm = heartbeatMisses.get(nodeId);
         if (hm == null) {
             hm = new HeartbeatMiss(nodeId, 0);
             heartbeatMisses.put(nodeId, hm);
         }
         hm.misses++;        
     }
 
     protected void resetMissCount(int nodeId) {
         HeartbeatMiss hm = heartbeatMisses.get(nodeId);
         if (hm == null) {
             hm = new HeartbeatMiss(nodeId, 0);
             heartbeatMisses.put(nodeId, hm);
         }
         hm.misses = 0; 
     }
 
     protected static class HeartbeatMiss implements Comparable<HeartbeatMiss> {
         public final int nodeId;
         public int misses;
         
         public HeartbeatMiss(int nodeId, int misses) {
             this.nodeId = nodeId;
             this.misses = misses;
         }
 
         @Override
         public boolean equals(Object other) {
             if (other instanceof HeartbeatMiss) {
                 HeartbeatMiss hm = (HeartbeatMiss) other;
                 if (nodeId == hm.nodeId && misses == hm.misses) {
                     return true;
                 }
             }
             return false;
         }
         
         @Override
         public int hashCode() {
             int prime = 31;
             int result = 1;
             result = prime * result + nodeId;
             result = prime * result + misses;
             return result;
         }
 
         public int compareTo(HeartbeatMiss o) {
             if (this.misses < o.misses) {
                 return -1;
             }
             if (this.misses > o.misses) {
                 return +1;
             }
             return 0;
         }
     }
     
 }
