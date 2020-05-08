 ///////////////////////////////////////////////////////////////////////////
 //
 // This program is part of Zenoss Core, an open source monitoring platform.
 // Copyright (C) 2007, Zenoss Inc.
 //
 // This program is free software; you can redistribute it and/or modify it
 // under the terms of the GNU General Public License version 2 as published by
 // the Free Software Foundation.
 //
 // For complete information please visit: http://www.zenoss.com/oss/
 //
 ///////////////////////////////////////////////////////////////////////////
 package com.zenoss.zenpacks.zenjmx.call;
 
 import java.util.Map;
 import java.util.List;
 import java.util.ArrayList;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 
 /**
  * <p> Commonly used methods.  </p>
  *
  * <p>$Author: chris $</p>
  *
  * @author Christopher Blunck
  * @version $Revision: 1.6 $
  */
 public class Utility {
 
     
   private static final Log _logger = LogFactory.getLog(Utility.class);
 
   /**
    * Looks in the config Map provided and extracts the JMX property
    * requested.  If it does not exist the method looks for the
    * zProperty in the Map.  If it can't find that it returns ""
    */
   public static String get(Map config, String jProperty, String zProperty) {
     String toReturn = "";
     
     if (config.containsKey(jProperty)) {
       toReturn = (String) config.get(jProperty);
     } 
     //@TODO: zProperties are not added to config so this shouldn't find anything
     if (zProperty != null && "".equals(toReturn.trim())
                 && config.containsKey(zProperty)) {
             toReturn = config.get(zProperty).toString();
         }
 
     return toReturn;
   }
 
 
   /**
    * Creates a URL based on the configuration provided.
    * @returns a jmx connection url
    * @throws ConfigurationException if the url is invalid (if the hostname 
    *         or port cannot be found in the configuration)
    */
   public static String getUrl(Map config) 
     throws ConfigurationException {
 
     String port = get(config, "jmxPort", "zJmxManagementPort");
     String protocol = get(config, "jmxProtocol", null);
     if ("".equals(port)) {
       String message = "jmxPort or zJmxManagementPort not specified";
       throw new ConfigurationException(message);
     }
 
     String hostAddr = null;
     
     if (config.containsKey("manageIp")) {
        _logger.debug("using manageIp for host address");
        hostAddr = (String) config.get("manageIp");
     } else {
       /*
        * Support pre-2.1.3+ hub xml-rpc responses. If manageIp is not
        * present in configuration, fallback to device, which will have
        * a better chance of working than null.
        */
         _logger.debug("manageIp not in config, using device for host address");
         hostAddr = (String) config.get("device"); 
     }
     
     if ((hostAddr == null) || ("".equals(hostAddr.trim()))) {
       String message = "manageIp or device properties not specified";
       throw new ConfigurationException(message);
     }
     
     String url = "service:jmx:";
     if (protocol.equals("JMXMP"))
       url += "jmxmp://" + hostAddr + ":" + port;
     else
      url += "rmi:///jndi/rmi://" + hostAddr + ":" + port + "/jmxrmi";
 
     _logger.debug("JMX URL is: "+url);
     return url;
   }
 
 
   /**
    * Returns the JMX username from the configuration provided
    */
   public static String getUsername(Map config) {
     return get(config, "username", "zJmxManagementUsername");
   }
 
 
   /**
    * Returns the JMX password from the configuration provided
    */
   public static String getPassword(Map config) {
     return get(config, "password", "zJmxManagementPassword");
   }
 
 
   /**
    * Downcasts the Object[] to a List<String>
    * @param source the Objects to downcast to their String representation
    * @return a List<String> that is the result of calling toString()
    * on each Object in the source
    */
   public static List<String> downcast(Object[] source) {
     List<String> dest = new ArrayList<String>();
     if (source != null) {
       for (Object obj : source) {
         dest.add(obj.toString());
       }
     }
 
     return dest;
   }
 
 
   /**
    * Returns true if the two objects are both null or are equal to
    * each other
    */
   public static boolean equals(Object obj1, Object obj2) {
     if ((obj1 == null) && (obj2 == null)) {
       return true;
     }
 
     if ((obj1 == null) && (obj2 != null)) {
       return false;
     }
 
     if ((obj1 != null) && (obj2 == null)) {
       return false;
     }
 
     return obj1.equals(obj2);
   }
 
 
   /**
    * Returns true the object arrays are both null or are equal to each
    * other
    */
   public static boolean equals(Object[] obj1, Object[] obj2) {
     if ((obj1 == null) && (obj2 == null)) {
       return true;
     }
 
     if ((obj1 == null) && (obj2 != null)) {
       return false;
     }
 
     if ((obj1 != null) && (obj2 == null)) {
       return false;
     }
 
     if (obj1.length != obj2.length) {
       return false;
     }
     
     boolean toReturn = true;
     for (int i = 0; i < obj1.length; i++) {
       toReturn &= equals(obj1[i], obj2[i]);
     }
 
     return toReturn;
   }
 
 }
