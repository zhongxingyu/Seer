 /**
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright (c) 2008 Sun Microsystems Inc. All Rights Reserved
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
 * $Id: TaskModelImpl.java,v 1.10 2009-03-16 22:43:46 asyhuang Exp $
  *
  */
 
 package com.sun.identity.console.task.model;
 
 import com.sun.identity.cot.COTException;
 import com.sun.identity.saml.xmlsig.JKSKeyProvider;
 import com.sun.identity.console.base.model.AMConsoleException;
 import com.sun.identity.console.base.model.AMModelBase;
 import com.sun.identity.cot.COTConstants;
 import com.sun.identity.cot.CircleOfTrustManager;
 import com.sun.identity.saml2.jaxb.entityconfig.BaseConfigType;
 import com.sun.identity.saml2.jaxb.entityconfig.EntityConfigElement;
 import com.sun.identity.saml2.jaxb.entityconfig.IDPSSOConfigElement;
 import com.sun.identity.saml2.jaxb.metadata.EntityDescriptorElement;
 import com.sun.identity.saml2.jaxb.metadata.IDPSSODescriptorElement;
 import com.sun.identity.saml2.jaxb.metadata.SingleLogoutServiceElement;
 import com.sun.identity.saml2.jaxb.metadata.SingleSignOnServiceElement;
 import com.sun.identity.saml2.meta.SAML2MetaException;
 import com.sun.identity.saml2.meta.SAML2MetaManager;
 import com.sun.identity.saml2.meta.SAML2MetaSecurityUtils;
 import com.sun.identity.saml2.meta.SAML2MetaUtils;
 import com.sun.identity.shared.xml.XMLUtils;
 import java.security.KeyStore;
 import java.security.KeyStoreException;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import javax.servlet.http.HttpServletRequest;
 import javax.xml.bind.JAXBException;
 import org.w3c.dom.Document;
 
 
 public class TaskModelImpl
     extends AMModelBase
     implements TaskModel
 {
     public TaskModelImpl(HttpServletRequest req, Map map) {
 	super(req, map);
     }
 
     /**
      * Returns realm names.
      *
      * @return realm names.
      * @throws AMConsoleException if realm cannot be retrieved.
      */
     public Set getRealms() 
         throws AMConsoleException {
         Set results = new TreeSet();
         results.addAll(super.getRealmNames("/", "*"));
         results.add("/");
         return results;
     }
 
     /**
      * Returns a set of signing keys.
      *
      * @return a set of signing keys.
      */
     public Set getSigningKeys()
         throws AMConsoleException {
         try {
             Set keyEntries = new HashSet();
             JKSKeyProvider kp = new JKSKeyProvider();
             KeyStore ks = kp.getKeyStore();
             Enumeration e = ks.aliases();
             if (e != null) {
                 while (e.hasMoreElements()) {
                     String alias = (String) e.nextElement();
                     if (ks.isKeyEntry(alias)) {
                         keyEntries.add(alias);
                     }
                 }
             }
             return keyEntries;
         } catch (KeyStoreException e) {
             throw new AMConsoleException(e.getMessage());
         }
     }
 
     /**
      * Returns a set of circle of trusts.
      * 
      * @param realm Realm.
      * @return a set of circle of trusts.
      * @throws AMConsoleException if unable to retrieve circle of trusts.
      */
     public Set getCircleOfTrusts(String realm) 
         throws AMConsoleException {
         try {
             CircleOfTrustManager mgr = new CircleOfTrustManager();
             return mgr.getAllCirclesOfTrust(realm);
         } catch (COTException ex) {
             throw new AMConsoleException(ex.getMessage());
         }
     }
     
     /**
      * Returns a set of entities in a circle of trust.
      * 
      * @param realm Realm.
      * @param cotName Name of circle of trust.
      * @return a set of entities in a circle of trust.
      * @throws AMConsoleException if unable to retrieve entities.
      */
     public Set getEntities(String realm, String cotName) 
         throws AMConsoleException {
         try {
             CircleOfTrustManager mgr = new CircleOfTrustManager();
             Set entities = mgr.listCircleOfTrustMember(realm, cotName, 
                 COTConstants.SAML2);
             return (entities == null) ? Collections.EMPTY_SET : entities;
         } catch (COTException ex) {
             throw new AMConsoleException(ex.getMessage());
         }
     }
     
     /**
      * Returns a set of hosted IDP in a circle of trust.
      * 
      * @param realm Realm.
      * @param cotName Name of circle of trust.
      * @return a set of hosted IDP in a circle of trust.
      * @throws AMConsoleException if IDP cannot be returned.
      */
     public Set getHostedIDP(String realm, String cotName)
         throws AMConsoleException {
         return getEntities(realm, cotName, true, true);
     }
     
     /**
      * Returns a set of remote IDP in a circle of trust.
      * 
      * @param realm Realm.
      * @param cotName Name of circle of trust.
      * @return a set of remote IDP in a circle of trust.
      * @throws AMConsoleException if IDP cannot be returned.
      */
     public Set getRemoteIDP(String realm, String cotName)
         throws AMConsoleException {
         return getEntities(realm, cotName, true, false);
     }
     
     
     /**
      * Returns a set of hosted SP in a circle of trust.
      * 
      * @param realm Realm.
      * @param cotName Name of circle of trust.
      * @return a set of hosted SP in a circle of trust.
      * @throws AMConsoleException if IDP cannot be returned.
      */
     public Set getHostedSP(String realm, String cotName)
         throws AMConsoleException {
         return getEntities(realm, cotName, false, true);
     }
     
     /**
      * Returns a set of remote SP in a circle of trust.
      * 
      * @param realm Realm.
      * @param cotName Name of circle of trust.
      * @return a set of remote SP in a circle of trust.
      * @throws AMConsoleException if IDP cannot be returned.
      */
     public Set getRemoteSP(String realm, String cotName)
         throws AMConsoleException {
         return getEntities(realm, cotName, false, false);
     }
 
     private Set getEntities(
         String realm, 
         String cotName, 
         boolean bIDP, 
         boolean hosted
     ) throws AMConsoleException {
         try {
             SAML2MetaManager mgr = new SAML2MetaManager();
             Set entities = getEntities(realm, cotName);
             Set results = new HashSet();
 
             for (Iterator i = entities.iterator(); i.hasNext();) {
                 String entityId = (String) i.next();
                 EntityConfigElement elm = mgr.getEntityConfig(realm, entityId);
                 if (elm.isHosted() == hosted) {
                     EntityDescriptorElement desc = mgr.getEntityDescriptor(
                         realm, entityId);
                     
                     if (bIDP) {
                         if (SAML2MetaUtils.getIDPSSODescriptor(desc) != null) {
                             results.add(entityId);
                         }
                     } else {
                         if (SAML2MetaUtils.getSPSSODescriptor(desc) != null) {
                             results.add(entityId);
                         }
                     }
                 }
             }
             return results;
         } catch (SAML2MetaException ex) {
             throw new AMConsoleException(ex.getMessage());
         }
     }
     
     /**
      * Returns a map of realm to a map of circle of trust name to a set of
      * Hosted Identity Providers.
      * 
      * @return a map of realm to a map of circle of trust name to a set of
      *         Hosted Identity Providers.
      * @throws AMConsoleException if this map cannot be constructed.
      */
     public Map getRealmCotWithHostedIDPs() 
         throws AMConsoleException {
         Map map = new HashMap();
         Set realms = getRealms();
         for (Iterator i = realms.iterator(); i.hasNext(); )  {
             String realm = (String)i.next();
             
             Set cots = getCircleOfTrusts(realm);
             for (Iterator j = cots.iterator(); j.hasNext(); ) {
                 String cotName = (String)j.next();
                 Set idps = getHostedIDP(realm, cotName);
                 
                 if ((idps != null) && !idps.isEmpty()) {
                     Map r = (Map)map.get(realm);
                     if (r == null) {
                         r = new HashMap();
                         map.put(realm, r);
                     }
                     r.put(cotName, idps);
                 }
             }
         }
         return map;
     }
     
      public Map getConfigureGoogleAppURLs(String realm, String entityId)
         throws AMConsoleException {
         Map map = new HashMap();
        IDPSSODescriptorElement idpssoDescriptor = null;
        try {
             SAML2MetaManager samlManager = new SAML2MetaManager();
             idpssoDescriptor =
                     samlManager.getIDPSSODescriptor(realm,entityId);
             if (idpssoDescriptor != null) {
                  
                 List signonList = idpssoDescriptor.getSingleSignOnService();
                 
                 for (int i=0; i<signonList.size(); i++) {
                     SingleSignOnServiceElement signElem = 
                             (SingleSignOnServiceElement) signonList.get(i);
                     String tmp = signElem.getBinding();
                     if (tmp.contains("HTTP-Redirect")) {
                         map.put("SigninPageURL",
                             returnEmptySetIfValueIsNull(
                             signElem.getLocation()));
                     }
                 }
             
                 List logoutList = idpssoDescriptor.getSingleLogoutService(); 
                 String signoutPageURL = null;
                 for (int i=0; i<logoutList.size(); i++) {
                     SingleLogoutServiceElement spslsElem = 
                             (SingleLogoutServiceElement) logoutList.get(i);
                     String tmp = spslsElem.getBinding();                    
                     if (tmp.contains("HTTP-Redirect")) {
                         signoutPageURL = spslsElem.getLocation();                      
                     }
                 }           
                 if(signoutPageURL != null) {
                     signoutPageURL = signoutPageURL 
                         + "?goto=" + entityId +"UI/Login?gx_charset=UTF-8";
                 }
                 map.put("SignoutPageURL",
                             returnEmptySetIfValueIsNull(signoutPageURL)); 
             }
                   
             map.put("ChangePasswordURL", 
                returnEmptySetIfValueIsNull(entityId+"/idm/EndUser"));
             
             // get pubkey                 
             Map extValueMap = new HashMap();
             IDPSSOConfigElement idpssoConfig = samlManager.getIDPSSOConfig(realm,entityId);
             if (idpssoConfig != null) {
                 BaseConfigType baseConfig = (BaseConfigType)idpssoConfig;
                 extValueMap = SAML2MetaUtils.getAttributes(baseConfig);
             }            
             List aList = (List) extValueMap.get("signingCertAlias");
             String signingCertAlias = null;
             if (aList != null) {
                 signingCertAlias = (String) aList.get(0);
             }
             String publickey = 
                     SAML2MetaSecurityUtils.buildX509Certificate(signingCertAlias);                      
             String str = "-----BEGIN CERTIFICATE-----\n" 
                     + publickey 
                     + "-----END CERTIFICATE-----\n";
             
             map.put("PubKey",returnEmptySetIfValueIsNull(str));
         } catch (SAML2MetaException ex) {
             throw new AMConsoleException(ex.getMessage());
         }
         return map;
      }     
              
     protected Set returnEmptySetIfValueIsNull(String str) {
         Set set = Collections.EMPTY_SET;
         if (str != null) {
             set = new HashSet(2);
             set.add(str);
         }
         return set;
     }
     
     protected Set returnEmptySetIfValueIsNull(Set set) {
         return (set != null) ? set : Collections.EMPTY_SET;
     }
     
     protected Set returnEmptySetIfValueIsNull(List l) {
         Set set = new HashSet();
         int size = l.size();
         for (int i=0;i<size;i++){
             set.add(l.get(i));
         }
         return set;
     }
 }
 
