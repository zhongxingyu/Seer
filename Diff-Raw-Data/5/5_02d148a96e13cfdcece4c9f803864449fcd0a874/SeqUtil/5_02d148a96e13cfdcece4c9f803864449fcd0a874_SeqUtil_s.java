 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.era7.lib.bioinfo.bioinfoutil.seq;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 
 /**
  *
  * @author Pablo Pareja Tobes <ppareja@era7.com>
  */
 public class SeqUtil {
     
     private static HashMap<String,String> translationMap = null;
     
     public static String getComplementaryInverted(String sequence){
         
         StringBuilder result = new StringBuilder();
         
         for (int i = sequence.length() - 1 ; i >= 0 ; i--) {
             
             char currentChar = sequence.charAt(i);
             char valueToAppend;
             
             if(Character.isUpperCase(currentChar)){
                 
                 if(currentChar == 'A'){
                     valueToAppend = 'T';
                 }else if(currentChar == 'T'){
                     valueToAppend = 'A';
                }if(currentChar == 'C'){
                     valueToAppend = 'G';
                 }else if(currentChar == 'G'){
                     valueToAppend = 'C';
                 }else{
                     valueToAppend = 'N';
                 }
                         
             }else{
                 
                 if(currentChar == 'a'){
                     valueToAppend = 't';
                 }else if(currentChar == 't'){
                     valueToAppend = 'a';
                }if(currentChar == 'c'){
                     valueToAppend = 'g';
                 }else if(currentChar == 'g'){
                     valueToAppend = 'c';
                 }else{
                     valueToAppend = 'n';
                 }
                 
             }
             
             result.append(valueToAppend);
             
         }
         
         
         return result.toString();
     }
  
     public static String translateDNAtoProtein(String sequence, File geneticCodeFile) throws FileNotFoundException, IOException{
         
         initTranslationMap(geneticCodeFile);
      
         StringBuilder result = new StringBuilder();
         
         for (int i = 0; i <= sequence.length() - 3 ; i+=3) {
             result.append(translationMap.get(sequence.substring(i, i+3)));
         }
         
         return result.toString();
         
     }
 
     private synchronized static void initTranslationMap(File geneticCodeFile) throws FileNotFoundException, IOException{
         if(translationMap == null){
             
             translationMap = new HashMap<String, String>();
             
             BufferedReader reader = new BufferedReader(new FileReader(geneticCodeFile));
             String line = null;
             while((line = reader.readLine()) != null){
                 String[] columns = line.split(" ");
                 translationMap.put(columns[0], columns[1]);
             }            
             reader.close();
         }
     }
     
 }
