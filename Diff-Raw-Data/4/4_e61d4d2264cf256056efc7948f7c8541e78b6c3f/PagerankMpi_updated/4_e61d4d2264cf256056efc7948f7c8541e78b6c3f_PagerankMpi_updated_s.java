 //package main.java;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import mpi.*;
 
 public class PagerankMpi_updated {
 	public static HashMap<Integer, ArrayList<Integer>> readInput(String filename, Intracomm mpiComm, int nodeId) {
 		// Get globalUrlCount
 		int globalUrlCount = 0;
 
 		if (nodeId == 0) {
 			PerformanceLogger perfL0 = new PerformanceLogger((long)nodeId);
 			try {
 				BufferedReader f = new BufferedReader(new FileReader(filename));
 
 				while (f.ready()) {
 					f.readLine();
 					globalUrlCount++;
 				}				
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			perfL0.log("Finished file read.");
 		}
 
 		// Transmit portion of globalAdjacencyMatrix to all nodes.
 		Object[] localAdjacencyMatrixB = new Object[1];
 		HashMap<Integer, ArrayList<Integer>> localAdjacencyMatrix = new HashMap<Integer, ArrayList<Integer>>();
 		HashMap<Integer, ArrayList<Integer>> globalAdjacencyMatrix = new HashMap<Integer, ArrayList<Integer>>();
 
 		if (nodeId == 0) {
 			PerformanceLogger plogTrans = new PerformanceLogger((long)nodeId);
 			try {
 				BufferedReader f = new BufferedReader(new FileReader(filename));
 				int blockSize = globalUrlCount/(mpiComm.Size() - 1);
 				int remBlockSize = globalUrlCount % (mpiComm.Size() - 1);
 
 				int outputNodeId = 1;
 				while (f.ready()) {					
 					if (remBlockSize > 0) {						
 						for (int i = 0; i < remBlockSize+blockSize; i++) {
 							ArrayList<Integer> tmpAdjacencyMatrix = new ArrayList<Integer>();
 							String[] adjacencyMatrix = f.readLine().split(" ");
 
 							for (int j = 1; j < adjacencyMatrix.length; j++)
 								tmpAdjacencyMatrix.add(Integer.parseInt(adjacencyMatrix[j]));
 
 							localAdjacencyMatrix.put(Integer.parseInt(adjacencyMatrix[0]), tmpAdjacencyMatrix);
 							globalAdjacencyMatrix.put(Integer.parseInt(adjacencyMatrix[0]), tmpAdjacencyMatrix);
 						}
 						localAdjacencyMatrixB[0] = (Object)localAdjacencyMatrix;
 						mpiComm.Send(localAdjacencyMatrixB, 0, 1, MPI.OBJECT, outputNodeId, 0);						
 						System.out.println(remBlockSize+blockSize + " adjacency lines sent to: " + outputNodeId);
 
 						outputNodeId++;
 						remBlockSize = 0;
 						localAdjacencyMatrix = new HashMap<Integer, ArrayList<Integer>>();
 					} else {
 						for (int i = 0; i < blockSize; i++) {
 							String[] adjacencyMatrix = f.readLine().split(" ");
 							ArrayList<Integer> tmpAdjacencyMatrix = new ArrayList<Integer>();
 
 							for (int j = 1; j < adjacencyMatrix.length; j++)
 								tmpAdjacencyMatrix.add(Integer.parseInt(adjacencyMatrix[j]));
 
 							localAdjacencyMatrix.put(Integer.parseInt(adjacencyMatrix[0]), tmpAdjacencyMatrix);	
 							globalAdjacencyMatrix.put(Integer.parseInt(adjacencyMatrix[0]), tmpAdjacencyMatrix);
 						}
 						localAdjacencyMatrixB[0] = (Object)localAdjacencyMatrix;
 						mpiComm.Send(localAdjacencyMatrixB, 0, 1, MPI.OBJECT, outputNodeId, 0);
 						System.out.println(blockSize + " adjacency lines sent to: " + outputNodeId);
 
 						outputNodeId++;
 						localAdjacencyMatrix = new HashMap<Integer, ArrayList<Integer>>();
 					}
 				}
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			plogTrans.log("Finished transmitting data to workers.");
 		} else {
 			PerformanceLogger plogRecv = new PerformanceLogger((long)nodeId);
 			mpiComm.Recv(localAdjacencyMatrixB, 0, 1, MPI.OBJECT, 0, 0);
 			localAdjacencyMatrix = (HashMap<Integer, ArrayList<Integer>>)localAdjacencyMatrixB[0];				
 			plogRecv.log("Finished recv from headnode.");
 		}
 
 		// Return adjacency matrix.
 		if (nodeId == 0)
 			return globalAdjacencyMatrix;
 		else
 			return localAdjacencyMatrix;
 	}
 
 	public static void writeLinks(HashMap<Integer, Double> finalPagerank, String outputFile) {
 		Iterator<Integer> k = finalPagerank.keySet().iterator();
 		String r = "";
 
 		try {
 		    FileWriter fstream = new FileWriter(outputFile);
 		    BufferedWriter out = new BufferedWriter(fstream);
 
 		    while(k.hasNext()) {
 			Integer d = k.next();
 			r += d + "\t" + finalPagerank.get(d) + "\n";
 		    }
 		    out.write(r);
 		    out.close();
 		} 
 		catch (Exception e) {
 		    System.err.println("Error: " + e.getMessage());
 		}
 	}
 
 	public static double getPagerank(HashMap<Integer, ArrayList<Integer>> links, 
 			HashMap<Integer, Double> localPagerank, HashMap<Integer, Double> globalPagerank, int globalUrlsNum) {
 		//NOTES: rewrite the logic of this function
 
 		Iterator<Integer> link_iter = links.keySet().iterator();
 		ArrayList<Integer> tmpstack;
 		double dangling = 0.0;
 
 		//Create and populate the temporary storage for Pageranks 
 
 		for(Integer i = 0; i < globalUrlsNum; i++){
 		    localPagerank.put(i, 0.0);
 		}
 
 		//Iterate through each web page in links
 		while(link_iter.hasNext()) {
 		    int linkKey = link_iter.next();
 		    tmpstack = links.get(linkKey);
 		    int outgoingLinkCount = tmpstack.size();
 		    Double tmp = 0.0;
 
 		    //Iterate through each of this webpage's neighbors and calculate their Pagerank based on this web pages current Pagerank and number of outgoing links 
 		    for(int j = 0; j < outgoingLinkCount; j++){
 				int neighbor = tmpstack.get(j);
 				tmp = localPagerank.get(neighbor) + globalPagerank.get(linkKey)/(double)outgoingLinkCount;
 				localPagerank.put(neighbor, tmp);
 		    }
 
 		    //If this webpage has no outgoing links, calculate its contribution to the overall dangling value 
 		    if(outgoingLinkCount == 0){
 		    	dangling += globalPagerank.get(linkKey);
 		    }
 		}
 
 		// Publish to finalPagerank.
 		//Iterator<Integer> prIter = tmpPagerank.keySet().iterator();
 		//while (prIter.hasNext()) {
 		//	Integer i = prIter.next();
 		//	finalPagerank.put(i, tmpPagerank.get(i));
 		//}
 		return dangling;
 	}
 
 	public static List sortByValue(final Map m) {
         List keys = new ArrayList();
         keys.addAll(m.keySet());
         Collections.sort(keys, new Comparator() {
             public int compare(Object o1, Object o2) {
                 Object v1 = m.get(o1);
                 Object v2 = m.get(o2);
                 if (v1 == null) {
                     return (v2 == null) ? 0 : 1;
                 }
                 else if (v1 instanceof Comparable) {
                     return ((Comparable) v1).compareTo(v2);
                 }
                 else {
                     return 0;
                 }
             }
         });
         return keys;
     }
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		String filename = "pagerank.input";
 		String outFilename = "pagerank.output";
 
 		int iterations = 5;
 		double damping = 0.85;
 		boolean debug = false;
 
 		// Change this if needed. Eclipse hack?
 		if (args.length == 7) {
			filename = System.getProperty("user.dir") + "/src/main/resources/" + args[3];
 			outFilename = System.getProperty("user.dir") + "/bin/main/resources/" + args[4];
 			iterations = Integer.parseInt(args[5]);
 			//damping = Double.parseDouble(args[6]);
 		} else {
 			for (int l = 0; l < args.length; l++)
 				System.out.println(args[l]);
 			System.exit(1);
 		}
 		// End change this.
 
 		MPI.Init(args);
 
 		HashMap<Integer, ArrayList<Integer>> localAdjacencyMatrix = new HashMap<Integer, ArrayList<Integer>>();
 		HashMap<Integer, ArrayList<Integer>> globalAdjacencyMatrix = new HashMap<Integer, ArrayList<Integer>>();
 		HashMap<Integer, Double> localPagerank = new HashMap<Integer, Double>();
 		HashMap<Integer, Double> globalPagerank = new HashMap<Integer, Double>();
 		double globalPagerankB[] = null;
 
 		int[] globalUrlCountB = new int[1];
 		int globalUrlCount = 0;
 		double totalPr = 0.0;
 		double[] globalDangling = {0.0};
 		double[] localDangling = {0.0};
 		double dangling = 0.0;
 
 
 		Intracomm mpiComm = MPI.COMM_WORLD;
 		int size = mpiComm.Size();
 		int nodeId = mpiComm.Rank();
 
 		boolean globalRankValueTableInit = false;
 
 		for (int k = 0; k < iterations; k++) {		
 			if (nodeId == 0) {
 				PerformanceLogger plogHeadCalc = new PerformanceLogger((long)nodeId);
 				// Get global adjacency matrix and distribute pieces to worker nodes.
 				globalAdjacencyMatrix = readInput(filename, mpiComm, nodeId);
 
 				// Broadcast globalUrlCount
 				globalUrlCountB[0] = globalAdjacencyMatrix.size();
 				mpiComm.Bcast(globalUrlCountB, 0, 1, MPI.INT, nodeId);
 				globalUrlCount = globalUrlCountB[0];
 
 				if (!globalRankValueTableInit){
 					globalPagerankB = new double[globalUrlCount];
 					for(int i=0;i<globalUrlCount;i++){
 					globalPagerankB[i] = 1.0/(double)globalUrlCount;
 					}
 					globalRankValueTableInit = true;
 				}
 
 				System.out.println(nodeId + " sent a globalUrlCount of: " + globalUrlCount);
 				// AllReduce dangling value.
 				mpiComm.Allreduce(localDangling, 0, globalDangling, 0, 1, MPI.DOUBLE, MPI.SUM);
 				mpiComm.Barrier();
 				dangling = globalDangling[0];
 
 				// Allreduce localPagerankB
 				double localPagerankB[] = new double[globalUrlCount];
 
 				mpiComm.Allreduce(localPagerankB, 0, globalPagerankB, 0, globalUrlCount, MPI.DOUBLE, MPI.SUM);
 				mpiComm.Barrier();
 
 				plogHeadCalc.log("Received results from workers.");
 				// Apply dangling and damping.
 				double dvp = dangling/(double)globalUrlCount;
 				for (int i = 0; i < globalUrlCount; i++) {
 					globalPagerankB[i] += dvp;
 					globalPagerankB[i] = (1 - damping)/(double)globalUrlCount + damping*globalPagerankB[i];
 				}
 
 				// Pull into HashMap.
 				for (int i = 0; i < globalUrlCount; i++) {
 					globalPagerank.put(i, globalPagerankB[i]);
 				}				
 				plogHeadCalc.log("Finished head node calculatons and result generation.");
 			}
 			else {
 				PerformanceLogger plogWorkCalc = new PerformanceLogger((long)nodeId);
 				// Get local adjacency matrix.
 				localAdjacencyMatrix = readInput(filename, mpiComm, nodeId);
 
 				// Broadcast globalUrlCount
 				mpiComm.Bcast(globalUrlCountB, 0, 1, MPI.INT, 0);
 				globalUrlCount = globalUrlCountB[0];
 
 				//NOTES: need to initilize global rank value table in the first iteration
 				if (!globalRankValueTableInit){
 					globalPagerankB = new double[globalUrlCount];
 					for(int i=0;i<globalUrlCount;i++){
 					globalPagerankB[i] = 1.0/(double)globalUrlCount;
 					}
 					globalRankValueTableInit = true;
 				}
 
 				for (int i = 0; i <globalUrlCount; i++) {
 					globalPagerank.put(i,globalPagerankB[i]);
 				}
 				double localPagerankB[] = new double[globalUrlCount];
 
 				//Get pagerank values.                 
 				//Notes: localPagerank should be the globalPagerank
 
 				//NOTES:  the previous getPagerank's logic is not correct; 
 				//1) the globalPagerank need to reuse in each iteration rather than initize to zero in each iteration
 				//2) need to initilize globalPagerank in the first iteration
 
 				dangling = getPagerank(localAdjacencyMatrix, localPagerank, globalPagerank, globalUrlCount);
 
 				// AllReduce dangling value.
 				localDangling[0] = dangling;
 				mpiComm.Allreduce(localDangling, 0, globalDangling, 0, 1, MPI.DOUBLE, MPI.SUM);
 				mpiComm.Barrier();
 				dangling = globalDangling[0];
 
 				System.out.println("node:"+nodeId + " recieved a global dangling value of: " + dangling);
 
 				Iterator<Integer> j = localPagerank.keySet().iterator();
 				while (j.hasNext()) {
 					Integer iVal = j.next();
 					localPagerankB[iVal] = localPagerank.get(iVal);
 				}
 				// Allreduce localPagerankB
 				mpiComm.Allreduce(localPagerankB, 0, globalPagerankB, 0, globalUrlCount, MPI.DOUBLE, MPI.SUM);
 
 				double dvp = dangling/(double)globalUrlCount;
 
 				//NOTES: need update the global page rank value table at computation processes as well
 				for (int i = 0; i < globalUrlCount; i++) {
 				if (debug)
 				System.out.format("node:%d globalPagerankB[%d]:=%f  dangling:%f localPagerankB[%d]:=%f\n",nodeId,i,globalPagerankB[i], dangling, i, localPagerankB[i]);
 
 					globalPagerankB[i] += dvp;
 					globalPagerankB[i] = (1 - damping)/(double)globalUrlCount + damping*globalPagerankB[i];
 
 				if (debug)	
 					System.out.format("node:%d globalPagerankB[%d]:=%f  dangling:%f localPagerankB[%d]:=%f\n", nodeId,i,globalPagerankB[i], dangling, i, localPagerankB[i]);
 
 				}
 				plogWorkCalc.log("Worker finished interim calculation.");
 				//System.out.println(globalPagerankB.length);
 				dangling = 0.0;
 				mpiComm.Barrier();				
 			}
 		}//for
 
 		// Write to file.
 		//Notes: write only to root process
 		if (nodeId == 0){
 
 		//	for (int i=0;i<globalUrlCount;i++){
 		//	System.out.format("node:%d globalPagerankB[%d]:=%f \n",nodeId,i,globalPagerankB[i]);
 		//	}
 
 		writeLinks(globalPagerank,  outFilename);
 		// Print top 10 sites.			
 		int count = 0;
 		for (Iterator<Integer> i = sortByValue(globalPagerank).iterator(); i.hasNext(); ) {
 			int key = i.next();
 			if (count >= globalUrlCount-10)
 				System.out.println("Key: " + key + " value: " + globalPagerank.get(key));
 			count++;
 			totalPr += globalPagerank.get(key);
 		}
 		System.out.println("Toatl pagerank value: " + totalPr);
 		}
 		MPI.Finalize();
 	}//main 
 }
