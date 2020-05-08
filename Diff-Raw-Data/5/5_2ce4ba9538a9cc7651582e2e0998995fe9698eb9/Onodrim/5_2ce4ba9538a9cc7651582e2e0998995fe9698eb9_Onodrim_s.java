 /*
  * Copyright 2012 Luis Rodero-Merino.
  * 
  * This file is part of Onodrim.
  * 
  * Onodrim is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Onodrim is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Onodrim.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package org.onodrim;
 
 import java.io.File;
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.logging.StreamHandler;
 
 import org.onodrim.annotations.AnnotationProcessor;
 
 /**
  * Class that implements the 'facade' pattern, to easily access Onodrim's main functionality.
  * 
  * @author Luis Rodero-Merino
  * @since 1.0
  */
 public class Onodrim {
 
     public static final String PROJECT_NAME = "onodrim";
 
     /**
      * Used to associated {@link Job} instances to the threads that are running them. See methods
      * {@link #getCurrentThreadJob()}, {@link #registerThreadJob(Job)} and {@link #unregisterThreadJob(Job)}.
      */
     private static InheritableThreadLocal<Job> threadJobMap = new InheritableThreadLocal<Job>();
 
     /**
      * This class cannot be instantiated.
      */
     private Onodrim() {}
     
     /**
      * This method only wraps a call to the {@link Configuration#buildConfigurations(File)} method.
      * @param jobsConfsFile File containing the definition of the configurations to build. It must be
      * written using the {@code .properties} files format ( @see http://en.wikipedia.org/wiki/.properties ).
      * @return The configurations generated, in a {@code List} of {@link Configuration} instances.
      * @throws ConfigurationException If some error is found in the definition.
      */
     public static List<Configuration> buildConfigurations(File jobsConfsFile) throws ConfigurationException {
     	return Configuration.buildConfigurations(jobsConfsFile);
     }
     
     /**
      * This method only wraps a call to the {@link Configuration#buildConfigurations(Properties)} method.
      * @param jobsConfsProps The definition of the configurations to build.
      * @return The configurations generated, in a {@code List} of {@link Configuration} instances.
      * @throws ConfigurationException If some error is found in the definition.
      */
     public static List<Configuration> buildConfigurations(Properties jobsConfsProps) throws ConfigurationException {
     	return Configuration.buildConfigurations(jobsConfsProps);
     }
 
     /**
      * Create an instance of {@link JobsSet} class. The {@link <a href="http://docs.oracle.com/javase/6/docs/api/java/util/Properties.html">Properties</a>}
      * instance passed as parameter is used to configure Onodrim itself and to generate the
      * {@link Configuration} instances that will configure each one of the {@link Job} instances
      * in the returned set.
      * 
      * This method just wraps a call to {@link JobsSet#JobsSet(Properties, JobEntryPoint)} method.
      *
      * @param confProperties Used to generate Onodrim and jobs configurations 
      * @param entryPoint Jobs entry point, i.e. instance that contains the functionality to be run.
      *                   It can be {@code null}, but then Onodrim will not be able to run automatically this set.
      * @return Instance of {@link JobsSet} created using the configuration in the {@code Properties} instance. 
      * @throws ConfigurationException Raised if some problem is found when reading and processing configuration
      * 
      * @see JobsSet#JobsSet(Properties, JobEntryPoint)
      */
     public static JobsSet buildJobsSet(Properties confProperties, JobEntryPoint entryPoint) throws ConfigurationException {
         return new JobsSet(confProperties, entryPoint);
     }
     
     /**
      * Similar to calling {@link #buildJobsSet(Properties, JobEntryPoint)} using {@code null} as the {@code JobEntryPoint} 
      * @param confProperties
      * @return A {@link JobsSet} instance containing one job per each configuration created from the {@code Properties} passed
      * as parameter.
      * @throws ConfigurationException
      */
     public static JobsSet buildJobsSet(Properties confProperties) throws ConfigurationException {
     	return buildJobsSet(confProperties, null);
     }
 
     /**
      * Create an instance of {@link JobsSet} class. The
      * {@link <a href="http://docs.oracle.com/javase/6/docs/api/java/io/File.html">File</a>}
      * instance passed as parameter is used to generate the
      * {@link <a href="http://docs.oracle.com/javase/6/docs/api/java/util/Properties.html">Properties</a>}
      * instance that will
      * be used to configure Onodrim itself and to generate the {@link Configuration} instances
      * that will configure each one of the {@link Job} instances in the returned set.
      * 
      * This method just wraps a call to {@link JobsSet#JobsSet(File, JobEntryPoint)} method.
      *
      * @param confFile This file will be read to generate the base configuration that will be used
      *                 to generate all jobs and Onodrim configuration. 
      * @param entryPoint Jobs entry point, i.e. instance that contains the functionality to be run.
      *                   It can be {@code null}, but then Onodrim will not be able to run automatically this set.
      * @return A {@link JobsSet} instance containing one job per each configuration created from the {@code Properties} passed
      * as parameter. 
      * @throws ConfigurationException Raised if some problem is found when reading and processing configuration
      * 
      * @see JobsSet#JobsSet(File, JobEntryPoint)
      */
     public static JobsSet buildJobsSet(File confFile, JobEntryPoint entryPoint) throws ConfigurationException {
         return new JobsSet(confFile, entryPoint);
     }
     
     /**
      * Similar to calling {@link #buildJobsSet(File, JobEntryPoint)} using {@code null} as the {@code JobEntryPoint}
      * @param confFile
      * @return A {@link JobsSet} instance containing one job per each configuration created from the {@code Properties} passed
      * as parameter. 
      * @throws ConfigurationException
      */
     public static JobsSet buildJobsSet(File confFile) throws ConfigurationException {
     	return buildJobsSet(confFile, null);
     }
 
     /**
      * Create the {@link JobsSet} instance corresponding to that configuration (see
      * {@link #buildJobsSet(Properties, JobEntryPoint)}) method) and immediately run the {@link Job} instances
      * in the set by calling to {@link JobsSet#runJobs()}.
      *   
      * @param confProperties Base configuration of Onodrim and the jobs to be run
      * @param entryPoint Jobs entry point, i.e. instance that contains the functionality to be run
      * @throws JobExecutionException Raised if some problem is found when executing {@link Job}s
      * @throws ConfigurationException Raised if some problem is found when reading and processing configuration
      */
     public static void runJobs(Properties confProperties, JobEntryPoint entryPoint) throws JobExecutionException, ConfigurationException {
         Onodrim.buildJobsSet(confProperties, entryPoint).runJobs();
     }
 
     /**
      * Create the {@link JobsSet} instance corresponding to tje configuration carried by the {@link File} passed
      * as paremeter (see {@link #buildJobsSet(File, JobEntryPoint)}) method) and immediately run the {@link Job}
      * instances in the set by calling to {@link JobsSet#runJobs()}.
      *   
      * @param confFile This file will be read to generate the base configuration that will be used
      *                 to generate all jobs and Onodrim configuration.Base configuration of Onodrim and the jobs to be run
      * @param entryPoint Jobs entry point, i.e. instance that contains the functionality to be run
      * @throws JobExecutionException Raised if some problem is found when executing {@link Job}s
      * @throws ConfigurationException Raised if some problem is found when reading and processing configuration
      */
     public static void runJobs(File confFile, JobEntryPoint entryPoint) throws JobExecutionException, ConfigurationException {
     	Onodrim.buildJobsSet(confFile, entryPoint).runJobs();
     }
 
     /**
      * Similar to {@link #runJobs(Properties, JobEntryPoint)}, but it also takes the {@link JobsExecutionWatcher}
      * passed as parameter when starting the execution to the {@link JobsSet} instance. In other words, it generates
      * the {@link JobsSet} instance (by {@link #buildJobsSet(Properties, JobEntryPoint)}) and calls the
      * {@link JobsSet#runJobs(JobsExecutionWatcher)} method on it.
      * 
      * @param confProperties Base configuration of Onodrim and the jobs to be run
      * @param entryPoint Jobs entry point, i.e. instance that contains the functionality to be run
      * @param watcher It will be notified of different events such as the start/finalization of each job
      * @throws JobExecutionException Raised if some problem is found when executing {@link Job}s
      * @throws ConfigurationException Raised if some problem is found when reading and processing configuration
      */
     public static void runJobs( Properties confProperties,
                                 JobEntryPoint entryPoint,
                                 JobsExecutionWatcher watcher) throws JobExecutionException, ConfigurationException {
         Onodrim.buildJobsSet(confProperties, entryPoint).runJobs(watcher);
     }
 
     /**
      * Similar to {@link #runJobs(File, JobEntryPoint)}, but it also takes the {@link JobsExecutionWatcher}
      * passed as parameter when starting the execution to the {@link JobsSet} instance. In other words, it generates
      * the {@link JobsSet} instance (by {@link #buildJobsSet(File, JobEntryPoint)}) and calls the
      * {@link JobsSet#runJobs(JobsExecutionWatcher)} method on it.
      * 
      * @param confFile This file will be read to generate the base configuration that will be used
      *                 to generate all jobs and Onodrim configuration.Base configuration of Onodrim and the jobs to be run
      * @param entryPoint Jobs entry point, i.e. instance that contains the functionality to be run
      * @param watcher It will be notified of different events such as the start/finalization of each job
      * @throws JobExecutionException Raised if some problem is found when executing {@link Job}s
      * @throws ConfigurationException Raised if some problem is found when reading and processing configuration
      */
     public static void runJobs( File confFile,
                                 JobEntryPoint entryPoint,
                                 JobsExecutionWatcher watcher) throws JobExecutionException, ConfigurationException {
         Onodrim.buildJobsSet(confFile, entryPoint).runJobs(watcher);
     }
 
     /**
      * Run the given {@link JobsSet} instance.
      * 
      * @param jobsSet Jobs to run.
      * @throws JobExecutionException Raised if some problem is found when executing {@link Job}s
      */
     public static void runJobs(JobsSet jobsSet) throws JobExecutionException {
         jobsSet.runJobs();
     }
 
     /**
      * Run the given {@link JobsSet} instance, notifying job events to the {@link JobsExecutionWatcher} instance.
      * 
      * @param jobsSet Jobs to run.
      * @param watcher It will be notified of different events such as the start/finalization of each job
      * @throws JobExecutionException Raised if some problem is found when executing {@link Job}s
      */
     public static void runJobs(JobsSet jobsSet, JobsExecutionWatcher watcher) throws JobExecutionException {
         jobsSet.runJobs(watcher);
     }
 
     /**
      * Utility method that reads the present {@link Job} configuration ('present job' refers to the one
      * being run by the calling thread) and stores it in the given object instance. The class object
      * must be annotated to set which fields correspond to which configuration parameters.
      * 
      * It wraps a call to {@code Onodrim.setConfByAnnotations(object, Onodrim.getPresentJob().getConfiguration());}
      * 
      * @param object instance where configuration will be stored
      * @throws ConfigurationException some error prevented to write the configuration on the object instance
      */
     public static void setConfByAnnotations(Object object) throws ConfigurationException {
         Onodrim.setConfByAnnotations(object, Onodrim.getCurrentThreadJob().getConfiguration());
     }
 
     /**
      * Utility method that stores the {@link Configuration} in the given object instance. The class object
      * must be annotated to set which fields correspond to which configuration parameters.
      * 
      * @param object instance where configuration will be stored
      * @param conf configuration 
      * @throws ConfigurationException some error prevented to write the configuration on the object instance
      */
     public static void setConfByAnnotations(Object object, Configuration conf) throws ConfigurationException {
         AnnotationProcessor.setConfiguration(object, conf);
     }
 
     /**
      * Onodrim uses internally a map (an instance of {@link InheritableThreadLocal}) to
      * associate threads with the {@link Job}s they are running (at any time one thread is running at most
      * one {@link Job}, and each {@link Job} is being run at most by one thread). This method
      * is used by Onodrim to associate the calling thread with the {@link Job} passed as parameter.
      * 
      * @param job {@link Job} to associate with the calling thread
      */
     protected static void registerThreadJob(Job job) {
         synchronized (threadJobMap) {
             threadJobMap.set(job);
         }
     }
 
     /**
      * Onodrim uses internally a map (an instance of {@link InheritableThreadLocal}) to
      * associate threads with the {@link Job}s they are running (at any time one thread is running at most
      * one {@link Job}, and each {@link Job} is being run at most by one thread). This method
      * is used to 'undo' that association between the calling thread and the {@link Job} passed as
      * parameter.
      * 
      * @param job
      */
     protected static void unregisterThreadJob(Job job) {
         synchronized (threadJobMap) {
             if(threadJobMap.get() == null) // Consistency check
                 throw new Error("Thread " + Thread.currentThread().getName() + " has no job associated");
             if(threadJobMap.get() != job) // Consistency check
                 throw new Error("Thread " + Thread.currentThread().getName() + " is associated to job " + threadJobMap.get().getIndex() 
                                 + ", but Onodrim thought it was associated with job " + job.getIndex());
             threadJobMap.remove();
         }
     }
 
     /**
      * Onodrim uses internally a map (an instance of {@link InheritableThreadLocal}) to
      * associate threads with the {@link Job}s they are running (at any time one thread is running at most
      * one {@link Job}, and each {@link Job} is being run at most by one thread). This method queries that
      * map to get which {@link Job} is associated with the calling thread, i.e., which {@link Job} this
      * thread is running.
      * 
      * @return {@link Job} associated with the calling thread.
      */
     public static Job getCurrentThreadJob() {
         synchronized (threadJobMap) {
             return threadJobMap.get();
         }
     }
     
     /**
      * Class that groups some utility methods to speedup Onodrim log setting up. Note Onodrim uses
      * default logging Java framework
      * ({@link <a href="http://docs.oracle.com/javase/6/docs/api/java/util/logging/package-frame.html"java.util.logging</a>}). 
      * You can set your own configuration and add your own handlers using the mechanisms provided by that
      * framework, the methods in this class just group some common functionality. If you use those mechanisms by your own,
      * then you don't need to use the methods defined here.
      * 
      * @author lrodero
      */
     public static class Log {
 
         /**
          * Logger associated to Onodrim root package, to capture all log messages sent by all classes
          * in Onodrim.
          */
         private static Logger onodrimRootLogger = Logger.getLogger(Onodrim.class.getPackage().getName());
         /**
          * Uility logger that sends message to {@code System.out} and performs automatic flushing.
          */
         private static StreamHandler streamHandler = Util.createStreamHandlerWithAutomaticFlushing(System.out, null);
 
         /**
          * Utility method to set a quick logging configuration for Onodrim classes.
          * It wraps a call to {@code Onodrim.configDefaultLogger(false);}
          */
         public static void enableOnodrimLogs() {
             enableOnodrimLogs(false);
         }
 
         /**
          * Utility method to set a quick logging configuration for Onodrim classes. Note Onodrim uses
          * default logging Java framework
          * ({@link <a href="http://docs.oracle.com/javase/6/docs/api/java/util/logging/package-frame.html"java.util.logging</a>}).
          * The logger is configured so all log messages are processed.
          * 
          * @param forwardLogsToParentLoggers flag to set whether log messages should be forwarded to parent
          * loggers or not. This is a convenient method to speed up Onodrim setup for quick tests.
          */
         public static void enableOnodrimLogs(boolean forwardLogsToParentLoggers) {
             streamHandler.setLevel(Level.ALL);
             onodrimRootLogger.setLevel(Level.ALL);
             onodrimRootLogger.setUseParentHandlers(forwardLogsToParentLoggers);
         }
         
         /**
          * Add a utility console handler that prints logs in {@code System.out}, with auto-flushing enabled.
          * Remember also to call to {@link #enableOnodrimLogs(boolean)}. This is a convenient method to speed
          * up Onodrim setup for quick tests. 
          */
         public static void enableUtilConsoleLogger() {
             onodrimRootLogger.addHandler(streamHandler);
         }
         
         /**
          * Utility method that 'switches off' logging from Onodrim. Note Onodrim uses
          * default logging Java framework ({@link <a href="http://docs.oracle.com/javase/6/docs/api/java/util/logging/package-frame.html"java.util.logging</a>}).
          */
         public static void disableOnodrimLogs() {
             streamHandler.setLevel(Level.OFF);
             onodrimRootLogger.setLevel(Level.OFF);
             onodrimRootLogger.setUseParentHandlers(false);
         }
         
         /**
          * Remove the convenient console handler that was added to Onodrim logger by calling to
          * {@link #enableUtilConsoleLogger()}.
          */
         public static void disableUtilConsoleLogger() {
         	onodrimRootLogger.removeHandler(streamHandler);
         }
     }
 
 }
