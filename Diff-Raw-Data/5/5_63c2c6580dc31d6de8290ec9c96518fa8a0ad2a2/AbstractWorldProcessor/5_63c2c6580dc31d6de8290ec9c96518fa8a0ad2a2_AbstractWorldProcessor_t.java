 package balle.world.processing;
 
 import balle.world.AbstractWorld;
 import balle.world.Snapshot;
 
 /**
  * A class that is supposed to be a base class for any class that needs to
  * process world snapshots.
  * 
  * It defines a basic interface one could use to do this.
  * 
  * @author s0909773
  * 
  */
 public abstract class AbstractWorldProcessor extends Thread {
 
     private Snapshot            snapshot;
     private Snapshot            prevSnapshot;
     private final AbstractWorld world;
 
     public AbstractWorldProcessor(AbstractWorld world) {
         super();
         this.world = world;
     }
 
     @Override
     public final void run() {
         snapshot = null;
         while (true) {
             Snapshot newSnapshot = world.getSnapshot();
 
             if ((newSnapshot != null) && (!newSnapshot.equals(prevSnapshot))) {
                actionOnChange();
                 prevSnapshot = snapshot;
                 snapshot = newSnapshot;
             }
            actionOnStep();
         }
     }
 
     /**
      * Return the latest snapshot
      * 
      * @return snapshot
      */
     protected final Snapshot getSnapshot() {
         return snapshot;
     }
 
     /**
      * This function is a step counter for the vision input. It is increased
      * every time a new snapshot of the world is received.
      */
     protected abstract void actionOnStep();
 
     /**
      * This function will run when a snapshot that is different from the
      * previous one was received.
      */
     protected abstract void actionOnChange();
 }
