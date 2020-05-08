 package actors;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.Future;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import monitor.WorkerMonitor;
 import queue.DistributedQueue;
 import queue.QueueFactory;
 import queue.TaskQueueFactory;
 import queue.hazelcast.QueueHazelcastUtil;
 
 import com.hazelcast.client.HazelcastClient;
 
 import entity.QueueDetails;
 import entity.Task;
 import entity.TaskBatch;
 
 public class Worker extends TimerTask implements Runnable {
 	private QueueHazelcastUtil objQueueHazelcastUtil;
 	private HazelcastClient hazelClinetObj;
 	private int numberofWorkerThreads = 10;
 	private ThreadPoolExecutor threadPoolExecutor;
 	private Map<String, Integer> waitCounter;
 	private Map<String, List<Task>> resultMap;
 	private Map<Task, Future<Boolean>> taskMap;
 	private int interation;
 	private double workerExecutionLimit;
 	private Properties properties;
 	// TODO
 	private final int batchInterval = 10;
	boolean clientNomoreTask=false;
 	private long startTime = 0;
 
 	public Worker() {
 		super();
 		try (FileReader reader = new FileReader("CloudKon.properties")) {
 			properties = new Properties();
 			properties.load(reader);
 			threadPoolExecutor = new ThreadPoolExecutor(numberofWorkerThreads,
 					numberofWorkerThreads, 0L, TimeUnit.MILLISECONDS,
 					new LinkedBlockingQueue<Runnable>());
 			// hazelClient
 			this.objQueueHazelcastUtil = new QueueHazelcastUtil();
 			this.hazelClinetObj = objQueueHazelcastUtil.getClient();
 			this.numberofWorkerThreads = Integer.parseInt(properties
 					.getProperty("numberofWorkerThreads"));
 			this.interation = Integer.parseInt(properties
 					.getProperty("interationPerWorker"));
 			this.workerExecutionLimit = Double.parseDouble(properties
 					.getProperty("initialLimit", "2"));
 			this.waitCounter = new ConcurrentHashMap<>();
 			this.resultMap = new ConcurrentHashMap<>();
 			this.taskMap = new ConcurrentHashMap<>();
 			this.startTime = System.nanoTime();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Main method for starting the worker
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Timer timer = new Timer();
 		Worker objWorker = new Worker();
 		WorkPoller poller = new WorkPoller(objWorker);
 		timer.schedule(objWorker, 2000, 2000);
 		timer.schedule(poller, 0, 2000);
 		// Loop never ends once the worker begins execution
 		WorkerMonitor.incrNumOfWorkerThreads(objWorker.hazelClinetObj);
 		DistributedQueue queue = QueueFactory.getQueue();
		
 		while (true) {
 			// Get one Q information
 			QueueDetails queueDetails = queue.pullFromQueue();
 			int clientCounter;
 			// loop for getting the tasks for the client mentioned in Q
 			// information we got.
 			// iteration per worker
 			// objWorker.interation > 0
 			clientLoop: // runs till there are no tasks in client queue
 			while (true) {
				objWorker.clientNomoreTask=false;
 				// Pulling only tasks for the worker threads
 				clientCounter = 0;
 				if (queueDetails == null) {
 					break;
 				}
 
 				while (clientCounter < objWorker.numberofWorkerThreads) {
 					Task task = TaskQueueFactory.getQueue().retrieveTask(
 							queueDetails.getRequestQueue(),
 							queueDetails.getUrl(),queueDetails.getClientName());
 					if (task != null) {
 						// Starting the Task
 						Future<Boolean> future = objWorker.threadPoolExecutor.submit(task);
 						objWorker.taskMap.put(task, future);
 					} else {
						objWorker.clientNomoreTask=true;
 						break clientLoop;// breaks if no tasks in client queue
 					}
 
 					clientCounter++;
 				}
 				objWorker.interation--;
 			}
 		}
 	}
 
 	/**
 	 * Timer Method for collating the results
 	 */
 	@Override
 	public void run() {
 		for (Task task : taskMap.keySet()) {
 			Future<Boolean> future = taskMap.get(task);
 			try {
 				Boolean result = future.get(1, TimeUnit.SECONDS);
 				if (result != null && true == result) {
 					taskMap.remove(task);
 					addResult(task);
 				}
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		sendBatchResults();
 		if (isTimeLimitReached()) {
 			try {
 				System.out.println("Terminating the instance");
 				WorkerMonitor.decrNumOfWorkerThreads(hazelClinetObj);
 				// Runtime.getRuntime().exec("shutdown -h 0");
 				System.exit(0);
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	private boolean isTimeLimitReached() {
 		// TODO Auto-generated method stub
 		double runTime = (System.nanoTime()) / (1e9 * 60) - startTime;
 		boolean limitBreached = Math.abs(runTime - workerExecutionLimit) <= 1000;
 		if (limitBreached) {
 			startTime = System.nanoTime();
 			workerExecutionLimit = Double.parseDouble(properties.getProperty(
 					"finalLimit", "60"));
 		}
 		if (isBusy() && limitBreached) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean isBusy() {
 		boolean isBusy = taskMap.isEmpty() && resultMap.isEmpty() && threadPoolExecutor.getActiveCount() <= 0;
 		return isBusy;
 	}
 
 	/**
 	 * Sends result back to the client once the task is completed
 	 */
 	private void sendBatchResults() {
 		// TODO Auto-generated method stub
 		for (String client : resultMap.keySet()) {
 			List<Task> tasks = resultMap.get(client);
 			// or expire time
 			int counter = 1;
 			if (waitCounter.containsKey(client)) {
 				counter = waitCounter.get(client) + 1;
 			}
 			waitCounter.put(client, counter);
 
 			if (tasks != null
					&& ((tasks.size() >= numberofWorkerThreads || counter == batchInterval)||clientNomoreTask )) {
 				Task task = tasks.get(0);
 				List<Task> batches = new ArrayList<>();
 				Task taskBatch = new TaskBatch();
 				taskBatch.setTasks(tasks);
 				batches.add(taskBatch);
 				TaskQueueFactory.getQueue().postTask(batches,
 						task.getResponseQueueName(), task.getQueueUrl(),task.getClientName());
 			}
 			resultMap.remove(client);
 			waitCounter.remove(client);
 		}
 	}
 
 	/**
 	 * Adds a completed task to the result map to be sent
 	 * 
 	 * @param task
 	 */
 	public void addResult(Task task) {
 		if (task != null) {
 			String clientName = task.getClientName();
 			List<Task> tasks = new ArrayList<>();
 			if (resultMap.containsKey(clientName)) {
 				tasks = resultMap.get(clientName);
 			}
 			tasks.add(task);
 			resultMap.put(clientName, tasks);
 		}
 	}
 
 }
 
 class WorkPoller extends TimerTask {
 	Worker worker;
 	
 	public WorkPoller(Worker worker){
 		this.worker=worker;
 	}
 	
 	public void run() {
 		if(worker.isBusy()){
 			/**
 			 * insert information
 			 */
 		}
 	}
 }
 	
 
