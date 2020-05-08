 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.tokenizer.util;
 
 import com.tokenizer.controller.*;
 import com.tokenizer.model.BigConcurentHashMap;
 import java.io.File;
 import java.io.IOException;
 import java.nio.charset.StandardCharsets;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.HashMap;
 import java.util.concurrent.Callable;
 
 /**
  *
  * @author irfannurhakim
  */
 public class FileReader implements Callable {
 
     private FileWalker fileWalker;
     private File file;
     private Path path;
     private int count;
 
     public FileReader(){
         
     }
     
     public FileReader(File file) {
         this.file = file;
     }
 
     public FileReader(Path path, int count) {
         this.path = path;
         this.count = count;
     }
 
     public File getFile() {
         return file;
     }
 
     public void setFile(File file) {
         this.file = file;
     }
     /*
      * public StringBuilder getText() { return text; }
      *
      * public void setText(StringBuilder text) { this.text = text; }
      */
 
     public void setCaller(FileWalker fileWalker) {
         this.fileWalker = fileWalker;
     }
 
     public FileWalker getCaller() {
         return fileWalker;
     }
 
     @Override
     public Object call() throws IOException, InterruptedException {
 
         String line = Files.readAllLines(this.path, StandardCharsets.UTF_8).toString().toLowerCase().replaceAll("x-to|x-from", "");
         //System.out.println(line);
      
         
         /*
          * raw -> array 0 head, array 1 tail
          */
         String[] raw = line.split("date: ", 2);
         
         String [] date = raw[1].split("from: ",2);
         HashMap <String,Integer> dateMap = dateTokenizer.getListDate(date[0]);    
         
         synchronized(BigConcurentHashMap.dateConcurentMap )
         {
         BigConcurentHashMap.mergeBigHashMap(BigConcurentHashMap.dateConcurentMap, dateMap);
         }
         
         //System.out.println(dateMap);
         
         if (date.length == 1) {
             date[1] = "";
         }
 
         String[] from;
         if (date[1].contains("to: ")) {
             from = date[1].split("to: ", 2);
         } else {
             from = date;
         }        
         
        HashMap <String,Integer> fromMap = FromTokenizer.getListFrom(from[0].replaceAll(", ", ""));
         
         synchronized(BigConcurentHashMap.fromConcurentMap )
         {
         BigConcurentHashMap.mergeBigHashMap(BigConcurentHashMap.fromConcurentMap, fromMap);
         }
         //System.out.println(fromMap);
         
         if (from.length == 1) {
             from[1] = "";
         }
 
         String[] to;
         if (from[1].contains("subject: ")) {
             to = from[1].split("subject: ", 2);
         } else {
             to = from;
         }
         
         HashMap <String,Integer> toMap = toTokenizer.getListTo(to[0]);
          
         synchronized(BigConcurentHashMap.toConcurentMap )
         {
         BigConcurentHashMap.mergeBigHashMap(BigConcurentHashMap.toConcurentMap, toMap);
         }
         
         if (to.length == 1) {
             to[1] = "";
         }
 
         String[] subject;
        if (to[1].contains("mime-version : ")) {
             subject = to[1].split("mime-version: ", 2);
         } else {
             subject = to;
         }
         
         HashMap <String,Integer> subjectMap = subject_bodyTokenizer.getListTerm(subject[0]);
         
         synchronized(BigConcurentHashMap.subjectConcurentMap )
         {
         BigConcurentHashMap.mergeBigHashMap(BigConcurentHashMap.subjectConcurentMap, subjectMap);
         }
 
         HashMap <String,Integer> bodyMap = new HashMap<String, Integer>();
         
         
         if (subject.length == 1) {
             subject[1] = "";
         }
 
         String[] body;
         if (subject[1].contains(".pst") || subject[1].contains(".nsf")) {
             body = subject[1].split("(\\.pst)|(\\.nsf)", 2);
         } else {
             body = subject;
         }
 
         if (body.length == 1) {
             body[1] = "";
         }        
         
         bodyMap= subject_bodyTokenizer.getListTerm(body[1]);
         
         synchronized(BigConcurentHashMap.bodyConcurentMap )
         {
         BigConcurentHashMap.mergeBigHashMap(BigConcurentHashMap.bodyConcurentMap, bodyMap);
         }
        
         HashMap <String,Integer> allFieldMap = AllFieldTokenizer.allFieldTermList(dateMap, toMap, fromMap, subjectMap, bodyMap);
        
         synchronized(BigConcurentHashMap.allConcurentMap )
         {
         BigConcurentHashMap.mergeBigHashMap(BigConcurentHashMap.allConcurentMap, allFieldMap);
         }
         //System.out.println(allFieldMap);
         
  
         //System.out.println(path.toString());
         fileWalker.callback(dateMap, fromMap, toMap, subjectMap, bodyMap, allFieldMap, count);
         return null;
     }
 }
