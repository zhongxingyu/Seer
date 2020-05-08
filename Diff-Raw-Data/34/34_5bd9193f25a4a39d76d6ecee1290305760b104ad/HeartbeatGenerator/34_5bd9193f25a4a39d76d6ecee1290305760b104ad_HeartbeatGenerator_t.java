 package org.phicluster.core;
 
 import org.apache.zookeeper.KeeperException;
 import org.apache.zookeeper.WatchedEvent;
 import org.apache.zookeeper.Watcher;
 import org.apache.zookeeper.ZooKeeper;
 import org.apache.zookeeper.data.Stat;
 import org.phicluster.core.util.ByteUtil;
 
 public class HeartbeatGenerator implements Runnable, Watcher {
 
     protected final ZooKeeper zk;
     protected final String heartbeatPath;
     protected final int interval; // in milliseconds
 
     private boolean generateHeartbeats;
    private volatile boolean suspended;
     
     public enum State {INSTANTIATED, RUNNING, SUSPENDED, STOPPED};
    private State state;
     
     public HeartbeatGenerator(ZooKeeper zk, String heartbeatPath, int interval) {
         this.zk = zk;
         this.heartbeatPath = heartbeatPath;
         this.interval = interval;
         
         this.generateHeartbeats = true;
         this.suspended = false;
         
         this.state = State.INSTANTIATED;
     }
 
     public void run() {
         while (generateHeartbeats) {
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
                 long ts = System.currentTimeMillis();
                 zk.setData(heartbeatPath, ByteUtil.toBytes(ts), -1);
                 Stat stat = new Stat();
                 zk.getData(heartbeatPath, this, stat); // set a watch on the node
                 Thread.sleep(interval);
             } catch (KeeperException e) {
                 e.printStackTrace();
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }
         state = State.STOPPED;
     }
 
     public void process(WatchedEvent event) {
         // someone changed the node!
     }
     
     public State state() {
         return state;
     }
     
     public void stopHeartbeats() {
         this.generateHeartbeats = false;
     }
     
     public synchronized void suspend() {
         suspended = true;
     }
     
     public synchronized void resume() {
         suspended = false;
         notifyAll();
     }
 
 }
