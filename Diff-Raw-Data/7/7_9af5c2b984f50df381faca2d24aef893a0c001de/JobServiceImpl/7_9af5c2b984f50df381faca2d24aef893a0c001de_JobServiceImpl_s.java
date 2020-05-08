 package de.otto.jobstore.service.impl;
 
 import de.otto.jobstore.common.*;
 import de.otto.jobstore.repository.api.JobInfoRepository;
 import de.otto.jobstore.service.api.JobService;
 import de.otto.jobstore.service.exception.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Collections;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CopyOnWriteArraySet;
 import java.util.concurrent.Executors;
 
 /**
  *
  */
 public final class JobServiceImpl implements JobService {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(JobServiceImpl.class);
 
     private final Map<String, JobRunnable> jobs = new ConcurrentHashMap<String, JobRunnable>();
     private final Set<Set<String>> runningConstraints = new CopyOnWriteArraySet<Set<String>>();
     private final JobInfoRepository jobInfoRepository;
     private boolean executionEnabled = true;
 
     /**
      * Creates a JobService Object.
      *
      * @param jobInfoRepository The jobInfo Repository to store the jobs in
      */
     public JobServiceImpl(final JobInfoRepository jobInfoRepository) {
         this.jobInfoRepository = jobInfoRepository;
     }
 
     public boolean isExecutionEnabled() {
         return executionEnabled;
     }
 
     /**
      * Disables or enables Job execution. Default value is true.
      *
      * @param executionEnabled true - Jobs will be executed<br/>
      *                         false - No jobs will be executed
      */
     public void setExecutionEnabled(boolean executionEnabled) {
         this.executionEnabled = executionEnabled;
     }
 
     @Override
     public boolean registerJob(final String name, final JobRunnable runnable) {
         final boolean inserted;
         if (jobs.containsKey(name)) {
             inserted = false;
         } else {
             jobs.put(name, runnable);
             inserted = true;
         }
         return inserted;
     }
 
     @Override
     public boolean addRunningConstraint(final Set<String> constraint) throws JobNotRegisteredException {
         for (String name : constraint) {
             checkJobName(name);
         }
         return runningConstraints.add(Collections.unmodifiableSet(constraint));
     }
 
     @Override
     public String executeJob(final String name) throws JobNotRegisteredException, JobAlreadyQueuedException,
             JobAlreadyRunningException, JobExecutionNotNecessaryException, JobExecutionDisabledException {
         return executeJob(name, false);
     }
 
     @Override
     public boolean removeJobFromQueue(String name) {
         return jobInfoRepository.markQueuedAsNotExecuted(name);
     }
 
     @Override
     public String executeJob(final String name, final boolean forceExecution) throws JobNotRegisteredException,
             JobAlreadyQueuedException, JobAlreadyRunningException, JobExecutionNotNecessaryException,
             JobExecutionDisabledException {
         final JobRunnable runnable = jobs.get(checkJobName(name));
         final String id;
         if (!executionEnabled) {
             throw new JobExecutionDisabledException("Execution of jobs has been disabled");
         } else if (jobInfoRepository.hasJob(name, RunningState.QUEUED.name())) {
             throw new JobAlreadyQueuedException("A job with name " + name + " is already queued for execution");
         } else if (jobInfoRepository.hasJob(name, RunningState.RUNNING.name())) {
             id = queueJob(name, runnable.getMaxExecutionTime(), forceExecution, "A job with name " + name + " is already running and queued for execution");
         } else if (violatesRunningConstraints(name)) {
             id = queueJob(name, runnable.getMaxExecutionTime(), forceExecution, "The job " + name + " violates running constraints and is already queued for execution");
         } else if (forceExecution || runnable.isExecutionNecessary()) {
             id = runJob(name, runnable.getMaxExecutionTime(), forceExecution, "A job with name " + name + " is already running");
             LOGGER.debug("ltag=JobService.runJob.executingJob jobInfoName={}", name);
             Executors.newSingleThreadExecutor().execute(new JobExecutionRunnable(name, runnable));
         } else {
             throw new JobExecutionNotNecessaryException("Execution of job " + name + " was not necessary");
         }
         return id;
     }
 
     @Override
     public void executeQueuedJobs() {
         if (executionEnabled) {
             LOGGER.info("ltag=JobServiceImpl.executeQueuedJobs");
             for (JobInfo jobInfo : jobInfoRepository.findQueuedJobsSortedAscByCreationTime()) {
                 executeQueuedJob(jobInfo);
             }
         }
     }
 
     @Override
     public void shutdownJobs() {
         for (String name : jobs.keySet()) {
             final JobInfo runningJob = jobInfoRepository.findByNameAndRunningState(name, RunningState.RUNNING.name());
             if (runningJob != null && runningJob.getHost().equals(InternetUtils.getHostName())) {
                 LOGGER.info("ltag=JobService.shutdownJobs jobInfoName={}", name);
                 jobInfoRepository.markRunningAsFinished(name, ResultState.FAILED, "shutdownJobs called from executing host");
             }
         }
     }
 
     @Override
     public void clean() {
         jobs.clear();
         runningConstraints.clear();
     }
 
     @Override
     public Set<String> listJobNames() {
         return Collections.unmodifiableSet(jobs.keySet());
     }
 
     @Override
     public Set<Set<String>> listRunningConstraints() {
         return Collections.unmodifiableSet(runningConstraints);
     }
 
     private void executeQueuedJob(final JobInfo jobInfo) {
         final String name = jobInfo.getName();
         final JobRunnable runnable = jobs.get(name);
         if (jobInfoRepository.hasJob(name, RunningState.RUNNING.name())) {
             LOGGER.debug("ltag=JobService.executeQueuedJob.alreadyRunning jobInfoName={}", name);
         } else if (violatesRunningConstraints(name)) {
             LOGGER.debug("ltag=JobService.executeQueuedJob.violatesRunningConstraints jobInfoName={}", name);
         } else if (jobInfo.isForceExecution() || runnable.isExecutionNecessary()) {
             activateQueuedJob(name, runnable);
         } else if (jobInfoRepository.markAsFinishedById(jobInfo.getId(), ResultState.NOT_EXECUTED)) {
             LOGGER.warn("ltag=JobService.executeQueuedJob.executionIsNotNecessary");
         }
     }
 
     private String queueJob(String name, long maxExecutionTime, boolean forceExecution, String exceptionMessage)
             throws JobAlreadyQueuedException{
         final String id = jobInfoRepository.create(name, maxExecutionTime, RunningState.QUEUED, forceExecution, null);
         if (id == null) {
             throw new JobAlreadyQueuedException(exceptionMessage);
         }
         return id;
     }
 
     private String runJob(String name, long maxExecutionTime, boolean forceExecution, String exceptionMessage)
             throws JobAlreadyRunningException {
         final String id = jobInfoRepository.create(name, maxExecutionTime, RunningState.RUNNING, forceExecution, null);
         if (id == null) {
             throw new JobAlreadyRunningException(exceptionMessage);
         }
         return id;
     }
 
     private void activateQueuedJob(String name, JobRunnable runnable) {
         if (jobInfoRepository.activateQueuedJob(name)) {
             jobInfoRepository.updateHostThreadInformation(name);
             LOGGER.debug("ltag=JobService.activateQueuedJob.activate jobInfoName={}", name);
             Executors.newSingleThreadExecutor().execute(new JobExecutionRunnable(name, runnable));
         } else {
             LOGGER.warn("ltag=JobService.activateQueuedJob.jobIsNotQueuedAnyMore");
         }
     }
 
     private String checkJobName(final String name) throws JobNotRegisteredException {
         if (jobs.containsKey(name)) {
             return name;
         } else {
             throw new JobNotRegisteredException("job with name " + name + " is not registered with this jobService instance");
         }
     }
 
     private boolean violatesRunningConstraints(final String name) {
         for (Set<String> constraint : runningConstraints) {
             if (constraint.contains(name)) {
                 for (String constraintJobName : constraint) {
                     if (jobInfoRepository.hasJob(constraintJobName, RunningState.RUNNING.name())) {
                         return true;
                     }
                 }
             }
         }
         return false;
     }
 
     private class JobExecutionRunnable implements Runnable {
         final String jobName;
         final JobRunnable jobRunnable;
 
         JobExecutionRunnable(String jobName, JobRunnable jobRunnable) {
             this.jobName = jobName;
             this.jobRunnable = jobRunnable;
         }
 
         @Override
         public void run() {
             try {
                 LOGGER.info("ltag=JobService.JobExecutionRunnable.run jobInfoName={}", jobName);
                 jobRunnable.execute(new SimpleJobLogger(jobName, jobInfoRepository));
                 jobInfoRepository.markRunningAsFinishedSuccessfully(jobName);
            } catch (Throwable t) {
                jobInfoRepository.markRunningAsFinishedWithException(jobName, t);
             }
         }
     }
 
 }
