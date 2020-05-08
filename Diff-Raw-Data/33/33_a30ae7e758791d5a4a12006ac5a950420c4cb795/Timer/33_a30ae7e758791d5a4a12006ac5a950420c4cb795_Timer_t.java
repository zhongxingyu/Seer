 package expectj;
 
 
 /**
  * This class acts like a timer and invokes the listener on time-out.
  */
 public class Timer implements Runnable
 {
     /**
      * The time interval in milliseconds up to which the process 
      * should be allowed to run.
      */
     private long timeOut = 0;
 
     /**
      * The entity that wants to be notified on timeout.
      */
     private TimerEventListener listener = null;
 
     /**
      * The waiting thread.
      */
     private Thread thread = null;
 
    /**
     * Timer not started.
     */
    public static final int NOT_STARTED = 0;
    
    /**
     * Timer started and still running.
     */
    public static final int STARTED     = 1;
    
    /**
     * Timer timed out.
     */
    public static final int TIMEDOUT    = 2;
    
    /**
     * Timer interrupted.
     */
    public static final int INTERRUPTED = 3;
   
     /**
      * Stores the current status of Timer
      */
     private int currentStatus = NOT_STARTED;
    
     /**
      * Constructor
      *
      * @param timeOut  Time interval after which the listener will be
      *                 invoked
      * @param listener Object implementing the TimerEventListener
      *                 interface
      */
     public Timer(long timeOut, TimerEventListener listener) {
    
         if (timeOut < 1) {
             throw new IllegalArgumentException("Time-Out value cannot be < 1");
         }
         if (listener == null ) {
             throw new IllegalArgumentException("Listener cannot be null");
         }
         this.timeOut = timeOut * 1000;
         this.listener = listener;
 
      }
 
     /**
      * Starts the timer
      */
     public void startTimer() {
 
         thread = new Thread(this);
         currentStatus = STARTED; 
         thread.start();
             
     }
   
     /**
     * Return timer status.  Can be one of {@link #NOT_STARTED}, {@link #STARTED},
     * {@link #TIMEDOUT} or {@link #INTERRUPTED}.
     * 
      * @return the status of the timer
      */
     public int getStatus() {
 
         return currentStatus;
 
     }
    
     
     // Thread method
     public void run() {
 
         try {
             // Sleep for the specified time
             Thread.sleep(timeOut);
             // Jag Utha Shaitan, Its time to invoke the listener
             currentStatus = TIMEDOUT;
             listener.timerTimedOut();
         }
         catch (InterruptedException iexp) {
             currentStatus = INTERRUPTED;
             listener.timerInterrupted(iexp);
         }
 
     }
 
 }
