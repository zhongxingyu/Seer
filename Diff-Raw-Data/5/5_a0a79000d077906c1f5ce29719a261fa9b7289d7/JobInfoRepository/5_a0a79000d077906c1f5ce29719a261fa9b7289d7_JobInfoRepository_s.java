 package de.otto.jobstore.repository;
 
 import com.mongodb.*;
 import de.otto.jobstore.common.*;
 import de.otto.jobstore.common.properties.JobInfoProperty;
 import de.otto.jobstore.common.util.InternetUtils;
 import org.bson.types.ObjectId;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.*;
 
 /**
  * A repository which stores information on jobs. For each distinct job name only one job can be running or queued.
  *
  * The method {@link #cleanupTimedOutJobs} needs to be called regularly to remove possible timed out jobs which would
  * otherwise stop new jobs from being able to execute.
  */
 public class JobInfoRepository extends AbstractRepository<JobInfo> {
 
     private static final String JOB_NAME_CLEANUP              = "JobInfo_Cleanup";
     private static final String JOB_NAME_TIMED_OUT_CLEANUP    = "JobInfo_TimedOut_Cleanup";
     private static final String JOB_NAME_CLEANUP_NOT_EXECUTED = "JobInfo_NotExecuted_Cleanup";
     private static final long FIVE_MINUTES = 5 * 60 * 1000;
 
     private int hoursAfterWhichOldJobsAreDeleted         = 7 * 24;
     private int hoursAfterWhichNotExecutedJobsAreDeleted = 2;
 
     public JobInfoRepository(Mongo mongo, String dbName, String collectionName) {
         super(mongo, dbName, collectionName);
     }
 
     public JobInfoRepository(Mongo mongo, String dbName, String collectionName, String username, String password) {
         super(mongo, dbName, collectionName, username, password);
     }
 
     public JobInfoRepository(Mongo mongo, String dbName, String collectionName, String username, String password, WriteConcern safeWriteConcern) {
         super(mongo, dbName, collectionName, username, password, safeWriteConcern);
     }
 
     public int getHoursAfterWhichOldJobsAreDeleted() {
         return hoursAfterWhichOldJobsAreDeleted;
     }
 
     /**
      * Sets the number of hours after which old jobs are removed.
      *
      * @param hours The number of hours
      */
     public void setHoursAfterWhichOldJobsAreDeleted(int hours) {
         this.hoursAfterWhichOldJobsAreDeleted = hours;
     }
 
     /**
      * Creates a new job with the given parameters. Host and thread executing the job are determined automatically.
      *
      * @param name The name of the job
      * @param maxIdleTime Sets the time after which a job is considered to be dead if unmodified (lastModifiedTime + timeout).
      * @param maxExecutionTime Sets the time after which a job is considered to be dead (startTime + timeout).
      * @param maxRetries Sets the number of maximum automatic retries if job fails.
      * @param runningState The state with which the job is started
      * @param executionPriority The priority with which the job is to be executed
      * @param additionalData Additional information to be stored with the job
      * @return The id of the job if it could be created or null if a job with the same name and state already exists
      */
     public String create(final String name, final long maxIdleTime, final long maxExecutionTime, final long maxRetries, final RunningState runningState,
                          final JobExecutionPriority executionPriority, final Map<String, String> parameters, final Map<String, String> additionalData) {
         final String host = InternetUtils.getHostName();
         final String thread = Thread.currentThread().getName();
         return create(name, host, thread, maxIdleTime, maxExecutionTime, maxRetries, runningState, executionPriority, parameters, additionalData);
     }
 
     /**
      * Creates a new job with the given parameters
      *
      * @param name The name of the job
      * @param host The host, on which the job is running
      * @param thread The thread, which runs the job
      * @param maxIdleTime Sets the time after which a job is considered to be dead if unmodified (lastModifiedTime + timeout).
      * @param maxExecutionTime Sets the time after which a job is considered to be dead (startTime + timeout).
      * @param maxRetries Sets the number of maximum automatic retries if job fails.
      * @param runningState The state with which the job is started
      * @param executionPriority The priority with which the job is to be executed
      * @param additionalData Additional information to be stored with the job
      * @return The id of the job if it could be created or null if a job with the same name and state already exists
      */
     public String create(final String name, final String host, final String thread, final long maxIdleTime, final long maxExecutionTime,
                          final long maxRetries, final RunningState runningState, final JobExecutionPriority executionPriority,
                          final Map<String, String> parameters, final Map<String, String> additionalData) {
         try {
             logger.info("Create job={} in state={} ...", name, runningState);
 
             long retries = evaluateRetriesBasedOnPreviouslyFailedJobs(name, maxRetries);
 
             final JobInfo jobInfo = new JobInfo(name, host, thread, maxIdleTime, maxExecutionTime, retries, runningState, executionPriority, additionalData);
             jobInfo.setParameters(parameters);
 
             save(jobInfo);
             return jobInfo.getId();
         } catch (MongoException.DuplicateKey e) {
             logger.warn("job={} with state={} already exists, creation skipped!", name, runningState);
             return null;
         }
     }
 
     public long evaluateRetriesBasedOnPreviouslyFailedJobs(String name, long maxRetries) {
         JobInfo jobInfo = findMostRecentFinished(name);
         if(jobInfo == null || jobInfo.getResultState() == ResultCode.SUCCESSFUL || jobInfo.getResultState() == ResultCode.NOT_EXECUTED) {
             return maxRetries;
         } else {
             return Math.max(0, jobInfo.getRetries()-1);
         }
     }
 
     /**
      * Returns job with the given name and running state
      *
      * @param name The name of the job
      * @param runningState The running state of the job
      * @return The running job or null if no job with the given name is currently running
      */
     public JobInfo findByNameAndRunningState(final String name, final RunningState runningState) {
         final DBObject jobInfo = collection.findOne(createFindByNameAndRunningStateQuery(name, runningState.name()));
         return fromDbObject(jobInfo);
     }
 
     /**
      * Checks if a job with the given name and state exists
      *
      * @param name The name of the job
      * @param runningState The running state of the job
      * @return true - A job with the given name is still running<br/>
      *          false - A job with the given name is not running
      */
     public boolean hasJob(final String name, final RunningState runningState) {
         return findByNameAndRunningState(name, runningState) != null;
     }
 
     /**
      * Returns all queued jobs sorted ascending by start time
      *
      * @return The queued jobs
      */
     public List<JobInfo> findQueuedJobsSortedAscByCreationTime() {
         final DBCursor cursor = collection.find(new BasicDBObject(JobInfoProperty.RUNNING_STATE.val(), RunningState.QUEUED.name())).
                 sort(new BasicDBObject(JobInfoProperty.CREATION_TIME.val(), SortOrder.ASC.val()));
         return getAll(cursor);
     }
 
     /**
      * Returns a list of jobs with the given name which have a last modified timestamp which is in between the supplied
      * dates. If the start and end parameter are null, the result list will contain all jobs with the supplied name.
      *
      * @param name The name of the jobs to return
      * @param start The date on or after which the jobs were last modified
      * @param end The date on or before which the jobs were last modified
      * @param resultCodes Limit to the jobs with the specified result states
      * @return The list of jobs sorted by creationTime in descending order
      */
     public List<JobInfo> findByNameAndTimeRange(final String name, final Date start, final Date end, final Collection<ResultCode> resultCodes) {
             return findByNameAndTimeRange(name, start, end, resultCodes, false);
     }
 
     private List<JobInfo> findByNameAndTimeRange(final String name, final Date start, final Date end, final Collection<ResultCode> resultCodes, boolean excludeLogLines) {
         final BasicDBObjectBuilder query = new BasicDBObjectBuilder().append(JobInfoProperty.NAME.val(), name);
 
         BasicDBObjectBuilder betweenTimeQuery = new BasicDBObjectBuilder();
         if(start != null) {
             betweenTimeQuery.append(MongoOperator.GTE.op(), start);
         }
         if (end != null) {
             betweenTimeQuery.append(MongoOperator.LTE.op(), end);
         }
         if(!betweenTimeQuery.isEmpty()) {
             query.append(JobInfoProperty.LAST_MODIFICATION_TIME.val(), betweenTimeQuery.get());
         }
 
         if (resultCodes != null && !resultCodes.isEmpty()) {
             final List<String> resultCodeAsStrings = toStringList(resultCodes);
             query.append(JobInfoProperty.RESULT_STATE.val(), new BasicDBObject(MongoOperator.IN.op(), resultCodeAsStrings));
         }
 
         BasicDBObject keys = new BasicDBObject();
         if(excludeLogLines) {
             keys.put(JobInfoProperty.LOG_LINES.val(), 0);
         }
 
         final DBCursor cursor = collection.find(query.get(), keys).
                 sort(new BasicDBObject(JobInfoProperty.CREATION_TIME.val(), SortOrder.DESC.val()));
         logger.info("findByNameAndTimeRange executing cursor {} ", cursor);
         return getAll(cursor);
     }
 
     /**
      * Sets the status of the queued job with the given name to running. The lastModified date of the job is set
      * to the current date.
      *
      * @param id The id of the job
      * @return true - If the job with the given name was activated successfully<br/>
      *         false - If no queued job with the current name could be found and thus could not activated
      */
     public boolean activateQueuedJobById(final String id) {
         logger.info("Activate job={} ...", id);
         return changeState(id, RunningState.RUNNING, new Date());
     }
 
     /**
      * sets status of running job back to queued. This is necessary if running constraints fail after activateQueuedJob. This seems to be
      * awkward, but is necessary to prevent race condition with parallel started jobs
      *
      * @param id The id of the job
      * @return true - If the job with the given name was activated successfully<br/>
      *         false - If no queued job with the current name could be found and thus could not activated
      */
     public boolean deactivateRunningJob(final String id) {
         logger.info("Aeactivate job={} ...", id);
         return changeState(id, RunningState.QUEUED, null);
     }
 
     private boolean changeState(final String id, RunningState toState, Date startTime) {
         final Date dt = new Date();
         final DBObject update = new BasicDBObject().append(MongoOperator.SET.op(),
                 new BasicDBObject(JobInfoProperty.RUNNING_STATE.val(), toState.name()).
                         append(JobInfoProperty.START_TIME.val(), startTime).
                         append(JobInfoProperty.LAST_MODIFICATION_TIME.val(), dt));
         try {
             final WriteResult result = collection.update(createIdQuery(id), update, false, false, getSafeWriteConcern());
             return result.getN() == 1;
         } catch (MongoException.DuplicateKey e){
             return false;
         }
     }
 
     /**
      * Set the aborted property of the job to true
      *
      * @param id The id of the Job to abort
      */
     public void abortJob(String id) {
         if (ObjectId.isValid(id)) {
             collection.update(createIdQuery(id),
                     new BasicDBObject(MongoOperator.SET.op(), new BasicDBObject(JobInfoProperty.ABORTED.val(), true)), false, false, getSafeWriteConcern());
         }
     }
 
     /**
      * Updates the host and thread information on the job with the given id. Host and thread information
      * are determined automatically.
      * The processing of this method is performed asynchronously. Thus the existance of a running job with the given
      * jobname ist not checked
      *
      * @param id The id of the job
      */
     public void updateHostThreadInformation(final String id) {
         updateHostThreadInformation(id, InternetUtils.getHostName(), Thread.currentThread().getName());
     }
 
     /**
      * Updates the host and thread information on the job with the given id
      * The processing of this method is performed asynchronously. Thus the existance of a running job with the given
      * jobname ist not checked
      *
      * @param id The id of the job
      * @param host The host to set
      * @param thread The thread to set
      */
     public void updateHostThreadInformation(final String id, final String host, final String thread) {
         final DBObject update = new BasicDBObject().append(MongoOperator.SET.op(),
                 new BasicDBObject(JobInfoProperty.HOST.val(), host).append(JobInfoProperty.THREAD.val(), thread));
         collection.update(createIdQuery(id), update);
     }
 
     /**
      * Marks a job with the given id as finished.
      *
      * @param id The id of the job
      * @param resultCode The result state of the job
      * @return true - The job was marked as requested<br/>
      *         false - No running job with the given name could be found
      */
     public boolean markAsFinished(final String id, final ResultCode resultCode) {
         return markAsFinished(id, resultCode, null);
     }
 
     /**
      * Marks a job with the given id as finished.
      *
      * @param id The id of the job
      * @param resultCode The result state of the job
      * @param resultMessage The resultMessage of the job
      * @return true - The job was marked as requested<br/>
      *         false - No running job with the given name could be found
      */
     public boolean markAsFinished(final String id, final ResultCode resultCode, final String resultMessage) {
         return ObjectId.isValid(id) &&
                 markAsFinished(createIdQuery(id), resultCode, resultMessage);
     }
 
     /**
      * Marks a job with the given id as finished.
      *
      * @param id The id of the job
      * @param t An exception
      * @return true - The job was marked as requested<br/>
      *         false - No running job with the given name could be found
      */
     public boolean markAsFinished(final String id, final Throwable t) {
         return markAsFinished(id, ResultCode.FAILED, t == null ? null : exceptionToMessage(t));
     }
 
     /**
      * Marks the current queued job of the given name as not executed
      *
      * @param name The name of the job
      * @return true - The job was marked as requestred<br/>
      *          false - No queued job with the given name could be found
      */
     public boolean markQueuedAsNotExecuted(final String name) {
         final Date dt = new Date();
         final DBObject update = new BasicDBObject().append(MongoOperator.SET.op(),
                 new BasicDBObject().append(JobInfoProperty.RESULT_STATE.val(), ResultCode.NOT_EXECUTED.name()).
                         append(JobInfoProperty.LAST_MODIFICATION_TIME.val(), dt).
                         append(JobInfoProperty.FINISH_TIME.val(), dt).
                         append(JobInfoProperty.RUNNING_STATE.val(), createFinishedRunningState()));
         final WriteResult result = collection.update(new BasicDBObject().append(JobInfoProperty.NAME.val(), name).
                 append(JobInfoProperty.RUNNING_STATE.val(), RunningState.QUEUED.name()), update, false, false, getSafeWriteConcern());
         return result.getN() == 1;
     }
 
     /**
      * Adds additional data to a running job with the given id. If information with the given key already exists
      * it is overwritten. The lastModified date of the job is set to the current date.
      *
      * The processing of this method is performed asynchronously. Thus the existance of a running job with the given
      * jobname ist not checked.
      *
      * @param id The id of the job
      * @param key The key of the data to save
      * @param value The information to save
      */
     public void addAdditionalData(final String id, final String key, final String value) {
         final DBObject update = new BasicDBObject().append(MongoOperator.SET.op(),
                 new BasicDBObjectBuilder().append(JobInfoProperty.LAST_MODIFICATION_TIME.val(), new Date()).
                         append(JobInfoProperty.ADDITIONAL_DATA.val() + "." + key, value).get());
         collection.update(createIdQuery(id), update);
     }
 
     /**
      * Sets a status message.
      *
      * The processing of this method is performed asynchronously. Thus the existence of a job with the given
      * id is not checked
      *
      * @param id The id of the job
      * @param message The message to set
      */
     public void setStatusMessage(final String id, String message) {
         final DBObject update = new BasicDBObject().append(MongoOperator.SET.op(),
                 new BasicDBObjectBuilder().append(JobInfoProperty.LAST_MODIFICATION_TIME.val(), new Date()).
                         append(JobInfoProperty.STATUS_MESSAGE.val(), message).get());
         collection.update(createIdQuery(id), update);
     }
 
     /**
      * Find a job by its id.
      *
      * @param id The id of the job
      * @return The job with the given id or null if no corresponding job was found.
      */
     public JobInfo findById(final String id) {
         if (ObjectId.isValid(id)) {
             return fromDbObject(collection.findOne(createIdQuery(id)));
         } else {
             return null;
         }
     }
 
     /**
      * Returns all jobs with the given name.
      * TODO: last modified ODER creation time?
      *
      * @param name The name of the jobs
      * @param limit The maximum number of jobs to return
      * @return All jobs with the given name sorted descending by last modified date
      */
     public List<JobInfo> findByName(final String name, final Integer limit) {
         final BasicDBObjectBuilder query = new BasicDBObjectBuilder().append(JobInfoProperty.NAME.val(), name);
         final DBCursor cursor = collection.find(query.get()).
                 sort(new BasicDBObject(JobInfoProperty.CREATION_TIME.val(), SortOrder.DESC.val()));
         if (limit == null) {
             return getAll(cursor);
         } else {
             return getAll(cursor.limit(limit));
         }
     }
 
     /**
      * Returns the job with the given name and the most current last modified timestamp.
      * TODO: last modified ODER creation time?
      *
      * @param name The name of the job
      * @return The job with the given name and the most current timestamp or null if none could be found.
      */
     public JobInfo findMostRecent(final String name) {
         final DBCursor cursor = collection.find(new BasicDBObject().
                 append(JobInfoProperty.NAME.val(), name)).
                 sort(new BasicDBObject(JobInfoProperty.CREATION_TIME.val(), SortOrder.DESC.val())).limit(1);
         return getFirst(cursor);
     }
 
     public JobInfo findMostRecentFinished(String name) {
         final List<String> resultStates = toStringList(EnumSet.complementOf(EnumSet.of(RunningState.FINISHED)));
         final DBCursor cursor = collection.find(new BasicDBObject().
                 append(JobInfoProperty.NAME.val(), name).
                 append(JobInfoProperty.RUNNING_STATE.val(), new BasicDBObject(MongoOperator.NIN.op(), resultStates))).
                 sort(new BasicDBObject(JobInfoProperty.CREATION_TIME.val(), SortOrder.DESC.val())).limit(1);
         return getFirst(cursor);
     }
 
     /**
      * Returns the job with the given name and result state(s) as well as the most current last modified timestamp.
      *
      * @param name The name of the job
      * @param resultStates The result states the job may have
      * @return The job with the given name and result state as well as the most current timestamp or null
      * if none could be found.
      */
     public JobInfo findMostRecentByNameAndResultState(final String name, final Set<ResultCode> resultStates) {
         DBObject query = createFindByNameAndResultStateQuery(name, resultStates);
         DBCursor cursor = collection.find(query).
                 sort(new BasicDBObject(JobInfoProperty.CREATION_TIME.val(), SortOrder.DESC.val())).limit(1);
         return getFirst(cursor);
     }
 
     /**
      * Returns for all existing job names the job with the most current last modified timestamp regardless of its state.
      *
      * @return The jobs with distinct names and the most current last modified timestamp
      */
     public List<JobInfo> findMostRecent() {
         final List<JobInfo> jobs = new ArrayList<>();
         for (String name : distinctJobNames()) {
             final JobInfo jobInfo = findMostRecent(name);
             if (jobInfo != null) {
                 jobs.add(jobInfo);
             }
         }
         return jobs;
     }
 
     /**
      * Returns the list of all distinct job names within this repository
      *
      * @return The list of distinct jobnames
      */
     @SuppressWarnings("unchecked")
     public List<String> distinctJobNames() {
         return collection.distinct(JobInfoProperty.NAME.val());
     }
 
     /**
      * Adds a logging line to the logging data of the running job with the supplied name
      * The processing of this method is performed asynchronously. Thus the existance of a running job with the given
      * jobname ist not checked
      *
      * @param jobId The id of the job
      * @param line The log line to add
      */
     public void addLogLine(final String jobId, final String line) {
         final Date dt = new Date();
         final LogLine logLine = new LogLine(line, dt);
         final DBObject update = new BasicDBObject().
                 append(MongoOperator.PUSH.op(), new BasicDBObject(JobInfoProperty.LOG_LINES.val(), logLine.toDbObject())).
                 append(MongoOperator.SET.op(), new BasicDBObject(JobInfoProperty.LAST_MODIFICATION_TIME.val(), dt));
         collection.update(createIdQuery(jobId), update);
     }
 
     /**
      * Appends the log lines of the job with the supplied id
      * to the already existing log lines.
      *
      * @param id The id of the job
      * @param lines the log lines to add
      * @return true - The data was successfully added to the job<br/>
      *         false - No running job with the given name could be found
      */
     public boolean appendLogLines(final String id, final List<String> lines) {
         final Date dt = new Date();
         final List<DBObject> logLines = new ArrayList<>();
         for (String line : lines) {
             logLines.add(new LogLine(line, dt).toDbObject());
         }
         final DBObject update = new BasicDBObject().
                 append(MongoOperator.PUSH_ALL.op(), new BasicDBObject(JobInfoProperty.LOG_LINES.val(), logLines)).
                 append(MongoOperator.SET.op(), new BasicDBObject(JobInfoProperty.LAST_MODIFICATION_TIME.val(), dt));
         final WriteResult result = collection.update(createIdQuery(id), update, false, false, getSafeWriteConcern());
         return result.getN() == 1;
     }
 
     /**
      * Removed the running job (flag it as timed out) with the given name if it is timed out
      *
      * @param name The name of the job
      * @param currentDate The current date
      */
     public void removeJobIfTimedOut(final String name, final Date currentDate) {
         final JobInfo job = findByNameAndRunningState(name, RunningState.RUNNING);
         if (job != null && (job.isTimedOut(currentDate) || job.isIdleTimeExceeded(currentDate))) {
             markAsFinished(job.getId(), ResultCode.TIMED_OUT);
         }
     }
 
     public void remove(final String id) {
         if (ObjectId.isValid(id)) {
             collection.remove(createIdQuery(id), getSafeWriteConcern());
         }
     }
 
     /**
      * Counts the number of documents in the repository.
      *
      * @return The number of documents in the repository
      */
     public long count() {
         return collection.count();
     }
 
     /**
      * Flags all running jobs as timed out if the have not be updated within the max execution time
      */
     public int cleanupTimedOutJobs() {
         logger.info("cleanupTimedOutJobs called");
         try {
             return doCleanupTimedOutJobs();
         } catch(Exception e) {
             logger.error("cleanupTimedOutJobs exception occurred",e);
         } finally {
             logger.info("cleanupTimedOutJobs finished");
         }
         return 0;
     }
 
     private int doCleanupTimedOutJobs() {
         final Date currentDate = new Date();
         removeJobIfTimedOut(JOB_NAME_TIMED_OUT_CLEANUP, currentDate);
         int numberOfRemovedJobs = 0;
         if (!hasJob(JOB_NAME_TIMED_OUT_CLEANUP, RunningState.RUNNING)) {
             final String id = create(JOB_NAME_TIMED_OUT_CLEANUP, FIVE_MINUTES, FIVE_MINUTES, 0, RunningState.RUNNING, JobExecutionPriority.CHECK_PRECONDITIONS, null, null);
             if (id != null) { //Job konnte wirklich von diesem Server erzeugt werden.
                 final DBCursor cursor = collection.find(new BasicDBObject(JobInfoProperty.RUNNING_STATE.val(), RunningState.RUNNING.name()));
                 final List<String> removedJobs = new ArrayList<>();
                 for (JobInfo jobInfo : getAll(cursor)) {
                     if (jobInfo.isTimedOut(currentDate) || jobInfo.isIdleTimeExceeded(currentDate)) {
                         if (markAsFinished(jobInfo.getId(), ResultCode.TIMED_OUT)) {
                             removedJobs.add(jobInfo.getName() + " - " + jobInfo.getId());
                             ++numberOfRemovedJobs;
                         } else {
                             logger.error("marking the job " + jobInfo.getName() + ":" + jobInfo.getId() + "as finished was not successful");
                         }
                     }
                 }
                 logger.info("Deleted {} timed-out jobs: {}", numberOfRemovedJobs, removedJobs);
                 addAdditionalData(id, "numberOfRemovedJobs", String.valueOf(numberOfRemovedJobs));
                 if (!removedJobs.isEmpty()) {
                     addAdditionalData(id, "removedJobs", removedJobs.toString());
                 }
                 markAsFinished(id, ResultCode.SUCCESSFUL);
             }
         }
         return numberOfRemovedJobs;
     }
 
     public int cleanupOldJobs() {
         logger.info("cleanupOldJobs called");
         try {
             return doCleanupOldJobs();
         } catch(Exception e) {
             logger.error("cleanupOldJobs exception occurred",e);
         } finally {
             logger.info("cleanupOldJobs finished");
         }
         return 0;
     }
 
     private int doCleanupOldJobs() {
         final Date currentDate = new Date();
         removeJobIfTimedOut(JOB_NAME_CLEANUP, currentDate);
         int numberOfRemovedJobs = 0;
         if (!hasJob(JOB_NAME_CLEANUP, RunningState.RUNNING)) {
             /* register clean up job with max execution time */
             final String id = create(JOB_NAME_CLEANUP, FIVE_MINUTES, FIVE_MINUTES, 0, RunningState.RUNNING, JobExecutionPriority.CHECK_PRECONDITIONS, null, null);
             if (id != null) { //Job konnte wirklich von diesem Server erzeugt werden.
                 final Date beforeDate = new Date(currentDate.getTime() - hoursAfterWhichOldJobsAreDeleted * 60 * 60 * 1000);
                 logger.info("Going to delete not runnnig jobs before {} ...", beforeDate);
                 /* ... good bye ... */
                 numberOfRemovedJobs = cleanupNotRunning(beforeDate);
                 logger.info("Deleted {} not runnnig jobs.", numberOfRemovedJobs);
                 addAdditionalData(id, "numberOfRemovedJobs", String.valueOf(numberOfRemovedJobs));
                 markAsFinished(id, ResultCode.SUCCESSFUL);
             }
         }
         return numberOfRemovedJobs;
     }
 
 
     public int cleanupNotExecutedJobs() {
         logger.info("cleanupNotExecutedJobs called");
         try {
             return doCleanupNotExecutedJobs();
         } catch(Exception e) {
             logger.error("cleanupNotExecutedJobs exception occurred",e);
         } finally {
             logger.info("cleanupNotExecutedJobs finished");
         }
         return 0;
 
     }
 
     private int doCleanupNotExecutedJobs() {
         final Date currentDate = new Date();
         removeJobIfTimedOut(JOB_NAME_CLEANUP_NOT_EXECUTED, currentDate);
         int numberOfRemovedJobs = 0;
         if (!hasJob(JOB_NAME_CLEANUP_NOT_EXECUTED, RunningState.RUNNING)) {
             /* register clean up job with max execution time */
             final String id = create(JOB_NAME_CLEANUP_NOT_EXECUTED, FIVE_MINUTES, FIVE_MINUTES, 0, RunningState.RUNNING, JobExecutionPriority.CHECK_PRECONDITIONS, null, null);
             if (id != null) { //Job konnte wirklich von diesem Server erzeugt werden.
                 final Date beforeDate = new Date(currentDate.getTime() -  hoursAfterWhichNotExecutedJobsAreDeleted * 60 * 60 * 1000);
                 logger.info("Going to delete not executed jobs before {} ...", beforeDate);
                 /* ... good bye ... */
                 numberOfRemovedJobs = cleanupNotExecuted(beforeDate);
                 logger.info("Deleted {} not executed jobs.", numberOfRemovedJobs);
                 addAdditionalData(id, "numberOfRemovedJobs", String.valueOf(numberOfRemovedJobs));
                 markAsFinished(id, ResultCode.SUCCESSFUL);
             }
         }
         return numberOfRemovedJobs;
     }
 
     // ~~
 
     protected int cleanupNotRunning(Date clearJobsBefore) {
         final WriteResult result = collection.remove(new BasicDBObject().
                 append(JobInfoProperty.CREATION_TIME.val(), new BasicDBObject(MongoOperator.LT.op(), clearJobsBefore)).
                 append(JobInfoProperty.RUNNING_STATE.val(), new BasicDBObject(MongoOperator.NE.op(), RunningState.RUNNING.name())),
                 getSafeWriteConcern());
         return result.getN();
     }
 
     protected int cleanupNotExecuted(Date clearJobsBefore) {
         final WriteResult result = collection.remove(new BasicDBObject().
                 append(JobInfoProperty.CREATION_TIME.val(), new BasicDBObject(MongoOperator.LT.op(), clearJobsBefore)).
                 append(JobInfoProperty.RESULT_STATE.val(), ResultCode.NOT_EXECUTED.name()).
                append(JobInfoProperty.RUNNING_STATE.val(), RunningState.FINISHED.name()),
                 getSafeWriteConcern());
         return result.getN();
     }
 
     protected void prepareCollection() {
         collection.ensureIndex(new BasicDBObject(JobInfoProperty.NAME.val(), 1));
         collection.ensureIndex(new BasicDBObject(JobInfoProperty.LAST_MODIFICATION_TIME.val(), 1));
         collection.ensureIndex(new BasicDBObject().
                 append(JobInfoProperty.RUNNING_STATE.val(), 1).append(JobInfoProperty.CREATION_TIME.val(), 1), "runningState_creationTime");
         collection.ensureIndex(new BasicDBObject().
                 append(JobInfoProperty.NAME.val(), 1).append(JobInfoProperty.CREATION_TIME.val(), 1), "name_creationTime");
         collection.ensureIndex(new BasicDBObject().
                 append(JobInfoProperty.NAME.val(), 1).append(JobInfoProperty.RUNNING_STATE.val(), 1), "name_state", true);
     }
 
     protected JobInfo fromDbObject(final DBObject dbObject) {
         if (dbObject == null) {
             return null;
         }
         return new JobInfo(dbObject);
     }
 
     private DBObject createIdQuery(String id) {
         return new BasicDBObject(JobInfoProperty.ID.val(), new ObjectId(id));
     }
 
     /**
      * Marks a running job with the given name as finished.
      *
      * @param query The query of to find object to update
      * @param resultCode The result state of the job
      * @param resultMessage An optional error message
      * @return true - The job was marked as requested<br/>
      *         false - No running job with the given name could be found
      */
     private boolean markAsFinished(final DBObject query, final ResultCode resultCode, final String resultMessage) {
         final Date dt = new Date();
         final BasicDBObjectBuilder set = new BasicDBObjectBuilder().
                 append(JobInfoProperty.RUNNING_STATE.val(), createFinishedRunningState()).
                 append(JobInfoProperty.LAST_MODIFICATION_TIME.val(), dt).
                 append(JobInfoProperty.FINISH_TIME.val(), dt).
                 append(JobInfoProperty.RESULT_STATE.val(), resultCode.name());
         if (resultMessage != null) {
             set.append(JobInfoProperty.RESULT_MESSAGE.val(), resultMessage);
         }
         final DBObject update = new BasicDBObject().append(MongoOperator.SET.op(), set.get());
         final WriteResult result = collection.update(query, update, false, false, getSafeWriteConcern());
         return result.getN() == 1;
     }
 
     private BasicDBObject createFindByNameAndRunningStateQuery(final String name, final String state) {
         return new BasicDBObject().append(JobInfoProperty.NAME.val(), name).
                 append(JobInfoProperty.RUNNING_STATE.val(), state);
     }
 
     private DBObject createFindByNameAndResultStateQuery(final String name, final Set<ResultCode> states) {
         final List<String> resultStates = toStringList(states);
         return new BasicDBObject().append(JobInfoProperty.NAME.val(), name).
                 append(JobInfoProperty.RESULT_STATE.val(), new BasicDBObject(MongoOperator.IN.op(), resultStates));
     }
 
     private String createFinishedRunningState() {
         return RunningState.FINISHED + "_" + UUID.randomUUID().toString();
     }
 
     private String exceptionToMessage(Throwable t) {
         final StringWriter sw = new StringWriter();
         t.printStackTrace(new PrintWriter(sw));
         return "Problem: " + t.getMessage() + ", Stack-Trace: " + sw.toString();
     }
 
     private List<String> toStringList(Collection<? extends Enum<?>> enumSet) {
         final List<String> strings = new ArrayList<>();
         if (enumSet != null) {
             for (Enum<?> e : enumSet) {
                 strings.add(e.name());
             }
         }
         return strings;
     }
 
 }
