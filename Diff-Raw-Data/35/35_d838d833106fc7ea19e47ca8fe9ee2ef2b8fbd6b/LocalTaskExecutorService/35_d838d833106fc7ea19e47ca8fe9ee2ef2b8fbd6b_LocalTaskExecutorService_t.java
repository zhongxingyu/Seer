 package com.hazeltask.executor.local;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 
import com.google.common.base.Function;
 import com.hazelcast.logging.ILogger;
 import com.hazelcast.logging.Logger;
 import com.hazeltask.HazeltaskTopology;
 import com.hazeltask.config.ExecutorConfig;
 import com.hazeltask.core.concurrent.collections.grouped.GroupedPriorityQueue;
 import com.hazeltask.core.concurrent.collections.grouped.GroupedQueueRouter;
 import com.hazeltask.core.concurrent.collections.router.ListRouterFactory;
 import com.hazeltask.core.concurrent.collections.tracked.ITrackedQueue;
 import com.hazeltask.core.concurrent.collections.tracked.TrackedPriorityBlockingQueue.TimeCreatedAdapter;
 import com.hazeltask.core.metrics.MetricNamer;
 import com.hazeltask.executor.DelegatingExecutorListener;
 import com.hazeltask.executor.ExecutorListener;
 import com.hazeltask.executor.IExecutorTopologyService;
 import com.hazeltask.executor.ResponseExecutorListener;
 import com.hazeltask.executor.metrics.CollectionSizeGauge;
 import com.hazeltask.executor.metrics.TaskThroughputGauge;
 import com.hazeltask.executor.task.HazeltaskTask;
 import com.yammer.metrics.core.MetricName;
 import com.yammer.metrics.core.MetricsRegistry;
 import com.yammer.metrics.core.Timer;
 import com.yammer.metrics.core.TimerContext;
 
 /**
  * 
  * @author Jason Clawson
  *
  * TODO: allow the specification of a regex for group name
  *       this regex will parse out interesting parameters 
  *       that we can query against to get queues... for example:
  *       
  *       groups:
  *         customer-123:com.example.Foo
  *         customer-123:com.example.Bar
  *         customer-456:com.example.Foo
  *       
  *       regex: customer-(\d+):(.*) -- customerId, className
  *       
  *       Then we can query like... getQueues("customerId", 123) : returns #1 and #2
  *       or 
  *       getQueues("className", "com.example.Foo") returns #1 and #3
  *       
  *       It might be nice to be able to use the hazelcast index class
  *       
  *       This will allow us to, for example, count the total items in a customer's queues
  *       or total up all queues of a certain priority number
  *
  */
 public class LocalTaskExecutorService<ID extends Serializable, G extends Serializable> {
 
     private static ILogger LOGGER = Logger.getLogger(LocalTaskExecutorService.class.getName());
     
 	private final HazeltaskTopology topology;
 	private QueueExecutor<ID,G> localExecutorPool;
 	private GroupedPriorityQueue<HazeltaskTask<ID, G>, G> taskQueue;
 	private final Collection<ExecutorListener<ID,G>> listeners = new LinkedList<ExecutorListener<ID,G>>();
 	private final IExecutorTopologyService executorTopologyService;
 	
 	private final int maxThreads;
 	
 	private MetricNamer metricNamer;
 	private Timer taskSubmittedTimer;
 	private Timer taskExecutedTimer;
 	
 	public LocalTaskExecutorService(HazeltaskTopology topology, ExecutorConfig<ID, G> executorConfig, IExecutorTopologyService executorTopologyService) {
 		this.topology = topology;
 		this.maxThreads = executorConfig.getThreadCount();
 		this.metricNamer = topology.getHazeltaskConfig().getMetricNamer();
 		this.executorTopologyService = executorTopologyService;
 		
 		ThreadFactory factory = executorConfig.getThreadFactory();
 		//List<Entry<String, ITrackedQueue<E>>>
 		ListRouterFactory<Entry<G, ITrackedQueue<HazeltaskTask<ID,G>>>> router = executorConfig.getLoadBalancingConfig().getTaskRouterFactory();
 		
 		taskQueue = new GroupedPriorityQueue<HazeltaskTask<ID, G>,G>(new GroupedQueueRouter.GroupRouterAdapter<HazeltaskTask<ID, G>,G>(router),
                 new TimeCreatedAdapter<HazeltaskTask<ID, G>>(){
             public long getTimeCreated(HazeltaskTask<ID, G> item) {
                 return item.getTimeCreated();
             }
         });
 		
 		if(topology.getHazeltaskConfig().getMetricsRegistry() != null) {
 			//TODO: move metrics to ExecutorMetrics class
 		    MetricsRegistry metrics = topology.getHazeltaskConfig().getMetricsRegistry();
 		    taskSubmittedTimer = metrics.newTimer(createName("task-submitted"), TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
 			taskExecutedTimer = metrics.newTimer(createName("task-executed"), TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
 			metrics.newGauge(createName("throughput"), new TaskThroughputGauge(taskSubmittedTimer, taskExecutedTimer));
 			metrics.newGauge(createName("queue-size"), new CollectionSizeGauge(taskQueue));
 		}
 		
 		localExecutorPool = new QueueExecutor<ID,G>(taskQueue, maxThreads, factory, taskExecutedTimer);
 		localExecutorPool.addListener(new DelegatingExecutorListener<ID,G>(listeners));
 		
 		if(executorConfig.isFutureSupportEnabled())
 		    addListener(new ResponseExecutorListener<ID,G>(executorTopologyService, topology.getLoggingService()));
 		
 		addListener(new TaskCompletionExecutorListener());
 		
 	}
 	
 	private MetricName createName(String name) {
 		return metricNamer.createMetricName(
 			"hazeltask", 
 			topology.getName(), 
 			LocalTaskExecutorService.class.getSimpleName(), 
 			name
 		);
 	}
 
 	public synchronized void startup(){
 		 localExecutorPool.startup();
 		 LOGGER.log(Level.FINE, "LocalTaskExecutorService started for "+topology.getName());
 	}
 	
 	/**
      * This is not thread safe
      * @param listener
      */
     public void addListener(ExecutorListener<ID,G> listener) {
         listeners.add(listener);
     }
     
     private class TaskCompletionExecutorListener implements ExecutorListener<ID,G> {
         public void afterExecute(HazeltaskTask<ID,G> runnable, Throwable exception) {
             HazeltaskTask<ID,G> task = (HazeltaskTask<ID,G>)runnable;
             //TODO: add task exceptions handling / retry logic
             //for now, just remove the work because its completed
             executorTopologyService.removePendingTask(task);
         }
 
         public boolean beforeExecute(HazeltaskTask<ID,G> runnable) {return true;}
     }
 	
 	/**
 	 * There is a race condition scenario here.  We want to get the best result possible as this value
 	 * is used to determine what work needs to be recovered.
 	 * 
 	 * @return
 	 */
 	public Long getOldestTaskCreatedTime(){
 	    long oldest = Long.MAX_VALUE;
 	    
 	    //there is a tiny race condition here... but we just want to make our best attempt
 	    for(Runnable r : localExecutorPool.getTasksInProgress()) {
 	        @SuppressWarnings("unchecked")
             long timeCreated = ((HazeltaskTask<ID,G>)r).getTimeCreated();
 	        if(timeCreated < oldest) {
 	            oldest = timeCreated;
 	        }
 	    }
 	    
 	    Long oldestQueueTime = this.taskQueue.getOldestQueueTime();
 	    
 	    if(oldestQueueTime != null && oldestQueueTime < oldest)
 	        oldest = oldestQueueTime;
 	    
 	    return oldest;
 	}
 	
 	public long getQueueSize() {
 	    return this.taskQueue.size();
 	}
 	
 	public Map<G, Integer> getGroupSizes() {
 		Map<G, Integer> result = new HashMap<G, Integer>();
 		for(Entry<G, ITrackedQueue<HazeltaskTask<ID,G>>> group : this.taskQueue.getGroups()) {
 			result.put(group.getKey(), group.getValue().size());
 		}
 		return result;
 	}
 	
 	public boolean execute(HazeltaskTask<ID,G> command) {
 		if(localExecutorPool.isShutdown()) {
 		    LOGGER.log(Level.WARNING, "Cannot enqueue the task "+command+".  The executor threads are shutdown.");
 		    return false;
 		}
 	    
 	    TimerContext tCtx = null;
 		if(taskSubmittedTimer != null)
 			tCtx = taskSubmittedTimer.time();
 		try {
			command.setHazelcastInstance(topology.getHazeltaskConfig().getHazelcast());
		    return taskQueue.add(command);
 		} finally {
 			if(tCtx != null)
 				tCtx.stop();
 		}
 	}
 	
 	public Collection<HazeltaskTask<ID,G>> stealTasks(long numberOfTasks) {
 	    if(!this.localExecutorPool.isShutdown()) {
     	    long totalSize = taskQueue.size();
     	    ArrayList<HazeltaskTask<ID,G>> result = new ArrayList<HazeltaskTask<ID,G>>((int)numberOfTasks);
     	    for(ITrackedQueue<HazeltaskTask<ID,G>> q : this.taskQueue.getQueuesByGroup().values()) {
     	        int qSize = q.size();
     	        if(qSize == 0) continue;
     	        
     	        double p = (double)qSize / (double)totalSize;
     	        long tasksToTake = Math.round(numberOfTasks * p);
     	        
     	        for(int i=0; i < tasksToTake; i++) {
     	            //TODO: this really sucks that we use q.poll() ... why can't this be a dequeue????
     	            HazeltaskTask<ID,G> task = q.poll();
     	            if(task == null)
     	                break;
     	            result.add(task);
     	        }
     	    }
     	    
     	    if(result.size() < numberOfTasks) {
     	        //FIXME: should we really care? or is this good enough...
     	    }   
     	    
     	    return result;
 	    } else {
 	        LOGGER.log(Level.WARNING,"Cannot steal "+numberOfTasks+" tasks.  The executor threads are shutdown.");
 	        return Collections.emptyList();
 	    }
 	}
 
 	//TODO: time how long it takes to shutdown
 	public void shutdown() {
 	    localExecutorPool.shutdown();
 	}
 	
 	//TODO: time how long it takes to shutdown
 	public List<HazeltaskTask<ID,G>> shutdownNow() {
 	    return localExecutorPool.shutdownNow();
 	}
 
 	public boolean isShutdown() {
 		return localExecutorPool.isShutdown();
 	}
 
 //	//FIXME: fix this
 //	public boolean isTerminated() {
 //		return localExecutorPool.isShutdown();
 //	}
 
 }
