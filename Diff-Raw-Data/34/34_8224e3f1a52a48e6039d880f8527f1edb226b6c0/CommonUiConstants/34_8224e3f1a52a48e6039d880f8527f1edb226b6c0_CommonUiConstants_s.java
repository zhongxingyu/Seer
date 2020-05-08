 package org.iplantc.core.uicommons.client;
 
 import org.iplantc.de.client.CommonConstants;
 
 /** Configurable constants shared by applications */
 public interface CommonUiConstants extends CommonConstants {
    
     /**
      * URL for error icon image.
      * 
      * @return a string representing the URL.
      */
     String iconError();
    
     /**
      * URL to forums
      * 
      * @return a string representing the URL.
      */
     String forumsUrl();
    
     /**
      * URL to iplant services
      * 
      * @return a string representing the URL.
      */
     String supportUrl();
 
 }
