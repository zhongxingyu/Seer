 /**
  * PROJECT   : Elbfisch - java process automation controller (jPac)
  * MODULE    : JPac.java
  * VERSION   : -
  * DATE      : -
  * PURPOSE   : 
  * AUTHOR    : Bernd Schuster, MSK Gesellschaft fuer Automatisierung mbH, Schenefeld
  * REMARKS   : -
  * CHANGES   : CH#n <Kuerzel> <datum> <Beschreibung>
  *
  * This file is part of the jPac process automation controller.
  * jPac is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * jPac is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with the jPac If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.jpac;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.Semaphore;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.log4j.Logger;
 import org.jpac.configuration.BooleanProperty;
 import org.jpac.configuration.Configuration;
 import org.jpac.configuration.IntProperty;
 import org.jpac.configuration.LongProperty;
 import org.jpac.configuration.StringProperty;
 import org.jpac.statistics.Histogramm;
 
 /**
  * central runtime engine of JPac
  * @author berndschuster
  */
 public class JPac extends Thread{
     static Logger Log = Logger.getLogger("jpac.JPac");
 
     private     final int       OWNMODULEINDEX          = 0;
     private     final long      DEFAULTCYCLETIME        = 100000000L; // 100 ms
     private     final long      DEFAULTCYCLETIMEOUTTIME = 1000000000L;// 1 s
     private     final long      MAXSHUTDOWNTIME         = 2000;       // 2 s
     private     final CycleMode DEFAULTCYCLEMODE        = CycleMode.FreeRunning;//TODO !!!! CycleMode.FreeRunning;
     private     final int       EXITCODEINITIALIZATIONERROR = 100;
     private     final int       EXITCODEINTERNALERROR       = 101;
 
     public enum CycleMode{OneCycle, Bound, LazyBound, FreeRunning}
     
     public      enum    Status{initializing, ready, running, halted};
 
     protected   static JPac  instance   = null;
 
     private     int                    tracePoint;//used for internal trace purposes
     private     Set<Fireable>          awaitedEventList;
     private     Set<Fireable>          awaitedSimEventList;
     private     Set<Fireable>          firedEventList;
     private     long                   minRemainingCycleTime;
     private     long                   maxRemainingCycleTime;
     private     long                   expectedCycleEndTime;
     private     long                   cycleStartTime;
     private     long                   nextCycleStartTime;
     private     long                   cycleNumber;
     private     Status                 status;
     private     boolean                emergencyStopRequested;
     private     boolean                emergencyStopActive;
     private     EmergencyStopException emergencyStopCausedBy;
     private     boolean                emergencyStopIsToBeThrown;
 
     private     boolean              readyToShutdown;
 
     private     Semaphore            startCycle;
     private     Semaphore            cycleEnded;
     
     private     boolean              running;
     
     private     final List<Runnable> synchronizedTasks;
     
     private LongProperty    propCycleTime;
     private LongProperty    propCycleTimeoutTime;
     private StringProperty  propCycleMode;
     private BooleanProperty propRunningInsideAnIde;
     private BooleanProperty propRunningInjUnitTest;
     private BooleanProperty propEnableTrace;
     private BooleanProperty propStoreConfigOnShutdown;
     private BooleanProperty propPauseOnBreakPoint;
     private IntProperty     propTraceTimeMinutes;
     private BooleanProperty propRemoteSignalsEnabled;
     private IntProperty     propRemoteSignalPort;
     private BooleanProperty propStoreHistogrammsOnShutdown;
     private StringProperty  propHistogrammFile;
  
     private String          instanceIdentifier;
     private long            cycleTime;
     private long            cycleTimeoutTime;
     private CycleMode       cycleMode;
     private boolean         runningInsideAnIde;
     private boolean         runningInjUnitTest;
     private boolean         enableTrace;
     private boolean         storeConfigOnShutdown;
     private boolean         pauseOnBreakPoint;
     private int             traceTimeMinutes;
     private boolean         remoteSignalsEnabled;
     private int             remoteSignalPort;
     private boolean         storeHistogrammsOnShutdown;
     private String          histogrammFile;
 
     private CountingLock    activeEventsLock;
     
     private Synchronisation startCycling;
     private Synchronisation stopCycling;
     private Synchronisation shutdownRequest;
     
     private boolean         shutdownByMySelf;
     private int             exitCode;
         
     private ProcessEvent    awaitedEventOfLastModule;
     
     private ArrayList<AbstractModule> moduleList;
     private TraceQueue                traceQueue;
     
     private Histogramm      cycleHistogramm;   //used to determine the overall load per cycle
     private Histogramm      systemHistogramm;  //used to determine the system load during a cycle
     private Histogramm      modulesHistogramm; //used to determine the load produced by the application modules during a cycle
     
     private AbstractModule  processedModule;   //
     
     private int             incrementCounter;
     private int             decrementCounter;
 
     private boolean         stopBeforeStartup;
     
     class ShutdownHook extends Thread{
 
         public ShutdownHook(){
             setName(getClass().getSimpleName());
         }
         
         private void waitUntilShutdownComplete(){
             int times100ms;
             for(times100ms = 0; times100ms < 100 && !readyToShutdown; times100ms++){
                 try {Thread.sleep(100);}catch(InterruptedException ex) {}
             }
             if (times100ms == 100){
                 //automation controller did not finish in time
                 Log.error("jPac stopped abnormaly !");
             }            
         }
         
         @Override
         public void run() {
             if (Log.isInfoEnabled()) Log.info("shutdown requested. Informing jPac ...");
 //    public      enum    Status{initializing, ready, running, halted};
             switch (status){
                 case initializing:
                 case ready:
                     //JPac has been initialized but did start cycling
                     stopBeforeStartUp();
                     waitUntilShutdownComplete();                    
                     break;
                 case running:
                     if (!readyToShutdown){
                         shutdownRequest.request();
                         waitUntilShutdownComplete();                    
                     }
                     else{
                         if (Log.isInfoEnabled()) Log.info("automation controller already shutdown");
                     }
                     break;
                 case halted:
                     //nothing to do
                     break;
             }
             if (Log.isInfoEnabled()) Log.info("shutdown complete");
         }
     }
         
     protected class CountingLock{
         private       int    count       = 0;
         private       int    maxcount    = 0;
         private final Object zeroReached = new Object();
         
         protected void increment(){
             synchronized(zeroReached){
                 count++;
                 maxcount++;
             }
         }
         
         protected boolean decrement() throws InconsistencyException{
             synchronized(zeroReached){
                 count--;
                 if (count < 0){
                     throw new InconsistencyException("CountingLock.decrement(): module counter inconsistent !!!!!!");
                 }
                 if (count == 0){
                     zeroReached.notify();
                 }
             }
             return count == 0;
         }
         
         protected int getCount(){
             int localCount = 0;
             synchronized(zeroReached){
                 localCount = count;
             }
             return localCount;
         }
         
         protected int getMaxCount(){
             int localCount = 0;
             synchronized(zeroReached){
                 localCount = maxcount;
             }
             return localCount;
         }
 
         protected void reset(){
             synchronized(zeroReached){
                 count    = 0;
                 maxcount = 0;
             }
         }
         
         protected void waitForUnlock(){
             synchronized(zeroReached){
                 while(count > 0){
                     try{zeroReached.wait();}catch(InterruptedException exc){};
                 }
             }            
         }
         
         protected void waitForUnlock(long waittime){//nanoseconds
             boolean waitReturned = false;
             if (waittime > 0){
                 synchronized(zeroReached){
                     while(count > 0 && !waitReturned){
                         try{
                             zeroReached.wait(waittime / 1000000L, (int)(waittime % 1000000));
                             //wait returned normally or due to timeout
                             waitReturned = true;
                         }catch(InterruptedException exc){};
                     }
                 }
             }
             else{
                 Log.error("cycle time expired before synchronization on modules !!!!!!: " + (-waittime) + " ns");
             }
         }
     }
     
     protected class Synchronisation{
         private boolean       requested                = false;
         private boolean       acknowledged             = false;
         private final Object  syncPointRequest         = new Object();
         private final Object  syncPointAcknowledgement = new Object();
         
         protected void waitForRequest(){
             synchronized(syncPointRequest){
                 while(!requested){
                     try{syncPointRequest.wait();}catch(InterruptedException exc){};
                 }
                 requested = false;
             }            
         }
         
         protected void acknowledge(){
             synchronized(syncPointAcknowledgement){
                 requested    = false;
                 acknowledged = true;
                 syncPointAcknowledgement.notify();
             }
         }
 
         protected void request(){
             synchronized(syncPointRequest){
                acknowledged = false;
                requested    = true;
                syncPointRequest.notify();
             }
             synchronized(syncPointAcknowledgement){
                 while(!acknowledged){
                     try{syncPointAcknowledgement.wait();}catch(InterruptedException exc){};
                 }
                acknowledged = false;
             }
         }
         
         protected boolean isRequested(){
             return requested;
         }
         
     }
 
     /**
      * used to hold the latest tracing information for a given number of execution cycles.
      */
     public class TraceQueue extends ArrayBlockingQueue<ModuleTrace>{
         private int  numberOfEntries;
                 
         protected TraceQueue(int numberOfEntries){
             super(numberOfEntries);
             this.numberOfEntries = numberOfEntries;
         }
                 
         public void putTrace(long cycleNumber, int moduleIndex, long startNanos, long endNanos) {
             ModuleTrace moduleTrace;
             synchronized(this){
                 moduleTrace = getNextModuleTrace();
                 moduleTrace.setCycleNumber(cycleNumber);
                 moduleTrace.setModuleIndex(moduleIndex);
                 moduleTrace.setStartNanos(startNanos);
                 moduleTrace.setEndNanos(endNanos);
                 //put the actual entry
                 try{super.put(moduleTrace);} catch(InterruptedException exc){/*cannot happen*/}
             }
         }
         private ModuleTrace getNextModuleTrace(){
             ModuleTrace moduleTrace;
             if(remainingCapacity() == 0){
                 //queue is full, remove the oldest entry (head)
                 //and recycle it
                 moduleTrace = poll();
             }
             else{
                 //create an new module trace record
                 moduleTrace = new ModuleTrace();
             }
             return moduleTrace;
         }
     }
 
     public class ModuleTrace{
         private long cycleNumber;
         private long moduleIndex;
         private long startNanos;
         private long endNanos;
                 
         public long getStartNanos() {
             return startNanos;
         }
 
         public long getEndNanos() {
             return endNanos;
         }
         
         protected void setCycleNumber(long cycleNumber){
             this.cycleNumber = cycleNumber;
         }
         protected void setModuleIndex(int moduleIndex){
             this.moduleIndex = moduleIndex;
         }
         protected void setStartNanos(long nanos){
             this.startNanos = nanos;
         }
         protected void setEndNanos(long nanos){
             this.endNanos = nanos;
         }
         
         public String toCSV(){
             StringBuffer csvString = new StringBuffer();
             csvString.append(cycleNumber);csvString.append(';');
             csvString.append(moduleIndex);csvString.append(';');
             csvString.append(startNanos);csvString.append(';');
             csvString.append(endNanos);csvString.append(';');
             return csvString.toString();
         }
     }
     
     protected JPac(){
         super();
         setName(getClass().getSimpleName());
         
         tracePoint                  = 0;
         minRemainingCycleTime       = Long.MAX_VALUE;
         maxRemainingCycleTime       = 0;
         expectedCycleEndTime        = 0;
         cycleStartTime              = 0;
         nextCycleStartTime          = 0;
         status                      = Status.initializing;
         cycleNumber                 = 0;
 
         awaitedEventList            = Collections.synchronizedSet(new HashSet<Fireable>());
         awaitedSimEventList         = Collections.synchronizedSet(new HashSet<Fireable>());
         firedEventList              = new HashSet<Fireable>();
 
         readyToShutdown             = false;
         emergencyStopRequested      = false;
         emergencyStopActive         = false;
         emergencyStopIsToBeThrown   = false;
         emergencyStopCausedBy       = null;
         
         synchronizedTasks           = Collections.synchronizedList(new ArrayList<Runnable>());
         
         startCycle                  = new Semaphore(1);
         cycleEnded                  = new Semaphore(1);
         
         startCycling                = new Synchronisation();
         stopCycling                 = new Synchronisation();
         shutdownRequest             = new Synchronisation();
         
         running                     = false;
                 
         activeEventsLock            = new CountingLock();
         awaitedEventOfLastModule    = null;
         
         moduleList                  = new ArrayList<AbstractModule>(20);
         traceQueue                  = null;
         
         cycleHistogramm             = null;
         systemHistogramm            = null;
         modulesHistogramm           = null;
         processedModule             = null;
         
         exitCode                    = 0;
         
         incrementCounter            = 0;
         decrementCounter            = 0;
         
         
         try{
             propCycleTime                  = new LongProperty(this,"CycleTime",DEFAULTCYCLETIME,"[ns]",true);
             propCycleTimeoutTime           = new LongProperty(this,"CycleTimeoutTime",DEFAULTCYCLETIMEOUTTIME,"[ns]",true);
             propCycleMode                  = new StringProperty(this,"CycleMode",CycleMode.FreeRunning.toString(),"[OneCycle | Bound | LazyBound | FreeRunning]",true);
             propRunningInsideAnIde         = new BooleanProperty(this,"RunningInsideAnIde",false,"will pop up a small window to close the application",true);
             propRunningInjUnitTest         = new BooleanProperty(this,"RunningInjUnitTest",false,"helpful, if jPac is run in a jUnit test",true);
             propEnableTrace                = new BooleanProperty(this,"EnableTrace",false,"enables tracing of the module activity",true);
             propStoreConfigOnShutdown      = new BooleanProperty(this,"StoreConfigOnShutdown",true,"if set, the configuration is stored on shutdown",true);
             propTraceTimeMinutes           = new IntProperty(this,"TraceTimeMinutes",0,"used to estimate the length of the trace buffer [min]",true);
             propPauseOnBreakPoint          = new BooleanProperty(this,"pauseOnBreakPoint", false, "cycle is paused, until all modules enter waiting state", true);
             propRemoteSignalsEnabled       = new BooleanProperty(this,"RemoteSignalsEnabled", false, "enable connections to/from remote JPac instances", true);
             propRemoteSignalPort           = new IntProperty(this,"RemoteSignalPort",10002,"server port for remote signal access",true);
             propStoreHistogrammsOnShutdown = new BooleanProperty(this,"storeHistogrammsOnShutdown",false,"enables storing of histogramm data on shutdown", true);
             propHistogrammFile             = new StringProperty(this,"HistogrammFile","./data/histogramm.csv","file in which the histogramms are stored", true);
             
             instanceIdentifier         = InetAddress.getLocalHost().getHostName() + ":" + propRemoteSignalPort.get();
             cycleTime                  = propCycleTime.get();
             cycleTimeoutTime           = propCycleTimeoutTime.get();
             cycleMode                  = CycleMode.valueOf(propCycleMode.get());
             runningInsideAnIde         = propRunningInsideAnIde.get();
             runningInjUnitTest         = propRunningInjUnitTest.get();
             enableTrace                = propEnableTrace.get();
             storeConfigOnShutdown      = propStoreConfigOnShutdown.get();
             traceTimeMinutes           = propTraceTimeMinutes.get();
             pauseOnBreakPoint          = propPauseOnBreakPoint.get();
             remoteSignalsEnabled       = propRemoteSignalsEnabled.get();
             remoteSignalPort           = propRemoteSignalPort.get();
             storeHistogrammsOnShutdown = propStoreHistogrammsOnShutdown.get();
             histogrammFile             = propHistogrammFile.get();
         }
         catch(UnknownHostException ex){
             Log.error("Error: ", ex);
             //properties cannot be initialized
             //kill application
             System.exit(99);            
         }
         catch(ConfigurationException ex){
             Log.error("Error: ", ex);
             //properties cannot be initialized
             //kill application
             System.exit(99);
         }
                 
         //install a shutdown hook to handle application shutdowns
         Runtime.getRuntime().addShutdownHook(new ShutdownHook());
         setPriority(MAX_PRIORITY);
         //start instance of the automationController
         start();
     }
 
     public static JPac getInstance(){
         if (instance == null) {
             instance = new JPac();
         }
         return instance;
     }
 
     @Override
     public void run(){
         boolean done                 = false;
         long    systemLoadStartTime  = 0L; //used to compute the system histogramm
         long    modulesLoadStartTime = 0L; //used to compute the modules histogramm
         
         if (Log.isInfoEnabled()) Log.info("STARTING");
         try{
             try{
                 //wait, until start up is requested
                 setStatus(Status.ready);
                 waitForStartUpSignal();
                 if (stopBeforeStartup){
                     //termination requested before first cycle (see ShutdownHook)
                     //shut down immediately
                     if (Log.isInfoEnabled()) Log.info("SHUTDOWN COMPLETE");
                     readyToShutdown = true;// inform the shutdown hook that we are done
                     return;
                 }
                 prepareTrace();
                 prepareHistogramms();
                 prepareRemoteConnections();                
             }
             catch(Exception exc){
                 //if an error occured during preparation of the system, shutdown immediately
                 Log.error("Error: ", exc);
                 invokeShutdownByMySelf(EXITCODEINITIALIZATIONERROR);
             }
             catch(Error exc){
                 //if an error occured during preparation of the system, shutdown immediately
                 Log.error("Error: ", exc);
                 invokeShutdownByMySelf(EXITCODEINITIALIZATIONERROR);
             }
             
             if (Log.isInfoEnabled()) Log.info("running in " + cycleMode + " mode ...");
             setStatus(Status.running);
             running = true;
             if (getCycleMode() == CycleMode.OneCycle){
                 prepareOneCycleMode();
             }
             //initialize values for cycle time computation
             initializeCycle();
             do{//!done && running
                 try{
                     tracePoint = 1000;
                     if (getCycleMode() == CycleMode.OneCycle){
                         waitForStartCycleSignal();
                         //initialize values for cycle time computation
                         initializeCycle();
                     }                
                     systemLoadStartTime = System.nanoTime();
                     tracePoint = 1100;
                     //update cycle var's for this cycle
                     prepareCycle();
                     
                     //handle deferred tasks, which must be synchronized to the cycle:
                     //propagation of signals, connect/disconnect of signals etc.
                     tracePoint = 1200;
                     handleDeferredTasks();
                     //now fire events awaited by application modules
                     tracePoint = 1300;
                     handleFireables(getAwaitedEventList());
                     
                     //acquire system histogramm information
                     modulesLoadStartTime = System.nanoTime();
                     systemHistogramm.update(modulesLoadStartTime - systemLoadStartTime);
                     //invoke cyclic task for every active module
                     tracePoint = 1400;
                     handleCyclicTasks();                    
                     //now start up application modules which have been awakenend before by fired process events
                     tracePoint = 1450;
                     handleAwakenedModules();
                     
                     //acquire modules histogramm information
                     modulesHistogramm.update(System.nanoTime() - modulesLoadStartTime);
                     
                     //handle emergency stop requests occured in current cycle
                     emergencyStopIsToBeThrown = false; //true only for one cycle
                     if (emergencyStopRequested){
                         emergencyStopRequested    = false;
                         //throw EmergencyStopException an all awaiting modules in next cycle
                         emergencyStopIsToBeThrown = true;
                     }
 
                     //acquire overall load histogramm information
                     cycleHistogramm.update(System.nanoTime() - systemLoadStartTime);
 
                     tracePoint = 1500;
                     long remainingCycleTime = expectedCycleEndTime - System.nanoTime();
                     acquireStatistics(remainingCycleTime);
                     if(getCycleMode() != CycleMode.FreeRunning){
                         //if not in FreeRunning  mode synchronize to the end of the cycle
                         //now, wait for the end of the cycle
                         wait4EndOfCycle(remainingCycleTime);
                     }
                 }
                 catch (Exception ex){
                     Log.error("Error",ex);
                     invokeShutdownByMySelf(EXITCODEINTERNALERROR);
                 }
                 catch (Error ex){
                     Log.error("Error",ex);
                     invokeShutdownByMySelf(EXITCODEINTERNALERROR);
                 }
                 //check, if the application is to be shutdown
                 if (isShutdownRequested()){
                     done = true;
                     //check, if cycling is to be stopped
                 } else if (isStopCyclingRequested()){
                     if (Log.isInfoEnabled()) Log.info("stop cycling ...");
                     //acknowledge request (see stopCycling())
                     acknowledgeStopRequest();
                     done = true;                
                 }
 
                 if (getCycleMode() == CycleMode.OneCycle){
                     signalEndOfCycle();
                 }            
                 //trace cycle statistics, if applicable
                 traceCycle();
             }
             while(!done);
             if (storeHistogrammsOnShutdown){
                storeHistogramms();
             }
             //shutdown all active modules
             shutdownAwaitingModules(getAwaitedEventList());
             //shutdown RemoteSignalConnection's
             closeRemoteConnections();
             //acknowledge request
             acknowledgeShutdownRequest();
             //new state is halted
             setStatus(Status.halted);
             logStatistics();
             if(storeConfigOnShutdown){
                 try{
                     if (Log.isInfoEnabled()) Log.info("saving the configuration ...");
                     Configuration.getInstance().save();
                     if (Log.isInfoEnabled()) Log.info("... saving of the configuration done");
                 }
                 catch(ConfigurationException exc){
                     Log.error("Error: while saving the configuration",exc);
                 }
             }
             if (Log.isInfoEnabled()) Log.info("SHUTDOWN COMPLETE");
             readyToShutdown = true;// inform the shutdown hook that we are done
             try{sleep(MAXSHUTDOWNTIME);} catch (InterruptedException ex){}
             //jUnit test need special handling
             if (!runningInjUnitTest){
                 System.exit(exitCode);
             }
         }
         catch(Exception exc){
             Log.error("Error: ", exc);
         }
         catch(Error exc){
             Log.error("Error: ", exc);
         }
     };
 
     private void handleFireables(Set<Fireable> fireableList) throws SomeEventsNotProcessedException, InconsistencyException{
         boolean fired = false;
         //check, if some of the currently registered Fireables can be fired in this cycle
         for (Fireable f: fireableList) {
             try{//the fireable is fired, if
                 //it is fired by its own,
                 //or if it is a ProcessEvent and an emergency stop is pending and the ProcessEvent is not awaited by a module, which threw
                 //an emergency stop exception during the last cycle,
                 //or if it is a ProcessEvent and it timed out during this cycle
                 boolean fFired    = f.evaluateFiredCondition();
                 boolean fTimedOut = (f instanceof ProcessEvent) && ((ProcessEvent)f).evaluateTimedOutCondition();
                 fired = fFired ||
                         (f instanceof ProcessEvent && (emergencyStopIsToBeThrown && !((ProcessEvent)f).getObservingModule().isRequestingEmergencyStop()) ||
                                                        fTimedOut                                                                                       );
             }
             catch(ProcessException exc){
                 //the fireable threw a process exception
                 //let the observing module handle this
                 fired = true;
             }
             if (fired){
                 //add Fireable to the list of fired events
                 if (f instanceof ProcessEvent && emergencyStopIsToBeThrown){
                     //if an emergency stop request is pending,
                     //let the awaiting module know about it
                     ((ProcessEvent)f).setEmergencyStopOccured(true);
                     ((ProcessEvent)f).setEmergencyStopCause(emergencyStopCausedBy.getMessage());                        
                 }
                 getFiredEventList().add(f);
             }
             if (f instanceof ProcessEvent && ((ProcessEvent)f).getObservingModule().isRequestingEmergencyStop()){
                 //emergency stop request has been recognized above. Reset it instantly
                ((ProcessEvent)f).getObservingModule().setRequestingEmergencyStop(false);                                                 
             }
         }
         //remove fireables from awaited event list
         for (Fireable f: getFiredEventList()) {
              fireableList.remove(f);
         }        
     }
     
     private void handleCyclicTasks(){
         try{
             for (AbstractModule module: moduleList){
                 //invoke inEveryCycleDo() for all active modules
                 processedModule = module;
                 module.invokeInEveryCycleDo();
                 processedModule = null;
             }
         }
         finally{
             processedModule = null;
         }
     }
     
     private void handleAwakenedModules() throws SomeEventsNotProcessedException, InconsistencyException{
         try{
             //awaken associated modules
             for (Fireable f: getFiredEventList()) {
                 tracePoint = 1501;
                 f.notifyObservingModule();
             }
             //wait until all fired events are processed by the associated modules
             switch(getCycleMode()){
                 case OneCycle:
                      //wait until all modules have completed their tasks without time limit
                      activeEventsLock.waitForUnlock();
                      break;
                 case Bound:
                      //wait until all modules have completed their tasks but not beyond the end of the cycle
                      activeEventsLock.waitForUnlock(expectedCycleEndTime - System.nanoTime());
                      break;
                 case LazyBound:
                      //wait until all modules have completed their tasks but not beyond the end of the cycle
                      activeEventsLock.waitForUnlock(expectedCycleEndTime - System.nanoTime());
                      if (activeEventsLock.getCount() > 0){
                        if (atLeastOneHangingModuleFound()){
                             //if some modules are still running, give them an additional period of time
                             activeEventsLock.waitForUnlock(cycleTimeoutTime);
                             if (Log.isInfoEnabled()) Log.info("cycle time exceeded for " + (System.nanoTime()- expectedCycleEndTime) + ", cycle# " + getCycleNumber());
                         }
                         else{
                             Log.error("activeEventsLock problem encountered !!!");
                             Log.error("  activeEventsCount      : " + activeEventsLock.getCount());
                             Log.error("  max. activeEventsCount : " + activeEventsLock.getMaxCount());
                             Log.error("  incrementCounter       : " + incrementCounter);
                             Log.error("  decrementCounter       : " + decrementCounter);
                             for (Fireable f: getFiredEventList()){
                                 ProcessEvent pe       = (ProcessEvent)f; 
                                 AbstractModule module = pe.getObservingModule();
                                 Log.info("  module '" + module + "' invoked by " + f + " state " + module.getStatus() + " ProcessEvent tracepoint: " + pe.getTracePoint());                                                    
                             }
                             activeEventsLock.reset();
                         }
                      }
                      break;
                 case FreeRunning:
                      //wait until all modules have completed their tasks without time limit
                      activeEventsLock.waitForUnlock();
                      break;
             }
             if (activeEventsLock.getCount() > 0){
                 if (pauseOnBreakPoint){
                     //a module might have run on a break point
                     //wait until all modules have completed their tasks without time limit
                     if (Log.isInfoEnabled()) Log.info("jPac paused ...");
                     activeEventsLock.waitForUnlock();                    
                     if (Log.isInfoEnabled()) Log.info("jPac continued ...");
                 }
                 else{
                     //assert failure, if at least one fired event
                     //has failed to be properly handled during the last cycle
                     Log.error("at least one module hung up !!!!: ");
                     Log.error("  elapsed cycle time     : " + (System.nanoTime() - cycleStartTime ));
                     Log.error("  trace point            : " + tracePoint);
                     Log.error("  activeEventsCount      : " + activeEventsLock.getCount());
                     Log.error("  max. activeEventsCount : " + activeEventsLock.getMaxCount());
                     Log.error("  incrementCounter       : " + incrementCounter);
                     Log.error("  decrementCounter       : " + decrementCounter);
                     
                     for (Fireable f: getFiredEventList()){
                         AbstractModule module = ((ProcessEvent)f).getObservingModule();
                         if (module.getState() != Thread.State.WAITING){
                             Log.error("  module '" + module + "' invoked by " + f + " hung up in state " + module.getStatus());                        
                         }
                         else{
                             Log.info("  module '" + module + "' invoked by " + f + " state " + module.getStatus());                                                    
                         }
                     }
                    if (atLeastOneHangingModuleFound()){
                         throw new SomeEventsNotProcessedException(getFiredEventList());
                     }
                 }
             }
             else{
                 //all modules came to an end for this cycle
                 //wait until the last made its wait() call
                 synchronizeOnLastModule();
             }
         }
         finally{
             //clear list of fired ProcessEvents (simulation or productive) for next usage
             getFiredEventList().clear();
         }
 
     }
 
     private void shutdownAwaitingModules(Set<Fireable> fireableList) throws InconsistencyException{
         try{
             //invoke all waiting modules and let them handle their ShutdownException
             for (Fireable f: fireableList) {
                 if (f instanceof ProcessEvent){
                     //retrieve all pending process events
                     getFiredEventList().add(f);
                 }
             }
             if (Log.isInfoEnabled()) Log.info("shutting down modules ...");
             //awaken waiting modules with a ShutdownException
             //which is automatically thrown by means of the ProcessEvent,
             //if this.shutdownRequested = true
             for (Fireable f: getFiredEventList()) {
                 AbstractModule module = f.getObservingModule();
                 String moduleName = module.toString();
                 if (Log.isDebugEnabled()) Log.debug("   shutting down module " + moduleName + " ...");
                 ((ProcessEvent)f).setShutdownRequested(true);
                 f.notifyObservingModule();
                 boolean moduleEnded = false;
                 do
                   try{
                       module.join(MAXSHUTDOWNTIME);
                       moduleEnded = true;
                   }
                   catch(InterruptedException exc){}
                 while(!moduleEnded);
                 if (module.getState() == Thread.State.TERMINATED){
                     if (Log.isDebugEnabled()) Log.debug("   module " + moduleName + " shutdown succeeded");
                 }
                 else{
                     Log.error("   !!!! failed to shutdown module " + moduleName + ". It's current state is " + module.getStatus());                    
                 }
             }
             if (Log.isInfoEnabled()) Log.info("... shutting down modules done");
         }
         finally{
             //clear list of fired ProcessEvents (simulation or productive) for next usage
             getFiredEventList().clear();
         }
     }
     /**
      * used to propagate signal states to connected signal instances
      */
     private void handleDeferredTasks() throws SignalAlreadyConnectedException, SignalInvalidException, ConfigurationException, RemoteSignalException {
         //if (Log.isDebugEnabled()) Log.debug("propagating signals ...");
         synchronized(synchronizedTasks){
             for(Runnable r: synchronizedTasks){
                 //run synchronized task
                 r.run();
             }            
             //everything done. Prepare list for next cycle
             synchronizedTasks.clear();
         }
         
         List<Signal> signals = SignalRegistry.getInstance().getSignals();
         synchronized(signals){
             for(Signal s: signals){
                 //handle requested (dis)connections of signals
                 s.handleConnections();
                 //propagate signal alterations
                 s.propagate();
             }
         }
         pushSignalsOverRemoteConnections();        
         //if (Log.isDebugEnabled()) Log.debug("... propagating signals done");
     }
     
     /*
      * used to invoke a task synchronized to the next jpac cycle
      * CAUTION: The given task may not call invokeLater() directly or indirectly by itself !
      */
     public void invokeLater(Runnable task){
         //TODO maintain 2 lists odd/even cycles to avoid alteration while iterating through the list
         synchronized(synchronizedTasks){
             synchronizedTasks.add(task);
         }
     }
 
     /*
      * @return the awaitedEventList
      */
     protected Set<Fireable> getAwaitedEventList() {
         return awaitedEventList;
     }
 
     /**
      * @return the awaitedSimEventList
      */
     protected Set<Fireable> getAwaitedSimEventList() {
         return awaitedSimEventList;
     }
 
     /**
      * @return the firedEventList
      */
     protected Set<Fireable> getFiredEventList() {
         return firedEventList;
     }
 
     /**
      * @return the cycleNumber
      */
     public long getCycleNumber() {
         return cycleNumber;
     }
 
 //    protected abstract void startModules() throws SignalAlreadyExistsException, SignalAlreadyAssignedException, IndexOutOfRangeException;
 
     /**
      * @return the cycleTime
      */
     public long getCycleTime() {
         return cycleTime;
     }
 
     public void shutdown() {
         shutdownRequest.request();
         instance = null; //discard actual instance
     }
 
     public void invokeShutdownByMySelf(int exitCode) {
         shutdownByMySelf = true;
         this.exitCode    = exitCode; 
     }
 
     public void startCycling() {
         if (Log.isInfoEnabled()) Log.info("startCycling requested");
         startCycling.request();
         if (Log.isInfoEnabled()) Log.info("startCycling() acknowledged");
     }
     
     public void stopCycling(){
         if (Log.isInfoEnabled()){Log.info("stopCycling requested");};
         stopCycling.request();
         if (Log.isInfoEnabled()){Log.info("stopCycling acknowledged");};
     }
     
     protected boolean isStopCyclingRequested(){
         return stopCycling.isRequested();
     }
 
     protected void acknowledgeStopRequest(){
         stopCycling.acknowledge();
     }
     
     /**
      * @return the shutdownRequested
      */
     public boolean isShutdownRequested() {
         return shutdownRequest.isRequested() || shutdownByMySelf;
     }
 
     protected void acknowledgeShutdownRequest(){
         if (shutdownRequest.isRequested()){
             shutdownRequest.acknowledge();
         }
         shutdownByMySelf = false;
     }
 
     /**
      * @return the emergencyStopRequested
      */
     public boolean isEmergencyStopActive() {
         return emergencyStopActive;
     }
 
     public void acknowledgeEmergencyStop(){
         emergencyStopActive    = false;
         emergencyStopCausedBy  = null;
     }
 
     /**
      * @param emergencyStopRequested the emergencyStopRequested to set
      */
     public void requestEmergencyStop(EmergencyStopException causedBy) {
         //set emergencyStopRequested. Will be reset by the automation controller after notification of all modules
         if (!this.emergencyStopActive){
             if (Log.isDebugEnabled()){Log.debug("EMERGENCY STOP REQUESTESD requestEmergencyStop: " + emergencyStopRequested);};//TODO
             //set request only once per emergency stop case
             this.emergencyStopRequested = true;
             this.emergencyStopActive    = true;
             this.emergencyStopCausedBy  = causedBy;
         }
     }
 
     /**
      * @return the emergencyStopCause
      */
     public String getEmergencyStopCause() {
         return emergencyStopCausedBy.getMessage();
     }
 
     protected void incrementAwakenedModulesCount() {
         incrementCounter++;// debug
         activeEventsLock.increment();
     }    
 
     protected void decrementAwakenedModulesCount(ProcessEvent awaitedEvent) throws InconsistencyException {
         decrementCounter++;// debug
         if (activeEventsLock.decrement()){
             //if last module went to sleep, store its awaited event
             awaitedEventOfLastModule = awaitedEvent;
         }
     }    
     
     protected void indicateCheckBack(ProcessEvent awaitedEvent) throws InconsistencyException{
         tracePoint = 100;
         if (enableTrace){
             AbstractModule module = awaitedEvent.getObservingModule();
             getTraceQueue().putTrace(cycleNumber, module.getModuleIndex(), module.getWakeUpNanoTime(), module.getSleepNanoTime());
         }
         tracePoint = 101;
         decrementAwakenedModulesCount(awaitedEvent);
         tracePoint = 102;
     }
 
     protected void synchronizeOnLastModule() throws InconsistencyException {
        if (awaitedEventOfLastModule != null){
            synchronized(awaitedEventOfLastModule){
                //do nothing meaningful.
                //Actually wait, until the module leaves the monitor 
                //on its awaited process event by calling awaitedEvent.wait()
                //Acquire it and leave it instantly
                //Note: see synchronized(this){} block inside ProcessEvent.awaitImpl() 
                long dummy = awaitedEventOfLastModule.getCycleNumber();
            }
        }
     }    
     
     protected void prepareOneCycleMode(){
         startCycle.acquireUninterruptibly();        
     }
     
     protected void waitForStartUpSignal(){
         if (Log.isInfoEnabled()) Log.info("awaiting start up signal ...");
         startCycling.waitForRequest();
         startCycling.acknowledge();
     }
 
     protected void waitForStartCycleSignal(){
         startCycle.acquireUninterruptibly();
         if (Log.isDebugEnabled()) Log.debug("-> start of cycle " + cycleNumber);        
     }
     
     protected void signalEndOfCycle(){
         //signal end of cycle
         cycleEnded.release();
         if (Log.isDebugEnabled()) Log.debug("<- end of cycle " + cycleNumber);        
         //aquire start semaphore in preparation of next cycle
         //must be released instantly
         startCycle.acquireUninterruptibly();
     }
     
     public void invokeNextCycle(){
         //aquire end of cycle semaphore in preparation for next cycle
         //must be released instantly
         cycleEnded.acquireUninterruptibly();
         //start cycle
         startCycle.release();
         if (Log.isDebugEnabled()) Log.debug("!!! next cycle invoked");        
         //wait, until cycle has finished
         cycleEnded.acquireUninterruptibly();
     }
 
     protected void prepareCycle(){
         //compute cycle time
         cycleStartTime       = System.nanoTime();//the cycle starts now
         expectedCycleEndTime = nextCycleStartTime + getCycleTime();
         if (expectedCycleEndTime < cycleStartTime){
             //if last cycle acceeded its end time more than one cycle time
             //sync cycle to current time
             expectedCycleEndTime = cycleStartTime + getCycleTime();
         }
         nextCycleStartTime = expectedCycleEndTime;
         cycleNumber++;
                 
         //initialize active event counter
         activeEventsLock.reset();  
         //reset awaited event of last module
         awaitedEventOfLastModule = null;
         
         //debugging info
         incrementCounter = 0;
         decrementCounter = 0;
     }
     
     protected void traceCycle(){
         if (enableTrace){
             getTraceQueue().putTrace(cycleNumber, OWNMODULEINDEX, cycleStartTime, System.nanoTime());
         }
     }
 
     protected void initializeCycle(){
         //set cycleEndTime, nextCycleStartTime to actual time, as if I am at the end of a previous cycle
         expectedCycleEndTime = System.nanoTime();
         nextCycleStartTime   = expectedCycleEndTime;
     }
 
     protected void wait4EndOfCycle(long remainingCycleTime){
         boolean done = remainingCycleTime <= 0;
         while(!done){
             try{    
                 Thread.sleep(remainingCycleTime / 1000000l, (int)(remainingCycleTime % 1000000));
                 done = true;
             }
             catch(InterruptedException exc){};
         }
     }
     
     protected void acquireStatistics(long remainingCycleTime){
         if(cycleNumber > 1){//TODO: (SN) vorlaeufig gepatched wg. Zyklus 1
             //acquire some statistics concerning the cycle time
             if (remainingCycleTime > getCycleTime()){
                 remainingCycleTime = getCycleTime();
             }
             else if (remainingCycleTime < 0){
                 remainingCycleTime = 0;
             }
             if (minRemainingCycleTime > remainingCycleTime)
                 minRemainingCycleTime = remainingCycleTime;
             else if (maxRemainingCycleTime < remainingCycleTime){
                 maxRemainingCycleTime = remainingCycleTime;
             }
         }
     }
     
     protected void logStatistics(){
         if (minRemainingCycleTime < 0){
             minRemainingCycleTime = 0;
         }
         if (maxRemainingCycleTime > getCycleTime()){
             maxRemainingCycleTime = getCycleTime();
         }
         
         int percentageMinRemainingCycleTime = (int)(100L * minRemainingCycleTime/getCycleTime());
         int percentageMaxRemainingCycleTime = (int)(100L * maxRemainingCycleTime/getCycleTime());
         
         if (Log.isInfoEnabled()){
             Log.info("cycle mode               : " + getCycleMode());
             Log.info("cycle time               : " + getCycleTime() + " ns");
             if (getCycleMode() != CycleMode.FreeRunning){
                 Log.info("min remaining cycle time : " + minRemainingCycleTime + " ns (" + percentageMinRemainingCycleTime + "%)");
                 Log.info("max remaining cycle time : " + maxRemainingCycleTime + " ns (" + percentageMaxRemainingCycleTime + "%)");
             }
         }
     }
     
     protected void storeHistogramms(){
         File         file  = new File(histogrammFile);
 
         try{
             
             if (Log.isInfoEnabled()) Log.info("storing histogramm informationen to " + histogrammFile);
             PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)), true);
             out.println("Cycle overall;" + cycleHistogramm.toCSV());
             out.println("System;" + systemHistogramm.toCSV());
             out.println("Modules;" + modulesHistogramm.toCSV());
             for (Fireable f: getAwaitedEventList()){
                 AbstractModule module = f.getObservingModule();
                 StringBuffer sb = new StringBuffer();
                 sb.append(module.getQualifiedName()).append(';').append(module.getHistogramm().toCSV());
                 out.println(sb);
             }
             out.close();
         }
         catch(IOException exc){
            Log.error("Error: ", exc); 
         }
     }
             
     protected void setStatus(Status status){
         this.status = status;
     }
 
     public Status getStatus(){
         return this.status;
     }
     
     protected int register(AbstractModule module){
         moduleList.add(module);
         return moduleList.size() - 1;
     }
     
     protected void prepareTrace() throws InvalidPropertyException{
         if (enableTrace){
            if (traceTimeMinutes <= 0){
                throw new InvalidPropertyException("traceTimeMinutes must be a positive integer:" + traceTimeMinutes);
            }
            if (Log.isInfoEnabled()) Log.info("tracing enabled");
            //determine estimated number of entries in relation to the given trace interval
            //assuming that one sample (ModuleTrace) will be generated per cycle
            int numberOfEntries = (int)(traceTimeMinutes * 60000000000L / cycleTime);
            traceQueue = new TraceQueue(numberOfEntries);
         }
     }
     
     protected void prepareRemoteConnections() throws ConfigurationException, RemoteException, RemoteSignalException{
         if (remoteSignalsEnabled){
             //start serving incoming remote signal requests
             RemoteSignalServer.start(remoteSignalPort);
             //instantiate outgoing remote signals
             ConcurrentHashMap<String, RemoteSignalConnection> remoteHosts = RemoteSignalRegistry.getInstance().getRemoteHosts();
             for (Entry<String, RemoteSignalConnection> entry: remoteHosts.entrySet()){
                 entry.getValue().open();
             }
         }
     }
     
     protected void closeRemoteConnections(){
         final long CLOSECONNECTIONTIMEOUT = 3000000000L; // 3 sec
         try{
             if (remoteSignalsEnabled){
                 if (Log.isInfoEnabled()) Log.info("closing remote connections ...");
                 //invoke closure of all open remote connections
                 ConcurrentHashMap<String, RemoteSignalConnection> remoteHosts = RemoteSignalRegistry.getInstance().getRemoteHosts();
                 for (Entry<String, RemoteSignalConnection> entry: remoteHosts.entrySet()){
                     entry.getValue().close();
                 }
                 //wait for all connections to close
                 for (Entry<String, RemoteSignalConnection> entry: remoteHosts.entrySet()){
                     long timeoutTime = System.nanoTime() + CLOSECONNECTIONTIMEOUT;
                     while(!entry.getValue().isClosed() && System.nanoTime() < timeoutTime);
                     if (!entry.getValue().isClosed()){
                         Log.error("   failed to close remote connection to " + entry.getValue().getRemoteJPacInstance());
                     }
                 }                    
                 RemoteSignalRegistry.getInstance().stopWatchdog();
                 if (Log.isInfoEnabled()) Log.info("... closing of remote connections done");
             }
         }
         catch(Exception exc){
             Log.error("Error:", exc);
         }
         catch(Error exc){
             Log.error("Error:", exc);
         }
     }
     
     protected void pushSignalsOverRemoteConnections() throws ConfigurationException, RemoteSignalException{
         if (remoteSignalsEnabled){
             ConcurrentHashMap<String, RemoteSignalConnection> remoteHosts = RemoteSignalRegistry.getInstance().getRemoteHosts();
             //remoteHosts.entrySet().iterator()
             for (Entry<String, RemoteSignalConnection> entry: remoteHosts.entrySet()){
                 entry.getValue().pushSignals(cycleNumber);
             }
         }        
     }
     
     /**
      * @return the traceQueue
      */
     public TraceQueue getTraceQueue() {
         return traceQueue;
     }
     
     protected void prepareHistogramms(){
         cycleHistogramm   = new Histogramm(cycleTime);
         systemHistogramm  = new Histogramm(cycleTime);
         modulesHistogramm = new Histogramm(cycleTime);
     }
     
     private boolean atLeastOneHangingModuleFound(){
         boolean found = false;
         for (Fireable f: getFiredEventList()){
             AbstractModule module = ((ProcessEvent)f).getObservingModule();
            if (module.getAwaitedProcessEvent() == null){
                 found = true;
             }
         }
         return found;
     }    
     
     
     public Histogramm getSystemHistogramm(){
         return systemHistogramm;
     }
     public Histogramm getModulesHistogramm(){
         return modulesHistogramm;
     }
 
     /**
      * 
      * @return the systems nanotime synchronized to the current cycle.
      */
     public long getCycleNanoTime(){
         return cycleStartTime;
     }
     
     public AbstractModule getModule(int i){
         return moduleList.get(i);
     }
 
     /**
      * @return the cycleMode
      */
     public CycleMode getCycleMode() {
         return cycleMode;
     }
     
     public String getInstanceIdentifier(){
         return instanceIdentifier;
     }
     
     public void setEmergencyStopExceptionCausedBy(EmergencyStopException causedBy){
         this.emergencyStopCausedBy = causedBy;
     }
  
     public EmergencyStopException getEmergencyStopExceptionCausedBy(){
         return this.emergencyStopCausedBy;
     }
     
     public AbstractModule getProcessedModule(){
         return this.processedModule;
     }
     
     public void stopBeforeStartUp(){
         if (Log.isInfoEnabled()) Log.info("aborting jPac before startup");
         this.stopBeforeStartup = true;
         startCycling.request();
     }
 }
