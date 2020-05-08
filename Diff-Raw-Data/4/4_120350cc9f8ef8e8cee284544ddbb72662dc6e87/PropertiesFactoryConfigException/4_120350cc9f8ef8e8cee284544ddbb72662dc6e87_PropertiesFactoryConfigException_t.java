 package de.crowdcode.jndi.properties;
 
 
 /**
  * @author Ingo Düppe
  */
 public class PropertiesFactoryConfigException extends Exception {
 
     private static final long serialVersionUID = 1L;
 
     public PropertiesFactoryConfigException() {
     }
 
     public PropertiesFactoryConfigException(String message) {
         super(message);
     }
 
     public PropertiesFactoryConfigException(Throwable cause) {
         super(cause);
     }
 
     public PropertiesFactoryConfigException(String message, Throwable cause) {
         super(message, cause);
     }
 
 }
