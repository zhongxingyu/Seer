 package ru.spbau.textminer.processor;
 
 import java.util.*;
 import java.io.*;
 
 public class ExecMystem {
 	public static void main(String[] args) {
         if (args.length != 5) {
             System.out.println("Usage: java [-cp ...] ru.spbau.textminer.ExecMystem path/to/mystem.exe input-file output-file temp-input temp-output");
             return;
         }
 
         String mystemPath = args[0];
         File inputFile = new File(args[1]);
         File outputFile = new File(args[2]);
         File tempFile = new File(args[3]);
 
         BufferedReader reader = null;
         PrintWriter writer = null;
 
         try {
             reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "cp1251"));
             writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "cp1251"));
 
             String line;
             while ((line = reader.readLine()) != null) {
                 System.out.println(line);
 
                 Writer tempWriter = new OutputStreamWriter(new FileOutputStream(tempFile), "cp1251");
                 tempWriter.write(line);
                 tempWriter.close();
 
                 Process p = Runtime.getRuntime().exec(new String[]{
                         mystemPath, "-ni",
                         args[3], args[4]}, null, tempFile.getAbsoluteFile().getParentFile());
                 try {
                     p.waitFor();
                 } catch (InterruptedException e) {}
 
                BufferedReader tempReader = new BufferedReader(new InputStreamReader(new FileInputStream("D:\\text\\apache-opennlp-1.5.1-incubating\\temp-output.txt"), "cp1251"));
                 String outputLine;
                 while ((outputLine = tempReader.readLine()) != null) {
                     writer.println(outputLine);
                 }
 
                 writer.println();
             }
         } catch (IOException e) {
             System.out.println(e.getMessage());
         } finally {
             if (reader != null) {
                 try {
                     reader.close();
                 } catch (IOException ex) {}
             }
             if (writer != null) {
                 writer.close();
             }
         }
 	}
}
