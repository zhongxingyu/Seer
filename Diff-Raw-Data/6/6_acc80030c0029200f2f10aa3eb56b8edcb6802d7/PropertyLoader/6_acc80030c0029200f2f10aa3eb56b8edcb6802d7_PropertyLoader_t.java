 package com.elgoooog.podb.util;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 /**
  * @author Nicholas Hauschild
  *         Date: 5/20/11
  *         Time: 12:12 AM
  */
 public class PropertyLoader {
     static {
        InputStream is = PropertyLoader.class.getResourceAsStream("/properties/local.properties");
        Properties properties = System.getProperties();
         try {
             properties.load(is);
         } catch(IOException e) {
             throw new RuntimeException(e);
         }
        System.setProperties(properties);
     }
 }
