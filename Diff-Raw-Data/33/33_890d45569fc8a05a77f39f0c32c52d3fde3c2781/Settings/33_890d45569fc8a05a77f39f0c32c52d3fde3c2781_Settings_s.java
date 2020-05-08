 /*
  * Copyright (c) 2013, Tripwire, Inc.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  *  o Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *
  *  o Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.jmxdatamart.Extractor;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.xml.DomDriver;
 import org.jmxdatamart.common.DataType;
 import org.slf4j.LoggerFactory;
 
 import java.io.*;
 import java.util.*;
 
 
 
 /**
  * This class contains settings for the extractor : polling rate, output file(s)
  * location, JMX server URL and what bean to collect.
  * @author Binh Tran <mynameisbinh@gmail.com>
  */
 
 public class Settings {
     private final static org.slf4j.Logger logger = LoggerFactory.getLogger(Extractor.class);
     private long pollingRate;
     private String folderLocation;
     private String url;
     private List<BeanData> beans;
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 67 * hash + (int) (this.pollingRate ^ (this.pollingRate >>> 32));
         hash = 67 * hash + (this.folderLocation != null ? this.folderLocation.hashCode() : 0);
         hash = 67 * hash + (this.url != null ? this.url.hashCode() : 0);
         hash = 67 * hash + (this.beans != null ? this.beans.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Settings other = (Settings) obj;
         if (this.pollingRate != other.pollingRate) {
             return false;
         }
         if ((this.folderLocation == null) ? (other.folderLocation != null) : !this.folderLocation.equals(other.folderLocation)) {
             return false;
         }
         if ((this.url == null) ? (other.url != null) : !this.url.equals(other.url)) {
             return false;
         }
         if (this.beans != other.beans && (this.beans == null || !this.beans.equals(other.beans))) {
             return false;
         }
         return true;
     }
     
     
     /**
      * @return the pollingRate
      */
     public long getPollingRate() {
         return pollingRate;
     }
 
     /**
      * @param pollingRate the pollingRate to set
      */
     public void setPollingRate(long pollingRate) {
         this.pollingRate = pollingRate;
     }
 
     /**
      * @return the folderLocation
      */
     public String getFolderLocation() {
         return folderLocation;
     }
 
     /**
      * @param folderLocation the folderLocation to set
      */
     public void setFolderLocation(String folderLocation) {
         this.folderLocation = folderLocation;
     }
 
     /**
      * @return the url
      */
     public String getUrl() {
         return url;
     }
 
     /**
      * @param url the url to set
      */
     public void setUrl(String url) {
         this.url = url;
     }
 
     /**
      * @return the beans
      */
     public List<BeanData> getBeans() {
         return beans;
     }
 
     /**
      * @param beans the beans to set
      */
     public void setBeans(List<BeanData> beans) {
         this.beans = beans;
     }
     
     public Settings() {
         XStream xstream = new XStream(new DomDriver());
         xstream.aliasField("BeanList", Settings.class, "beans");
         xstream.aliasField("AttributeList", MBeanData.class, "attributes");
         xstream.alias("Settings", Settings.class);
         xstream.alias("Bean", MBeanData.class);
         xstream.alias("Attribute", Attribute.class);
     }
     
     public void sanitize() {
         for (BeanData bd : getBeans()) {
             if ("".equals(bd.getAlias())) {
                 bd.setAlias(bd.getName());
             }
             for (Attribute a : bd.getAttributes()) {
                 if ("".equals(a.getAlias())) {
                     a.setAlias(a.getName());
                 }
             }
         }
     }
     
     public String toXML() {
         XStream xstream = new XStream(new DomDriver());
         xstream.aliasField("BeanList", Settings.class, "beans");
         xstream.aliasField("AttributeList", MBeanData.class, "attributes");
         xstream.alias("Settings", Settings.class);
         xstream.alias("Bean", MBeanData.class);
         xstream.alias("Attribute", Attribute.class);
         
         return xstream.toXML(this);
     }
     
     public static Settings fromXML(String s) {
         XStream xstream = new XStream(new DomDriver());
         xstream.aliasField("BeanList", Settings.class, "beans");
         xstream.aliasField("AttributeList", MBeanData.class, "attributes");
         xstream.alias("Settings", Settings.class);
         xstream.alias("Bean", MBeanData.class);
         xstream.alias("Attribute", Attribute.class);
         
         Settings settings = (Settings)xstream.fromXML(s);
         settings.sanitize();
         return settings;
         
     }
     
     public static Settings fromXML(InputStream s) {
         XStream xstream = new XStream(new DomDriver());
         xstream.aliasField("BeanList", Settings.class, "beans");
         xstream.aliasField("AttributeList", MBeanData.class, "attributes");
         xstream.alias("Settings", Settings.class);
         xstream.alias("Bean", MBeanData.class);
         xstream.alias("Attribute", Attribute.class);
 
         Settings settings = (Settings)xstream.fromXML(s);
         settings.sanitize();
         return settings;
     }
     
     @Override
     public String toString() {
         String nl = System.getProperty("line.separator");
         return  "Rate = " + pollingRate + nl +
                 "Loc = " + folderLocation + nl +
                 "URL = " + url + nl +
                 beans.toString();
         
     }
     
     public static void main( String[] args )
     {
         //Test reading file
         Settings s1 = null;
 		try {
 			s1 = Settings.fromXML(new FileInputStream("Settings.xml"));
 		} catch (FileNotFoundException e) {
 			logger.error("Error while getting data from Settings.xml", e);
			System.exit(0); //this is a fatal error and cannot be resolved later
 		}
         System.out.println(s1.toString());
         System.out.println("Read xml settings complete");
         
         Settings s = new Settings();
         s.setFolderLocation("\\project\\");
         s.setPollingRate(5);
         s.setUrl("service:jmx:rmi:///jndi/rmi://:9999/jmxrmi");
         s.setBeans(new ArrayList<BeanData>());
         
         MBeanData bd = new MBeanData("com.example:type=Hello","", new ArrayList<Attribute>(), true);
         bd.getAttributes().add(new Attribute("Name", "", DataType.STRING));
         bd.getAttributes().add(new Attribute("CacheSize", "", DataType.INT));
         s.getBeans().add(bd);
         s.sanitize();
         System.out.println(s.toString());
         
         String sXML = s.toXML();
         System.out.println(sXML);
         FileWriter out = null;
 		try {
 			out = new FileWriter("settings.xml");
 		} catch (IOException e) {
 			logger.error("Error while creating FileWriter for settings file", e);
			System.exit(0); //this is a fatal error and cannot be resolved later
 		}
         try {
 			out.write(sXML);
 		} catch (IOException e) {
 			logger.error("Error while writing " + sXML + " to settings file", e);
 		}
         try {
 			out.close();
 		} catch (IOException e) {
 			logger.error("Error while closing settings file", e);
 		}
     }
 }
