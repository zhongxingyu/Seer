 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ai_project;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.nio.file.FileVisitResult;
 import java.nio.file.FileVisitor;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.nio.file.SimpleFileVisitor;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.StringTokenizer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  *
  * @author ryantlk
  */
 public class Naive_Bayes {
 	//paths index 0 = DR, 1 = DT, 2 = L, 3 = TEST
 	String paths[];
 	int DRFileCount, DTFileCount, LFileCount;
 	Double ProbabilityOfDR, ProbabilityOfDT, ProbabilityOfL;
 	Double DRProbabilityVector[];
 	Double DTProbabilityVector[];
 	Double LProbabilityVector[];
 	List<String> features;
 	List<String> thelist;
 
 	public Naive_Bayes(String path1, String path2, String path3, String path4) {
 		paths = new String[4];
 		this.paths[pathIndex(path1)] = path1;
 		this.paths[pathIndex(path2)] = path2;
 		this.paths[pathIndex(path3)] = path3;
 		this.paths[pathIndex(path4)] = path4;
 		this.thelist = new ArrayList<>();
 		DRFileCount = new File(path1).list().length;
 		DTFileCount = new File(path2).list().length;
 		LFileCount = new File(path3).list().length;
 		ProbabilityOfDR = (double) DRFileCount/(DRFileCount + DTFileCount + LFileCount);
 		ProbabilityOfDT = (double) DTFileCount/(DRFileCount + DTFileCount + LFileCount);
 		ProbabilityOfL = (double) LFileCount/(DRFileCount + DTFileCount + LFileCount);
 	}
 	
 	public void train() throws IOException{
 		//total word counts for classes same indexing as paths
 		int totalWordCounts[] = new int[3];
 		
 		/* Hashmaps built from files key is word in document collection and 
 		 * the value is the number of times that word occurs in the document
 		 * collection.
 		 */
 		HashMap<String, Integer> DRHashMap = new HashMap<>();
 		HashMap<String, Integer> DTHashMap = new HashMap<>();
 		HashMap<String, Integer> LHashMap = new HashMap<>();
 		
 		//Building hashmaps from files in provided paths.
 		FileVisitor<Path> fileprocessor = new WordCounts(DRHashMap, totalWordCounts, 0);
 		Files.walkFileTree(Paths.get(paths[0]), fileprocessor);
 		fileprocessor = new WordCounts(DTHashMap, totalWordCounts, 1);
 		Files.walkFileTree(Paths.get(paths[1]), fileprocessor);
 		fileprocessor = new WordCounts(LHashMap, totalWordCounts, 2);
 		Files.walkFileTree(Paths.get(paths[2]), fileprocessor);
 		
 		/* Build sorted array listed sorted by value then key from
 		 * hashmap entries. Gives list of words in document collection
 		 * sorted by amount then alphabetically.
 		 */
 		List<Map.Entry<String, Integer>> DRSortedEntrys = new ArrayList<>(DRHashMap.entrySet());
 		Collections.sort(DRSortedEntrys, new MapComparator());
 		List<Map.Entry<String, Integer>> DTSortedEntrys = new ArrayList<>(DTHashMap.entrySet());
 		Collections.sort(DTSortedEntrys, new MapComparator());
 		List<Map.Entry<String, Integer>> LSortedEntrys = new ArrayList<>(LHashMap.entrySet());
 		Collections.sort(LSortedEntrys, new MapComparator());
 		ListIterator<Map.Entry<String, Integer>> iter1 = DRSortedEntrys.listIterator();
 		ListIterator<Map.Entry<String, Integer>> iter2 = DTSortedEntrys.listIterator();
 		ListIterator<Map.Entry<String, Integer>> iter3 = LSortedEntrys.listIterator();
 		
 		features = new ArrayList<>();
 		
 		//create list of features
 		for(int i = 0; i < 20; i++){
 			Map.Entry iter1ent = iter1.next();
 			Map.Entry iter2ent = iter2.next();
 			Map.Entry iter3ent = iter3.next();
 			if (!features.contains(iter1ent.getKey().toString())) {
 				features.add(iter1ent.getKey().toString());
 			}
 			if (!features.contains(iter2ent.getKey().toString())) {
 				features.add(iter2ent.getKey().toString());
 			}
 			if (!features.contains(iter3ent.getKey().toString())) {
 				features.add(iter3ent.getKey().toString());
 			}
 		}
 		
 		int featureCount = features.size();
 		
 		//Arrays of feature vectors
 		Byte DRTrainedFeatures[][] = new Byte[DRFileCount][featureCount];
 		Byte DTTrainedFeatures[][] = new Byte[DTFileCount][featureCount];
 		Byte LTrainedFeatures[][] = new Byte[LFileCount][featureCount];
 		
 		//building feature vectors by running through files again
 		FileVisitor<Path> vectorProcessor = new BuildFeatureVectors(DRTrainedFeatures, features);
 		Files.walkFileTree(Paths.get(paths[0]), vectorProcessor);
 		vectorProcessor = new BuildFeatureVectors(DTTrainedFeatures, features);
 		Files.walkFileTree(Paths.get(paths[1]), vectorProcessor);
 		vectorProcessor = new BuildFeatureVectors(LTrainedFeatures, features);
 		Files.walkFileTree(Paths.get(paths[2]), vectorProcessor);
 		
 		DRProbabilityVector = new Double[featureCount];
 		DTProbabilityVector = new Double[featureCount];
 		LProbabilityVector = new Double[featureCount];
 		
 		ComputeProbabilities(DRTrainedFeatures, DRProbabilityVector, featureCount, DRFileCount);
 		ComputeProbabilities(DTTrainedFeatures, DTProbabilityVector, featureCount, DTFileCount);
 		ComputeProbabilities(LTrainedFeatures, LProbabilityVector, featureCount, LFileCount);
 	}
 	
 	public void Test() throws IOException{
 		FileVisitor<Path> classifyProcessor = new ClassifyDocuments(DRProbabilityVector, DTProbabilityVector, LProbabilityVector, features,
 																	ProbabilityOfDR, ProbabilityOfDT, ProbabilityOfL, thelist);
 		Files.walkFileTree(Paths.get(paths[3]), classifyProcessor);
 		File file = new File("./NB_results.txt");
 		file.createNewFile();
 		PrintWriter pw = new PrintWriter(file);
 		for (String string : thelist) {
 			pw.println(string);
 		}
 		pw.close();
 	}
 	
 	private static class ClassifyDocuments extends SimpleFileVisitor<Path>{
 		Double DRProbabilities[], DTProbabilities[], LProbabilities[];
 		Double ProbabilityOfDR, ProbabilityOfDT, ProbabilityOfL;
 		List<String> featureList;
 		List<String> thelist;
 		
 		public ClassifyDocuments(Double DRProbabilities[], Double DTProbabilities[], Double LProbabilities[], List<String> featureList,
 								 Double ProbabilityOfDR, Double ProbabilityOfDT, Double ProbabilityOfL, List<String> thelist){
 			this.featureList = featureList;
 			this.thelist = thelist;
 			this.DRProbabilities = DRProbabilities;
 			this.DTProbabilities = DTProbabilities;
 			this.LProbabilities = LProbabilities;
 			this.ProbabilityOfDR = ProbabilityOfDR;
 			this.ProbabilityOfDT = ProbabilityOfDT;
 			this.ProbabilityOfL = ProbabilityOfL;
 		}
 		
 		@Override
 		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
 			Scanner thescanner = new Scanner(new File(file.toString())).useDelimiter("\\Z");
 			String fileText = thescanner.next();
 			fileText = fileText.replaceAll("[^a-zA-Z]", " ").toLowerCase().trim();
 			fileText = fileText.replaceAll("\n", " ");
 			fileText = fileText.replaceAll(" +", " ");
 			Double DRProbability, DTProbability, LProbability;
 			DRProbability = DTProbability = LProbability = 1.0;
 			for (int i = 0; i < featureList.size(); i++) {
 				Pattern p = Pattern.compile("\\b"+featureList.get(i)+"\\b");
 				Matcher m = p.matcher(fileText);
 				if (m.find()) {
 					DRProbability = DRProbability * DRProbabilities[i];
 					DTProbability = DTProbability * DTProbabilities[i];
 					LProbability = LProbability * LProbabilities[i];
 				}else{
 					DRProbability = DRProbability * (1 - DRProbabilities[i]);
 					DTProbability = DTProbability * (1 - DTProbabilities[i]);
 					LProbability = LProbability * (1 - LProbabilities[i]);
 				}
 			}
 			DRProbability = DRProbability * ProbabilityOfDR;
 			DTProbability = DTProbability * ProbabilityOfDT;
 			LProbability = LProbability * ProbabilityOfL;
 			Double maxProbability = Math.max(DRProbability, Math.max(DTProbability, LProbability));
 			String classification;
 			if (DRProbability.equals(maxProbability)) {
 				classification = "DR";
 			}else if (DTProbability.equals(maxProbability)){
 				classification = "DT";
 			}else{
 				classification = "L";
 			}
 			thelist.add("Naive Bayes Baseline," + file.getFileName().toString() + "," + classification);
 //			System.out.println("Naive Bayes Baseline," + file.getFileName().toString() + "," + classification);
 			return FileVisitResult.CONTINUE;
 		}
 	}
 	
 	private void ComputeProbabilities(Byte[][] TrainedFeatures, Double[] Probabilities, int featureCount, int classFileCount){
 		for (int i = 0; i < featureCount; i++) {
 			int sum = 0;
 			for (int j = 0; j < classFileCount; j++) {
 				if (TrainedFeatures[j][i] > 0) {
 					sum++;
 				}
 			}
 			if (sum == 0) {
 				Probabilities[i] = (double) 1/classFileCount;
 			}else{
 				Probabilities[i] = (double) sum/classFileCount;
 			}
 		}
 	}
 	
 	private static class MapComparator implements Comparator<Map.Entry<String, Integer>>{
 		/* Sorts by value then alphabetically */
 		@Override
 		public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
 			if(a.getValue() == b.getValue()){
 				return a.getKey().compareTo(b.getKey());
 			}else{
 				return b.getValue().compareTo(a.getValue());
 			}
 		}
 		
 	}
 	
 	private int pathIndex(String path){
 		/* Returns index based on which path is given
 		 * The index is returned based on the chosen order of paths[]
 		 * See comment line 25
 		 */
 		int returnIndex;
 		
 		if(path.contains("DR")){
 			returnIndex = 0;
 		}else if(path.contains("DT")){
 			returnIndex = 1;
 		}else if(path.contains("L")){
 			returnIndex = 2;
 		}else{
 			returnIndex = 3;
 		}
 		return returnIndex;
 	}
 	
 	private static class BuildFeatureVectors extends SimpleFileVisitor<Path>{
 		Byte[][] Vector;
 		List<String> featureList;
 		int vectorFileIndex = 0;
 		int vectorFeatureIndex = 0;
 		
 		public BuildFeatureVectors(Byte[][] Vector, List<String> featureList){
 			this.Vector = Vector;
 			this.featureList = featureList;
 		}
 		
 		@Override
 		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
 			Scanner thescanner = new Scanner(new File(file.toString())).useDelimiter("\\Z");
 			String fileText = thescanner.next();
 			fileText = fileText.replaceAll("[^a-zA-Z]", " ").toLowerCase().trim();
 			fileText = fileText.replaceAll("\n", " ");
 			fileText = fileText.replaceAll(" +", " ");
 			for (int i = 0; i < featureList.size(); i++) {
 				Pattern p = Pattern.compile("\\b"+featureList.get(i)+"\\b");
 				Matcher m = p.matcher(fileText);
 				if (m.find()) {
 					Vector[vectorFileIndex][i] = 1;
 				}else{
 					Vector[vectorFileIndex][i] = 0;
 				}
 			}
 			vectorFileIndex++;
 			return FileVisitResult.CONTINUE;
 		}
 	}
 	
 	private static class WordCounts extends SimpleFileVisitor<Path>{
 		HashMap<String, Integer> themap;
 		int totalWordCounts[];
 		int index;
 		
 		public WordCounts(HashMap themap, int totalWordCounts[], int index) {
 			this.themap = themap;
 			this.totalWordCounts = totalWordCounts;
 			this.index = index;
 		}
 		
 		@Override
 		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
 			Scanner thescanner = new Scanner(new File(file.toString())).useDelimiter("\\Z");
 			String fileText = thescanner.next();
 			/* Replace all non alphabetic characters and newlines with spaces.
 			 * Then reduce all whitespace to a single space.
 			 */
 			fileText = fileText.replaceAll("[^a-zA-Z]", " ").toLowerCase().trim();
 			fileText = fileText.replaceAll("\n", " ");
 			fileText = fileText.replaceAll(" +", " ");
 			StringTokenizer st = new StringTokenizer(fileText);
 			/* Goes through each word in the current document and if the
 			 * word is in the hashmap icrements the count. Otherwise 
 			 * the word is added to the hashmap.
 			 */
 			while(st.hasMoreTokens()){
 				String token = st.nextToken();
 				if(themap.containsKey(token)){
 					themap.put(token, themap.get(token) + 1);
 				}else{
 					themap.put(token, 1);
 				}
 				totalWordCounts[index]++;
 			}
 			return FileVisitResult.CONTINUE;
 		}
 	}
 }
