 package fm.audiobox.sync.util;
 
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.RejectedExecutionException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fm.audiobox.core.observables.Event;
 
 public class JobManager extends Observable implements Observer {
 
   private static final Logger log = LoggerFactory.getLogger( JobManager.class );
   
   public static final int MAX_CONCURRENT_JOBS = 10;
   
   private int maxConcurrentJob = MAX_CONCURRENT_JOBS;
   private ExecutorService executor;
   
   private long jobsRunning = 0;
 
   
   public JobManager() {
     this( MAX_CONCURRENT_JOBS );
   }
   
   public JobManager(int maxJobs) {
     this.maxConcurrentJob = maxJobs;
   }
   
   public synchronized boolean isCompleted(){
     return this.jobsRunning <= 0;
   }
   
   public boolean isInternallyTerminated() {
     return this.executor.isShutdown() || this.executor.isTerminated();
   }
   
   private void setup(){
     log.info( "Setupping new ExecutorManager for " + this.maxConcurrentJob + " concurrent jobs" );
     this.jobsRunning = 0;
     this.executor = Executors.newFixedThreadPool( this.maxConcurrentJob );
   }
   
   public boolean execute(JobTask job) {
     
     if ( this.executor == null || this.isInternallyTerminated() ){
       this.setup();
       
       this.setChanged();
       Event.fireEvent(this, Event.States.START_LOADING, job);
     }
     
     log.debug("New Job will be queued");
     
     job.addObserver(this);
     
     try {
       Future<?> future = this.executor.submit(job);
       job.setReferencedTask( future );
       
       log.debug("Job has been queued: " + (++this.jobsRunning) );
       
       this.setChanged();
       Event.fireEvent(this, Event.States.ENTITY_ADDED, job);
       
     } catch( RejectedExecutionException  e ){
       
       log.error("Job will not be queued due to rejection: " + e.getMessage() );
       log.info("JabManager is shutdown or already terminated: " + (this.executor.isShutdown() || this.executor.isTerminated() ) );
       
       this.setChanged();
       Event.fireEvent(this, Event.States.ERROR, job);
       
       return false;
     }
     
     return true;
   }
 
   
   public boolean stop() {
     log.warn("JobManager is going to be stopped");
    List<?> list = this.executor.shutdownNow();
     log.info( list.size() + " will not be executed");
     this.jobsRunning -= list.size();
     return true;
   }
   
   
   public void update(Observable j, Object e) {
     JobTask job = (JobTask) j;
     
     if ( e instanceof Event ) {
       Event event = (Event) e;
       
       if ( event.state == Event.States.START_LOADING ) {
         log.debug("[Event - " + job.getName() + "] JobTask is starting");
         
         this.setChanged();
         Event.fireEvent(this, Event.States.ENTITY_REFRESHED, job);
         
       } else if ( event.state == Event.States.END_LOADING ) {
         
         this.jobsRunning--;
         
         if ( this.isCompleted() ) {
           // No more jobs should be started
           this.setChanged();
           Event.fireEvent(this, Event.States.END_LOADING, job);
         }
         
         log.debug("[Event - " + job.getName() + "] JobTask has ended");
         
       } else if ( event.state == Event.States.ENTITY_REFRESHED ) {
         
       }
     }
     
   }
   
 }
