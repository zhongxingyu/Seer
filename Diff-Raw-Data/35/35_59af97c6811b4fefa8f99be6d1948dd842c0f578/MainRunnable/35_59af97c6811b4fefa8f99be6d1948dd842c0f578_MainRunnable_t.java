 package rtsjcomponents.example2;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.realtime.MemoryArea;
 import javax.realtime.MemoryParameters;
 import javax.realtime.PriorityParameters;
 import javax.realtime.PriorityScheduler;
 import javax.realtime.RealtimeThread;
 import javax.realtime.RelativeTime;
 
 import rtsjcomponents.active.ActiveComponentFacade;
 import rtsjcomponents.example2.generated.MyPCFacade;
 import rtsjcomponents.utils.Constants;
 
 /**
  * Example of main runnable for running active and passive components.
  * 
  * @author juancol
  */
 public class MainRunnable implements Runnable {
     
     public static final int NUM_OF_ACTIVE_COMPONENTS = 5;
     public static final int NUM_OF_PASSIVE_COMPONENTS = 5;
     public static final int NUM_OF_RUNNABLES_PER_PASSIVE_COMPONENTS = 10;
     public static final int NUM_OF_STATELESS_PASSIVE_COMPONENT_IMPLS = 10;
 
     public static final int UNIT_PERIOD = 250; // miliseconds
     public static final int ITER_MULTIPLIER = 1000;
     public static final int MEASUREMENTS = 100;
     public static final long AC_TIMES[][] = new long[NUM_OF_ACTIVE_COMPONENTS][];
     static {
         for (int i = 0; i < AC_TIMES.length; i++) {
             AC_TIMES[i] = new long[MEASUREMENTS];
         }
     }
     
     public static final MyPC[] myPCFacades = new MyPC[MainRunnable.NUM_OF_PASSIVE_COMPONENTS];
     
     public static final String RECORDS = "timeRecords-comp";
     public static final String CASE = "-case";
     public static final String RUN = "-run";
     public static final String TXT = ".txt";
     
     
     /**
      * Creates a set of active components
      * 
      * @param num number of active components to be created
      * @param facade active component facade
      */
     private ActiveComponentFacade[] createActiveComponents(final int num) {
         if (num <= 0) {
             System.out.println("num must be greater than zero.");
             System.exit(-1);
         }
 
 //       This is not supported by jRate
 //        int priority = 
 //            PriorityScheduler.getMaxPriority(RealtimeThread.currentRealtimeThread());
 
         PriorityScheduler ps = PriorityScheduler.instance();
         int priority = ps.getMaxPriority(RealtimeThread.currentRealtimeThread());
        //System.out.println("Max priority: " + priority);       
 
         ActiveComponentFacade[] facades = new ActiveComponentFacade[num];
 
         for (int i = 0; i < num; i++) {
             facades[i] = ActiveComponentFacade.getInstance();
 
             // TODO Set parameters of active components properly
             PriorityParameters priorityParams = 
                 new PriorityParameters(priority - i);
             
             RelativeTime start = new RelativeTime(Constants.A_SECOND, 0);
             
             long p = (i + 1) * UNIT_PERIOD; // in miliseconds
             RelativeTime period = new RelativeTime(p, 0);
             RelativeTime cost = new RelativeTime(p, 0);
             RelativeTime deadline = new RelativeTime(p, 0);
             MemoryParameters memoryParams = new MemoryParameters(MemoryParameters.NO_MAX, MemoryParameters.NO_MAX);
 
             facades[i].createPeriodicComponent(priorityParams, start, period,
                     cost, deadline, memoryParams, MyAC.class, i);
         }
 
         return facades;
     }
 
     /**
      * Creates a set of passive components
      * @param num number of active components to be created
      * @param facade active component facade
      */
     private MyPCFacade[] createPassiveComponents(final int num) {
         if (num <= 0) {
             System.out.println("num must be greater than zero.");
             System.exit(-1);
         }
 
         MyPCFacade[] facades = new MyPCFacade[num];
 
         for (int i = 0; i < num; i++) {
             facades[i] = MyPCFacade.getInstance();
             facades[i].createPassiveComponent(i);
             MainRunnable.myPCFacades[i] = facades[i];
         }
 
         return facades;
     }
 
     public void run() {
         MemoryArea area = RealtimeThread.getCurrentMemoryArea();
        System.out.println("Executing MainRunnable.run() in " + area);
 
         ActiveComponentFacade[] actFacades = null;
         MyPCFacade[] pasFacades = null;
 
         try {
             pasFacades = this.createPassiveComponents(NUM_OF_PASSIVE_COMPONENTS);
            System.out.println("Passive components created ...");
             
             actFacades = this.createActiveComponents(NUM_OF_ACTIVE_COMPONENTS);
            System.out.println("Active components created ...");
             
             RealtimeThread.sleep(2 * Constants.A_MINUTE);
            
  
             FileOutputStream os = new FileOutputStream("component-memscopes-hashcodes.txt");
             PrintWriter file = new PrintWriter(os);
             
             //file.println("Active components");
             for (int i = 0; i < actFacades.length; i++) {
                 //System.out.println("active facade #: " + i);
                 file.println("Active component id:" + i + ' ' 
                         + actFacades[i].getComponentScopeHashCode());
                 ActiveComponentFacade.freeInstance(actFacades[i]);
             }                
 
             //file.println("Passive components");
             for (int i = 0; i < pasFacades.length; i++) {
                 file.println("Passive component id:" + i + ' ' 
                         + pasFacades[i].getComponentScopeHashCode());
                 //System.out.println("passive facade #: " + i);
                 //System.out.println("Memory area of passive facade #: " + i + " is " + 
                 //    MemoryArea.getMemoryArea(pasFacades[i]));
                 MyPCFacade.freeInstance(pasFacades[i]);
             }
             
             file.close();
             
             // During this time we let the components terminate.
             // We had to do this because of a Timesys RTSJ-RI's bug.
             RealtimeThread.sleep(30 * Constants.A_SECOND);
             
             // Printing outpur files
             for (int i = 0; i < AC_TIMES.length; i++) {
            //    System.out.println("Writing file for comp#: " + i);
                this.logRecords(AC_TIMES[i], i, Example2.testcase, Example2.runId);
             }
             
         } catch (Exception e) {
             e.printStackTrace();
             System.exit(-1);
         }
     }
     
    private void logRecords(long times[], int compId, int testcase, int run) {
         try {
             FileOutputStream os = 
                 new FileOutputStream(RECORDS + compId + CASE + testcase + RUN + run + TXT);
             PrintWriter file = new PrintWriter(os);
 
             for (int i = 0; i < times.length; i++) {
                 file.println(i + ": " + times[i]);
             }
             file.close();
         } catch (IOException e) {
             System.out.println("Error writing to file: " + e);
         }
     }
 }
