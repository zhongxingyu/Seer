 package ru.spbau.bioinf.tagfinder.util;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 public class ReaderUtil {
     public static Properties readPropertiesUntil(BufferedReader result, String sign) throws IOException {
         String s;
         Properties ans = new Properties();
         while ((s = result.readLine())!= null) {
             int equalIndex = s.indexOf("=");
             if (equalIndex < 0) {
                 equalIndex = s.indexOf(" ");
                 if (equalIndex == 0 || equalIndex == s.length() -1) {
                     equalIndex = 0;
                 }
             }
             if (equalIndex > 0) {
                 ans.put(s.substring(0, equalIndex).trim(), s.substring(equalIndex + 1).trim());
             }
             if (s.startsWith(sign))
                 break;
         }
         return ans;
     }
 
     public static List<String[]> readDataUntil(BufferedReader result, String sign) throws IOException {
         String s;
         List<String[]> ans = new ArrayList<String[]>();
         while ((s = result.readLine()) != null) {
             if (s.equalsIgnoreCase(sign)) {
                 break;
             }
             ans.add(getDataArray(s));
         }
         return ans;
     }
 
 
     public static String[] getDataArray(String s) {
         return s.split("[ \t]");
     }
 
     public static String getValue(Properties prop, String key) {
         return prop.getProperty(key);
     }
 
     public static int getIntValue(Properties prop, String key) {
         return Integer.parseInt(prop.getProperty(key));
     }
 
     public static float getFloatValue(Properties prop, String key) {
         return Float.parseFloat(prop.getProperty(key));
     }
 
     public static double getDoubleValue(Properties prop, String key) {
         return Double.parseDouble(prop.getProperty(key));
     }
 
     public static boolean getBooleanValue(Properties prop, String key) {
         return "true".equalsIgnoreCase(prop.getProperty(key));
     }
 
     public static BufferedReader getBufferedReader(File file)
          throws UnsupportedEncodingException, FileNotFoundException {
         return new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
     }
 
     public static PrintWriter createOutputFile(File file)
            throws UnsupportedEncodingException, FileNotFoundException {
        file.getParentFile().mkdirs();
         return new PrintWriter(new OutputStreamWriter(
                 new FileOutputStream(file), "UTF-8"));
     }
 
     public static BufferedReader createInputReader(File proteinDatabase) throws UnsupportedEncodingException, FileNotFoundException {
         return new BufferedReader(new InputStreamReader(new FileInputStream(
                     proteinDatabase), "UTF-8"));
     }
 }
