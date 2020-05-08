 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.asascience.edc.utils;
 
 import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author KWilcox
  */
 public class CsvFileUtils {
   
   public static ArrayList<String> getTimesteps(File csvFile, String timeHeader) throws IOException {
     CsvReader reader = new CsvReader(new FileReader(csvFile));
     reader.readHeaders();
     reader.skipLine(); // skip headers
     ArrayList<String> timesteps = new ArrayList<String>();
     while (reader.readRecord()) {
       timesteps.add(reader.get(timeHeader));
     }
     reader.close();
     return timesteps;
   }
   
   public static ArrayList<String> getVariables(File csvFile, List<String> knownHeaders) throws IOException {
     CsvReader reader = new CsvReader(new FileReader(csvFile));
     reader.readHeaders();
     reader.skipLine(); // skip headers
     ArrayList<String> variables = new ArrayList<String>();
     for (String s : reader.getHeaders()) {
       variables.add(s.trim());
     }
     variables.removeAll(knownHeaders);
     reader.close();
     return variables;
   }
  
  public static void convertTimestepsToEsri(File csvFile, String timeHeader) throws IOException {
    File outFile = new File(csvFile.getParentFile() + File.separator + "temp.csv");
    CsvWriter writer = new CsvWriter(outFile.getAbsolutePath());
    CsvReader reader = new CsvReader(new FileReader(csvFile));
    reader.readHeaders();
    reader.skipLine(); // skip headers
    writer.writeRecord(reader.getHeaders());
    String[] values;
    while (reader.readRecord()) {
      values = reader.getValues();
      values[reader.getIndex(timeHeader)] = convertZuluToEsriTime(reader.get(timeHeader));
      writer.writeRecord(values);
    }
    writer.close();
    reader.close();
    csvFile.delete();
    outFile.renameTo(csvFile.getAbsoluteFile());
  }
  
  private static String convertZuluToEsriTime(String zulu) {
    return zulu.replace("T", " ").replace("Z", "");
  }
  
 }
