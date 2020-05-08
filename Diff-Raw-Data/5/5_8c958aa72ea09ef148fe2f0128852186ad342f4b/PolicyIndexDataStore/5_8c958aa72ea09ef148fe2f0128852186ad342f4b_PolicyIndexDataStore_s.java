 /*
  * 
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright (c) 2009 Sun Microsystems Inc. All Rights Reserved
  *
  * The contents of this file are subject to the terms
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
 * $Id: PolicyIndexDataStore.java,v 1.3 2009-01-17 02:34:08 veiming Exp $
  */
 
 package com.sun.identity.sm;
 
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.sun.identity.entitlement.EntitlementException;
 import com.sun.identity.entitlement.IPolicyIndexDataStore;
 import com.sun.identity.security.AdminTokenAction;
 import com.sun.identity.shared.encode.Base64;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.security.AccessController;
 import java.text.MessageFormat;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Using OpenSSO Service Management Layer to index policy.
  */
 public class PolicyIndexDataStore implements  IPolicyIndexDataStore {
     private static final String START_DN_TEMPLATE = 
          "ou=default,ou=GlobalConfig,ou=1.0,ou=PolicyIndex,ou=services,{0}";
     private static final String DN_TEMPLATE = "ou={1}," + START_DN_TEMPLATE;
     private static final String HOST_INDEX_KEY = "hostindex";
     private static final String PATH_INDEX_KEY = "pathindex";
     private static final String SERIALIZABLE_INDEX_KEY = "serializable";
 
     private static final String FILTER_TEMPLATE = 
         "(|(" + SMSEntry.ATTR_XML_KEYVAL + "=" + HOST_INDEX_KEY + "={0})" +
           "(" + SMSEntry.ATTR_XML_KEYVAL + "=" + PATH_INDEX_KEY + "={1})" +
         ")";
 
     /**
      * Adds an index entry.
      * 
      * @param name Name of the entry to be added.
      * @param hostIndex Host index.
      * @param pathIndex Path index.
      * @param policy Policy Object.
      * @throws EntitlementException if the entry already exists.
      */
     public void add(
         String name, 
         Set<String> hostIndexes,
         Set<String> pathIndexes,
         Serializable policy
     ) throws EntitlementException {
         Object[] params = {SMSEntry.getRootSuffix(), name};
         String dn = MessageFormat.format(DN_TEMPLATE, params);
         
         SSOToken adminToken = (SSOToken)AccessController.doPrivileged(
                  AdminTokenAction.getInstance());
         try {
             SMSEntry s = new SMSEntry(adminToken, dn);
             Map<String, Set<String>> map = new HashMap<String, Set<String>>();
             
             Set<String> searchable = new HashSet<String>();
             map.put(SMSEntry.ATTR_XML_KEYVAL, searchable);
             
             for (String i : hostIndexes) {
                 searchable.add(HOST_INDEX_KEY + "=" + i);
             }
             for (String i : pathIndexes) {
                 searchable.add(PATH_INDEX_KEY + "=" + i);
             }
             
             Set<String> set = new HashSet<String>(2);
             map.put(SMSEntry.ATTR_KEYVAL, set);
             set.add(SERIALIZABLE_INDEX_KEY + "=" + serializeObject(policy));
 
             Set<String> setObjectClass = new HashSet<String>(4);
             map.put(SMSEntry.ATTR_OBJECTCLASS, setObjectClass);
             setObjectClass.add(SMSEntry.OC_TOP);
             setObjectClass.add(SMSEntry.OC_SERVICE_COMP);
             
             s.setAttributes(map);
             s.save();
         } catch (SMSException e) {
             throw new EntitlementException(e.getMessage(), 
                 e.getExceptionCode());
         } catch (SSOException e) {
             throw new EntitlementException(e.getMessage(), -1);
         }
     }
     
     private String serializeObject(Serializable object) 
         throws EntitlementException {
         ObjectOutputStream oos = null;
         try {
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             oos = new ObjectOutputStream(out);
             oos.writeObject(object);
             oos.close();
             return Base64.encode(out.toByteArray());
         } catch (IOException ex) {
             throw new EntitlementException(ex.getMessage(), -1);
         } finally {
             if (oos != null) {
                 try {
                     oos.close();
                 } catch (IOException e) {
                     // ignore
                 }
             }
         }
     }
 
     private Object deserializeObject(String strSerialized) 
         throws EntitlementException {
         ObjectInputStream ois = null;
         try {
             InputStream in = new ByteArrayInputStream(
                 Base64.decode(strSerialized));
             ois = new ObjectInputStream(in);
             return ois.readObject();
         } catch (ClassNotFoundException ex) {
             throw new EntitlementException(ex.getMessage(), -1);
         } catch (IOException ex) {
             throw new EntitlementException(ex.getMessage(), -1);
         } finally {
             try {
                 if (ois != null) {
                     ois.close();
                 }
             } catch (IOException ex) {
                 // ignore
             }
         }
     }
 
     /**
      *  Delete an idex entry.
      * 
      * @param name Name of the entry to be deleted.
      * @throws EntitlementException if deletion fails.
      */
     public void delete(String name)
         throws EntitlementException {
         Object[] params = {SMSEntry.getRootSuffix(), name};
         String dn = MessageFormat.format(DN_TEMPLATE, params);
         
         SSOToken adminToken = (SSOToken)AccessController.doPrivileged(
                  AdminTokenAction.getInstance());
         if (SMSEntry.checkIfEntryExists(dn, adminToken)) {
             try {
                 SMSEntry s = new SMSEntry(adminToken, dn);
                 s.delete();
             } catch (SMSException e) {
                 throw new EntitlementException(e.getMessage(),
                     e.getExceptionCode());
             } catch (SSOException e) {
                 throw new EntitlementException(e.getMessage(), -1);
             }
         }
     }
 
     /**
      * Searches for policy objects.
      * 
      * @param hostIndex Host index.
      * @param pathIndex Path index.
      * @return a set of matching policy objects.
      * @throws EntitlementException if search operation fails.
      */
     public Set<Object> search(String hostIndex, String pathIndex)
         throws EntitlementException {
         Set<Object> results = new HashSet<Object>();
 
         SSOToken adminToken = (SSOToken) AccessController.doPrivileged(
             AdminTokenAction.getInstance());
         Object[] params = {SMSEntry.getRootSuffix()};
         String startDN = MessageFormat.format(START_DN_TEMPLATE, params);
         Object[] p = {hostIndex, pathIndex};
         String filter = MessageFormat.format(FILTER_TEMPLATE, p);
         
         try {
             Set<String> setDN = SMSEntry.search(adminToken, startDN, filter);
             
             for (String dn : setDN) {
                 SMSEntry s = new SMSEntry(adminToken, dn);
                 Map<String, Set<String>> map = s.getAttributes();
                 Set<String> set = map.get(SMSEntry.ATTR_KEYVAL);
                 String ser = set.iterator().next();
                ser = ser.substring(SERIALIZABLE_INDEX_KEY.length());
                 results.add(deserializeObject(ser));
             }
 
             return results;
         } catch (SMSException e) {
             throw new EntitlementException(e.getMessage(),
                 e.getExceptionCode());
         } catch (SSOException e) {
             throw new EntitlementException(e.getMessage(), -1);
         }
     }
 }
