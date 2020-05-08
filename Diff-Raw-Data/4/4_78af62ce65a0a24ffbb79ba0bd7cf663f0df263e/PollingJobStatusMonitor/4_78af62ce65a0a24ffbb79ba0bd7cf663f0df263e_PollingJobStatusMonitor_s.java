 package org.imirsel.nema.flowservice.monitor;
 
 import net.jcip.annotations.GuardedBy;
 import net.jcip.annotations.ThreadSafe;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 
 import org.imirsel.nema.dao.DaoFactory;
 import org.imirsel.nema.dao.JobDao;
 import org.imirsel.nema.model.Job;
 
 import org.springframework.dao.DataAccessException;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.logging.Logger;
 
 
 /**
  * A job monitor that polls the database for status updates.
  *
  * @author shirk
  * @since 1.0
  * @proposedRating red TODO: author
  * @acceptedRating red TODO: reviewer
  */
 @ThreadSafe
 public class PollingJobStatusMonitor implements JobStatusMonitor {
 
    //~ Static fields/initializers ----------------------------------------------
 
    private static final int POLL_PERIOD = 10;
 
    /** Logger for this class. */
    static private final Logger logger = Logger.getLogger(
       PollingJobStatusMonitor.class.getName());
 
    //~ Instance fields ---------------------------------------------------------
 
    /** Used to access jobs in the data store. */
    private DaoFactory daoFactory;
 
    /**
     * Maps jobs that are being monitored to objects that are waiting to be
     * notified of status changes.
     */
    @GuardedBy("jobsLock")
    private final Map<Job, Set<JobStatusUpdateHandler>> jobs =
       new HashMap<Job, Set<JobStatusUpdateHandler>>();
 
    /** Concurrency lock for the jobs list. */
    private final Lock jobsLock = new ReentrantLock();
 
    /** Periodically checks for jobs status changes. */
    @SuppressWarnings("unused")
    private ScheduledFuture<?> updateDetectorFuture;
 
    //~ Instance initializers ---------------------------------------------------
 
    {
       ScheduledExecutorService executor = Executors
          .newSingleThreadScheduledExecutor();
 
       updateDetectorFuture = executor.scheduleWithFixedDelay(
          new StatusUpdateDetector(), 5, POLL_PERIOD, TimeUnit.SECONDS);
    }
 
    //~ Constructors ------------------------------------------------------------
 
    /**
     * Create a new instance.
     */
    public PollingJobStatusMonitor() {
    }
 
    //~ Methods -----------------------------------------------------------------
 
    /**
     * @see JobStatusMonitor#start(Job, JobStatusUpdateHandler)
     */
    public void start(Job job, JobStatusUpdateHandler updateHandler) {
       logger.fine(
          "Starting to monitor job " + job.getId() + " for " + updateHandler +
          ".");
       jobsLock.lock();
       try {
          if (jobs.containsKey(job)) {
             jobs.get(job)
             .add(updateHandler);
             logger.fine(
                "Adding a handler for job " + job.getId() +
                " to an existing handler set.");
          } else {
             Set<JobStatusUpdateHandler> handlerSet =
                new HashSet<JobStatusUpdateHandler>();
             handlerSet.add(updateHandler);
             logger.fine("Created a handler set for job " + job.getId() + ".");
             jobs.put(job.clone(), handlerSet);
          }
       } finally {
          jobsLock.unlock();
       }
    }
 
    /**
     * @see JobStatusMonitor#stop(Job, JobStatusUpdateHandler)
     */
    public void stop(Job job, JobStatusUpdateHandler updateHandler) {
       logger.fine(
          "Stopping the monitoring of job " + job.getId() + " for " +
          updateHandler + ".");
       jobsLock.lock();
       try {
          Set<JobStatusUpdateHandler> handlers = jobs.get(job);
          if (handlers != null) {
             handlers.remove(updateHandler);
             if (handlers.isEmpty()) {
                jobs.remove(job);
             }
          }
       } finally {
          jobsLock.unlock();
       }
    }
 
    /**
     * Return the {@link DaoFactory} currently in use.
     *
     * @return The {@link DaoFactory} currently in use.
     */
    public DaoFactory getDaoFactory() { return daoFactory; }
 
    /**
     * Set the {@link DaoFactory} to use.
     *
     * @param daoFactory jobDao The {@link DaoFactory} to use.
     */
    public void setDaoFactory(DaoFactory daoFactory) {
       this.daoFactory = daoFactory;
    }
 
    //~ Inner Classes -----------------------------------------------------------
    
    /**
     * Runs each time the monitor wakes up to get updated job statuses.
     *
     * @author TODO: author
     * @since TODO: version
     * @proposedRating red TODO: author
     * @acceptedRating red TODO: reviewer
     */
    private class StatusUpdateDetector implements Runnable {
       public void run() {
          jobsLock.lock();
 
          Session session = null;
          JobDao jobDao = null;
          try {
             logger.fine(
                      "Checking for job status updates...");
             if(jobs.size() > 0) {
                logger.fine("Found " + jobs.size() + " jobs with possible status updates.");
             } else {
                logger.fine("No jobs are currently being monitored.");
             }
              
             Iterator<Job> jobIterator = jobs.keySet()
                .iterator();
 
             jobDao = daoFactory.getJobDao();
 
             session = jobDao.getSessionFactory()
                .openSession();
             jobDao.startManagedSession(session);
 
             while (jobIterator.hasNext()) {
                Job cachedJob = jobIterator.next();
                Job persistedJob = null;
                try {
             	  logger.fine("Checking status of job " + cachedJob.getId() + ".");
                   persistedJob = jobDao.findById(cachedJob.getId(), false);
                   logger.fine("Job status: " + Job.JobStatus.toJobStatus(persistedJob.getStatusCode()));
                } catch (HibernateException e) {
                   logger.warning("Data access exception: " + e.getMessage());
                } catch (DataAccessException e) {
                   logger.warning("Data access exception: " + e.getMessage());
                } catch (Exception e) {
             	   e.printStackTrace();
                   logger.warning(e.getMessage());
                }
 
                if (persistedJob == null) {
                   return;
                }
 
                Integer oldStatus = cachedJob.getStatusCode();
                Integer newStatus = persistedJob.getStatusCode();
                if (!oldStatus.equals(newStatus)) {
                   try {
                      logger.fine(
                         "Status update for job " + cachedJob.getId() +
                         " occurred: was " + cachedJob.getJobStatus() +
                         ", now " + persistedJob.getJobStatus() + ".");
                      cachedJob.setStatusCode(persistedJob.getStatusCode());
                      cachedJob.setUpdateTimestamp(
                         persistedJob.getUpdateTimestamp());
                      cachedJob.setSubmitTimestamp(
                         persistedJob.getSubmitTimestamp());
                      cachedJob.setStartTimestamp(
                         persistedJob.getStartTimestamp());
                      cachedJob.setEndTimestamp(persistedJob.getEndTimestamp());
 
                      // Invoke the update handlers for this job
                      Set<JobStatusUpdateHandler> handlers = jobs.get(cachedJob);
                      if (handlers == null) {
                         logger.fine(
                            "No handlers registered for job " +
                            cachedJob.getId() + ".");
                         return;
                      } else {
                         for (JobStatusUpdateHandler handler : handlers) {
                            logger.fine(
                               "Dispatching a job status update for job " +
                               cachedJob.getId() + " to handler " + handler +
                               ".");
 
                            // anything can go wrong in a handler, so
                            // catch exceptions that are thrown
                            try {
                               handler.jobStatusUpdate(cachedJob);
                            } catch (Exception e) {
                         	   e.printStackTrace();
                               logger.warning(e.getMessage());
                            }
                         }
                      }
 
                      // Stop monitoring this job if it is finished
                      if (cachedJob.isDone()) {
                         logger.fine("Job " + cachedJob.getId() +
                            " has ended. Removing it from the status monitor.");
                         jobIterator.remove();
                      }
                   } catch (Exception e) {
                      e.printStackTrace();
                   } // end try-catch
                } // end if
             } // end while
          } catch (Exception e){
         	 e.printStackTrace();
          }finally {
             jobsLock.unlock();
             jobDao.endManagedSession();
            session.close();
          } // end try-finally
       } // end method run
    }
    
 }
