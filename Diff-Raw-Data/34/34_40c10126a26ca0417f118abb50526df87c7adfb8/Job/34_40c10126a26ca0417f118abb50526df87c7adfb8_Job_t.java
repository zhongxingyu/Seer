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
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.TreeSet;
 import java.util.Map.Entry;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class Job implements Runnable {
 
     private static Logger logger = Logger.getLogger(Job.class.getCanonicalName());
 
     protected static final String SUCESSFUL_EXECUTION_REPORT_FILE_NAME = "JobResults.txt";
     private static final String FAILED_EXECUTION_REPORT_FILE_NAME = "JOB_ERROR_REPORT.txt";
     protected static final String CONFIG_STORE_FILE_NAME = "JobConfiguration.properties";
 
     /**
      * Configuration instance that sets the parameters for this job.
      */
     private Configuration conf = null;
     /**
      * Index of job in the set to be executed (each job belongs to one set)
      */
     private int index = -1;
     /**
      * Set this job belongs to
      */
     private JobsSet jobsSet = null;
     /**
      * When this job is executed, it will call to the {@link JobEntryPoint#startJob()} method of
      * this instance
      */
     private JobEntryPoint entryPoint = null;
     /**
      * Folder where this job results will be stored
      */
     private File resultsDir = null;
     /**
      * Flag that signals whether this job has already been started
      */
     private boolean alreadyStarted = false;
     /**
      * Flag that signals whether this job has finished
      */
     private boolean finished = false;
     /**
      * Flag that signals whether there was some error during the execution of this job
      */
     private boolean errorInExecution = false;
     /**
      * In case there was an error, this string should store a description of what happened
      */
     private String errorInExecutionMsg = null;
     /**
      * In case there was an error, and it was caused by an exception (or any throwable instance),
      * it should be stored here
      */
     private Throwable errorInExecutionCause = null;
     /**
      * Mapping of results names and values of this job
      */
     private Map<String, Object> results = new HashMap<String, Object>();
     /**
      * Lock to control concurrent access to state flags {@link #alreadyStarted} and {@link #finished}
      */
     private Lock stateLock = new ReentrantLock();
     /**
      * Flag that signals whether this job should be discarded (i.e. not run)
      */
     private boolean discard = false;
 
     /**
      * Creates a job.
      * @param conf Job configuration, (parameters names and values)
      * @param jobIndex Index of job in the {@link JobsSet} instance it belongs to
      * @param jobsSet Set of jobs this job belongs to
      * @param jobEntryPoint This entity implements the functionality to run when this job is executed
      * @param jobResultsDir Folder where job output should be stored
      * @throws IllegalArgumentException If any of the parameters given is {@code null}, or the job index has a negative value
      */
     public Job(Configuration conf, int jobIndex, JobsSet jobsSet, JobEntryPoint jobEntryPoint, File jobResultsDir) {
         if(conf == null)
             throw new IllegalArgumentException("Cannot create a job instance with a null configuration");
         if(jobsSet == null)
             throw new IllegalArgumentException("Cannot create a job instance that does not belong to a job set");
         if (jobResultsDir == null)
             throw new IllegalArgumentException("Cannot create a job instance with a null dir to store results");
         if(jobIndex < 0)
             throw new IllegalArgumentException("Cannot create a job with a negative index");
         this.conf = conf;
         this.index = jobIndex;
         this.jobsSet = jobsSet;
         this.entryPoint = jobEntryPoint;
         this.resultsDir = jobResultsDir;
         // Just in case, checking no other experiment with the same index is already in the set
         for(Job job: jobsSet.getJobs())
             if(job.getIndex() == jobIndex)
                 throw new IllegalArgumentException("Cannot add a new job with index '" + jobIndex + "', there is already one job with that index in the same set");
     }
 
     /** 
      * This job configuration
      * @return This job configuration.
      */
     public Configuration getConfiguration() {
         return conf;
     }
 
     /**
      * Index in the {@link JobsSet} this job belongs to.
      * @return Index in the {@link JobsSet} this job belongs to.
      */
     public int getIndex() {
         return index;
     }
     
     /**
      * {@link JobsSet} this job belongs to.
      * @return {@link JobsSet} this job belongs to.
      */
     public JobsSet getJobsSet() {
         return jobsSet;
     }
 
     /**
      * Returns the folder this job results must be stored into.
      * @return Folder to store results into.
      */
     public File getResultsDir() {
         return resultsDir;
     }
 
     /**
      * Returns {@code true} if this job has finished, {@code false} otherwise.
      * @return Whether this job is finished or not. 
      */
     public boolean finished() {
         return finished;
     }
 
     /**
      * Returns {@code true} if this job has already been started (and maybe finished), {@code false} otherwise.
      * @return Whether this job has started or not.
      */
     public boolean started() {
         return alreadyStarted;
     }
 
     /**
      * Store a result of this experiment
      * @param resultName Result name
      * @param result Result value
      */
     public void addResult(String resultName, Object result) {
         if (discard)
             throw new Error("Cannot add a result to a discarded experiment");
         results.put(resultName, result);
     }
 
     /**
      * It calls to {@link #setErrorInExecution(String, Throwable)}, passing {@code null}
      * as the throwable instance.
      */
     public void setErrorInExecution(String errorInExecutionMsg) {
     	setErrorInExecution(errorInExecutionMsg, null);
     }
 
     /**
      * Used to signal that this Job execution failed.
      * @param errorInExecutionMsg Description of the error found when executing the experiment, it can be null
      * @param errorInExecutionCause If the problem was caused by any exception, it can be stored here. If there
      *                              is not such exception/error, just set it here {@code null}.
      */
     public void setErrorInExecution(String errorInExecutionMsg, Throwable errorInExecutionCause) {
         this.errorInExecution = true;
         this.errorInExecutionMsg = errorInExecutionMsg;
         this.errorInExecutionCause = errorInExecutionCause;
     }
 
     /**
      * Returns {@code true} if the execution of this job failed (see method {@link #setErrorInExecution(String)}, {@code false} otherwise.
      * @return Whether there was an error during the execution of this job.
      */
     public boolean executionFailed() {
         return errorInExecution;
     }
 
     /**
      * All results (names and values) obtained by this job, they have been set by calls to {@link #addResult(String, Object)}
      * method
      * @return Results obtained during the execution of this job.
      */
     public Map<String, Object> getResults() {
         return results;
     }
 
     /**
      * Returns {@code true} if this job was discarded because the results from a previous experiment
      * were found in this job folder, and previous experiments are not to be overwritten (see method
      * {@link #discardAndLoadResulfsFromFile(File)})
      */
     public boolean discarded() {
         return discard;
     }
 
     /**
      * Method called when results from a previous experiment
      * were found in this job folder, and previous experiments are not to be overwritten. The results
      * to be returned (see method {@link #getResults()}) are loaded from the previous experiment.
      * @param resultsFile Results from the previous experiment
      */
     protected void discardAndLoadResulfsFromFile(File resultsFile) {
         discard = true;
         errorInExecution = false;
         Properties prevResults = new Properties();
         try {
             prevResults.load(new FileReader(resultsFile));
         } catch (FileNotFoundException exception) {
             throw new Error("Could not load results file " + resultsFile.getAbsolutePath(), exception);
         } catch (IOException exception) {
             throw new Error("Could not load results file " + resultsFile.getAbsolutePath(), exception);
         }
         for (Entry<Object, Object> entry : prevResults.entrySet()) {
             results.put(entry.getKey().toString(), entry.getValue());
         }
     }
 
     /**
      * Method automatically called by the framework right before the job is executed, it creates the folder
      * where results will be stored.
      */
     protected void preExecutionProcess() {
         
         if(discard) {
             // Well, it could be that the job results directory is not present, in that case we have to create it
             if(!resultsDir.exists()) {
                 logger.log(Level.FINE, "Job " + index + " discarded, just creating results folder");
                 if(!resultsDir.mkdirs()) {
                     logger.log(Level.SEVERE, "Could not create results directory '" + resultsDir.getAbsolutePath() + "'");
                     throw new Error("Could not create results directory '" + resultsDir.getAbsolutePath() + "'");
                 }
             } else
                 logger.log(Level.FINE, "Job " + index + " discarded");
             return;
         }
 
         logger.log(Level.FINE, "Running pre-execution of job " + index);
 
         // Removing previous experiment results
         logger.log(Level.FINE, "Removing folder with previous execution results '" + resultsDir.getAbsolutePath() + "' (if any)");
         if(!Util.removeDirRecursively(resultsDir)) {
             logger.log(Level.SEVERE, "Could not remove previous results directory '" + resultsDir.getAbsolutePath() + "'");
             throw new Error("Could not remove previous results directory '" + resultsDir.getAbsolutePath() + "'");
         }
         if (!resultsDir.mkdirs()) {
             logger.log(Level.SEVERE, "Could not create results directory '" + resultsDir.getAbsolutePath() + "'");
             throw new Error("Could not create results directory '" + resultsDir.getAbsolutePath() + "'");
         }
 
         // Storing job configuration in corresponding file, if a conf file from
         // a previous execution
         // exists it will be overwritten
         File fileToStoreConf = new File(resultsDir, CONFIG_STORE_FILE_NAME);
         try {
             conf.saveInFile(fileToStoreConf);
         } catch (IOException exception) {
             logger.log(Level.SEVERE, "IOException caught when trying to save configuration in file "
                             + fileToStoreConf.getAbsolutePath(), exception);
             throw new Error("IOException caught when trying to save configuration in file "
                             + fileToStoreConf.getAbsolutePath(), exception);
         }
 
     }
 
     /**
      * Method automatically called by the framework right before the job is executed, it creates the folder
      * where results will be stored if the job was successfully executed, or where information about the found error
      * can be found otherwise.
      * @param throwable It contains the {@link Throwable} instance that caused the job to fail
      */
     protected void postExecutionProcess(Throwable throwable) {
 
         logger.log(Level.FINE, "Job " + index + " finished, running after-execution process");
 
         if ((throwable != null) || errorInExecution)
             try {
                 // Something went wrong when running the job, we record error
                 // message in file for user check.
                 File failedExecutionReportFile = new File(resultsDir, FAILED_EXECUTION_REPORT_FILE_NAME);
                 logger.log(Level.INFO, "Job " + index + " execution failed, recording failure information in file "
                                         + failedExecutionReportFile.getAbsolutePath());
                 PrintWriter writer = new PrintWriter(failedExecutionReportFile);
                 if (throwable != null) {
                     writer.println("(Non processed) exception caught when running experiment: " + throwable.getMessage());
                     throwable.printStackTrace(writer);
                 } else {
                     writer.println("Execution was not successful, recorded error message:\n" + errorInExecutionMsg);
                     if(errorInExecutionCause != null) {
                     	writer.println("Cause: ");
                     	writer.println(errorInExecutionCause.getMessage());
                     	errorInExecutionCause.printStackTrace(writer);
                     }
                 }
                 writer.close();
             } catch (IOException ioException) {
                 logger.log(Level.SEVERE, "Could not store error message for job " + index
                                         + ", IOException caught", ioException);
                 throw new Error("Could not store error message for job " + index
                                 + ", IOException caught", ioException);
             }
         else
             try {
                 // Storing results
                 File successfulExecutionReportFile = new File(resultsDir, SUCESSFUL_EXECUTION_REPORT_FILE_NAME);
                 if(discard)
                     logger.log(Level.INFO, "Job " + index + " was discarded, storing results in file "
                                             + successfulExecutionReportFile.getAbsolutePath());
                 else
                     logger.log(Level.INFO, "Job " + index + " execution successful, storing results in file "
                                             + successfulExecutionReportFile.getAbsolutePath());
                 PrintWriter writer = new PrintWriter(successfulExecutionReportFile);
                 writer.println("# " + new Date().toString() + " #");
                 for(String resultName: new TreeSet<String>(results.keySet()))
                     writer.println(resultName + "=" + results.get(resultName));
                 writer.close();
             } catch (IOException ioException) {
                 logger.log(Level.SEVERE, "Could not store results of job " + index + ", IOException caught",
                             ioException);
                 throw new Error("Could not store results of job " + index + ", IOException caught", ioException);
             }
     }
     
     /**
     * This method is used by {@link JobsSet#runJobs()} to make sure all jobs have the entry point
     * properly set before proceeding with the automated execution. 
     * @return {@code true} if the entry point of this job is {@code null}.
      */
     protected boolean jobEntryPointIsNull() {
     	return (entryPoint == null);
     }
 
     /**
      * Run the job! (unless discarded because results from a previous execution were found and cannot override them)
      * This method is called right after {@link #preExecutionProcess()} is called, when it finishes then
      * {@link #postExecutionProcess(Throwable)} will be executed likewise.
      */
     @Override
     public void run() {
 
         if (discard) {
             logger.log(Level.FINE, "Job " + index + " discarded, nothing to do");
             return;
         }
             
         logger.log(Level.FINE, "Starting job " + index);
 
         boolean illegalState = false;
         stateLock.lock();
         if (alreadyStarted)
             illegalState = true;
         alreadyStarted = true;
         stateLock.unlock();
         if (illegalState) // This should not happen ever
             throw new IllegalStateException("Job " + index + " was already started");
 
         entryPoint.startJob(); // Well, finally! Running job!!
 
         finished = true;
 
     }
 
     /**
      * Utility function to get the job index in a convenient manner (as a string lead by '0's)
      * @param jobIndex Job index
      * @param maxIndex Max index of all jobs (possibly in the same {@link JobsSet}
      * @return String containing this job index with leading '0's if necessary.
      */
     protected static String indexAsString(int jobIndex, int maxIndex) {
         return String.format("%1$0" + (maxIndex + "").length() + "d", jobIndex);
     }
 
 }
