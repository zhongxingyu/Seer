 
 import java.io.BufferedWriter;
 import java.io.ByteArrayOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 
 import org.apache.zookeeper.*;
 import org.apache.zookeeper.KeeperException.ConnectionLossException;
 import org.apache.zookeeper.ZooDefs.Ids;
 import org.slf4j.*;
 
 import java.io.File;
 
 
 public class zkNaiveClient implements Watcher{
 	boolean genericRandomize = true;
 	public ArrayList<Integer> replicas = new ArrayList<Integer>();
 	static final String[] machines = {"172.22.138.16", "172.22.138.18", "172.22.138.52"};
 	static final String[] ports = {"12181", "22181", "32181"};
 	static final String[] znodes = {"/db1", "/db2", "/db3"};
 	//BufferedWriter logger;
 	String logFileName;
 	/**
 	 * 2D array of dimensions 3*3
 	 * each row stands for the replica for which client wants to send read/write request
 	 * 1st column in each row stands for the replica which is on SSD
 	 * 2nd and 3rd columns are for HDD replicas
 	 */
 	private ZooKeeper[] paxosInstances = new ZooKeeper[3];
 	
 	private int noOfOps;
 	
 	static long HKey = 0;
 	static long LKey = 0;
 	static final long maxKey = 268435456;		//hex - 10000000
 	//static final long maxKey = ;		//hex - 10000000
 	
 	public static String randKey() {
 		long randomKey;
 		randomKey = (long)(Math.random() * (zkNaiveClient.maxKey) + 1);
 		return new Long(randomKey).toString();
 	}
 	
 	public static String randOperation(float readWritePercentage) {
 		if(Math.random() >= (double) readWritePercentage) {
 			return "READ";
 		} else {
 			return "WRITE";
 		}
 	}
 	
 	private BufferedWriter getLoggerHandle() {
 		
 		File logFile = new File(this.logFileName);
 		FileWriter fop = null;
 		BufferedWriter out = null;
 		try {
 			// if file doesn't exists, then create it
 			if (!logFile.exists()) {
 				logFile.createNewFile();
 			}
 			fop = new FileWriter(logFile, false);
 			out = new BufferedWriter(fop);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return out;		
 	}
 	
 	private void closeLoggerHandle(BufferedWriter logger) {
 		
 		try {
 			logger.flush();
 			logger.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public static zkNaiveClient initializeZKClient() {
 		
 		zkNaiveClient zkObj = new zkNaiveClient();
 		zkObj.replicas.add(0, new Integer(1));
 		zkObj.replicas.add(1, new Integer(2));
 		zkObj.replicas.add(2, new Integer(3));
 		
 		ZooKeeper readWrite = null;
 		int i = 0, j = 0, k = 1;
 		String hostPort;
 		try {
 
 			for(; i < 3; i++) {
 				if(zkObj.genericRandomize) {
					int randRep = (int)Math.floor((Math.random() * (machines.length + 1)));
					System.out.println("randRep decided for replica no - " + i + " is - " + randRep);
 					
 					hostPort = machines[randRep]+":"+ports[i];
 					readWrite = new ZooKeeper(hostPort, 30000, zkObj);
 
 					byte[] tmp = new String(znodes[i]).getBytes("UTF-16");
 					if (readWrite.exists(znodes[i], null) == null) {
 						readWrite.create(znodes[i], tmp, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
 					}
 					zkObj.paxosInstances[i] = readWrite;
 					System.out.println("ZooKeeper connection for replica - (" + i +") " + "established with :" + hostPort);
 				} else {
 					boolean selectNext = false;
 					for(j = 0; j < 3; j++) {
 						if(j != i) {
 							if(Math.random() < 0.5 || selectNext) {
 								hostPort = machines[j]+":"+ports[i];
 								readWrite = new ZooKeeper(hostPort, 30000, zkObj);
 								zkObj.paxosInstances[k] = readWrite;
 								System.out.println("ZooKeeper connection for replica - ( " + i +" ) "+ "established with :" + hostPort);
 								break;
 							} else {
 								selectNext = true;
 							}
 						}
 					}
 				}
 			}
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (KeeperException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return zkObj;
 	}
 	
 	
 	
 	
 	private void runReadWriteLoad(float readWritePercentage) {
 		/**
 		 * readWritePercentage specified the read/write distribution -
 		 * 0 - read only
 		 * 1 - write only
 		 * 0-1 - read/write workload
 		 */
 		
 		int reads = 0, writes = 0;
         
         try {
         	
         	long startTime = System.currentTimeMillis();
         	long localStartTime = 0;
         	long totalReadTime = 0;
         	long totalWriteTime = 0;
         	
         	long counter = 0;
         	/*long readMin = 0;
         	long writeMin = 0;
         	long readMax = 0;
         	long writeMax = 0;*/
         	
         	String operation;
         	
         	System.out.println("Start Time : " + startTime);
         	BufferedWriter logger = getLoggerHandle();
         	logger.write("********************** START TIME: " + startTime + " ***********************\n");
         	closeLoggerHandle(logger);
         	
         	String randKey = null;
         	byte[] retData = null;
         	ByteArrayOutputStream bOut = new ByteArrayOutputStream(100);
         	
     		byte[] lastWrittenKey = new byte[16];//TEMP
     		Arrays.fill(lastWrittenKey, (byte)'0');//TEMP
     		while(true) {
     			
     			ZooKeeper zkFinal = null;
     			randKey = randKey();
     			operation = randOperation(readWritePercentage);
     			Collections.shuffle(replicas);
     			int randomPartition = replicas.get(0).intValue();
     			//LKey++;
     			//randKey = String.valueOf(HKey) + String.valueOf(LKey);
 
     			bOut.reset();
     			bOut.write(randKey.getBytes());
 
     			byte[] key = randKey.getBytes();
     			byte[] value = bOut.toByteArray();
 
     			byte[] forGetData = new byte[16];
     			byte[] forSetData = new byte[116];
     			Arrays.fill(forSetData, (byte)'0');
     			Arrays.fill(forGetData, (byte)'0');
 
     			System.arraycopy(key, 0, forSetData, 16-key.length, key.length);
     			System.arraycopy(value, 0, forSetData, 116-value.length, value.length);
     			System.arraycopy(key, 0, forGetData, 16-key.length, key.length);
     			try {
     				if(operation.equals("READ")) {
     					zkFinal = paxosInstances[randomPartition-1];
     					localStartTime = System.currentTimeMillis();
 
     					//System.out.println("TESTING" + new String(lastWrittenKey) +" "+ new String(forGetData));
     					retData = zkFinal.getDataByKey(znodes[randomPartition-1], new String(forGetData));
     					totalReadTime += (System.currentTimeMillis() - localStartTime);        			
     					reads++;
 
     					//System.out.println(" Key : " + new String(forGetData) + ", from Partition : " + randomPartition + /*" Read Value - " + new String(retData) + */", Reads : " + reads + " *** read latency : " + totalReadTime/(reads));
     				} else if(operation.equals("WRITE")) {
     					if(Math.random() < 0.5) {
     						zkFinal = paxosInstances[randomPartition-1];
     					} else {
     						zkFinal = paxosInstances[randomPartition-1];
     					}
     					localStartTime = System.currentTimeMillis();
     					zkFinal.setData(znodes[randomPartition-1], forSetData, -1);
     					totalWriteTime += (System.currentTimeMillis() - localStartTime);        		
     					writes++;
 
     					//Arrays.fill(lastWrittenKey, (byte)'0');//TEMP
     					//System.arraycopy(forSetData, 0, lastWrittenKey, 0, 16);//TEMP
     					//System.out.println("lastwrittenKey - "+new String(lastWrittenKey));
     					//System.out.println(" KeyValue:  " + new String(forSetData) + " KeyValueLength:  " + forSetData.length + "in Partition : " + randomPartition + " Writes : " + writes + " *** Write latency : " + totalWriteTime/(writes));
 
     				}
     				counter++;
 
     			} catch (ConnectionLossException e) {
     				System.err.println("ConnectionLossException recved for PaxosInstance - ");
     				e.printStackTrace();
     			}
         		if(counter % 1000 == 0) {
         			logger = getLoggerHandle();
         			//System.out.println(" Progress : " + (float) i*100/maxKey+ " % " + " KeyValue:  " + new String(c) + " KeyValueLength:  " + c.length + " Writes : " + writes + " *** Write latency : " + totalWriteTime/(writes));
         			System.out.println("READS: " + reads + " TOTAL READ TIME: " + totalReadTime + " WRITES:" + writes + " TOTAL WRITE TIME: "  + totalWriteTime + " TOTAL TIME: " + (System.currentTimeMillis() - startTime));
         			logger.write("READS: " + reads + "; TOTAL READ TIME: " + totalReadTime + "; WRITES:" + writes + "; TOTAL WRITE TIME: "  + totalWriteTime + "; TOTAL TIME: " + (System.currentTimeMillis() - startTime));
         			//logger.newLine();
         			logger.write("; READ LATENCY: " + totalReadTime/(reads));
         			//logger.newLine();
         			logger.write("; WRITE LATENCY: " + totalWriteTime/(writes));
         			logger.newLine();
         			closeLoggerHandle(logger);
         		}
         		
         		if(counter == noOfOps) {
         			logger = getLoggerHandle();
         			logger.write("READS: " + reads + "; TOTAL READ TIME: " + totalReadTime + "; WRITES:" + writes + "; TOTAL WRITE TIME: "  + totalWriteTime + "; TOTAL TIME: " + (System.currentTimeMillis() - startTime));
         			//logger.newLine();
         			logger.write("; READ LATENCY: " + totalReadTime/(reads));
         			//logger.newLine();
         			logger.write("; WRITE LATENCY: " + totalWriteTime/(writes));
         			logger.newLine();
         			closeLoggerHandle(logger);
         			break;
         		}
         	}
 
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
 	
 	void close() {
 		int i = 0;
 		int j = 0;
 		for(; i < 3; i++) {
 			j = 0;
 			for(; j < 3; j++) {
 				try {
 					paxosInstances[i].close();
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}	
 	}
 	
 	
 	@Override
 	public void process(WatchedEvent event) {
 		// TODO Auto-generated method stub
 
 		
 	}
 	
 	public static void main(String[] args) {
 		
 		if(args.length < 3) {
 			System.out.println("USAGE: <program> <workloadDescription> <logFilePath> <noOfOps>");
 			return;
 		}
 		
 		float readWritePercentage = Float.parseFloat(args[0]);
 		System.out.println("The user given load type is - "+String.valueOf(readWritePercentage));
 	
 		String logFileName = args[1];
 
 		
         zkNaiveClient zkObj = initializeZKClient();
         zkObj.logFileName = logFileName;
         zkObj.noOfOps = Integer.parseInt(args[2]);
         
         zkObj.runReadWriteLoad(readWritePercentage);
 
         zkObj.close();
 	}
 }
