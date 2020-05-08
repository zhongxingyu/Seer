 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package utils;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 /**
  * Logging class
  * @author pavel
  */
 public class Log {
     
     /**
      * Output file path
      */
     private File outputFile;
 
     public Log(String outputPath) {
         this.outputFile = new File(outputPath);
     }
     
     public void logNode(String log, int logTime, int id){
         try{
             BufferedWriter bw = openFile();
             
             String logMess = "["+logTime +":"+ id+"] -> "+log;
             System.out.println(logMess);
             bw.write(logMess);
             bw.newLine();
             bw.close();
         }catch(IOException e){
             System.out.print("ERROR - "+e.toString());
         }
     }
     
         public void logDirectory(String log){
         try{
             BufferedWriter bw = openFile();
             
             String logMess = log;
             System.out.println(logMess);
             bw.write(logMess);
             bw.newLine();
             bw.close();
         }catch(IOException e){
             System.out.print("ERROR - "+e.toString());
         }
     }
 
 
     /**
      * Opens file with bufferedWriter
      * @return 
      */
     private BufferedWriter openFile() throws IOException {
        return new BufferedWriter(new FileWriter(this.outputFile));
     }
     
     
 }
