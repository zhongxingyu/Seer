 /**
  * JavaExecutorFrameworkExample
  *
  * An example of using the Java Executor Framework for asynchronous processing.
  *
  * @author Ariel Gerardo Rios <mailto:ariel.gerardo.rios@gmail.com>
  *
  */
 
 package org.gabrielle.example;
 
 import java.io.IOException;
 
 import java.util.ArrayList;
 
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Future;
 
 import java.util.List;
 import java.util.UUID;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 
 import org.apache.commons.lang.exception.ExceptionUtils;
 
 import org.apache.log4j.Logger;
 
 import org.gabrielle.example.enums.Status;
 
 import org.gabrielle.example.tasks.TaskExample;
 import org.gabrielle.example.util.PIDFile;
 
 
 import org.javatuples.Pair;
 import org.javatuples.Tuple;
 
 /** 
  * A class containing the main method to execute the example for using the
  * Executor Framework.
  *
  * @author Ariel Gerardo Ríos <mailto:ariel.gerardo.rios@gmail.com>
  * @version 1.00    
 */
 public class JavaExecutorFrameworkExample extends Object { 
 
     // nthreads parameter
 
     public static final String PARAM_NAME_NTHREADS = "nthreads";
 
     public static final int TUPLE_INDEX_NTHREADS = 0;
 
     public static final int DEFAULT_NTHREADS = 1;
 
     // pidfile parameter
 
     public static final String PARAM_NAME_PIDFILE_PATH = "pidfile";
 
     public static final int TUPLE_INDEX_PIDFILE_PATH = 1;
 
     public static final String DEFAULT_PIDFILE_PATH = "/tmp/pid-example";
 
     // task constants
 
     public static final int DEFAULT_NTASKS = 1000;
 
     // Loggers
 
     private static final Logger logger = Logger.getLogger(
             JavaExecutorFrameworkExample.class);
 
     private static final Logger exceptionLogger = Logger.getLogger("exception");
 
     // Other variables
 
     private final UUID uuid;
 
     /** 
      * Generates the Options object to use for command line argument parsing.
      *
      * @return An Options object instance.
     */
     public static Options createOptions() {
 
         Options options = new Options();
         Option option;
                                                                                 
         OptionBuilder.withArgName(PARAM_NAME_NTHREADS);
         OptionBuilder.withDescription("Overwrites the configured value for " +
                 "the number of threads to create.");
         option = OptionBuilder.create(PARAM_NAME_NTHREADS);
         options.addOption(option);
                                                                                 
         OptionBuilder.withArgName(PARAM_NAME_PIDFILE_PATH);
         OptionBuilder.withDescription(String.format("Overwrites the default " +
                     "path of the PID file. Default: %s",
                     DEFAULT_PIDFILE_PATH));
         option = OptionBuilder.create(PARAM_NAME_PIDFILE_PATH);
         options.addOption(option);
 
         return options;
     }
 
     /** 
      * Parses the command line arguments using the Options object previously
      * initialized.
      *
      * @param args The command line arguments as String[].
      * @param options The Options object previously initialized to use for
      * parsing.
      * @return A Tuple object containig the parsed values or null instead.
      * @throws ParseException 
     */
     public static Tuple parseArguments(final String[] args, final Options
             options) throws ParseException { 
 
         int nthreads = 0;
         String pidfilePath = null;
 
         CommandLineParser parser = new GnuParser();
         CommandLine line = parser.parse(options, args);
 
         if (line.hasOption(PARAM_NAME_NTHREADS)) {
             nthreads = Integer.parseInt(line.getOptionValue(
                         PARAM_NAME_NTHREADS));
         }
         else {
             nthreads = DEFAULT_NTHREADS;
         }
                                                              
         if (line.hasOption(PARAM_NAME_PIDFILE_PATH)) {
             pidfilePath = line.getOptionValue(
                     PARAM_NAME_PIDFILE_PATH);
         }
         else {
             pidfilePath = DEFAULT_PIDFILE_PATH;
         }
 
         return new Pair<Integer, String>(nthreads, pidfilePath);
     }
 
     /** 
      * Constructor.
      *
      * @param uuid The unique id to use in this execution.
     */
     public JavaExecutorFrameworkExample(final UUID uuid) {
         this.uuid = uuid;
     }
 
     /** 
      * Process required tasks, with validated parameters.
      *
      * @param nthreads The number of threads to execute or null.
      * @return The execution status, as integer.
      * @throws IOException 
      * @throws ExecutionException 
      * @throws InterruptedException 
     */
    public int process(final Integer nthreads) throws IOException, InterruptedException, ExecutionException {
 
         logger.info(String.format("UUID=%s - Thread pool size: %d", this.uuid,
                     nthreads));
 
         ExecutorService executorThreadPool = Executors.newFixedThreadPool(
                 nthreads);
 
         List<Future<UUID>> futures = new ArrayList<Future<UUID>>(nthreads);
 
         // Creating many tasks to be processed
         for (int i = 0; i < DEFAULT_NTASKS; i++) {
             // Adding the futures for later result
             futures.add(
                     // enqueuing tasks as they are created into the internal
                     // queue
                     executorThreadPool.submit(new TaskExample(this.uuid))
             );
         }
                     
         // Getting the tasks processing results
         UUID task_uuid;
         for(Future<UUID> future : futures){
             task_uuid = future.get();
             logger.info(String.format("UUID=%s - Tasks process result: %s",
                         this.uuid, task_uuid));
         }
 
         // Asking to all threads to shut down.
         executorThreadPool.shutdown();
 
         // TODO Here goes some general processing with all task results
 
         // Returning a general processing result
         return Status.SUCCESSFUL.getId();
     }
 
     /** 
      * Contains the logic to execute the application.
      *
      * @param args The program arguments, as a String array.
      * @return 0 for successful execution, 1 instead.
     */
    public static int main (String [] args) {
 
         Options options = JavaExecutorFrameworkExample.createOptions();
 
         UUID uuid = UUID.randomUUID();
 
         JavaExecutorFrameworkExample jefe = new JavaExecutorFrameworkExample(
                 uuid);
 
         PIDFile pidfile = null;
         
         try {
             // Parsing program parameters 
             Tuple parsedArgs = JavaExecutorFrameworkExample.parseArguments(args,
                     options);
 
             Integer nthreads = (Integer) parsedArgs.getValue(
                     TUPLE_INDEX_NTHREADS);
 
             String pidfilePath = (String) parsedArgs.getValue(
                     TUPLE_INDEX_PIDFILE_PATH);
             pidfile = new PIDFile(pidfilePath);
             pidfile.lock();
             logger.info(String.format("UUID=%s - Locked PID file: %s", uuid,
                         pidfile));
 
             return jefe.process(nthreads);
 
         }
         catch (Exception e) {
             exceptionLogger.error(String.format("UUID=%s - There was an " +
                         "exception in the program: %s", uuid, ExceptionUtils.
                         getStackTrace(e)));
         }
         finally{
             if (pidfile != null) {
                 try {
                     pidfile.unlock();
                     logger.info(String.format("UUID=%s - Released PID file: " +
                                 "%s", uuid, pidfile));
                 }
                 catch(Exception e) {
                     exceptionLogger.error(String.format("UUID=%s - There was " +
                                 "an exception releasing the PID file: %s", uuid,
                                 ExceptionUtils.getStackTrace(e)));
                 }
             }
         }
 
         return Status.FAILED.getId();
     }
 }
 
 
 // vim:ft=java:
