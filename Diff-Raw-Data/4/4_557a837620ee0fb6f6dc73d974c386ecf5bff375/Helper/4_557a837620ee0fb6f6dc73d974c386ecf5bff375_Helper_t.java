 package com.cloudbees.sdk.utils;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.Properties;
 
 /**
  * @author Fabian Donze
  */
 public class Helper {
     public static String promptForAppId() throws IOException {
         return promptFor("Enter application ID (ex: account/appname) : ", true);
     }
 
     public static String promptFor(String message, boolean cannotBeNull) throws IOException {
         String input = promptFor(message);
         if (cannotBeNull && (input == null || input.trim().length() == 0))
             return promptFor(message, cannotBeNull);
         return input;
     }
 
     public static boolean promptMatches(String message, String successPattern) throws IOException {
         String input = promptFor(message);
         if (input == null || input.trim().length() == 0)
             return promptMatches(message, successPattern);
         return input.matches(successPattern);
     }
 
     public static String promptFor(String message) throws IOException {
         BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
         System.out.print(message);
         return inputReader.readLine();
     }
 
     public static boolean loadProperties(File propertyFile, Properties properties) {
         if (propertyFile.exists()) {
             FileInputStream fis = null;
             try {
                 fis = new FileInputStream(propertyFile);
                 properties.load(fis);
                 fis.close();
                 return true;
             } catch (IOException e) {
                 e.printStackTrace();
             } finally {
                 if (fis != null) {
                     try {
                         fis.close();
                     } catch (IOException ignored) {
                     }
                 }
             }
         }
         return false;
     }
 
     public static void deleteDirectory(File dir) {
         if (dir.exists()) {
             if (dir.isDirectory()) {
                 File[] files = dir.listFiles();
                 if (files != null) {
                     for (File f : files) {
                         if (f.isDirectory())
                             deleteDirectory(f);
                         else
                             f.delete();
                     }
                 }
             }
             dir.delete();
         }
     }
 
     public static void deleteDirectoryOnExit(File dir) {
         if (dir.exists()) {
             dir.deleteOnExit();
             if (dir.isDirectory()) {
                 File[] files = dir.listFiles();
                 if (files != null) {
                     for (File f : files) {
                         if (f.isDirectory())
                             deleteDirectoryOnExit(f);
                         else
                             f.deleteOnExit();
                     }
                 }
             }
         }
     }
 
     public static void copyFile(File from, File to) throws IOException {
         byte[] buf = new byte[1024];
         FileInputStream in = new FileInputStream(from);
         try {
             FileOutputStream out = new FileOutputStream(to);
             try {
                 int numRead = in.read(buf);
                 while (numRead != -1) {
                     out.write(buf, 0, numRead);
                     numRead = in.read(buf);
                 }
             } finally {
                 out.close();
             }
         } finally {
             in.close();
         }
     }
 
     public static void downloadFile(String url, String fileName) throws IOException {
         byte[] buf = new byte[1024];
         FileOutputStream fos = new FileOutputStream(fileName);
         BufferedOutputStream bos = new BufferedOutputStream(fos);
         InputStream in = new URL(url).openStream();
         try {
             int numRead = in.read(buf);
             while (numRead != -1) {
                 bos.write(buf, 0, numRead);
                 numRead = in.read(buf);
             }
         } finally {
             bos.close();
         }
     }
 
     public static String[] getFiles(File dir, final String extension) {
         return dir.list(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return (name.endsWith(extension));
             }
         });
     }
 
 
     public static String getPaddedString(String str, int length) {
        StringBuffer sb;
        if (str != null) sb = new StringBuffer(str);
        else sb = new StringBuffer();
         int size = sb.length();
         if (size < length) {
             for (int i=0; i<length-size; i++)
                 sb.append(" ");
         }
         return sb.toString();
     }
 
     public static String[] getEnvironmentList(String environments, String...prependEnvs)
     {
         if(environments == null && prependEnvs.length == 0)
             return new String[0];
 
         if(environments == null)
             environments = "";
 
         //split the environments string and prepend the run environment
         String[] envSplit = environments.equals("") ? new String[0] : environments.split(",");
         String[] envList = new String[envSplit.length+prependEnvs.length];
         for(int i=0; i<prependEnvs.length; i++)
             envList[i] = prependEnvs[i];
 
         for(int i=0; i<envSplit.length; i++)
             envList[prependEnvs.length + i] = envSplit[i].trim();
         return envList;
     }
 }
