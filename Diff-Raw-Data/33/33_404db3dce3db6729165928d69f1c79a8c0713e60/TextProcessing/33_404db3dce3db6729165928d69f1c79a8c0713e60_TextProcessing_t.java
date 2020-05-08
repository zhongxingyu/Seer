 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.github.nethad.clustermeister.example.textproc;
 
 import com.github.nethad.clustermeister.api.Clustermeister;
import com.github.nethad.clustermeister.api.impl.Job;
 import com.github.nethad.clustermeister.api.impl.ClustermeisterFactory;
 import com.github.nethad.clustermeister.api.impl.JobFactory;
 import com.google.common.base.Strings;
 import com.google.common.io.CharStreams;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.jppf.utils.StringUtils;
 
 /**
  *
  * @author thomas
  */
 public class TextProcessing {
     private Clustermeister clustermeister;
     
     public static void main(String... args) {
         new TextProcessing().execute();
     }
 
     private void execute() {
         try {
             if (clustermeister == null) {
                 clustermeister = ClustermeisterFactory.create();
             }
             
             Map<String, Object> jobData = new HashMap<String, Object>();
             
             // input file is 1.4 MB.
             float inputFileSize = 1.4F;
             int numberOfMegabytes = 60;
             int howManyTimesToRepeat = (int) (numberOfMegabytes / inputFileSize);
             System.out.println("Repeat input file: "+howManyTimesToRepeat);
             String text = Strings.repeat(readTextFile(), howManyTimesToRepeat);
             jobData.put("text", text);
             
             Job<TextProcessingResult> job = JobFactory.create("Clustermeister Mandelbrot", jobData);
             
             job.addTask(new TextProcessingTask());
             job.addTask(new TextProcessingTask());
             job.addTask(new TextProcessingTask());
             job.addTask(new TextProcessingTask());
             
                         long start = System.currentTimeMillis();
 
             List<TextProcessingResult> results = clustermeister.executeJob(job);
             
             long elapsed = System.currentTimeMillis() - start;
             System.out.println("Computation performed in " + StringUtils.toStringDuration(elapsed));
             
             for (TextProcessingResult result : results) {
                 StringBuilder sb = new StringBuilder("Result:\n");
                       sb.append("word count = ").append(result.getWordCount()).append("\n")
                         .append("avg word length = ").append(result.getAvgWordLength()).append("\n")
                         .append("vowel a count = ").append(result.getVowelCountA()).append("\n")
                         .append("vowel e count = ").append(result.getVowelCountE()).append("\n")
                         .append("vowel i count = ").append(result.getVowelCountI()).append("\n")
                         .append("vowel o count = ").append(result.getVowelCountO()).append("\n")
                         .append("vowel u count = ").append(result.getVowelCountU()).append("\n");
                 System.out.println(sb.toString());
             }
         } catch (Exception ex) {
             System.err.println(ex.getMessage());
             ex.printStackTrace();
         } finally {
             if (clustermeister != null) {
                 clustermeister.shutdown();
             }
         }
     }
     
     private String readTextFile() {
         try {
             return CharStreams.toString(new InputStreamReader(getClass().getResourceAsStream("/all.txt"), Charset.defaultCharset()));
         } catch (IOException ex) {
             System.err.println("Could not read text file. Exit.");
             System.exit(0);
         }
         return null; // not reachable
     }
     
 }
