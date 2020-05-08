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
 import java.util.List;
 import java.util.Random;
 import java.util.Scanner;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  *
  * @author ryantlk
  */
 public class Intelli_Grep {
 	
 	String testPath;
 	List<String> thelist;
 
 	public Intelli_Grep(String testPath) {
 		this.testPath = testPath;
 		this.thelist = new ArrayList<>();
 	}
 	
 	public void classify() throws IOException{
 		FileVisitor<Path> fileprocessor = new ProcessFile(thelist);
 		Files.walkFileTree(Paths.get(testPath), fileprocessor);
 		File file = new File("./Intelli_Grep_Results.txt");
 		file.createNewFile();
 		PrintWriter pw = new PrintWriter(file);
 		for (String string : thelist) {
 			pw.println(string);
 		}
 		pw.close();
 	}
 	
 	private static final class ProcessFile extends SimpleFileVisitor<Path>{
 		List<String> thelist;
 		
 		public ProcessFile(List<String> thelist){
 			this.thelist = thelist;
 		}
 		
 		@Override
 		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
 			Scanner thescanner = new Scanner(new File(file.toString())).useDelimiter("\\Z");
 			String fileText = thescanner.next();
 			int DT, DR, L;
 			DT = DR = L = 0;
 			String classification = "DT";
 			fileText = fileText.replaceAll("[^a-zA-Z]", " ").toLowerCase().trim();
 			fileText = fileText.replaceAll("\n", " ");
 			fileText = fileText.replaceAll(" +", " ");
 			Pattern p = Pattern.compile("\\bdeed of trust\\b");
 			Matcher m = p.matcher(fileText);
 			while(m.find()){
 				DT++;
 			}
 			p = Pattern.compile("\\bdeed of reconveyance\\b");
 			m = p.matcher(fileText);
 			while(m.find()){
 				DR++;
 			}
 			p = Pattern.compile("\\blien\\b");
 			m = p.matcher(fileText);
 			while(m.find()){
 				L++;
 			}
 			int max = Math.max(DR, Math.max(DT, L));
 			List<String> maxStrings = new ArrayList<>();
 			if (DR == max) {
 				maxStrings.add("DR");
 			}
 			if (DT == max) {
 				maxStrings.add("DT");
 			}
 			if (L == max) {
 				maxStrings.add("L");
 			}
 			if (maxStrings.size() > 1) {
 				Random generator = new Random();
 				classification = maxStrings.get(generator.nextInt(maxStrings.size()));
 			}else{
 				classification = maxStrings.get(0);
 			}
 			thelist.add("Intelli-Grep,"+file.getFileName().toString()+","+classification);
 //			System.out.println(file.getFileName().toString() + "," + classification);
 			return FileVisitResult.CONTINUE;
 		}
 	}
 }
