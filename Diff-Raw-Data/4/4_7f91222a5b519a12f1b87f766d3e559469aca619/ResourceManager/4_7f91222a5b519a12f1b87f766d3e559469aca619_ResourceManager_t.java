 /*
  * Created on Mar 8, 2004 To change the template for this generated file go to Window - Preferences - Java - Code
  * Generation - Code and Comments
  */
 package com.edsdev.jconvert.util;
 
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.util.Properties;
 
 import javax.swing.ImageIcon;
 
 /**
  * @author elsarrazin To change the template for this generated type comment go to Window - Preferences - Java - Code
  *         Generation - Code and Comments
  */
 public class ResourceManager {
     private static Logger log = Logger.getInstance(ResourceManager.class);
 
     /**
      * Loads and Image from the classpath as a resource
      * 
      * @param imageRelativeURL
      * @return Image object
      */
     public static Image getImage(String imageRelativeURL) {
         Image image = null;
         try {
             ClassLoader cl = ResourceManager.class.getClassLoader();
             image = Toolkit.getDefaultToolkit().getImage(cl.getResource(imageRelativeURL));
         } catch (Exception exp) {
             log.error("Failed to get Image " + imageRelativeURL, exp);
         }
         return image;
     }
 
     /**
      * Loads a properties file from the classpath based on the name provided
      * 
      * @param filename
      * @return Properties object
      * @throws java.io.IOException
      */
     public static Properties loadProperties(String filename) throws java.io.IOException {
         return loadProperties(filename, ResourceManager.class);
     }
 
     /**
      * Attempts to find a resource in the classpath and returns an InputStream to that resrouce
      * 
      * @param resourceName
      * @return
      */
     public static InputStream getResourceAsStream(String resourceName) {
         InputStream is = null;
 
         ClassLoader cl = ResourceManager.class.getClassLoader();
         is = cl.getResourceAsStream(resourceName);
         if (is != null) {
             return is;
         }
 
         cl = Thread.currentThread().getContextClassLoader();
         is = cl.getResourceAsStream(resourceName);
         if (is != null) {
             return is;
         }
         return is;
     }
 
     /**
      * Responsible for determining what the path is to the jar file.
      * 
      * @return String representation of the path
      */
     public static String getJarPath() {
         String rv = "";
 
         ClassLoader cl = ResourceManager.class.getClassLoader();
 
         try {
             String fileName = "jconvert-" + JConvertProperties.getMajorVersion() + "."
                     + JConvertProperties.getMinorVersion() + "." + JConvertProperties.getRevision() + ".jar";
             log.debug("Searching for file " + fileName + " to determine the jar path.");
             URL url = cl.getResource(fileName);
             if (url != null) {
                 rv = url.getPath();
                 rv = URLDecoder.decode(rv, System.getProperty("file.encoding"));
                 rv = rv.substring(0, rv.indexOf(fileName));
             }
         } catch (Exception e) {
             log.error("Failed to get the path of the jar", e);
         }
 
         return rv;
     }
 
     /**
      * Loads a properties file
      * 
      * @param filename Name of the properties file
      * @param loadClass class used when getting the classloader
      * @return Properties object loaded with properties from the resource
      * @throws java.io.IOException
      */
     public static Properties loadProperties(String filename, Class loadClass) throws java.io.IOException {
         InputStream is = loadClass.getClassLoader().getResourceAsStream(filename);
         return loadProperties(is);
     }
 
     /**
      * Loads properties from an InputStream into a Properties object
      * 
      * @param Istream
      * @return Properties object
      * @throws java.io.IOException
      */
     public static Properties loadProperties(InputStream Istream) throws java.io.IOException {
         if (Istream == null) {
            throw new IOException("Can't load properties file from the inputstream - it is null");
         }
 
         Properties props = null;
 
         synchronized (ResourceManager.class) {
             try {
                 props = new Properties();
                 props.load(Istream);
                 Istream.close();
             } catch (IOException e) {
                 log.error("Can't load properties for Input stream", e);
                 throw e;
             }
         }
         return props;
     }
 
     /**
      * Gets and ImageIcom from the classloader based on the image resource URL
      * 
      * @param imageRelativeURL
      * @return
      */
     public static ImageIcon getImageIcon(String imageRelativeURL) {
         ImageIcon icon = null;
         try {
             ClassLoader cl = ResourceManager.class.getClassLoader();
             icon = new ImageIcon(cl.getResource(imageRelativeURL));
         } catch (Exception exp) {
             log.error("Could not load image " + imageRelativeURL, exp);
         }
         return icon;
     }
 }
