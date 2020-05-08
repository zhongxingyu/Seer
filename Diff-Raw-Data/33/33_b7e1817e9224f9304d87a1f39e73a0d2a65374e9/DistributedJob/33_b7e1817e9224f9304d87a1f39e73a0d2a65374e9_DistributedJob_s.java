 package org.opengeo.gwcdistributed.seed;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ExecutionException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geowebcache.GeoWebCacheException;
 import org.geowebcache.layer.TileLayer;
 import org.geowebcache.seed.GWCTask;
 import org.geowebcache.seed.GWCTask.STATE;
 import org.geowebcache.seed.GWCTask.TYPE;
 import org.geowebcache.seed.Job;
 import org.geowebcache.seed.JobNotFoundException;
 import org.geowebcache.seed.JobStatus;
 import org.geowebcache.seed.JobUtils;
 import org.geowebcache.seed.TaskStatus;
 import org.geowebcache.seed.TileBreeder;
 import org.geowebcache.seed.TileRequest;
 import org.geowebcache.seed.threaded.ThreadedTileBreeder;
 import org.geowebcache.storage.TileRange;
 import org.geowebcache.storage.TileRangeIterator;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.util.Assert;
 
 import com.google.common.base.Preconditions;
 import com.hazelcast.core.AtomicNumber;
 import com.hazelcast.core.Hazelcast;
 import com.hazelcast.core.Member;
 import com.hazelcast.core.MultiTask;
 import com.hazelcast.spring.context.SpringAware;
 
 import static com.google.common.base.Preconditions.*;
 
 @SpringAware
 public abstract class DistributedJob implements Job, Serializable {
 
     final private AtomicNumber activeThreads;
     final protected int threadCount;
     final protected long id;
     final protected DistributedTileRangeIterator trItr;
     final protected String layerName;
     
     public static final long TIME_NOT_STARTED=-1;
     private long groupStartTime=TIME_NOT_STARTED;
     
     private final boolean doFilterUpdate;
     
 
     transient protected TileLayer tl;
     transient protected GWCTask[] threads;
     transient protected DistributedTileBreeder breeder;
 
     
     public static Log log = LogFactory.getLog(DistributedJob.class);
 
     /**
      * 
      * @param id unique ID of the Job
      * @param breeder the TileBreeder that created this job
      * @param tl the layer this job is operating on
      * @param threadCount the number of threads to try to use
      * @param tri iterator over the tiles to be handled
      * @param doFilterUpdate update relevant filters on the layer after the job completes
      */
     protected DistributedJob(long id, DistributedTileBreeder breeder, TileLayer tl, int threadCount, 
             DistributedTileRangeIterator tri, boolean doFilterUpdate) {
 
         checkNotNull(breeder);
         checkNotNull(tl);
         checkNotNull(tri);
         checkArgument(threadCount>0,"threadCount must be positive");
         checkArgument(id>=0,"Job id must be non-negative");
         
         this.breeder = breeder;
         this.threadCount = threadCount;
         this.id = id;
         this.trItr = tri;
         this.tl = tl;
         this.layerName = tl.getName();
         this.doFilterUpdate = doFilterUpdate;
         com.hazelcast.core.HazelcastInstance hz = breeder.getHz();
         this.activeThreads = hz.getAtomicNumber(getKey("activeThreadCount"));
         
         createTasks();
     }
 
     /**
      * Create the tasks for this job.
      */
     protected abstract void createTasks();
     
     /**
      * Get a key unique to this Job but shared by all instances of it across the cluster.
      * @param baseKey 
      * @return A string incorporating the base key and the unique identifier for this job.
      */
     protected String getKey(String baseKey){
     	return getKey(baseKey, id);
     }
     
     static String getKey(String baseKey, long id){
     	if(baseKey!=null)
     		return String.format("job-%d-%s", id, baseKey);
     	else
     		return String.format("job-%d", id);    	
     }
 	
 	public long getId() {
 		return id;
 	}
 	@SpringAware
 	static class DistributedTileRequest implements TileRequest, Serializable {
 		
 		final long[] gridLoc;
 		final long jobId;
 		long retryAt = 0;
 		long failures = 0;
 		
 		transient Job job;
 		
 		
 
 		public DistributedTileRequest(long[] gridLoc, Job job) {
 			super();
 			this.gridLoc = gridLoc;
 			this.job = job;
 			this.jobId = job.getId();
 		}
 
 		public long[] getGridLoc() {
 			return gridLoc;
 		}
 
 		public long getX() {
 			return gridLoc[0];
 		}
 
 		public long getY() {
 			return gridLoc[1];
 		}
 
 		public long getZoom() {
 			return gridLoc[2];
 		}
 
 		public long getRetryAt() {
 			return retryAt;
 		}
 
 		public long getFailures() {
 			return failures;
 		}
 
 		public Job getJob() {
 			return job;
 		}
 
 		public int compareTo(TileRequest o) {
 			return Long.signum(getRetryAt()-o.getRetryAt());
 		}
 		
 		@Autowired
 		public void setBreeder(DistributedTileBreeder breeder) throws GeoWebCacheException{
 			this.job = breeder.getJobByID(jobId);
 		}
 	}
 	
 	public TileRequest getNextLocation() throws InterruptedException {
 		long[] gridLoc = trItr.nextMetaGridLocation();
 		if(gridLoc==null) return null;
 		TileRequest tr = new DistributedTileRequest(gridLoc, this);
 		return tr;
 	}
 
 	public GWCTask[] getTasks() {
 		return threads;
 	}
 	
 	/**
 	 * Get the status of each task in the job, across the cluster.
 	 * @return
 	 * @throws ExecutionException
 	 * @throws InterruptedException
 	 */
 	@Override
 	public Collection<TaskStatus> getTaskStatus(){
     	checkInitialized();
     	
     	Collection<Collection<TaskStatus>> statusTree=Collections.EMPTY_LIST;
     	final MultiTask<Collection<TaskStatus>> mtask;
     	try {
 			 mtask = breeder.executeCallable(new GetTaskStatus(this));
 			 statusTree = mtask.get();
     	} catch (ExecutionException ex){
     		log.fatal("Could not get status of tasks for job "+this.getId(), ex);
     	} catch(InterruptedException ex) {
     		log.fatal("Could not get status of tasks for job "+this.getId(), ex);
     	}
 			
 		// Flatten into single collection
 		List<TaskStatus> result = new ArrayList<TaskStatus>((int)(statusTree.size()*this.getThreadCount()));
 		for(Collection<TaskStatus> statusForNode: statusTree) {
 			result.addAll(statusForNode);
 		}
 		
 		return result;
   
 	}
 
 	public void terminate() {
 		checkInitialized();
 		MultiTask<Object> mtask = breeder.executeCallable(new DoTerminate(this));
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
 	
 	void terminateLocal() {
 		checkInitialized();
         for(GWCTask task: threads){
             synchronized(task) {
                 if(task.getState()!=STATE.DEAD && task.getState()!=STATE.DONE){
                     task.terminateNicely();
                 }
             }
         }
 	}
 
 	public long getThreadCount() {
 		return activeThreads.get();
 	}
 
 	public void threadStarted(GWCTask thread) {
 		checkMyTask(thread);
 		activeThreads.incrementAndGet();
 	}
 
 	public void threadStopped(GWCTask thread) {
 		checkMyTask(thread);
 		activeThreads.decrementAndGet();
 	}
 
 	public TileBreeder getBreeder() {
     	checkInitialized();
 		return breeder;
 	}
 
 	public TileLayer getLayer() {
 		checkInitialized();
 		return tl;
 	}
 
 	public TileRange getRange() {
 		return trItr.getTileRange();
 	}
 
     public JobStatus getStatus() {
     	checkInitialized();
         Collection<TaskStatus> taskStatuses;
 		try {
 			taskStatuses = this.getTaskStatus();
 		} catch (Exception e) {
 			// TODO should handle this better, maybe allow getStatus() to throw GeoWebCacheException
 			log.error("Could not retreive state of tasks in job "+this.id+"", e);
 			return null;
 		} 
 		return new JobStatus(this);
     }
 
     class StateIterator implements Iterator<GWCTask.STATE> {
     	
     	Iterator<TaskStatus> it = getStatus().getTaskStatuses().iterator();
     	
 		public boolean hasNext() {
 			return it.hasNext();
 		}
 
 		public STATE next() {
 			return it.next().getState();
 		}
 
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
     	
     }
     
 	public STATE getState() {
 		return JobUtils.combineState(new StateIterator());
 	}
 	
 	/**
 	 * Assert that a task belongs to this job
 	 * @param task
 	 */
     protected void checkMyTask(GWCTask task) {
     	checkArgument(task.getJob()==this, "Task %s does not belong to Job %s", task.getTaskId(), this.getId());
     }
 
     /**
      * Check that transient fields have been initialized after being deserialized.
      */
     protected void checkInitialized() {
     	checkState(this.breeder!=null && this.threads!=null, "Local state was not correctly set after being deserialized.");
     }
     
     /**
      * Property setter for Spring, should only be called once.
      * @param breeder
      * @throws GeoWebCacheException 
      */
     @Autowired
     public void setBreeder(final DistributedTileBreeder breeder) throws GeoWebCacheException {
     	checkState(this.breeder==null, "Breeder should only be set once by Spring");
     	checkNotNull(breeder);
     	
     	this.breeder = breeder;
     	
     	// Set up other transient fields from breeder.
     	this.tl = breeder.getTileLayerDispatcher().getTileLayer(layerName);
     	try{
     		this.threads = ((DistributedJob)breeder.getJobByID(this.id)).threads;
     		log.info("Local job found, acquired local tasks.");
     	} catch (JobNotFoundException ex) {
    		log.info("Local job not found, creating new set of local tasks.");
    		createTasks();
     	}
 
     }
     
 }
