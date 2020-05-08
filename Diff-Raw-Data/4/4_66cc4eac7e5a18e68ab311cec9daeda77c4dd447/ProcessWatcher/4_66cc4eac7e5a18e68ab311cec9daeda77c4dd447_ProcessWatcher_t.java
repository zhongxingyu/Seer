 package de.skuzzle.polly.process;
 
 
 /**
  * A ProcessWatcher waits until a certain process exited and then invokes a callback 
  * method. This allows asynchronous observation of a process created by a 
  * {@link ProcessExecutor}. Use {@link ProcessExecutor#setProcessWatcher(ProcessWatcher)}
  * to assign a watcher to the process being created.
  * 
  * Additionally, a ProcessWatcher can be assigned a timeout in milliseconds. If so, the
  * callback will be invoked if the time runs out or the observed process exited - whatever
  * happened earlier.
  * 
  * Note that observation and timeout check both require separate threads.
  * 
  * <pre>
  *     ProcessExecutor pe = ProcessExecutor.getOsInstance(false);
  *     pe.setProcessWatcher(new ProcessWatcher(60000) {
  *         public void processExit(ProcessWrapper proc, int exitType) {
  *             if (exitType == ProcessWatcher.EXIT_TYPE_TIMEOUT) {
  *                 System.out.println("Timeout while waiting for " + proc);
  *             }
  *         }}).start();
  * </pre>
  * 
  * @author Simon
   */
 public abstract class ProcessWatcher extends Thread {
 
     /**
      * Exit type if a timeout occurred.
      */
     public final static int EXIT_TYPE_TIMEOUT = -2;
     
     /**
      * Exit type if an error occurred.
      */
     public final static int EXIT_TYPE_ERROR = -1;
     
     /**
      * Exit type if the process terminated.
      */
     public final static int EXIT_TYPE_SUCCESS = 0;
     
     
     
     private ProcessWrapper proc;
     private int timeout;
     private Thread timeoutWatcher;
     private boolean timeouted;
     
     
     
     /**
      * Creates a new ProcessWatcher with no timeout.
      */
     public ProcessWatcher() {
         this(-1);
     }
     
     
     
     /**
      * Creates a new ProcessWatcher with a given timeout.
      * 
      * @param timeout The timeout in milliseconds when waiting for the observed process. 
      *      Values < 0 will be ignored (= no timeout will be set).
      */
     public ProcessWatcher(int timeout) {
         this.timeout = timeout;
         this.setDaemon(true);
     }
         
     
     
     /**
      * Sets the process being watched by this watcher. This is done by the 
      * {@link ProcessExecutor}.
      * 
      * @param proc The process to watch.
      */
     void setProc(ProcessWrapper proc) {
         this.proc = proc;
     }
     
     
     
     /**
      * Starts observation for the Process and additionally starts a thread which checks
      * this thread for timeouts. If timeouted, this thread is interrupted to stop 
      * observation.
      */
     @Override
     public synchronized void start() {
         super.start();
         if (this.timeout > 0) {
             this.timeoutWatcher = new Thread() {
                 public void run() {
                     try {
                         Thread.sleep(ProcessWatcher.this.timeout);
                         ProcessWatcher.this.timeouted = true;
                         ProcessWatcher.this.interrupt();
                     } catch (InterruptedException ignore) {
                         // ignore
                     }
                 };
             };
             this.timeoutWatcher.start();
         }        
     }
     
     
     
     /**
      * The callback method that is invoked when the observed process terminates or
      * a timeout occured.
      *  
      * @param proc The wrapper of the process being watched.
      * @param exitType The event type which caused this callback being called. The value 
      *      is either of {@link #EXIT_TYPE_SUCCESS} (if process terminates properly), 
      *      {@link #EXIT_TYPE_TIMEOUT} (if a timeout occurred before termination of the 
     *      process) or {@link #EXIT_TYPE_ERROR} (if an error occurred while waiting for 
     *      the process to terminate).
      */
     public abstract void processExit(ProcessWrapper proc, int exitType);
     
     
     
     @Override
     public void run() {
         try {
             this.proc.getProcess().waitFor();
             if (this.timeoutWatcher != null) {
                 this.timeoutWatcher.interrupt();
             }
             this.processExit(this.proc, EXIT_TYPE_SUCCESS);
         } catch (InterruptedException e) {
             if (this.timeouted) {
                 this.processExit(this.proc, EXIT_TYPE_TIMEOUT);
                 return;
             } else if (this.timeoutWatcher != null) {
                 this.timeoutWatcher.interrupt();
             }
            this.processExit(this.proc, EXIT_TYPE_ERROR);
         }
     }
 }
