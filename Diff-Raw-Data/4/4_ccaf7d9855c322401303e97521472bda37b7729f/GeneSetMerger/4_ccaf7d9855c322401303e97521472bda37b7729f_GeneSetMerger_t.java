 package worker;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import obj.GeneSet;
 
 public class GeneSetMerger extends DistributedWorker{
 	ArrayList<GeneSet> allGeneSets;
 	public static int mergeCount = 0;
 	
 	public GeneSetMerger(int id, int totalComputers, long jobID){
 		super(id, totalComputers, jobID);
 		allGeneSets = new ArrayList<GeneSet>();
 	}
 	
 	public void mergeGeneSets(String path, int numFiles, boolean finalOutput) throws IOException{
 		if(!path.endsWith("/")) path = path + "/";
 		int start = id * numFiles/totalComputers;
 		int end = (id+1) * numFiles/totalComputers;
 		BufferedReader br;
 		System.out.println("Processing file " + start + " to file " + end );
 		for(int i = start; i < end; i++){
 			br = new BufferedReader(new FileReader(path + "caf."+ String.format("%05d", i)+".txt"));
 			String line = br.readLine();
 			// Greedily merge gene set
 			while(line != null){
 				if(!line.equals("NA"))
 				{
 					String[] tokens = line.split("\t");
 					HashSet<Integer> gIdx = new HashSet<Integer>();
 					for(String s : tokens){
 						gIdx.add(Integer.parseInt(s));
 					}
 					GeneSet rookie = new GeneSet(gIdx); 
 					if(allGeneSets.size() == 0){
 						allGeneSets.add(rookie);
 					}
					int origSize = allGeneSets.size();
					for(int j = 0; j < origSize; j++){
						GeneSet gs = allGeneSets.get(j);
 						if(gs.merge(rookie)){
 							break;
 							// gene set merged
 						}else{
 							allGeneSets.add(rookie);
 						}
 					}
 				}
 				line = br.readLine();
 			}
 			br.close();
 		}
 		if(finalOutput){
 			new File("output").mkdir();
 			new File("output/" + jobID).mkdir();
 			PrintWriter pw = new PrintWriter(new FileWriter("output/" + jobID + "/attractors.gct"));
 			int cnt = 0;
 			for(GeneSet gs : allGeneSets){
 				pw.print("Attractor" + String.format("%03d", cnt) + "\tNA\t");
 				pw.println(gs.toGenes());
 				cnt++;
 			}
 			pw.close();
 		}else{
 			prepare("merge" + mergeCount);
 			PrintWriter pw = new PrintWriter(new FileWriter("tmp/" + jobID + "/merge" + mergeCount + "/caf."+ String.format("%05d", id)+".txt"));
 			for(GeneSet gs : allGeneSets){
 				pw.println(gs.toString());
 			}
 			pw.close();
 			mergeCount++;
 		}
 	}
 	
 }
