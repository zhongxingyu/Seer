 package matching;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Scanner;
 import java.util.Set;
 
 import analysis.Analyze;
 
 import wrappers.Options;
 import wrappers.PerformanceFactor;
 import wrappers.Result;
 
 public class Main {
 	public static boolean DEBUG = false;
 	
 	public static void main(String[] args) throws IOException, InterruptedException{
 		Options opt = new Options(args);
 		if(opt.usage)
 			return;
 		
 		if(checkFiles(opt))
 			return;
 		
 		File output = new File(opt.OUTPUT);
 		BufferedWriter out = new BufferedWriter(new FileWriter(output));
 		Freebase fb = Freebase.loadFreebase(true, opt.FREEBASE, opt.WIKI_ALIAS, opt.LUCENE_THRESHOLD);
 		
 		do{
 			if(new File(opt.INPUT).exists()){
 				List<String> rv = loadTuples(opt);
				moveFile(opt);
 				int cnt = 0;
 				long timer = System.nanoTime();
 				for(String rvEnt : rv){
 					Result res = fb.getMatches(rvEnt, new PerformanceFactor());
 					out.write(res.toString(opt.MAX_MATCHES));
 					out.flush();
 					
 					cnt++;
 					if(cnt % 50000 == 0){
 						double perEntry = (System.nanoTime() - timer) / (double)cnt;
 						System.out.println("Average match rate: " + 1.0 / (perEntry / 1000000000.0) + " entities per second at " + cnt / (double)rv.size() + "%.");
 					}
 				}
 				timer = System.nanoTime() - timer;
 				double perEntry = timer / (double)cnt;
 				System.out.println("Average match rate: " + 1.0 / (perEntry / 1000000000.0) + " entities per second.");
 				
 				if(opt.test){
 					System.out.println("Accuracy for top 5: " + Analyze.analyze(fb, Analyze.loadCorrectMatches(), output, 5));
 				}
 			}else if(opt.monitor){
 				System.out.print(".");
 				Thread.sleep(10000);
 			}else{
 				System.out.println("Nothing to do!");
 			}
 		} while(opt.monitor);
 	}
 	
 	private static void moveFile(Options opt){
 		File source = new File(opt.INPUT);
 		File dest = new File("processed");
 		if(!dest.exists() || !dest.isDirectory())
 			dest.mkdir();
 		String newName = source.getName() + "." + new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
 		File newFile = new File(dest, newName);
 		boolean success = source.renameTo(newFile);
 		if(!success){
 			opt.monitor = false;
 			System.out.println("ERROR: Failed to move input file into processed folder!");
 		}
 	}
 	
 	private static List<String> loadTuples(Options opt) throws FileNotFoundException{
 		System.out.print("Loading ReVerb Strings...");
 		Set<String> rv = new HashSet<String>();
 		File input = new File(opt.INPUT);
 		Scanner s = new Scanner(input);
 		while(s.hasNextLine()){
 			rv.add(s.nextLine().split("\t")[1].trim());
 		}
 		
 		List<String> rtn = new ArrayList<String>(rv);
 		Collections.sort(rtn);
 		s.close();
 		System.out.println("Complete!");
 		return rtn;
 	}
 	
 	private static boolean checkFiles(Options opt){
 		boolean failed = false;
 		File f = new File(opt.FREEBASE);
 		if(!f.exists()){
 			System.out.println("Could not find Freebase file: \"" + f.getAbsolutePath() + "\"");
 			failed = true;
 		}
 		
 		f = new File(opt.WIKI_ALIAS);
 		if(!f.exists()){
 			System.out.println("Could not find Wiki Alias file: \"" + f.getAbsolutePath() + "\"");
 			failed = true;
 		}
 		f = new File(opt.FREEBASE);
 		if(!f.exists()){
 			System.out.println("Could not find Freebase file: \"" + f.getAbsolutePath() + "\"");
 			failed = true;
 		}
 		f = new File(Freebase.WEIGHTS_CONFIG);
 		if(!f.exists()){
 			System.out.println("Could not find weights.config");
 			failed = true;
 		}
 		return failed;
 	}
 }
