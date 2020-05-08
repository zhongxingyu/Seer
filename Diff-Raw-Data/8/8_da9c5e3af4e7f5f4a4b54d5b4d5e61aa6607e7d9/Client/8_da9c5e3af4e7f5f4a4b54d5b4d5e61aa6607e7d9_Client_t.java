 package actors;
 
 import static utility.Constants.REQUESTQ;
 import static utility.Constants.RESPONSEQ;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Scanner;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 
 import monitor.ClientMonior;
 import monitor.WorkerMonitor;
 import monitor.cassandra.SimpleClient;
 import queue.DistributedQueue;
 import queue.QueueFactory;
 import queue.TaskQueueFactory;
 
 import com.datastax.driver.core.utils.UUIDs;
 import com.hazelcast.client.ClientConfig;
 import com.hazelcast.client.HazelcastClient;
 
 import entity.QueueDetails;
 import entity.Task;
 import entity.TemplateTask;
 
 public class Client implements Runnable {
 
 	Map<String, Task> submittedTasks ;
 	String url;
 	QueueDetails qu;
 	long pollTime = 3000;
 	HazelcastClient hazelClinetObj;
 	long numberofWorkerThreads = 1;
 	String fileName;
 	String resouceAllocationMode;
 	SimpleClient cassandraClient;
     ClientMonior objClientMonior;
     String clientName ;
     String mqPortnumber;
 	public Client() {
 		super();
 		try (FileReader reader = new FileReader("CloudKon.properties")) {
 			 clientName =genUniQID();
 			 submittedTasks = new ConcurrentHashMap<>();
 			Properties properties = new Properties();
 			properties.load(reader);
 			// hazelClient
 			ClientConfig clientConfig = new ClientConfig();
 			String serverLoc = properties.getProperty("hazelCastServerList");
 			clientConfig.addAddress(serverLoc);
 			hazelClinetObj = HazelcastClient.newHazelcastClient(clientConfig);
 
 			// Cassandra Client
 			String cassServerlist = properties.getProperty("cassServerlist");
 			cassandraClient = new SimpleClient();
 			cassandraClient.connect(cassServerlist);
 			//Start monitor
 			objClientMonior = new ClientMonior(clientName, cassandraClient, submittedTasks);
 			new Thread(objClientMonior).start();
 			
 			numberofWorkerThreads = Long.parseLong(properties
 					.getProperty("numberofWorkerThreads"));
 			pollTime = Long.parseLong(properties.getProperty("clientPollTime"));
 			fileName = properties.getProperty("taskFilePath");
 			resouceAllocationMode = properties
 					.getProperty("resouceAllocationMode");
 			mqPortnumber =properties
 					.getProperty("mqPortnumber");
 			
 
 		} catch (IOException e) {
 			// TODO remove Exceptin
 			e.printStackTrace();
 		}
 	}
 
 	public static void main(String args[]) throws Exception {
 		Client client = new Client();
 		client.postTasks();
 		
 	}
 
 	private String getUrl() {
 		InetAddress addr;
 		String ipAddress = null;
 		try {
 			addr = InetAddress.getLocalHost();
 			ipAddress = addr.getHostAddress();
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return "tcp://" + ipAddress + ":" + mqPortnumber;
 	}
 
 	private void postTasks() throws NumberFormatException,
 			FileNotFoundException {
 		
 		url = getUrl();
 		qu = new QueueDetails(REQUESTQ, RESPONSEQ, clientName, url);
 		List<Task> objects = readFileAndMakeTasks(fileName, clientName);
 		TaskQueueFactory.getQueue().postTask(objects, REQUESTQ, url,clientName);
 		//Stop the client from exiting due to submittedTasks not filled.
 		new Thread(this).start();
 		
 		//check the mode of operation
 		//Dont think this if else is needed as anyways will client post messages at the beginning
 		if (resouceAllocationMode.equals("static")) {
 			// Get the already running workers
 			long numOfWorkers = WorkerMonitor
 					.getNumOfWorkerThreads(hazelClinetObj);
 			System.out.println("numOfWorkers "+numOfWorkers);
			if(numOfWorkers==0){numOfWorkers=1;}
 			long loopCount = objects.size() / (numOfWorkers * numberofWorkerThreads);
 			System.out.println("loopCount "+loopCount);
 			loopCount = loopCount == 0 ? 1 : loopCount;
 			System.out.println("loopCount "+loopCount);
 			DistributedQueue queue = QueueFactory.getQueue();
 			for (int loopIndex = 0; loopIndex < loopCount; loopIndex++) {
 				queue.pushToQueue(qu);
 			}
 		} else {
 			// TODO : logic for dynamic allocator where workers wont be stared
 			// up front
 		}
 
 	}
 
 	@Override
 	public void run() {
 		// TODO Auto-generated method stub
 		long startTime = System.currentTimeMillis();
 		while (!submittedTasks.isEmpty()) {
 			 //System.out.println("Pending Task length[" + submittedTasks.size()
 			 //+ "]");
 			Task task = TaskQueueFactory.getQueue()
 					.retrieveTask(RESPONSEQ, url,clientName);
 			if (task != null) {
 				if (task.isMultiTask()) {
 					for (Task tasks : task.getTasks()) {
 						System.out.println("Task[" + tasks.getTaskId()
 								+ "]completed");
 						submittedTasks.remove(tasks.getTaskId());
 					}
 				} else {
 					System.out.println("Task[" + task.getTaskId()
 							+ "]completed");
 					submittedTasks.remove(task.getTaskId());
 				}
 
 			}
 			/**
 			 * Advertise tasks again after some time
 			 */
 			if (System.currentTimeMillis() - startTime > pollTime) {
 				startTime = System.currentTimeMillis();
 				DistributedQueue queue = QueueFactory.getQueue();
 				queue.pushToQueue(qu);
 			}
 
 		}
 		// Shutdown Hazel
 		objClientMonior.setClientShutoff(true);
 		HazelcastClient.shutdownAll();
 		System.exit(0);
 	}
 
 	private ArrayList<Task> readFileAndMakeTasks(String fileName,
 			String clientName) throws NumberFormatException,
 			FileNotFoundException {
 		Scanner s = new Scanner(new File(fileName));
 		ArrayList<Task> list = new ArrayList<Task>();
 		Task task = null;
 		while (s.hasNext()) {
 			task = new TemplateTask(genUniQID()+clientName, clientName,  RESPONSEQ,url,
 					Long.parseLong(s.next()));
 			list.add(task);
 			submittedTasks.put(task.getTaskId(), task);
 		}
 		s.close();
 		return list;
 	}
 
 	private String genUniQID() {
 		UUID uniqueID = UUIDs.timeBased();
 		return uniqueID.toString();
 	}
 }
