 package org.javabenchmark.jvmwatchdog;
 
 import com.sun.tools.attach.AgentInitializationException;
 import com.sun.tools.attach.AgentLoadException;
 import com.sun.tools.attach.AttachNotSupportedException;
 import com.sun.tools.attach.VirtualMachine;
 import java.io.File;
 import java.io.IOException;
 import java.lang.management.ManagementFactory;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.List;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 import javax.management.InstanceAlreadyExistsException;
 import javax.management.MBeanRegistrationException;
 import javax.management.MBeanServer;
 import javax.management.MalformedObjectNameException;
 import javax.management.NotCompliantMBeanException;
 import javax.management.ObjectName;
 import joptsimple.OptionParser;
 import joptsimple.OptionSet;
 import org.pmw.tinylog.Logger;
 
 /**
  * The main class.
  *
  * @author julien.paoletti@gmail.com
  */
 public class JvmWatchdog implements JvmWatchdogMXBean {
 
     public static final String AGENT_OPTION = "agent";
     public static final String PID_OPTION = "pid";
     public static final String PORT_OPTION = "port";
     public static final String METRICS_DIR_OPTION = "metrics-dir";
     public static final String MXBEAN_NAME = "org.javabenchmark.jvmwatchdog:type=JvmWatchdog";
     private File agentJarFile;
     private String[] pids;
     private int port = 10001;
     private String metricsDir = "metrics";
     private ServerSocket serverSocket;
     private ExecutorService listeningService;
     private ExecutorService heartbeatService;
     private AtomicBoolean mustListen = new AtomicBoolean(true);
     private CountDownLatch exitLatch = new CountDownLatch(1);
 
     /**
      * instantiates a new JVM watchdog.
      */
     public JvmWatchdog() {
         attachShutDownHook();
     }
 
     /**
      * The main method.
      *
      * @param args the command line arguments
      */
     public static void main(String[] args) {
 
         // instantiates a new watchdog
         JvmWatchdog watchDog = new JvmWatchdog();
 
         // registers the watchdog as MxBean
         MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
         try {
             ObjectName mxbeanName = new ObjectName(MXBEAN_NAME);
             mbs.registerMBean(watchDog, mxbeanName);
             Logger.debug("MxBean registred.");
         } catch (InstanceAlreadyExistsException ex) {
             Logger.error(ex);
         } catch (MBeanRegistrationException ex) {
             Logger.error(ex);
         } catch (NotCompliantMBeanException ex) {
             Logger.error(ex);
         } catch (MalformedObjectNameException ex) {
             Logger.error(ex);
         }
 
         // starts the watchdog and waits for agent
         if (watchDog.processJvmOptions(args)) {
             if (watchDog.listenToAgents()) {
                 watchDog.loadAgentIntoJvms();
                 watchDog.await();
             }
         }
 
         // the wait is over
         Logger.info("Exiting ..");
     }
 
     private static String getValueOfOption(OptionSet options, String option) {
         if (checkOptionAndArgument(options, option)) {
             return (String) options.valueOf(option);
         } else {
             return null;
         }
     }
 
     private static List<String> getValuesOfOption(OptionSet options, String option) {
         if (checkOptionAndArgument(options, option)) {
             return (List<String>) options.valuesOf(option);
         } else {
             return null;
         }
     }
 
     private static boolean checkOptionAndArgument(OptionSet options, String option) {
         if (!options.has(option)) {
             Logger.error("The --{0} option is missing", option);
             return false;
         }
         if (!options.hasArgument(option)) {
             Logger.error("The argument of the --{0} option is missing", option);
             return false;
         }
         return true;
     }
 
     /**
      * processes the JVM options provided in the command line when the watch dog
      * is started.
      *
      * @param args the args of the main method.
      * @return true if options are valid, false otherwise.
      */
     private boolean processJvmOptions(String[] args) {
 
         // options parsing
         OptionParser parser = new OptionParser();
         parser.accepts(AGENT_OPTION).withRequiredArg();
         parser.accepts(PID_OPTION).withRequiredArg();
         parser.accepts(PORT_OPTION).withOptionalArg();
         parser.accepts(METRICS_DIR_OPTION).withOptionalArg();
         OptionSet options = parser.parse(args);
 
         // options
         String agentPath = getValueOfOption(options, AGENT_OPTION);
         final String literalPids = getValueOfOption(options, PID_OPTION);
 
         // aborts in case of missing mandatory options
         if (agentPath == null || literalPids == null) {
             Logger.error("Aborting because of missing mandatory options");
             return false;
         }
 
         // checks pids
         pids = literalPids.split(",");
         if (pids.length == 0) {
             Logger.error("Aborting because of no pid provided");
             return false;
         }
 
         // checks agent JAR file
         agentJarFile = new File(agentPath);
         if (!agentJarFile.exists()) {
             Logger.error("Aborting because the agent JAR file does not exist: {1}", agentJarFile.getAbsolutePath());
             return false;
         }
 
         // checks port
         if (options.has(PORT_OPTION) && options.hasArgument(PORT_OPTION)) {
             port = (Integer) options.valueOf(PORT_OPTION);
         }
         
         // checks metrics directory
         if (options.has(METRICS_DIR_OPTION) && options.hasArgument(METRICS_DIR_OPTION)) {
             metricsDir = (String) options.valueOf(METRICS_DIR_OPTION);
         }
 
         Logger.info("---------------");
         Logger.info("Process Id(s)  : {0}", literalPids);
         Logger.info("Agent JAR file : {0}", agentJarFile.getAbsolutePath());
         Logger.info("Watchdog port  : {0}", port);
         Logger.info("Metrics dir.   : {0}", metricsDir);
         Logger.info("---------------");
         return true;
     }
 
     /**
      * adds a shutdown hook to shutdown on exit.
      */
     private void attachShutDownHook() {
         Runtime.getRuntime().addShutdownHook(new Thread() {
             @Override
             public void run() {
                 shutdown();
             }
         });
         Logger.debug("Shutdown hook added.");
     }
 
     private boolean listenToAgents() {
 
         // starts a server socket
         try {
             serverSocket = new ServerSocket(port);
         } catch (IOException e) {
             Logger.error("Could not listen on port {0} because of: {1}", port, e.getMessage());
             return false;
         }
 
         // dedicated thread for listening
         listeningService = Executors.newSingleThreadExecutor(new ThreadFactory() {
             @Override
             public Thread newThread(Runnable r) {
                 return new Thread(r, "WATCHDOG-LISTENING-THREAD");
             }
         });
 
         // dedicated threads for processing incoming heart beats
         heartbeatService = Executors.newFixedThreadPool(2, new ThreadFactory() {
             private int threadCounter = 0;
 
             @Override
             public Thread newThread(Runnable r) {
                 return new Thread(r, "HEART-BEAT-THREAD-" + threadCounter++);
             }
         });
 
         // starts listening
         listeningService.execute(new Runnable() {
             @Override
             public void run() {
 
                 Logger.info("Listening to agents ..");
                 Socket clientSocket = null;
 
                 // listening loop
                 while (mustListen.get()) {
                     try {
                         clientSocket = serverSocket.accept();
                     } catch (IOException e) {
 
                         // only logs error if the watchdog is listening
                         if (mustListen.get()) {
                             Logger.error("Failed to accept a connection: {0}", e.getMessage());
                         }
                     }
 
                     // processes heart beat
                     if (clientSocket != null) {
                         heartbeatService.execute(new HeartbeatProcessor(clientSocket, JvmWatchdog.this));
                     }
                 }
 
             }
         });
 
         return true;
     }
 
     /**
      * loads the watch dog agent into each JVM provided with the pid option.
      */
     private void loadAgentIntoJvms() {
 
         // loads the watchdog agent into each JVM
         for (int i = 0; i < pids.length; i++) {
 
             String pid = pids[i];
             try {
                 VirtualMachine vm = VirtualMachine.attach(pid);
                 StringBuilder agentOptions = new StringBuilder();
                 agentOptions.append(pid).append(",").append(port);
                 vm.loadAgent(agentJarFile.getAbsolutePath(), agentOptions.toString());
                Logger.info("An Agent was loaded into JVM with pid {0} ({1})", pid, vm.provider().name());
                 vm.detach();
 
             } catch (AttachNotSupportedException ex) {
                Logger.error("The JVM with pid {0} does not support dynamic attach !");
             } catch (IOException ex) {
                 Logger.error("An IO error occurs when attaching to JVM with pid {0} because: {1}", pid, ex.getMessage());
             } catch (AgentLoadException ex) {
                 Logger.error("The agent can not be loaded into the JVM with pid {0} because: {1}", pid, ex.getMessage());
             } catch (AgentInitializationException ex) {
                 Logger.error("The agent can not be initialized into the JVM with pid {0} because: {1}", pid, ex.getMessage());
             }
         }
     }
 
     /**
      * waits till the watch dog is shutdown or interrupted.
      */
     public void await() {
         try {
             exitLatch.await();
         } catch (InterruptedException ex) {
             Logger.warn("The watch dog was interrupted ..");
         }
     }
 
     /**
      * shutdowns listening and processing services, closes server socket and stops waiting.
      */
     @Override
     public void shutdown() {
 
         // stops listening service
         Logger.debug("Stopping the listening service ..");
         mustListen.set(false);
         stopService(listeningService);
         
         Logger.debug("Stopping heartbeat service ..");
         stopService(heartbeatService);
 
         // closes server socket
         Logger.debug("Closing server socket ..");
         if (serverSocket != null) {
             try {
                 serverSocket.close();
             } catch (IOException ex) {
                 Logger.warn("An error occurs when closing the server socket: {0}", ex.getMessage());
             }
         }
 
         // stops waiting
         Logger.debug("Stopping the wait ..");
         exitLatch.countDown();
     }
 
     private void stopService(ExecutorService service) {
         if (service != null) {
             service.shutdown();
             try {
                 // waits one second to let all the processes finish
                 service.awaitTermination(1, TimeUnit.SECONDS);
             } catch (InterruptedException ex) {
                 Logger.warn("Can not wait till the end of the service ..");
             }
         }
     }
 
     public String getMetricsDirectory() {
         return metricsDir;
     }
 }
