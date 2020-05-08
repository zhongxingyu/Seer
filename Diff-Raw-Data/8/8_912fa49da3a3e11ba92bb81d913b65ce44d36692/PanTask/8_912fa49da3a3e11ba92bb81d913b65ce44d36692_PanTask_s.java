 package authorverification;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Reader;
 import java.util.HashMap;
 
 import authorverification.reader.BasicAlphabetReader;
 import authorverification.reader.NoInterpunctionReader;
 import authorverification.reader.StaticNumberReader;
 
 public class PanTask {
 
 	public static PrintWriter resultOutput;
 
 	public static void main(String[] args) throws IOException{
 		if(args == null || args.length < 2){
 			args = new String[2];
 			args[0] = "corpus/training";
 			args[1] = ".";
 		}
 		
 		if(args.length < 2){
 			System.out.println("Not enough parameters given. Expected: [input path] [output path]");
 			System.exit(0);
 		}
 		
 		File corpus = new File(args[0]);
 		if(!corpus.isDirectory()){
			System.out.println("First parameter is not a directory. Expected: [input path] [output path]");
 			System.exit(0);
 		}
 
 		resultOutput = new PrintWriter(new FileWriter(new File(args[1]+"/answers.txt")));
 		
 		baseNGrams(corpus, "EN", 4, 2300);
 		baseNGrams(corpus, "GR", 3, 1500);
 		baseNGrams(corpus, "SP", 4, 2300);
 		
 		resultOutput.flush();
 		resultOutput.close();
 	}
 
 	private static void baseNGrams(File corpus, String language, int n, int profileSize) throws IOException{
 		
 		File[] instances = corpus.listFiles();
 
 		HashMap<String, HashMap<String, Double>> knownAuthorForInstances = new HashMap<String, HashMap<String, Double>>();
 		HashMap<String, HashMap<String, Double>> unknownForInstances = new HashMap<String, HashMap<String, Double>>();
 		
 		for(File instance : instances){
 			if(instance.isDirectory()){
 				
 				String name = instance.getName();
 				
 				if(name.startsWith(language)){
 				
 					File[] authorfiles = instance.listFiles();
 					
 					HashMap<String, Double> knownAuthor = new HashMap<String, Double>();
 					HashMap<String, Double> unknown = new HashMap<String, Double>();
 					
 					for(File f : authorfiles){
 						Reader reader = null;
 						if(language.equals("GR")) 
 							reader = new StaticNumberReader(new NoInterpunctionReader(new FileReader(f)));
 						else
 							reader = new StaticNumberReader(new BasicAlphabetReader(new NoInterpunctionReader(new FileReader(f))));
 						
 						if(f.getName().startsWith("known")){
 							knownAuthor = Tools.addCharacterNGrams(reader, n, knownAuthor);
 						}
 						else if(f.getName().startsWith("unknown")){
 							unknown = Tools.addCharacterNGrams(reader, n, unknown);
 						}
 						else{
 							System.err.println("ERROR: Unexpected file: "+f.getAbsolutePath());
 						}
 						reader.close();
 					}
 					
 					knownAuthorForInstances.put(name, knownAuthor);
 					unknownForInstances.put(name, unknown);
 				}	
 			}
 		}
 		
 		HashMap<String, Double> distances = new HashMap<String, Double>();
 		
 		for(File instance : instances){
 			if(instance.isDirectory()){
 				String name = instance.getName();
 				
 				if(name.startsWith(language)){
 					
 					HashMap<String, Double> knownAuthor = knownAuthorForInstances.get(name);
 					HashMap<String, Double> unknown = unknownForInstances.get(name);
 					
 					knownAuthor = Tools.keepHighestN(knownAuthor, profileSize, true);
 
 					knownAuthor = Tools.normalizeNGrams(knownAuthor);
 					unknown = Tools.normalizeNGrams(unknown);
 					
 					double distance = Tools.distanceStamatatos2007(unknown, knownAuthor);
 
 					distances.put(name, distance);
 				}
 			}
 		}
 		
 		HashMap<String, Boolean> judgements = getJudgements(distances);
 		
 		for(File instance : instances){
 			if(instance.isDirectory()){
 				String name = instance.getName();
 				
 				if(name.startsWith(language)){
 					boolean judgement = judgements.get(name);
 					String output = name+" " + (judgement ? "Y" : "N"); 
 					resultOutput.println(output);
 				}
 			}
 		}
 		
 	}
 
 	/**
 	 * Evaluates a list of judgments per instance, returning for every instance a true/false judgment.
 	 *  
 	 * @param distances The list of judgments per instance. True means the author is considered the same for the known and unknown documents.
 	 * @return A list of true/false judgments, mapped from the instance names.
 	 */
 	private static HashMap<String, Boolean> getJudgements(HashMap<String, Double> distances){
 		HashMap<String, Boolean> judgements = new HashMap<String, Boolean>();
 		
 		double average = 0d;
 		
 		for(String instance : distances.keySet()){
 			average += distances.get(instance);
 		}
 		
 		average = average / ((double)distances.size());
 
 		for(String instance : distances.keySet()){
 			if(distances.get(instance) < average){
 				judgements.put(instance, true);
 			}
 			else{
 				judgements.put(instance, false);
 			}
 		}
 		
 		return judgements;
 	}
 }
