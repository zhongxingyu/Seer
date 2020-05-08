 package barc.subsumption;
 
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 /**
  * This is an implementation of the subsumption architecture as described in:
  * <p/>
  * RA Brooks. Intelligence without representation. Artificial intelligence,
  * 1991. Elsevier.
  * <p/>
  * This specific implementation was adapted from the subsumption architecture in
  * Lejos.
  *
  * @author Jeremiah Via <jxv911@cs.bham.ac.uk>
  */
 public class Subsumption implements Runnable {
     private static final int NONE = -1;
     private int active = NONE;
     private int highestPriority = NONE;
 
    private List<barc.subsumption.Behavior> behaviors;
 
     /**
      * Create the subsumption architecture with the list of behaviors a robot
      * will use.
      * <p/>
      * *NOTE* The behaviors are listed in decreasing priority, this must be kept
      * in mind when designing this type of system.
      *
      * @param behaviors the list of behaviors
      */
    public Subsumption(List<barc.subsumption.Behavior> behaviors) {
         this.behaviors = behaviors;
     }
 
     /**
      * Runs the subsumption system forever. It chooses the behavior with the
      * highest priority that is ready to run. Highest priority is defined by
      * position in the list of behaviors, with the head of the list having the
      * highest priority.
      */
     @Override
     public void run() {
         new Timer().scheduleAtFixedRate(new TimerTask() {
             @Override
             public void run() {
                 synchronized (this) {
                     highestPriority = NONE;
                     for (int i = 0; i < behaviors.size(); i++) {
                         if (behaviors.get(i).canRun()) {
                             highestPriority = i;
                             break;
                         }
                     }
 
                     int currentlyActive = active;
                     // closer to 0 means higher priority
                     if (currentlyActive != NONE && highestPriority < active)
                         behaviors.get(currentlyActive).stop();
                 }
             }
         }, 0, 100);
 
         while (highestPriority == NONE)
             Thread.yield();
 
         //noinspection InfiniteLoopStatement
         while (true) {
             synchronized (this) {
                 if (highestPriority != NONE)
                     active = highestPriority;
             }
 
             if (active != NONE) {
                 behaviors.get(active).run();
                 active = NONE;
             }
             Thread.yield();
         }
     }
 }
