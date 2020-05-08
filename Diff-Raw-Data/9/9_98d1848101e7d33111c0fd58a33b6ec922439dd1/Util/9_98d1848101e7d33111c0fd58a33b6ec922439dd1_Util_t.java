 package com.id.util;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 public class Util {
   public static List<String> readFile(String filename) {
     try {
       BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("resources/diff")));
       List<String> result = new ArrayList<String>();
       String line;
       while ((line = reader.readLine()) != null) {
         result.add(line);
       }
       return result;
     } catch (IOException e) {
       e.printStackTrace();
     }
     return null;
   }
 
   public static List<String> exec(String command) {
     try {
      Process process = Runtime.getRuntime().exec(command);
       BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
       List<String> result = new ArrayList<String>();
       String line;
       while ((line = br.readLine()) != null) {
         result.add(line);
       }
       return result;
     } catch (IOException e) {
       e.printStackTrace();
     }
     return null;
   }
 }
