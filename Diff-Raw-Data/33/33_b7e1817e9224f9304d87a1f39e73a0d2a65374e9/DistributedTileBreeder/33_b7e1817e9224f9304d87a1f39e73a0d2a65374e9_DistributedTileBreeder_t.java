 package org.opengeo.gwcdistributed.seed;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geowebcache.GeoWebCacheException;
 import org.geowebcache.layer.TileLayer;
 import org.geowebcache.seed.GWCTask;
 import org.geowebcache.seed.GWCTask.STATE;
 import org.geowebcache.seed.GWCTask.TYPE;
 import org.geowebcache.seed.Job;
 import org.geowebcache.seed.JobStatus;
 import org.geowebcache.seed.SeedJob;
 import org.geowebcache.seed.SeedRequest;
 import org.geowebcache.seed.SeedTask;
 import org.geowebcache.seed.SeederThreadPoolExecutor;
 import org.geowebcache.seed.TaskStatus;
 import org.geowebcache.seed.TileBreeder;
 import org.geowebcache.seed.TruncateJob;
 import org.geowebcache.seed.TruncateTask;
 import org.geowebcache.storage.StorageBroker;
 import org.geowebcache.storage.TileRange;
 import org.geowebcache.storage.TileRangeIterator;
 import org.geowebcache.util.GWCVars;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanInitializationException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 
 import com.hazelcast.core.AtomicNumber;
 import com.hazelcast.core.HazelcastInstance;
 import com.hazelcast.core.ISet;
 import com.hazelcast.core.IdGenerator;
 import com.hazelcast.core.ItemEvent;
 import com.hazelcast.core.ItemListener;
 import com.hazelcast.core.Member;
 import com.hazelcast.core.MultiTask;
 
 import static com.google.common.base.Preconditions.*;
 
 public class DistributedTileBreeder extends TileBreeder implements ApplicationContextAware {
 
	@Override
	public Job createJob(TileRange tr, TileLayer tl, TYPE type,
			int threadCount, boolean filterUpdate) throws GeoWebCacheException {
		Job j = super.createJob(tr, tl, type, threadCount, filterUpdate);
		while(j.getState()!=STATE.READY){}; // FIXME
		return j;
	}
	
 	public DistributedTileBreeder(HazelcastInstance hz) {
 		super();
 		this.hz = hz;
 		this.currentTaskId = hz.getIdGenerator("taskIdGenerator");
 		this.currentJobId = hz.getIdGenerator("jobIdGenerator");
 		this.jobCloud = hz.getSet("jobCloud");
 		jobCloud.addItemListener(new JobAddedListener(), true);
 	}
 	
 	private final ISet<DistributedJob> jobCloud;
 
 	private final HazelcastInstance hz;
     private static final String GWC_SEED_ABORT_LIMIT = "GWC_SEED_ABORT_LIMIT";
 
     private static final String GWC_SEED_RETRY_WAIT = "GWC_SEED_RETRY_WAIT";
 
     private static final String GWC_SEED_RETRY_COUNT = "GWC_SEED_RETRY_COUNT";
 
     private static Log log = LogFactory.getLog(DistributedTileBreeder.class);
 
     /**
      * How many retries per failed tile. 0 = don't retry, 1 = retry once if failed, etc
      */
     private int tileFailureRetryCount = 0;
 
     /**
      * How much (in milliseconds) to wait before trying again a failed tile
      */
     private long tileFailureRetryWaitTime = 100;
 
     /**
      * How many failures to tolerate before aborting the seed task. Value is shared between all the
      * threads of the same run.
      */
     private long totalFailuresBeforeAborting = 1000;
 
     private Map<Long, SubmittedTask> currentPool = new TreeMap<Long, SubmittedTask>();
 
     private final IdGenerator currentTaskId;
     private final IdGenerator currentJobId;
     
     private StorageBroker broker;
     private SeederThreadPoolExecutor executor;
 
     //private final ReadWriteLock lock = new ReentrantReadWriteLock();
 
     private static class SubmittedTask {
         public final GWCTask task;
 
         public final Future<GWCTask> future;
 
         public SubmittedTask(final GWCTask task, final Future<GWCTask> future) {
             this.task = task;
             this.future = future;
         }
     }
     
     /**
      * Callback for new jobs being added to the cluster
      */
 	private class JobAddedListener implements ItemListener<DistributedJob> {
 
 		
 		public void itemAdded(ItemEvent<DistributedJob> item) {
 			DistributedJob j = item.getItem();
 			log.info(String.format("Job %d added to cluster, adding to breeder on node %s\n",j.getId(), hz.getName()));

				j.createTasks();
				jobs.put(j.getId(), j);
 		}
 
 		public void itemRemoved(ItemEvent<DistributedJob> item) {
 			// TODO Auto-generated method stub
 			
 		}
 		
 	}
 	
 	/**
      * Initializes the seed task failure control variables either with the provided environment
      * variable values or their defaults.
      * 
      * @see {@link ThreadedTileBreeder class' javadocs} for more information
      * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
      */
     public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
         String retryCount = GWCVars.findEnvVar(applicationContext, GWC_SEED_RETRY_COUNT);
         String retryWait = GWCVars.findEnvVar(applicationContext, GWC_SEED_RETRY_WAIT);
         String abortLimit = GWCVars.findEnvVar(applicationContext, GWC_SEED_ABORT_LIMIT);
 
         tileFailureRetryCount = (int) toLong(GWC_SEED_RETRY_COUNT, retryCount, 0);
         tileFailureRetryWaitTime = toLong(GWC_SEED_RETRY_WAIT, retryWait, 100);
         totalFailuresBeforeAborting = toLong(GWC_SEED_ABORT_LIMIT, abortLimit, 1000);
 
         checkPositive(tileFailureRetryCount, GWC_SEED_RETRY_COUNT);
         checkPositive(tileFailureRetryWaitTime, GWC_SEED_RETRY_WAIT);
         checkPositive(totalFailuresBeforeAborting, GWC_SEED_ABORT_LIMIT);
     }
     @SuppressWarnings("serial")
     private void checkPositive(long value, String variable) {
         if (value < 0) {
             throw new BeanInitializationException(
                     "Invalid configuration value for environment variable " + variable
                             + ". It should be a positive integer.") {
             };
         }
     }
     private long toLong(String varName, String paramVal, long defaultVal) {
         if (paramVal == null) {
             return defaultVal;
         }
         try {
             return Long.valueOf(paramVal);
         } catch (NumberFormatException e) {
             log.warn("Invalid environment parameter for " + varName + ": '" + paramVal
                     + "'. Using default value: " + defaultVal);
         }
         return defaultVal;
     }
 
 	@Override
 	public void seed(String layerName, SeedRequest sr)
 			throws GeoWebCacheException {
 
         TileLayer tl = findTileLayer(layerName);
 
         TileRange tr = createTileRange(sr, tl);
 
         Job job = createJob(tr, tl, sr.getType(), sr.getThreadCount(),
                 sr.getFilterUpdate());
 
         dispatchJob(job);
 	}
 
 	@Override
 	public void dispatchJob(Job job) {
 		log.info(String.format("Dispatching Job %d to cluster.  Originating with node %s;", job.getId(), hz.getName()));
 		final MultiTask<Object> mtask = executeCallable(new DoDispatch((DistributedJob)job));
 		try {
 			mtask.get();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	void localDispatchJob(Job job) {
 		log.debug(String.format("Dispatching Job %d on node %s", job.getId(), hz.getName()));
 		for(GWCTask task: ((DistributedJob)job).getTasks()){
 			try {
 				log.trace(String.format("Starting task %d for job %d on node %s", task.getTaskId(), job.getId(), hz.getName()));
                 final Long taskId = task.getTaskId();
                 Future<GWCTask> future = executor.submit(wrapTask(task));
                 this.currentPool.put(taskId, new SubmittedTask(task, future));
 			} catch (Exception e) {
 				log.error(String.format("Exception dispatching Job %d Task %d on node %s \n", job.getId(), task.getTaskId(), hz.getName()));
 			}
 		}
 	}
 
 	@Override
 	public long[][] getStatusList() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public long[][] getStatusList(String layerName) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Collection<JobStatus> getJobStatusList() {
 		Collection<JobStatus> statusList = new LinkedList<JobStatus>();
 		for(Job job: jobs.values()){
 			statusList.add(job.getStatus());
 		}
 		return statusList;
 	}
 
 	@Override
 	public Collection<JobStatus> getJobStatusList(String layerName) {
 		Collection<JobStatus> statusList = new LinkedList<JobStatus>();
 		for(Job job: jobs.values()){
 			if (job.getLayer().getName()==layerName){
 				statusList.add(job.getStatus());
 			}
 		}
 		return statusList;
 	}
 
 	@Override
 	public Collection<TaskStatus> getTaskStatusList() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Collection<TaskStatus> getTaskStatusList(String layerName) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void setThreadPoolExecutor(SeederThreadPoolExecutor stpe) {
 		checkState(executor==null);
 		checkNotNull(stpe);
 		executor = stpe;
 	}
 
 	@Override
 	public void setStorageBroker(StorageBroker sb) {
 		checkState(broker==null);
 		checkNotNull(sb);
 		this.broker=sb;
 	}
 
 	@Override
 	public StorageBroker getStorageBroker() {
 		checkState(broker!=null);
 		return broker;
 	}
 
 	@Override
 	public Iterator<GWCTask> getRunningTasks() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Iterator<GWCTask> getRunningAndPendingTasks() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Iterator<GWCTask> getPendingTasks() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public boolean terminateGWCTask(long id) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public Iterable<TileLayer> getLayers() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	protected long getNextTaskId() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	/**
 	 * Get the HazelCast instance for the node this Breeder is running on. 
 	 * @return
 	 */
 	public HazelcastInstance getHz() {
 		return hz;
 	}
 
 	@Override
 	protected Callable<GWCTask> wrapTask(GWCTask task) {
 		return super.wrapTask(task);
 	}
 	@Override
 	protected void setTaskState(GWCTask task, STATE state) {
 		super.setTaskState(task, state);
 	}
 	@Override
 	protected SeedTask createSeedTask(SeedJob job) {
 		return super.createSeedTask(job);
 	}
 	@Override
 	protected TruncateTask createTruncateTask(TruncateJob job) {
 		return super.createTruncateTask(job);
 	}
 
 	static final String ITERATOR_STEP_KEY = "iteratorStep";
 	
 	@Override
 	protected SeedJob createSeedJob(int threadCount, boolean reseed,
 			TileRangeIterator trIter, TileLayer tl, boolean filterUpdate) {
 		final long id = currentJobId.newId();
 		final AtomicNumber step = hz.getAtomicNumber(DistributedJob.getKey(ITERATOR_STEP_KEY, id));
 		DistributedTileRangeIterator dTrIter = new DistributedTileRangeIterator(trIter, step);
 		DistributedSeedJob job = new DistributedSeedJob(id, this, tl, threadCount, dTrIter, filterUpdate);
 		log.trace(String.format("Seed Job %d created on node %s, adding to cluster.",job.getId(), hz.getName()));
 		jobCloud.add(job);
 		return job;
 	}
 	@Override
 	protected TruncateJob createTruncateJob(TileRangeIterator trIter,
 			TileLayer tl, boolean filterUpdate) {
 		final long id = currentJobId.newId();
 		final AtomicNumber step = hz.getAtomicNumber(DistributedJob.getKey(ITERATOR_STEP_KEY, id));
 		DistributedTileRangeIterator dTrIter = new DistributedTileRangeIterator(trIter, step);
 		DistributedTruncateJob job = new DistributedTruncateJob(id, this, tl, dTrIter, filterUpdate);
 		log.trace(String.format("Truncate Job %d created on node %s, adding to cluster.",job.getId(), hz.getName()));
 		jobCloud.add(job);
 		return job;
 	}
 
 	/**
 	 * Dispatch the given callable to each of the given member nodes
 	 * @param callable
 	 * @param members
 	 * @return
 	 */
 	<T> MultiTask<T> executeCallable(Callable<T> callable, Set<Member> members){
 		final MultiTask<T> mtask = new MultiTask<T>(callable, members);
 		getHz().getExecutorService().submit(mtask);
 		return mtask;
 	}
 	
 	/**
 	 * Dispatch the given callable to each node in teh cluster
 	 * @param callable
 	 * @return
 	 */
 	<T> MultiTask<T> executeCallable(Callable<T> callable){
 		final Set<Member> members = getHz().getCluster().getMembers();
 		return executeCallable(callable, members);
 	}
 }
