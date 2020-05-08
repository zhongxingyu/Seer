 package ir.ac.iust.nlp.postagger;
 
 import java.io.*;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  *
  * @author Mojtaba Khallash
  */
 public class RunnableTagging implements Runnable {
 
     POSTaggerForm target = null;
     public static PrintStream out = System.out;
     
     String model;
     String inputFile;
     String outputFile;
     String goldFile;
 
     public RunnableTagging(POSTaggerForm target, String model, String input, String output, String gold) {
         this.target = target;
         this.model = model;
         this.inputFile = input;
         this.outputFile = output;
         this.goldFile = gold;
         
     }
 
     @Override
     public void run() {
         int total = 0;
         int correct = 0;
                 
         BufferedReader inputReader = null;
         BufferedReader goldReader = null;
         Writer outputWriter = null;
         try {
             Process p = Runtime.getRuntime().exec("java -Dfile.encoding=UTF8 -cp lib" + File.separator + 
                     "mxpost.jar tagger.TestTagger " + this.model);
             /* Equal to */
             // tagger.TestTagger.main(new String[] {this.model});
             /*          */
 
             BufferedReader stdError = new BufferedReader(new InputStreamReader(
                     p.getErrorStream()));
             BufferedReader stdRead = new BufferedReader(new InputStreamReader(
                     p.getInputStream(), "UTF-8"));
             PrintWriter processWriter = new PrintWriter(p.getOutputStream(), true);
 
             inputReader = new BufferedReader(new InputStreamReader(
                     new FileInputStream(inputFile), "UTF8"));
             if (goldFile != null) {
                 goldReader = new BufferedReader(new FileReader(goldFile));
             }
             String line;
             outputWriter = new BufferedWriter(new OutputStreamWriter(
                     new FileOutputStream(outputFile, true), "UTF-8"));
 
             String s;
             while ((s = stdError.readLine()) != null) {
                 out.println(s);
                 if (s.equals("*Copyright (c) 1997 Adwait Ratnaparkhi*")) {
                     while ((line = inputReader.readLine()) != null) {
                         processWriter.println(line);
 
                         s = stdError.readLine();
                         if (s != null) {
                             out.println(s);
                         }
                         s = stdRead.readLine();
                         StringBuilder newLine = new StringBuilder();
                         
                         List<String> words = new LinkedList<>();
                         List<String> predictTags = new LinkedList<>();
                         List<String> goldTags = new LinkedList<>();
 
                         String[] parts = line.split(" ");
                         words.addAll(Arrays.asList(parts));
                         total += parts.length;
                         String[] parts2 = null;
                         if (goldReader!=null) {
                             line = goldReader.readLine();
                             if (line != null) {
                                 parts2 = line.split(" ");
                             }
                         }
                         String[] parts1 = s.split(" ");
                         String[] vals1, vals2 = null;
                         for (int i=0; i<parts1.length; i++) {
                             vals1 = parts1[i].split("_");
                             if (parts2!= null) {
                                 vals2 = parts2[i].split("_");
                             }
                             
                             String predictTag = vals1[vals1.length - 1];
                             if (newLine.length() != 0) {
                                 newLine.append(" ");
                             }
                             newLine.append(parts[i]).append("_").append(predictTag);
                             
                             predictTags.add(predictTag);
                             if (goldTags != null && vals2 != null) {
                                 String goldTag = vals2[vals2.length - 1];
                                 goldTags.add(goldTag);
                                 
                                if (predictTag.equals(goldTag))
                                     correct++;
                             }
                         }
                         outputWriter.write(newLine.toString() + "\n");
                         
                         if (target != null) {
                             target.UpdateList(words, predictTags, goldTags);
                         }
                     }
                     break;
                 }
             }
             p.destroy();
         } catch (Exception ex) {
         } finally {
             if (inputReader != null) {
                 try {
                     inputReader.close();
                 } catch (IOException ex) {
                 }
             }
             if (outputWriter != null) {
                 try {
                     outputWriter.close();
                 } catch (IOException ex) {
                 }
             }
             
                 int acc = 0;
                if (correct != 0)
                     acc = (correct * 100) / total;
                 String res = "Total: " + total + 
                         " - Correct: " + correct + " - Accuracy: " + acc + "%";
             if (target != null) {
                 target.tagThreadFinished(res);
             }
             else if (out == System.out) {
                 out.println(res);
             }
         }
     }
 }
