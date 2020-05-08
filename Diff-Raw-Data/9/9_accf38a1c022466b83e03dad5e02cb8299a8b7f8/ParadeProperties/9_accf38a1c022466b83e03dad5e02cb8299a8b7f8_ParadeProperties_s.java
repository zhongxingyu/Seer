 package org.makumba.parade.init;
 
import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
import java.io.InputStream;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 import org.apache.log4j.Logger;
 
 public class ParadeProperties {
 
     static String DEFAULT_PROPERTYFILE = "/parade.properties";
 
     private static Properties paradeConfig;
     
     private static Properties tomcatConfig;
     
     static Logger logger = Logger.getLogger(ParadeProperties.class.getName());
 
     static {
 
         try {
             paradeConfig = new Properties();
             paradeConfig.load(ParadeProperties.class.getResourceAsStream(DEFAULT_PROPERTYFILE));
             
         } catch (Throwable t) {
             logger
                     .error(
                             "Error while loading parade.properties. Make sure you have configured a parade.properties in webapp/WEB-INF/classes (you can copy the example file)",
                             t);
         }
         
         try {
             tomcatConfig = new Properties();
            tomcatConfig.load(new FileInputStream(new java.io.File(getParadeBase())));
 
         } catch (Throwable t) {
             logger
             .error(
                     "Error while loading tomcat.properties. Make sure you have configured a tomcat.properties in parade's root dir (you can copy the example file)",
                     t);
             
         }
         
     }
 
     public static String getParadeProperty(String configProperty) {
         return paradeConfig.getProperty(configProperty);
     }
     
     public static String getTomcatProperty(String configProperty) {
         return tomcatConfig.getProperty(configProperty);
     }
 
     public static List<String> getElements(String configProperty) {
         List<String> l = new LinkedList<String>();
 
         String s = getParadeProperty(configProperty);
         if (s == null)
             return null;
         StringTokenizer st = new StringTokenizer(s, ",");
         while (st.hasMoreElements()) {
             l.add((st.nextToken()).trim());
         }
         return l;
     }
 
     public static String getParadeBase() {
 
         String paradeBase = ".\\";
         try {
             paradeBase = new java.io.File("." + java.io.File.separator).getCanonicalPath();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return paradeBase;
     }
 
     public static String getClassesPath() {
         return new java.io.File(RowProperties.class.getResource(DEFAULT_PROPERTYFILE).getPath()).getParent();
     }
 
 }
