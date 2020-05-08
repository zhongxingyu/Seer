 package cgl.imr.samples.parallcap.ivy.a;
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.safehaus.uuid.UUIDGenerator;
 
 import cgl.imr.base.TwisterMonitor;
 import cgl.imr.base.Value;
 import cgl.imr.base.impl.JobConf;
 import cgl.imr.client.TwisterDriver;
 import cgl.imr.types.MemCacheAddress;
 import cgl.imr.base.SerializationException;
 
 public class ParallCAPMain {
 	private static Map<Integer, Map<Integer, List<Integer>>> queryIdMatrix;
 	private static Qns qns;
 	
 	public ParallCAPMain() throws SerializationException{
 		qns = new Qns();
 
 		try {
 		Class testClass = Class.forName("cgl.imr.samples.parallcap.ivy.a.Node");
 		Value node = (Value)testClass.newInstance();
 		System.out.println("test node id: " + ((Node)node).getId());
 		} catch (Exception e) {
 			throw new SerializationException(e);
 			};
 
 		queryIdMatrix = new HashMap<Integer, Map<Integer, List<Integer>>>();
 		final List<Node> queryList = qns.getQueryNodes();
 		for (int i = 0; i < queryList.size() - 1; i++) {
 			queryIdMatrix.put(queryList.get(i).getId(), 
 					new HashMap<Integer, List<Integer>>());
 		}
 		
 		for (Integer integer : queryIdMatrix.keySet()) {
 			for (int i = 1; i < queryList.size(); i++) {
 				//initialize the matrix so as each cell is empty
 				queryIdMatrix.get(integer).put(queryList.get(i).getId(), new ArrayList<Integer>());
 			}
 		}
 		
 		for (Integer m : queryIdMatrix.keySet()) {
 			for (Integer n: queryIdMatrix.get(m).keySet()) {
 				System.out.println("( " + m + ", " + n + " )");
 			}
 			System.out.println("\n");
 		}
 	}
 	
 	//mark query matrix based on the current gray node list 
 	//and query nodes
 	private static void markQueryMatrix(List<Value> grayNodes) {
 		
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws Exception {
 		/*
 		 * Main program to run parallel cap MapReduce job
 		 * @param args
 		 * @throws Exception
 		 * */
 		ParallCAPMain pcapMain = new ParallCAPMain();	
 		if (args.length != 4) {
 			String errorReport = "ParallelCap: the Correct arguments are \n"
 					+ "java cgl.imr.samples.parallcap.parallelcapmain "
 					+ "<partition file> <num map tasks> <num reduce tasks> <numLoop>";
 			System.out.println(errorReport);
 			System.exit(0);
 		}
 		
 		String partitionFile = args[0];
 		int numMapTasks = Integer.parseInt(args[1]);
 		int numReduceTasks = Integer.parseInt(args[2]);
 		int numLoop = Integer.parseInt(args[3]);
 		List<Value> grayNodes = null;
 
 		double beginTime = System.currentTimeMillis();
 		try {
 			grayNodes = ParallCAPMain.driveMapReduce(numMapTasks, numReduceTasks, partitionFile, numLoop);
 			System.out.println("Current gray nodes: ");
 			for (Value val : grayNodes) {
				NodeVectorValue tmpVec = (NodeVectorValue)val;
				for (Node node : tmpVec.getGrayNodeList()) {
					System.out.println(node.getId());
				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		double endTime = System.currentTimeMillis();
 		
 		// Print test stats
 		double timeInSeconds = ((double)(endTime - beginTime)) / 1000;
 		System.out
 				.println("------------------------------------------------------");
 		System.out.println("Parallel Cap took " + timeInSeconds
 				+ " seconds.");
 		System.out
 				.println("------------------------------------------------------");
 
 	}
 	
 	public static List<Value> driveMapReduce(int numMapTasks, int numReduceTasks, 
 			String partitionFile, int numLoop) throws Exception {
 		//JobConfigurations
 		JobConf jobConf = new JobConf("ParallCap-map-reduce"
 				+ UUIDGenerator.getInstance().generateTimeBasedUUID());
 		jobConf.setMapperClass(ParallCAPMapTask.class);
 		jobConf.setReducerClass(ParallelCAPReduceTask.class);
 		jobConf.setCombinerClass(ParallelCAPCombiner.class);
 		jobConf.setNumMapTasks(numMapTasks);
 		jobConf.setNumReduceTasks(numReduceTasks);
 		jobConf.setFaultTolerance();
 		TwisterDriver driver = new TwisterDriver(jobConf);
 		driver.configureMaps(partitionFile);
 		
 		List <Value> grayNodes = new ArrayList<Value>();
 		//initialize the dynamic data first
 		int grayNodeSize = qns.getQueryNodes().size();
 		NodeVectorValue nodeVecVal = new NodeVectorValue(grayNodeSize, qns.getQueryNodes());
 		grayNodes.add(nodeVecVal);
 		
 		int bfsIterCnt = 0;
 		for (; bfsIterCnt < numLoop; bfsIterCnt++) {
 			MemCacheAddress memCacheKey = driver.addToMemCache(grayNodes);
 			TwisterMonitor monitor = driver.runMapReduceBCast(memCacheKey);
 			monitor.monitorTillCompletion();
 			driver.cleanMemCache();
 			grayNodes = ((ParallelCAPCombiner) driver.getCurrentCombiner())
 					.getResults();
 			markQueryMatrix(grayNodes);
 			System.out.println("Search step " + bfsIterCnt);
 		}
 		driver.close();
 		return grayNodes;
 		
 	}
 
 }
 
 
 
 
 
 
 
 
 
 
 
