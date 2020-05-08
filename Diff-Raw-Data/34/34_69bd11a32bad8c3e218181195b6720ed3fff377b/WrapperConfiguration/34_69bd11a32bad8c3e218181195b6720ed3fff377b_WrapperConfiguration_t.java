 /*
 * Copyright 2012 INRIA
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
 package com.mymed.model.core.configuration;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import ch.qos.logback.classic.Logger;
 
 import com.mymed.utils.MLogger;
 
 /**
  * 
  * @author lvanni
  */
 public class WrapperConfiguration {
 
   /* --------------------------------------------------------- */
   /* Attributes */
   /* --------------------------------------------------------- */
   private static final Logger LOGGER = MLogger.getLogger();
 
   /** Cassandra */
   private String cassandraListenAddress;
   private int thriftPort;
 
   /** Synapse */
   private String chordListenAddress;
   private int chordStoragePort;
   private String kadListenAddress;
   private int kadStoragePort;
 
   private String configName;
 
   /* --------------------------------------------------------- */
   /* Constructors */
   /* --------------------------------------------------------- */
   /**
    * Register a new Configuration for the backend Node
    * 
    * @param chordListenAddress
    * @param chordStoragePort
    * @param kadListenAddress
    * @param kadStoragePort
    */
   public WrapperConfiguration(final String cassandraListenAddress, final int thriftPort,
       final String chordListenAddress, final int chordStoragePort, final String kadListenAddress,
       final int kadStoragePort) {
     this.cassandraListenAddress = cassandraListenAddress;
     this.thriftPort = thriftPort;
     this.chordListenAddress = chordListenAddress;
     this.chordStoragePort = chordStoragePort;
     this.kadListenAddress = kadListenAddress;
     this.kadStoragePort = kadStoragePort;
   }
 
   /**
    * Register a new configuration for the backend node
    * <p>
    * Use this method only if the file is in a known fixed location
    * 
    * @param file
    *          The xml configuration file
    */
   public WrapperConfiguration(final File file) {
     configName = file.getName();
    InputStream input = null;
 
     try {
      input = new FileInputStream(file);
 
      createConfiguration(input);
    } catch (final FileNotFoundException ex) {
       LOGGER.info("Error opening the configuration file");
       LOGGER.debug("Error opening the configuration file", ex);
    } finally {
      try {
        if (input != null) {
          input.close();
        }
      } catch (final IOException ex) {
        LOGGER.debug("Error closing InputStream", ex);
      }
     }
   }
 
   /**
    * Register a new configuration for the backend node
    * 
    * @param configXml
    *          The name of the configuration file in the Classpath
    */
   public WrapperConfiguration(final String configXml) {
     configName = configXml;
     final InputStream input = this.getClass().getClassLoader().getResourceAsStream(configXml);
 
     createConfiguration(input);
   }
 
   /**
    * Read and create the configuration parameters
    */
   private void createConfiguration(final InputStream input) {
     try {
       final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
       final DocumentBuilder db = dbf.newDocumentBuilder();
       final Document doc = db.parse(input);
       doc.getDocumentElement().normalize();
       final NodeList backend = doc.getElementsByTagName("backend");
       final NodeList config = backend.item(0).getChildNodes();
 
       for (int i = 0; i < config.getLength(); i++) {
         final Node fstNode = config.item(i);
         if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
           final NodeList cassandraInfo = config.item(i).getChildNodes();
           if (config.item(i).getNodeName().equals("cassandra")) {
             for (int c = 0; c < cassandraInfo.getLength(); c++) {
               final Node info = cassandraInfo.item(c);
               if (info.getNodeType() == Node.ELEMENT_NODE) {
                 if (info.getNodeName().equals("ListenAddress")) {
                   cassandraListenAddress = info.getFirstChild().getTextContent();
                 } else if (info.getNodeName().equals("ThriftPort")) {
                   thriftPort = Integer.parseInt(info.getFirstChild().getTextContent());
                 }
               }
             }
           } else if (config.item(i).getNodeName().equals("synapse")) {
             final NodeList dhts = config.item(i).getChildNodes();
             for (int s = 0; s < dhts.getLength(); s++) {
               final Node node = dhts.item(s);
               if (node.getNodeType() == Node.ELEMENT_NODE) {
                 final NodeList dhtInfo = dhts.item(s).getChildNodes();
                 String address = "";
                 int port = 0;
                 for (int j = 0; j < cassandraInfo.getLength(); j++) {
                   final Node info = dhtInfo.item(j);
                   if (info.getNodeType() == Node.ELEMENT_NODE) {
                     if (info.getNodeName().equals("ListenAddress")) {
                       address = info.getFirstChild().getTextContent();
                     } else if (info.getNodeName().equals("StoragePort")) {
                       port = Integer.parseInt(info.getFirstChild().getTextContent());
                     }
                   }
                 }
                 if (dhts.item(s).getNodeName().equals("chord")) {
                   chordListenAddress = address;
                   chordStoragePort = port;
                 } else if (dhts.item(s).getNodeName().equals("kad")) {
                   kadListenAddress = address;
                   kadStoragePort = port;
                 }
               }
             }
           }
         }
       }
     } catch (final ParserConfigurationException e) {
       LOGGER.info("Error parsing configuration file");
       LOGGER.debug("Error parsing configuration file", e);
     } catch (final SAXException e) {
       LOGGER.info("Error parsing configuration file");
       LOGGER.debug("Error parsing configuration file", e);
     } catch (final IOException e) {
       LOGGER.info("Config file '{}' not found", configName);
       // If the config xml file is not found, the configuration
       // will be defined with the default values
       String host = "127.0.0.1";
       try {
         host = InetAddress.getLocalHost().getHostAddress();
       } catch (final UnknownHostException e1) {
         LOGGER.info("Impossible to find the local host.");
         LOGGER.debug("Impossible to find the local host", e1);
       }
 
       cassandraListenAddress = host;
       thriftPort = 4201;
       chordListenAddress = host;
       chordStoragePort = 0;
       kadListenAddress = host;
       kadStoragePort = 0;
 
       LOGGER.info("WARNING: no XML configuration file found!");
     }
   }
 
   @Override
   public String toString() {
     final StringBuffer buffer = new StringBuffer(300);
 
     buffer.append("Cassandra:\n\t ListenAddress = ");
     buffer.append(cassandraListenAddress);
     buffer.append("\n\t ThriftPort = ");
     buffer.append(thriftPort);
     buffer.append("\nSynapse:\n\t. Chord:\n\t\t ListenAddress = ");
     buffer.append(chordListenAddress);
     buffer.append("\n\t\t StoragePort = ");
     buffer.append(chordStoragePort);
     buffer.append("\n\t. Kad:\n\t\t ListenAddress = ");
     buffer.append(kadListenAddress);
     buffer.append("\n\t\t StoragePort = ");
     buffer.append(kadStoragePort);
     buffer.append('\n');
 
     buffer.trimToSize();
 
     return buffer.toString();
   }
 
   /* --------------------------------------------------------- */
   /* GETTER&SETTER */
   /* --------------------------------------------------------- */
   public String getCassandraListenAddress() {
     return cassandraListenAddress;
   }
 
   public void setCassandraListenAddress(final String cassandraListenAddress) {
     this.cassandraListenAddress = cassandraListenAddress;
   }
 
   public int getThriftPort() {
     return thriftPort;
   }
 
   public void setThriftPort(final int thriftPort) {
     this.thriftPort = thriftPort;
   }
 
   public String getChordListenAddress() {
     return chordListenAddress;
   }
 
   public void setChordListenAddress(final String chordListenAddress) {
     this.chordListenAddress = chordListenAddress;
   }
 
   public int getChordStoragePort() {
     return chordStoragePort;
   }
 
   public void setChordStoragePort(final int chordStoragePort) {
     this.chordStoragePort = chordStoragePort;
   }
 
   public String getKadListenAddress() {
     return kadListenAddress;
   }
 
   public void setKadListenAddress(final String kadListenAddress) {
     this.kadListenAddress = kadListenAddress;
   }
 
   public int getKadStoragePort() {
     return kadStoragePort;
   }
 
   public void setKadStoragePort(final int kadStoragePort) {
     this.kadStoragePort = kadStoragePort;
   }
 }
