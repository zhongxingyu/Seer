 package org.imirsel.nema.flowservice;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.logging.Logger;
 
 import javax.annotation.PostConstruct;
 
 import net.jcip.annotations.GuardedBy;
 import net.jcip.annotations.ThreadSafe;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.imirsel.meandre.client.ExecResponse;
 import org.imirsel.nema.dao.DaoFactory;
 import org.imirsel.nema.dao.JobDao;
 import org.imirsel.nema.flowservice.config.FlowServiceConfig;
 import org.imirsel.nema.flowservice.config.MeandreServerProxyConfig;
 import org.imirsel.nema.flowservice.config.MeandreServerProxyStatus;
 import org.imirsel.nema.model.Job;
 import org.imirsel.nema.model.Job.JobStatus;
 import org.springframework.dao.DataAccessException;
 
 /**
  * Receives {@link Job} execution requests and distributes them to a set of
  * Meandre servers.
  * 
  * @author shirk
  * @since 0.4.0
  */
 @ThreadSafe
 public class MeandreJobScheduler implements JobScheduler {
 
    private static final int POLL_PERIOD = 5;
 
    private static final Logger logger = Logger
          .getLogger(MeandreJobScheduler.class.getName());
 
    private static final int MAX_EXECUTION_TRIES = 5;
 
    //~ Instance fields ---------------------------------------------------------
 
    /** The configuration for this job scheduler. */
    private FlowServiceConfig config;
 
    /** Used to get references to Meandre server proxies. */
    private MeandreServerProxyFactory serverFactory;
    
    /** All jobs are placed on this queue as they come into the scheduler. */
    @GuardedBy("queueLock")
    private final Queue<Job> jobQueue = new LinkedList<Job>();
 
    /** Decides which server gets the next job. */
    @GuardedBy("workersLock")
    private MeandreLoadBalancer loadBalancer;
 
    /** Lock for the job queue. */
    private final Lock queueLock = new ReentrantLock();
 
    /** Periodically checks for jobs in the queue and runs them. */
    @SuppressWarnings("unused")
    private ScheduledFuture<?> runJobsFuture;
 
    /** Meandre servers for processing jobs. */
    @GuardedBy("workersLock")
   private Set<MeandreServerProxy> workers = new HashSet<MeandreServerProxy>();
 
    private DaoFactory daoFactory;
 
    /**
     * Lock for both the set of workers and the load balancer which contains
     * references to the workers.
     */
    private final Lock workersLock = new ReentrantLock();
 
 
    //~ Constructors ------------------------------------------------------------
 
    /**
     * Creates a new instance.
     */
    public MeandreJobScheduler() {
    }
 
    //~ Methods -----------------------------------------------------------------
 
    /**
     * Initializes this instance.
     */
    @PostConstruct
    public void init() {
       assert config != null : "No configuration was provided to the job scheduler.";
       Set<MeandreServerProxyConfig> workerConfigs = config.getWorkerConfigs();
       for(MeandreServerProxyConfig workerConfig : workerConfigs) {
          MeandreServerProxy server = 
             serverFactory.getServerProxyInstance(workerConfig);
         workers.add(server);
          loadBalancer.addServer(server);
       }
       ScheduledExecutorService executor = Executors
             .newSingleThreadScheduledExecutor();
 
       runJobsFuture = executor.scheduleWithFixedDelay(new RunQueuedJobs(), 15,
       POLL_PERIOD, TimeUnit.SECONDS);
    }
 
    /**
     * @see org.imirsel.nema.flowservice.JobScheduler#abortJob(org.imirsel.nema.model.Job)
     */
    public void abortJob(Job job) {
       MeandreServerProxy executingServer = findExecutingServer(job);
       try {
          executingServer.abortJob(job);
       } catch (MeandreServerException e) {
          // TODO Perhaps do something more intelligent here
          throw new RuntimeException(e);
       }
    }
 
    /**
     * Find the server that is executing the given {@link Job}.
     * 
     * @param job The {@link Job} to find the executing server instance for.
     * @return The {@link MeandreServerProxy} that is currently executing the
     *         job, or <code>null</code> if no known server is executing the job.
     */
    private MeandreServerProxy findExecutingServer(Job job) {
       workersLock.lock();
       try {
          for (MeandreServerProxy server : workers) {
             if (job.getHost().equals(
                   server.getConfig().getHost())
                   && job.getPort().equals(
                         server.getConfig().getPort())) {
                return server;
             }
          }
       } finally {
          workersLock.unlock();
       }
       return null;
    }
 
    /**
     * @see org.imirsel.nema.flowservice.JobScheduler#scheduleJob(org.imirsel.nema.model.Job)
     */
    public void scheduleJob(Job job) {
       queueLock.lock();
       try {
          jobQueue.offer(job);
       } finally {
          queueLock.unlock();
       }
    }
 
    /**
     * @see org.imirsel.nema.flowservice.JobScheduler#getScheduledJobs()
     */
    public List<Job> getScheduledJobs() {
       queueLock.lock();
       try {
          List<Job> scheduledJobs = new ArrayList<Job>();
          scheduledJobs.addAll(jobQueue);
          return scheduledJobs;
       } finally {
          queueLock.unlock();
       }
    }
    
    /**
     * Attempt to run any queued jobs.
     */
    public void runJobs() {
       Session session = null;
       JobDao jobDao = daoFactory.getJobDao();
       try {
          queueLock.lock();
          workersLock.lock();
          if (jobQueue.size() < 1) {
             logger.fine("No queued jobs.");
             return;
          }
          
          session = jobDao.getSessionFactory().openSession();
          jobDao.startManagedSession(session);
          
          while (!jobQueue.isEmpty()) {
             logger.fine("Found " + jobQueue.size() + " queued jobs.");
             MeandreServerProxy server = loadBalancer.nextAvailableServer();
             if (server == null) {
                logger.info("All servers are busy. Will try again in "
                      + POLL_PERIOD + " seconds.");
                return;
             }
             logger.fine("Server " + server + " is available for processing.");
 
             Job job = jobQueue.peek();
 
             job.incrementNumTries();
             job.setJobStatus(JobStatus.SUBMITTED);
             job.setSubmitTimestamp(new Date());
         
             logger.info("Preparing to update job " + job.getId()
                   + " as submitted.");
 
             Transaction transaction = session.beginTransaction();
             transaction.begin();
             try {
                jobDao.makePersistent(job);
                transaction.commit();
                logger.info("Job " + job.getId() + " updated.");
             } catch (HibernateException e) {
                logger.warning("Data access exception: " + e.getMessage());
                rollback(transaction);
                return;
             } catch (DataAccessException e) {
                logger.warning("Data access exception: " + e.getMessage());
                rollback(transaction);
                return;
             } catch (Exception e) {
                logger.warning(e.getMessage());
                rollback(transaction);
                return;
             }
 
             try {
                logger.info("Attempting to contact server " + server
                      + " to execute job.");
 
                ExecResponse response = server.executeJob(job);
                logger.info("Execution response received.");
                
                // If the executeJob() method above succeeded, the Meandre
                // server will have (most likely) changed the job status to
                // "started". If the status changes, the NEMA probe running
                // on the Meandre server will have written the new status to
                // the same database the flow service uses. Therefore, we
                // want to refresh the state of the job here to pick up the
                // status change. Otherwise, the old status (submitted) will
                // be rewritten to the database, and the "started" state
                // will be lost.
                session.refresh(job);
                
                logger.info("Attempting to record job execution response.");
                job.setHost(server.getConfig().getHost());
                job.setPort(server.getConfig().getPort());
                job.setExecPort(response.getPort());
                job.setExecutionInstanceId(response.getUri());
                
                transaction = session.beginTransaction();
                transaction.begin();
                try {
                   jobDao.makePersistent(job);
                   transaction.commit();
                   logger.fine("Job execution response recorded in database.");
                   jobQueue.remove();
                } catch (HibernateException e) {
                   logger.warning("Data access exception: " + e.getMessage());
                   rollback(transaction);
                   return;
                } catch (DataAccessException e) {
                   logger.warning("Data access exception: " + e.getMessage());
                   rollback(transaction);
                   return;
                } catch (Exception e) {
                   logger.warning(e.getMessage());
                   rollback(transaction);
                   return;
                }
             } catch (MeandreServerException serverException) {
                logger.warning(serverException.getMessage());
                job.setSubmitTimestamp(null);
                job.setJobStatus(JobStatus.SCHEDULED);
 
                if (job.getNumTries() == MAX_EXECUTION_TRIES) {
                   logger.info("Unsuccessfully tried " + MAX_EXECUTION_TRIES
                         + " times to execute job " + job.getId()
                         + ". Will mark job as failed.");
                   job.setJobStatus(JobStatus.FAILED);
                   job.setEndTimestamp(new Date());
                   job.setUpdateTimestamp(new Date());
                   jobQueue.remove();
                }
 
                transaction = session.beginTransaction();
                transaction.begin();
                try {
                   jobDao.makePersistent(job);
                   transaction.commit();
                } catch (HibernateException e) {
                   logger.warning("Data access exception: " + e.getMessage());
                   rollback(transaction);
                   return;
                } catch (DataAccessException e) {
                   logger.warning("Data access exception: " + e.getMessage());
                   rollback(transaction);
                   return;
                } catch (Exception e) {
                   logger.warning(e.getMessage());
                   rollback(transaction);
                   return;
                }
             }
          }
       } catch (Exception e) {
          e.printStackTrace();
       } finally {
          workersLock.unlock();
          queueLock.unlock();
          if (session != null) {
             try {
                jobDao.endManagedSession();
                session.close();
             } catch (HibernateException e) {
                logger.warning(e.getMessage());
             }
          }
       }
    }
 
    /**
     * Rollback the specified transaction.
     * 
     * @param transaction The transaction to rollback.
     */
    private void rollback(Transaction transaction) {
       try {
          logger.fine("Rolling back.");
          transaction.rollback();
       } catch (HibernateException e2) {
          logger.warning(e2.getMessage());
       }
    }
 
    /**
     * Add a server to the job scheduler.
     * 
     * @param server Server to add.
     */
    public void addServer(MeandreServerProxy server) {
       workersLock.lock();
       try {
          workers.add(server);
          loadBalancer.addServer(server);
       } finally {
          workersLock.unlock();
       }
    }
 
    /**
     * Remove a server from the job scheduler.
     * 
     * @param server Server to remove.
     */
    public void removeServer(MeandreServerProxy server) {
       workersLock.lock();
       try {
          workers.remove(server);
          loadBalancer.removeServer(server);
       } finally {
          workersLock.unlock();
       }
    }
 
    /**
     * Return the number of servers the scheduler is managing.
     * 
     * @return Number of servers the scheduler is managing.
     */
    public int numServers() {
       workersLock.lock();
       try {
          return workers.size();
       } finally {
          workersLock.unlock();
       }
    }
 
 
    /**
     * @see JobScheduler#getWorkerStatus()
     */
    public Map<MeandreServerProxyConfig,MeandreServerProxyStatus> getWorkerStatus() {
       Map<MeandreServerProxyConfig,MeandreServerProxyStatus> statusMap =
          new HashMap<MeandreServerProxyConfig,MeandreServerProxyStatus>();
       workersLock.lock();
       try {
          for (MeandreServerProxy server : workers) {
             statusMap.put(server.getConfig(), server.getStatus());
          }
       } finally {
          workersLock.unlock();
       }
       return statusMap;
    }
    
    /**
     * Return the {@link MeandreLoadBalancer} currently in use.
     * 
     * @return The load balancer currently in use.
     */
    public MeandreLoadBalancer getLoadBalancer() {
       return loadBalancer;
    }
 
    /**
     * Set the {@link MeandreLoadBalancer} to use.
     * 
     * @param loadBalancer The {@link MeandreLoadBalancer} to use.
     */
    public void setLoadBalancer(MeandreLoadBalancer loadBalancer) {
       this.loadBalancer = loadBalancer;
    }
 
    /**
     * Return the {@link FlowServiceConfig} currently in use.
     * 
     * @return The {@link FlowServiceConfig} currently in use.
     */
    public FlowServiceConfig getFlowServiceConfig() {
       return config;
    }
 
    /**
     * Set the {@link FlowServiceConfig} to use.
     * 
     * @param config The {@link FlowServiceConfig} to use.
     */
    public void setFlowServiceConfig(FlowServiceConfig config) {
       this.config = config;
    }
 
    /**
     * Set the {@link DaoFactory} to use.
     * 
     * @param daoFactory The {@link DaoFactory} implementation to use.
     */
    public void setDaoFactory(DaoFactory daoFactory) {
       this.daoFactory = daoFactory;
    }
 
    /**
     * Return the {@link DaoFactory} implementation currently in use.
     * 
     * @return {@link DaoFactory} implementation currently in use.
     */
    public DaoFactory getDaoFactory() {
       return daoFactory;
    }
 
    /**
     * Return the {@link MeandreServerProxyFactory} currently in use.
     * 
     * @return {@link MeandreServerProxyFactory} currently in use.
     */
    public MeandreServerProxyFactory getMeandreServerProxyFactory() {
       return serverFactory;
    }
 
    /**
     * Set the {@link MeandreServerProxyFactory} to use.
     * 
     * @param daoFactory The {@link MeandreServerProxyFactory} implementation to use.
     */
    public void setMeandreServerProxyFactory(MeandreServerProxyFactory serverFactory) {
       this.serverFactory = serverFactory;
    }
    
    //~ Inner Classes -----------------------------------------------------------
 
    private class RunQueuedJobs implements Runnable {
       public void run() {
          logger.fine("Checking for queued jobs...");
          runJobs();
       }
    }
 }
