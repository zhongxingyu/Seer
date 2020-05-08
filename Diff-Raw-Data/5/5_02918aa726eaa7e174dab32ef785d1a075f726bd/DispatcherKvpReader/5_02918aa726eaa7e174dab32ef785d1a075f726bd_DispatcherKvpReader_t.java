 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.requests.readers;
 
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.vfny.geoserver.servlets.Dispatcher;
 
 
 /**
  * Reads in a generic request and attempts to determine its type.
  *
  * @author Chris Holmes, TOPP
  * @author Gabriel Roldn
 * @version $Id: DispatcherKvpReader.java,v 1.10 2004/07/20 20:13:49 cholmesny Exp $
  */
 public class DispatcherKvpReader {
     /** Class logger */
     private static Logger LOGGER = Logger.getLogger(
             "org.vfny.geoserver.requests.readers");
 
     /**
      * Returns the request type for a given KVP set.
      *
      * @param kvPairs DOCUMENT ME!
      *
      * @return Request type.
      */
     public static int getRequestType(Map kvPairs) {
         String responseType = ((String) kvPairs.get("REQUEST"));
         LOGGER.finer("dispatcher got request " + responseType);
 
         if (responseType != null) {
             responseType = responseType.toUpperCase();
 
             if (responseType.equals("GETCAPABILITIES") 
                || responseType.equals("CAPABILITIES")) {
                 return Dispatcher.GET_CAPABILITIES_REQUEST;
             } else if (responseType.equals("DESCRIBEFEATURETYPE")) {
                 return Dispatcher.DESCRIBE_FEATURE_TYPE_REQUEST;
             } else if (responseType.equals("GETFEATURE")) {
                 return Dispatcher.GET_FEATURE_REQUEST;
             } else if (responseType.equals("TRANSACTION")) {
                 return Dispatcher.TRANSACTION_REQUEST;
             } else if (responseType.equals("GETFEATUREWITHLOCK")) {
                 return Dispatcher.GET_FEATURE_LOCK_REQUEST;
             } else if (responseType.equals("LOCKFEATURE")) {
                 return Dispatcher.LOCK_REQUEST;
             } else if (responseType.equals("GETMAP")) {
                 return Dispatcher.GET_MAP_REQUEST;
             } else if (responseType.equals("GETFEATUREINFO")) {
                 return Dispatcher.GET_FEATURE_INFO_REQUEST;
             }
             else {
                 return Dispatcher.UNKNOWN;
             }
         } else {
             return Dispatcher.UNKNOWN;
         }
     }
 
     /**
      * Returns the request type for a given KVP set.
      *
      * @param kvPairs DOCUMENT ME!
      *
      * @return Request type.
      */
     public static int getServiceType(Map kvPairs) {
         String serviceType = ((String) kvPairs.get("SERVICE"));
 
         if (serviceType != null) {
             serviceType = serviceType.toUpperCase();
 
             if (serviceType.equals("WFS")) {
                 return Dispatcher.WFS_SERVICE;
             } else if (serviceType.equals("WMS")) {
                 return Dispatcher.WMS_SERVICE;
             } else {
                 return Dispatcher.UNKNOWN;
             }
         } else {
             return Dispatcher.UNKNOWN;
         }
     }
 }
