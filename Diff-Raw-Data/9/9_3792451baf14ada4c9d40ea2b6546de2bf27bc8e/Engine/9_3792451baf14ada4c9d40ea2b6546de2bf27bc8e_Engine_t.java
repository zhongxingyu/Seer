 package openske.engine;
 
 import java.io.File;
 import java.io.PrintWriter;
 
 import openske.drools.DroolsFacade;
 import openske.model.Infrastructure;
 import openske.nessus.NessusFileParser;
 
 
 public class Engine {
 
     protected static File currentWorkingDirectory;
     protected static Engine instance;
     protected DroolsFacade drools;
     protected boolean started;
     protected PrintWriter outputWriter;
     protected long startTime;
     protected EngineMode mode = EngineMode.NORMAL;
     protected Infrastructure infrastructure;
     protected File infrastructureFile;
     protected File nessusFile;
     protected NessusFileParser nessusParser;
 
     public Engine() {
         // Engine Initialization
         outputWriter = new PrintWriter(System.out, true);
         // Drools Initialization
         drools = new DroolsFacade();
         drools.setOutputWriter(outputWriter);
         Engine.currentWorkingDirectory = new File(".");
         // Mark engine as not started yet
         started = false;
         Engine.instance = this;
     }
     
     public static Engine getInstance() {
         if(Engine.instance == null) {
             Engine.instance = new Engine();
         }
         return Engine.instance;
     }
     
     public void log(String text, Object... args) {
         outputWriter.printf("[OPENSKE] " + text, args);
     }
 
     public void start() {
         if (this.isStarted()) {
             log("OpenSKE's engine is already started !");
             return;
         } else {
             this.startTime = System.currentTimeMillis();
             log("Starting OpenSKE engine in '%s' mode...", mode.toString().toLowerCase());
             try {
                 // Marking the engine as started from the beginning,
                 // since it's running in it's own thread
                 started = true;
                 
                 // Drools Initialization
                 drools.initialize();
                 log("Drools initialization completed at : %.2f seconds", this.getRunningTime());
                 
                 // Loading rules
                 drools.loadRules();
                 log("Loading rules into Drools completed at : %.2f seconds", this.getRunningTime());
                 
                 // Infrastructure loading (Nessus is optional)
                 infrastructure = new Infrastructure(infrastructureFile);
                 
                 if(nessusFile != null && nessusFile.exists()) {
                     nessusParser = new NessusFileParser(nessusFile, infrastructure);
                     nessusParser.parse();
                     log("Parsing Nessus output into Drools completed at : %.2f seconds", this.getRunningTime());
                 }
                 
                // Loading facts
                drools.loadFacts(infrastructure);
                log("Loading facts into Drools completed at : %.2f seconds", this.getRunningTime());
                
                 // Fire all activations
                 drools.fireRules();
                 log("Firing rules completed at : %.2f seconds", this.getRunningTime());
 		
                 log("Engine took in total : %.2f seconds !", this.getRunningTime());
             } catch (Throwable t) {
                 t.printStackTrace(outputWriter);
                 this.stop();
             }
         }
     }
     
     public void stop() {
         if (this.isStarted()) {
             log("Stopping OpenSKE engine...");
             drools.cleanup();
             started = false;
         }
     }
 
     public boolean isStarted() {
         return started;
     }
 
     public PrintWriter getOutputWriter() {
         return outputWriter;
     }
     
     public static void print(String text, Object... args) {
         Engine.getInstance().getOutputWriter().printf(text, args);
     }
 
     public void setOutputWriter(PrintWriter outputWriter) {
         this.outputWriter = outputWriter;
         // Change Drools output writer as well
         drools.setOutputWriter(outputWriter);
     }
 
     public float getRunningTime() {
         if (this.isStarted()) {
             return (float) ((System.currentTimeMillis() - this.startTime) / 1000.0);
         } else {
             return 0L;
         }
     }
 
     public static File currentWorkingDirectory() {
         return currentWorkingDirectory;
     }
 
     public EngineMode getMode() {
         return mode;
     }
 
     public void setMode(EngineMode mode) {
         this.mode = mode;
     }
 
     public void setInfrastructureFile(File infrastructureFile) {
         this.infrastructureFile = infrastructureFile;
     }
 
     public void setNessusFile(File nessusFile) {
         this.nessusFile = nessusFile;
     }
     
     public String status() {
         if(!this.isStarted()) {
             return "The OpenSKE engine is not running.";
         }
         
         StringBuffer sb = new StringBuffer();
         
         sb.append("OpenSKE Status:\n\n");
         
         sb.append(infrastructure.statistics());
         
         return sb.toString();
     }
 }
