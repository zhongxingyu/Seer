 package com.volkan.accesspattern;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.concurrent.ConcurrentHashMap;
 
 import com.volkan.Configuration;
 
 public class GPartPartitioner {
 
 	public static void buildGrfFile(Map<Long, List<Long>> nodeIDNeiIDArrayMap) throws IOException{
 		String content = generateGrfFileContent(nodeIDNeiIDArrayMap);
 		writeGrfFile(content);
 	}
 	
 	protected static String generateGrfFileContent(Map<Long, List<Long>> nodeIDNeiIDArrayMap){
 		
 		StringBuilder sb = new StringBuilder();
 		int nodeCount = 0;
 		int relCount  = 0;
		
		for (Long nodeID : nodeIDNeiIDArrayMap.keySet()) {
 			SortedSet<Long> sortedNeis = new TreeSet<>();
 			sortedNeis.addAll(nodeIDNeiIDArrayMap.get(nodeID));
 			
 			sb.append("\n" + sortedNeis.size());
 			for (Long neiNodeID : sortedNeis) {
 				sb.append("\t" + neiNodeID);
 			}
 			
 			nodeCount++;
 			relCount += sortedNeis.size();
 		}
 		
 		StringBuilder sbAna = new StringBuilder("0\n" + nodeCount + "\t" + relCount + "\n");
 		sbAna.append("1\t000");
 		sbAna.append(sb.toString());
 
 		return sbAna.toString();
 	}
 	
 	protected static void writeGrfFile(String content) throws IOException {
 
 		BufferedWriter gpartInputFile = 
 				new BufferedWriter(new FileWriter(Configuration.GPART_GRF_PATH));
 		
 		gpartInputFile.write( content );
 		
 		if (gpartInputFile != null)
 			gpartInputFile.close();
 	}
 
 	public static void performGpartingAndWriteGidPartitionMap(
 			int partitionCount, Map<Long, Long> nodeIDGidMap, int lastPartition, 
 			int maxNodeCount) throws IOException, InterruptedException 
 	{
 		partitionCount -= 1;//We are saving last 1 partition for unmapped gids.
 		
 		//SCOTCH
 		Process pr = Runtime.getRuntime().exec("gpart " + partitionCount + " " 
 				+ Configuration.GPART_GRF_PATH + " " + Configuration.GPART_RESULT_PATH
 				+ " -b0.5 -vmst");
 		pr.waitFor();
 		BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
 
 		String line = null;
 		while ( (line = buf.readLine() ) != null ) 
 		{
 		  System.out.println(line);
 		}
 
 		ConcurrentHashMap<Long, Integer> gidPartitionMap = readGpartResult(nodeIDGidMap);
 		System.out.println(gidPartitionMap.size() + "");
 		mapNotAccessedNodesToLastPartition(gidPartitionMap, maxNodeCount, lastPartition);
 		writeGidPartitionMapForRuby(gidPartitionMap);
 	}
 	
 	protected static ConcurrentHashMap<Long, Integer> readGpartResult(Map<Long, Long> nodeIDGidMap) 
 			throws IOException 
 	{
 		BufferedReader gpartResultFile = 
 				new BufferedReader(new FileReader(Configuration.GPART_RESULT_PATH));
 		int i = 0;
 		String line = "";
 		ConcurrentHashMap<Long, Integer> gidPartitionMap = new ConcurrentHashMap<Long, Integer>();
 		while ( (line = gpartResultFile.readLine()) != null ) {
 			if (i == 0){//skip the first line
 				i++; continue;
 			}
 			
 			String[] splitted = line.split("\t");
 			Long nodeID       = new Long(splitted[0]);
 			Integer partition = new Integer(splitted[1]);
 			
 			Long gid = nodeIDGidMap.get(nodeID);
 			if(gid != null){//If null that means nodeID is a path holding reference node, so skip it.
 				gidPartitionMap.put(gid, partition + 6474);//0 => 6474, 1 => 6475...
 			}
 		}
 		
 		return gidPartitionMap;
 	}
 	
 	protected static Map<Long,Integer> mapNotAccessedNodesToLastPartition(
 					   	ConcurrentHashMap<Long, Integer> gidPartitionMap, 
 							int maxNodeCount, int lastPartition) 
 	{
 		for (long i = 1; i <= maxNodeCount; i++) {
 			gidPartitionMap.putIfAbsent(i, lastPartition);//lastPartition is 6483
 		}
 		
 		return gidPartitionMap;
 	}
 	
 	protected static void writeGidPartitionMapForRuby(Map<Long, Integer> gidPartitionMap) throws IOException {
 		BufferedWriter gpartInputFile = 
 				new BufferedWriter(new FileWriter(Configuration.GID_PARTITION_MAP));
 		
 		for (Long gid : gidPartitionMap.keySet()) {
 			int partition = gidPartitionMap.get(gid);
 			gpartInputFile.write( gid + "," + partition + "\n");
 		}
 		
 		if (gpartInputFile != null)
 			gpartInputFile.close();
 	}
 	
 //	public static void injectPorts(Map<Long, Integer> gidPartitionMap, int maxNodeCount) {
 //		for (Long gid : gidPartitionMap.keySet()) {
 //			int partition = gidPartitionMap.get(gid);
 //			gidPartitionMap.put(gid, partition + 6474); //0 => 6474, 1 => 6475...
 //		}
 //	}
 	
 }
