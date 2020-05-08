 package edu.illinois.cs.mr.te;
 
 import java.io.IOException;
 import java.net.ConnectException;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.TreeMap;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.Semaphore;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import edu.illinois.cs.mr.Node;
 import edu.illinois.cs.mr.NodeConfiguration;
 import edu.illinois.cs.mr.NodeID;
 import edu.illinois.cs.mr.fs.FileSystemService;
 import edu.illinois.cs.mr.fs.Path;
 import edu.illinois.cs.mr.jm.JobManagerService;
 import edu.illinois.cs.mr.jm.AttemptID;
 import edu.illinois.cs.mr.jm.AttemptStatus;
 import edu.illinois.cs.mr.util.Status.State;
 
 public class TaskExecutor implements TaskExecutorService {
 
     private static class TaskExecution {
         private final Future<?> future;
         private final TaskExecutorTask task;
         private final Semaphore completion;
 
         TaskExecution(Future<?> future, TaskExecutorTask task, Semaphore completion) {
             this.future = future;
             this.task = task;
             this.completion = completion;
         }
 
         public Future<?> getFuture() {
             return future;
         }
 
         public TaskExecutorTask getTask() {
             return task;
         }
 
         public Semaphore getCompletion() {
             return completion;
         }
     }
 
     private final NodeConfiguration config;
     private final ThreadPoolExecutor executorService;
     private final int numThreads;
     private final int statusUpdateInterval;
     private final Timer timer;
     private Node node; // quasi immutable
 
     // mutable state
     private final Map<AttemptID, TaskExecution> executions;
     private double throttle;
     private long[] taskRuntimes;
     private int index;
     private int capacity;
     private long averageRuntime;
 
     public TaskExecutor(NodeConfiguration config) {
         this.config = config;
         this.numThreads = config.teNumThreads;
         this.throttle = config.teThrottle;
         this.statusUpdateInterval = config.teStatusUpdateInterval;
         this.executorService = (ThreadPoolExecutor)Executors.newFixedThreadPool(config.teNumThreads);
         this.executions = new TreeMap<AttemptID, TaskExecution>();
         this.timer = new Timer();
         this.capacity = 10;
         this.index = 0;
         this.taskRuntimes = new long[capacity];
         this.averageRuntime = 0;
     }
 
     @Override
     public void start(Node node) {
         this.node = node;
         this.timer.schedule(new StatusUpdateTask(), 0, this.statusUpdateInterval);
     }
 
     @Override
     public void stop() {
         this.timer.cancel();
         this.executorService.shutdown();
     }
 
     @Override
     public synchronized void setThrottle(double value) {
         this.throttle = value;
     }
 
     public synchronized double getThrottle() {
         return this.throttle;
     }
 
     public int getNumThreads() {
         return this.numThreads;
     }
 
     public int getNumActiveThreads() {
         return executorService.getActiveCount();
     }
 
     public int getQueueLength() {
         return executorService.getQueue().size();
     }
 
     private void updateAverage() {
     	long average = 0;
     	for (long taskRuntime : this.taskRuntimes) {
     		average += taskRuntime;
     	}
     	this.averageRuntime = average / capacity;
     }
     
     public synchronized long done(TaskExecutorTask task) {
     	long runtime = task.getDoneTime() - task.getBeginRunningTime();
     	taskRuntimes[index] = runtime;
     	int temp = (index++) % capacity;
     	this.index = temp;
    	this.updateAverage();    	
     	return (this.averageRuntime / ((long)this.throttle / 100)) - this.averageRuntime;    	
     }
     
     @Override
     public void execute(TaskExecutorTask task) throws RemoteException {
         Semaphore completion = new Semaphore(0);
         TaskRunner runner = new TaskRunner(this, task, completion, node);
         synchronized (executions) {
             task.setState(State.WAITING);
             Future<?> future = executorService.submit(runner);
             executions.put(task.getId(), new TaskExecution(future, task, completion));
         }
     }
 
     @Override
     public boolean cancel(AttemptID id, long timeout, TimeUnit unit) throws IOException, TimeoutException {
         TaskExecution execution;
         synchronized (executions) {
             execution = executions.get(id);
         }
         if (execution != null) {
             TaskExecutorTask task = execution.getTask();
             if (task.isDone())
                 return true;
             Future<?> future = execution.getFuture();
             if (!future.isDone())
                 future.cancel(true);
             Semaphore completion = execution.getCompletion();
             try {
                 if (completion.tryAcquire(timeout, unit))
                     return true;
             } catch (InterruptedException e) {
                 Thread.currentThread().interrupt();
             }
             if (task.isDone())
                 return true;
             return false;
         }
         return true;
     }
 
     @Override
     public boolean delete(AttemptID id) throws IOException {
         TaskExecution execution;
         synchronized (executions) {
             execution = executions.get(id);
         }
         if (execution != null) {
             TaskExecutorTask task = execution.getTask();
             if (!task.isDone())
                 return false;
             FileSystemService fileSystem = node.getFileSystemService(task.getTargetNodeID());
             Path outputPath = task.getOutputPath();
             synchronized (outputPath) {
                 if (fileSystem.exists(outputPath) && !fileSystem.delete(outputPath))
                     return false;
             }
             synchronized (executions) {
                 executions.remove(id);
             }
         }
         return true;
     }
 
     private class StatusUpdateTask extends TimerTask {
         @Override
         public void run() {
             try {
                 Map<NodeID, List<AttemptStatus>> map = new TreeMap<NodeID, List<AttemptStatus>>();
                 synchronized (executions) {
                     for (TaskExecution execution : executions.values()) {
                         AttemptStatus status = execution.getTask().toImmutableStatus();
                         NodeID nodeId = status.getNodeID();
                         List<AttemptStatus> nodeStatus = map.get(nodeId);
                         if (nodeStatus == null)
                             map.put(nodeId, nodeStatus = new ArrayList<AttemptStatus>());
                         nodeStatus.add(status);
                     }
                 }
                 for (Entry<NodeID, List<AttemptStatus>> entry : map.entrySet()) {
                     JobManagerService jobManager = node.getJobManagerService(entry.getKey());
                     AttemptStatus[] statuses = entry.getValue().toArray(new AttemptStatus[0]);
                     try {
                         jobManager.updateStatus(statuses);
                     } catch (ConnectException e) {
                         System.out.println("cannot reach node " + entry.getKey() + " for status update");
                     }
                 }
             } catch (Throwable t) {
                 System.out.println("node " + config.nodeId + " failed to update status");
                 t.printStackTrace();
             }
         }
     }
 }
