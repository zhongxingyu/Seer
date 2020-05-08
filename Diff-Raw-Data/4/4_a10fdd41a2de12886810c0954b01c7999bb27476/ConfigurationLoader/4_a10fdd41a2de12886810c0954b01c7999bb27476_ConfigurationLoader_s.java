 package com.hrktsoft.portfowardutility;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.hrktsoft.portfowardutility.entry.ForwardEntryL;
 
 /**
  * A configuration loader class
  * 
  * @author hrkt
  * 
  */
 public class ConfigurationLoader {
     /**
      * file path
      */
     private String filePath = null;
 
     /**
      * @param filePath
      */
     public ConfigurationLoader(String filePath) {
         super();
         this.filePath = filePath;
     }
 
     public Configuration getConfiguration() throws FileNotFoundException,
             IllegalConfigurationException, IOException {
 
         BufferedReader br = null;
         try {
             br = new BufferedReader(new FileReader(filePath));
 
             Configuration configuration = new Configuration();
             List<ForwardEntryL> entriesL = new ArrayList<ForwardEntryL>();
 
             String line = null;
             while (null != (line = br.readLine())) {
                 if (line.startsWith("USER:")) {
                     // parse USER:XXXX
                     if (null != configuration.getUser()) {
                         throw new IllegalConfigurationException(
                                 "duplicate user line:" + line);
                     }
                     String[] user = line.split(":");
                     if (user.length != 2) {
                         throw new IllegalConfigurationException("invalid line:"
                                 + line);
                     }
                     configuration.setUser(user[1]);
                 } else if (line.startsWith("HOST:")) {
                     // parse HOST:XXX.YYY.ZZZ
                     if (null != configuration.getHost()) {
                         throw new IllegalConfigurationException(
                                 "duplicate host line:" + line);
                     }
                     String[] host = line.split(":");
                     if (host.length != 2) {
                         throw new IllegalConfigurationException("invalid line:"
                                 + line);
                     }
                     configuration.setHost(host[1]);
                 } else if (line.startsWith("PORT:")) {
                     // parse PORT:XX
                     String[] portLine = line.split(":");
                     if (portLine.length != 2) {
                         throw new IllegalConfigurationException("invalid line:"
                                 + line);
                     }
                     int port = 0;
                     try{
                         port = Integer.parseInt(portLine[1]);
                     } catch (NumberFormatException nfe) {
                         throw new IllegalConfigurationException("invalid line:"
                                 + line);
                     }
                     
                     configuration.setPort(port);
                 } else if (line.startsWith("L:")) {
                     // parse
                     String[] host = line.split(":");
                     if (host.length != 4) {
                         throw new IllegalConfigurationException("invalid line:"
                                 + line);
                     }
                     int localPort = 0;
                     int destPort = 0;
                     try{
                         localPort = Integer.parseInt(host[1]);
                     } catch (NumberFormatException nfe) {
                         throw new IllegalConfigurationException("invalid line:"
                                 + line);
                     }
                     String destHost = host[2];
                     if("".equals(destHost)) {
                         throw new IllegalConfigurationException("invalid line:"
                                 + line);
                     }
                     try{
                         destPort = Integer.parseInt(host[3]);
                     } catch (NumberFormatException nfe) {
                         throw new IllegalConfigurationException("invalid line:"
                                 + line);
                     }
                     
                     ForwardEntryL l = new ForwardEntryL(localPort, destHost, destPort);
                     entriesL.add(l);
                 } else if (line.startsWith("#")) {
                     // comment line
                 } else {
                     // ignore
                 }
             }
             configuration.setForwardEntriesL(entriesL);
 
             return configuration;
         } catch (IOException e) {
             System.err.println("An error occured in reading file.");
             throw e;
         } finally {
             try {
                br.close();
             } catch (IOException e) {
                 System.err.println("An error occured in closing file.");
             }
         }
     }
 }
