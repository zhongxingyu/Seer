 package edu.iit.cs.cs553;
 
 import java.io.File;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Map;
 import java.util.HashMap;
 
 public class WordCountJ {
 
   private static void print_usuage() {
     System.out.println("Usuage: <javaClass> <input> <output>");
   }
 
   private static void addWord(Map<String, Integer> result, String word, Integer times) {
     Integer t = result.get(word);
     if(t!=null)
       t += times;
     else
       t = times;
 
     result.put(word, t);
   }
 
   private static void mergeResult(Map<String, Integer> result, File input) throws IOException {
     FileReader fr = new FileReader(input);
     BufferedReader br = new BufferedReader(fr);
 
     String line;
     while((line = br.readLine()) != null) {
       StringTokenizer st = new StringTokenizer(line, ":");
       String word = st.nextToken();
       Integer times = Integer.getInteger(st.nextToken());
       addWord(result, word, times);
     }
 
     br.close();
     fr.close();
   }
 
   public static void main(String[] args) throws IOException {
     if(args.length != 2) {
       print_usuage();
       return;
     }
     
     File input = new File(args[0]);
     File output = new File(args[1]);
 
     if(!input.exists() || !input.isDirectory()) {
       print_usuage();
       return;
     }
 
     File[] inputFiles = input.listFiles();
     Thread[] workers = new Thread[inputFiles.length];
 
     for(int i=0;i<inputFiles.length;i++) {
       workers[i] = new WordCounterThread(inputFiles[i]);
       workers[i].start();
     }
 
     for(int i=0;i<inputFiles.length;i++) {
       try {
         workers[i].join();
       } catch (InterruptedException e) {
         e.printStackTrace();
       }
     }
 
     if(!output.exists())
       output.mkdir();
 
     File[] outputFiles = output.listFiles();
     Map<String, Integer> result = new HashMap<String, Integer>();
     for(int i=0;i<outputFiles.length;i++) {
       // read the result of each file & merge them here
       mergeResult(result, outputFiles[i]);
     }
 
     FileWriter fw = new FileWriter(new File(output.getPath() + "/_result_trimmed.txt"));
     BufferedWriter bw = new BufferedWriter(fw);
     
     for(String key: result.keySet()) {
       bw.write(key);
       bw.write(" : ");
       bw.write(result.get(key));
       bw.newLine();
     }
 
     bw.close();
     fw.close();
   }
 }
