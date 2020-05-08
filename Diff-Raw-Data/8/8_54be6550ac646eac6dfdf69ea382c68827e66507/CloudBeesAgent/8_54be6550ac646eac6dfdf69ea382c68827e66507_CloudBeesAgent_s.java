 /*
  * Copyright 2010-2013, CloudBees Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.cloudbees.run;
 
 import java.io.*;
 import java.lang.instrument.Instrumentation;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author Fabian Donze
  */
 public class CloudBeesAgent {
     private final static Logger logger = Logger.getLogger(CloudBeesAgent.class.getName());
 
     public static void main(String[] args) {
         if (args.length == 1) {
             premain(args[0], null);
         }
     }
 
     public static void premain(String agentArgs, Instrumentation inst) {
         if (agentArgs == null) {
             logger.severe("Properties file not passed as Java Agent arg, don't load properties");
             return;
         }
         List<String> propertiesFiles = extractPropertiesFilePathsWithBackwardCompatibility(agentArgs);
         for (String propertiesFilePath : propertiesFiles) {
             try {
                 loadSystemProperties(propertiesFilePath);
             } catch (IOException e) {
                 logger.log(Level.SEVERE, "Failure load properties from " + agentArgs, e);
             }
         }
     }
 
     private static List<String> extractPropertiesFilePathsWithBackwardCompatibility(String agentArgs) {
         Map<String, String> argumentsByName = new HashMap<String, String>();
         String[] args = agentArgs.split(",");
         for (String arg : args) {
             String[] parts = arg.split(":");
             if (parts.length == 2) {
                 argumentsByName.put(parts[0], parts[1]);
             } else {
                 logger.fine("Ignore argument " + arg);
             }
         }
         String propFileName = argumentsByName.get("sys_prop");
 
         List<String> results;
         if (propFileName == null) {
             results = Arrays.asList(agentArgs.split(","));
         } else {
             results = Collections.singletonList(propFileName);
         }
 
         return results;
     }
 
     public static void loadSystemProperties(String propertiesFilePath) throws IOException {
         if (propertiesFilePath == null) {
             logger.severe("System properties file not defined with parameter " + "sys_prop");
             return;
         }
         File propertyFile = new File(propertiesFilePath);
         if (!propertyFile.exists()) {
             logger.severe("Property file not found: path=" + propertiesFilePath + ", absolutePath=" + propertyFile.getAbsolutePath());
             return;
         }
 
         BufferedReader reader = new BufferedReader(new FileReader(propertyFile));
         try {
             String line;
             while ((line = reader.readLine()) != null) {
                 int idx = line.indexOf("=");
                 if (idx > -1) {
                     String name = line.substring(0, idx);
                     String value = line.substring(idx + 1);
 
                     if (value.startsWith("\"") && value.endsWith("\"")) {
                         value = value.substring(1, value.length() - 1);
                         logger.fine("unwrapped double quotes around value=" + value + "]");
                     }
 
                     logger.finest("Process name=" + name + ", rawValue=" + value + "]");
 
                     // un-escape \" to "
                     value = value.replaceAll("\\\\\"", "\"");
                     logger.fine("un-escaped \\\" to \" value=" + value + "]");
 
                     // un-escape \\ to \
                     value = value.replaceAll("\\\\\\\\", "\\\\");
                     logger.fine("un-escaped \\\\ to \\ value=" + value + "]");
 
                    logger.fine("System.setProperty(" + name + ", " + value + ")");
                    System.setProperty(name, value);
                 } else {
                     logger.fine("Ignore line " + line);
                 }
 
             }
             logger.info("Java System Properties loaded from " + propertyFile.getAbsolutePath());
         } finally {
             reader.close();
         }
     }
 }
