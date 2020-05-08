 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: SPCache.java,v 1.9 2007-11-15 16:42:45 qcheng Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 
 package com.sun.identity.saml2.profile;
 
 import com.sun.identity.common.PeriodicCleanUpMap;
 import com.sun.identity.saml2.common.SAML2Constants;
 import com.sun.identity.saml2.common.SAML2Utils;
 import com.sun.identity.shared.Constants;
 import com.sun.identity.shared.configuration.SystemPropertiesManager;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.HashSet;
 import java.util.Set;
 import netscape.ldap.util.DN;
 
 
 /**
  * This class provides the memory store for
  * SAML request and response information on Service Provider side.
  *
  */
 
 public class SPCache {
 
     public static int interval = SAML2Constants.CACHE_CLEANUP_INTERVAL_DEFAULT;
     
     static {
         String intervalStr = SystemPropertiesManager.get(
             SAML2Constants.CACHE_CLEANUP_INTERVAL);
         try {
             if (intervalStr != null && intervalStr.length() != 0) {
                 interval = Integer.parseInt(intervalStr);
                 if (interval < 0) {
                     interval = 
                         SAML2Constants.CACHE_CLEANUP_INTERVAL_DEFAULT;
                 }
             }
         } catch (NumberFormatException e) {
             if (SAML2Utils.debug.messageEnabled()) {
                 SAML2Utils.debug.message("SPCache.constructor: "
                     + "invalid cleanup interval. Using default.");
             }
         }
     }
     
     private SPCache() {
     }
 
     /**
      * Map saves the request info.
      * Key   :   requestID String
      * Value : AuthnRequestInfo object
      */
     public static PeriodicCleanUpMap requestHash = new PeriodicCleanUpMap(
         interval * 1000, interval * 1000); 
 
     /**
      * Map saves the MNI request info.
      * Key   :   requestID String
      * Value : ManageNameIDRequestInfo object
      */
     protected static PeriodicCleanUpMap mniRequestHash = new PeriodicCleanUpMap(
         interval * 1000, interval * 1000);
 
     /**
      * Map to save the relayState URL.
      * Key  : a String the relayStateID 
      * Value: a String the RelayState Value 
      */
     public static PeriodicCleanUpMap relayStateHash= new PeriodicCleanUpMap(
         interval * 1000, interval * 1000); 
 
     /**
      * Hashtable stores information required for LogoutRequest consumption.
      * key : String NameIDInfoKey (NameIDInfoKey.toValueString())
      * value : List of SPFedSession's
      *       (SPFedSession - idp sessionIndex (String)
      *                     - sp token id (String)                     
      * one key --- multiple SPFedSession's
      */
     protected static Hashtable fedSessionListsByNameIDInfoKey = new Hashtable();
 
     /**
      * SP: used to map LogoutRequest ID and inResponseTo in LogoutResponse
      * element to the original LogoutRequest object
      * key : request ID (String)
      * value : original logout request object  (LogotRequest)
      */
     public static Hashtable logoutRequestIDHash = new Hashtable();
 
     /**
      * Map saves response info for local auth.
      * Key: requestID String
      * Value: ResponseInfo object
      */
     protected static PeriodicCleanUpMap responseHash = new PeriodicCleanUpMap(
         interval * 1000, interval * 1000);
 
     /**
      * Hashtable saves AuthnContext Mapper object.
      * Key: hostEntityID+realmName
      * Value: SPAuthnContextMapper
      */
     public static Hashtable authCtxObjHash = new Hashtable();
 
     /**
      * Hashtable saves AuthnContext class name and the authLevel. 
      * Key: hostEntityID+realmName
      * Value: Map containing AuthContext Class Name as Key and value
      *              is authLevel.
      */
     public static Hashtable authContextHash = new Hashtable();
 
     /**
      * Hashtable saves the Request Parameters before redirecting
      * to IDP Discovery Service to retreive the preferred IDP.
      * Key: requestID a String
      * Value : Request Parameters Map , a Map
      */
     public static Hashtable reqParamHash = new Hashtable();
 
 
     /**
      * Cache saves the sp account mapper.
      * Key : sp account mapper class name
      * Value : sp account mapper object
      */
     public static Hashtable spAccountMapperCache = new Hashtable();
     
     /**
      * Cache saves the sp adapter class instance.
      * Key : realm + spEntityID + adapterClassName
      * Value : sp adapter class instance 
      * (<code>SAML2ServiceProviderAdapter</code>)
      */
     public static Hashtable spAdapterClassCache = new Hashtable();
 
     /**
      * Cache saves the ecp request IDP list finder.
      * Key : ecp request IDP list finder class name
      * Value : ecp request IDP list finder object
      */
     public static Hashtable ecpRequestIDPListFinderCache = new Hashtable();
 
     /**
      * Clears the auth context object hash table.
      *
      * @param realmName Organization or Realm
      */
     public static void clear(String realmName) {
         boolean isDefault = isDefaultOrg(realmName);
         if ((authCtxObjHash != null) && (!authCtxObjHash.isEmpty())) {
             Enumeration keys = authCtxObjHash.keys();
             while (keys.hasMoreElements()) {
                 String key = (String) keys.nextElement();
                 if (key.indexOf("|"+realmName) != -1) {
                         authCtxObjHash.remove(key);
                 }
                 if (isDefault && key.endsWith("|/")) {
                     authCtxObjHash.remove(key);
                 }
             }
         }
         if ((authContextHash != null) && (!authContextHash.isEmpty())) {
             Enumeration keys = authContextHash.keys();
             while (keys.hasMoreElements()) {
                 String key = (String) keys.nextElement();
                 if (key.indexOf("|"+realmName) != -1) {
                         authContextHash.remove(key);
                 }
                 if (isDefault && key.endsWith("|/")) {
                    authCtxObjHash.remove(key);
                 }
             }
         }
 
     }
 
 
     /**
      * Clears the auth context object hash table.
      */
     public static void clear() {
         if ((authCtxObjHash != null) &&
                         (!authCtxObjHash.isEmpty())) {
             authCtxObjHash.clear();
         }
         if ((authContextHash != null) && 
                         (!authContextHash.isEmpty())) {
             authContextHash.clear();
         }
    }
 
 
     /**
      * Returns <code>true</code> if the realm is root.
      *
      * @param orgName the organization name
      * @return <code>true</code> if realm is root.
      */
     public static boolean isDefaultOrg(String orgName) {
         return (orgName !=null) || orgName.equals("/");
     }
 
 }
