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
 * $Id: SiteConfiguration.java,v 1.4 2008-04-17 16:25:11 veiming Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.common.configuration;
 
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.sun.identity.shared.Constants;
 import com.sun.identity.sm.SMSException;
 import com.sun.identity.sm.ServiceConfig;
 import com.sun.identity.sm.ServiceSchema;
 import com.sun.identity.sm.ServiceSchemaManager;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * This manages site configuration information.
  */
 public class SiteConfiguration extends ConfigurationBase {
 
     // prevent instantiation of this class.
     private SiteConfiguration() {
     }
     
     /**
      * Returns a set of site information where each entry in a set is
      * a string of this format <code>site-instance-name|siteId</code>.
      *
      * @param ssoToken Single Sign-On Token which is used to query the service
      *        management datastore.
      * @return a set of site information.
      * @throws SMSException if errors access in the service management
      *         datastore.
      * @throws SSOException if the <code>ssoToken</code> is not valid.
      */
     public static Set getSiteInfo(SSOToken ssoToken) 
         throws SMSException, SSOException {
         Set siteInfo = null;
         
         if (isLegacy(ssoToken)) {
             siteInfo = legacyGetSiteInfo(ssoToken);
         } else {
             siteInfo = new HashSet();
             ServiceConfig sc = getRootSiteConfig(ssoToken);
             if (sc != null) {
                 Set names = sc.getSubConfigNames("*");
                 
                 for (Iterator i = names.iterator(); i.hasNext(); ) {
                     String name = (String)i.next();
                     siteInfo.addAll(getSiteInfo(sc, name));
                 }
             }
         }
         return siteInfo;
     }
 
     private static Set getSiteInfo(
         ServiceConfig rootNode,
         String name
     ) throws SMSException, SSOException {
         Set info = new HashSet();
         ServiceConfig sc = rootNode.getSubConfig(name);
         ServiceConfig accessPoint = sc.getSubConfig(SUBCONFIG_ACCESS_URL);
 
         Map map = accessPoint.getAttributes();
         Set setId = (Set)map.get(ATTR_PRIMARY_SITE_ID);
         Set setURL = (Set)map.get(ATTR_PRIMARY_SITE_URL);
         info.add((String)setURL.iterator().next() + "|" +
             (String)setId.iterator().next());
                 
         Set secURLs = accessPoint.getSubConfigNames("*");
         if ((secURLs != null) && !secURLs.isEmpty()) {
             for (Iterator i = secURLs.iterator(); i.hasNext(); ) {
                 String secName = (String)i.next();
                 ServiceConfig s = accessPoint.getSubConfig(secName);
                 Map mapValues = s.getAttributes();
                 setId = (Set)mapValues.get(ATTR_SEC_ID);
                 info.add(secName + "|" + (String)setId.iterator().next()); 
             }
         }
         return info;
     }
     
     /**
      * Returns a set of site instance name (String).
      *
      * @param ssoToken Single Sign-On Token which is used to query the service
      *        management datastore.
      * @return a set of site instance name.
      * @throws SMSException if errors access in the service management
      *         datastore.
      * @throws SSOException if the <code>ssoToken</code> is not valid.
      */
     public static Set getSites(SSOToken ssoToken)
         throws SMSException, SSOException {
         Set sites = new HashSet();
 
         if (isLegacy(ssoToken)) {
             Set siteInfo = legacyGetSiteInfo(ssoToken);
             if ((siteInfo != null) && !siteInfo.isEmpty()) {
                 for (Iterator i = siteInfo.iterator(); i.hasNext(); ) {
                     String site = (String)i.next();
                     int idx = site.indexOf('|');
                     if (idx != -1) {
                         site = site.substring(0, idx);
                     }
                     sites.add(site);
                 }
             }
         } else {
             ServiceConfig sc = getRootSiteConfig(ssoToken);
             if (sc != null) {
                 sites.addAll(sc.getSubConfigNames("*"));
             }
         }
         return sites;
     }
 
     /**
      * Deletes a site.
      *
      * @param ssoToken Single Sign-On Token which is used to access to the
      *        service management datastore.
      * @param siteName Name of the site.
      * @throws SMSException if errors access in the service management
      *         datastore.
      * @throws SSOException if the <code>ssoToken</code> is not valid.
      */
     public static boolean deleteSite(
         SSOToken ssoToken,
         String siteName
     ) throws SMSException, SSOException {
         boolean deleted = false;
         
         if (isLegacy(ssoToken)) {
             ServiceSchemaManager sm = new ServiceSchemaManager(
                 Constants.SVC_NAME_PLATFORM, ssoToken);
             ServiceSchema sc = sm.getGlobalSchema();
             Map attrs = sc.getAttributeDefaults();
             String site = siteName + "|";
             Set sites = (Set)attrs.get(OLD_ATTR_SITE_LIST);
 
             for (Iterator i = sites.iterator(); i.hasNext() && !deleted; ) {
                 String s = (String)i.next();
                 if (s.startsWith(site)) {
                     i.remove();
                     deleted = true;
                 }
             }
 
             if (deleted) {
                 sc.setAttributeDefaults(OLD_ATTR_SITE_LIST, sites);
             }
         } else {
             ServiceConfig sc = getRootSiteConfig(ssoToken);
             
             if (sc != null) {
                 ServiceConfig cfg = sc.getSubConfig(siteName);
                 if (cfg != null) {
                    Set svrs = listServers(ssoToken, siteName);
                    if ((svrs != null) && !svrs.isEmpty()) {
                        removeServersFromSite(ssoToken, siteName, svrs);
                    }
                     sc.removeSubConfig(siteName);
                     deleted = true;
                 } 
             }
         }
 
         return deleted;
     }
 
     /**
      * Creates a site.
      *
      * @param ssoToken Single Sign-On Token which is used to access to the
      *        service management datastore.
      * @param siteName Name of the site.
      * @param siteURL primary URL of the site.
      * @param secondaryURLs secondary URLs of the site.
      * @throws SMSException if errors access in the service management
      *         datastore.
      * @throws SSOException if the <code>ssoToken</code> is not valid.
      * @throws ConfigurationException if site url is invalid.
      */
     public static boolean createSite(
         SSOToken ssoToken,
         String siteName,
         String siteURL,
         Collection secondaryURLs    
     ) throws SMSException, SSOException, ConfigurationException {
         boolean created = false;
 
         if (isLegacy(ssoToken)) {
             ServiceSchemaManager sm = new ServiceSchemaManager(
                 Constants.SVC_NAME_PLATFORM, ssoToken);
             String siteId = getNextId(ssoToken);
             ServiceSchema sc = sm.getGlobalSchema();
             Map attrs = sc.getAttributeDefaults();
             Set sites = (Set)attrs.get(OLD_ATTR_SITE_LIST);
             //need to do this because we are getting Collections.EMPTY.SET;
             if ((sites == null) || sites.isEmpty()) {
                 sites = new HashSet();
             }
             sites.add(siteName + "|" + siteId);
             sc.setAttributeDefaults(OLD_ATTR_SITE_LIST, sites);
         } else {
             ServiceConfig sc = getRootSiteConfig(ssoToken);
             
             if (sc != null) {
                 String siteId = getNextId(ssoToken);
                 created = createSite(ssoToken, siteName, siteURL, siteId,
                     secondaryURLs);
             }
         }
 
         if (created) {
             updateOrganizationAlias(ssoToken, siteURL, true);
         }
 
         return created;
     }
 
     
     /**
      * Creates a site.
      *
      * @param ssoToken Single Sign-On Token which is used to access to the
      *        service management datastore.
      * @param siteName Name of the site.
      * @param siteURL Primary URL of the site.
      * @param siteId Identifier of the site.
      * @param secondaryURLs Secondary URLs of the site.
      * @throws SMSException if errors access in the service management
      *         datastore.
      * @throws SSOException if the <code>ssoToken</code> is not valid.
      */
     private static boolean createSite(
         SSOToken ssoToken,
         String siteName,
         String siteURL,
         String siteId,
         Collection secondaryURLs    
     ) throws SMSException, SSOException, ConfigurationException {
         boolean created = false;
         ServiceConfig sc = getRootSiteConfig(ssoToken);
         
         if (sc != null) {
             try {
                 new URL(siteURL);
             } catch (MalformedURLException ex) {
                 String[] param = {siteURL};
                 throw new ConfigurationException("invalid.site.url", param);
             }
             
             Set allURLs = getAllSiteURLs(ssoToken);
             if (allURLs.contains(siteURL)) {
                 String[] param = {siteURL};
                 throw new ConfigurationException("duplicated.site.url", param);                
             }
             
             if ((secondaryURLs != null) && !secondaryURLs.isEmpty()) {
                 for (Iterator i = secondaryURLs.iterator(); i.hasNext(); ) {
                     String url = (String)i.next();
                     if (allURLs.contains(url)) {
                         String[] param = {url};
                         throw new ConfigurationException("duplicated.site.url", 
                             param);                
                     }
                 }
             }
             
             sc.addSubConfig(siteName, SUBSCHEMA_SITE, 0, Collections.EMPTY_MAP);
             ServiceConfig scSite = sc.getSubConfig(siteName);
             
             Map siteValues = new HashMap(2);
             Set setSiteId = new HashSet(2);
             setSiteId.add(siteId);
             siteValues.put(ATTR_PRIMARY_SITE_ID, setSiteId);
             Set setSiteURL = new HashSet(2);
             setSiteURL.add(siteURL);
             siteValues.put(ATTR_PRIMARY_SITE_URL, setSiteURL);
             scSite.addSubConfig(SUBCONFIG_ACCESS_URL, SUBCONFIG_ACCESS_URL, 0,
                 siteValues);
 
             if ((secondaryURLs != null) && !secondaryURLs.isEmpty()) {
                 setSiteSecondaryURLs(ssoToken, siteName, secondaryURLs);
             }
 
             created = true;
         }
         return created;
     }
 
     /**
      * Returns the primary URL of a site.
      *
      * @param ssoToken Single Sign-On Token which is used to access to the
      *        service management datastore.
      * @param siteName Name of the site.
      * @return the primary URL of a site.
      * @throws SMSException if errors access in the service management
      *         datastore.
      * @throws SSOException if the <code>ssoToken</code> is not valid.
      */
     public static String getSitePrimaryURL(SSOToken ssoToken, String siteName)
         throws SMSException, SSOException {
         ServiceConfig rootNode = getRootSiteConfig(ssoToken);
         ServiceConfig sc = rootNode.getSubConfig(siteName);
         ServiceConfig accessPoint = sc.getSubConfig(SUBCONFIG_ACCESS_URL);
 
         Map map = accessPoint.getAttributes();
         Set set = (Set)map.get(ATTR_PRIMARY_SITE_URL);
         return (String)set.iterator().next();
     }
 
     /**
      * Returns the primary and secondary URLs of a site.
      *
      * @param ssoToken Single Sign-On Token which is used to access to the
      *        service management datastore.
      * @param siteName Name of the site.
      * @return the primary and secondary URLs of a site.
      * @throws SMSException if errors access in the service management
      *         datastore.
      * @throws SSOException if the <code>ssoToken</code> is not valid.
      */
     public static Set getSiteURLs(SSOToken ssoToken, String siteName)
         throws SMSException, SSOException {
         Set urls = new HashSet();
         ServiceConfig rootNode = getRootSiteConfig(ssoToken);
         ServiceConfig sc = rootNode.getSubConfig(siteName);
         ServiceConfig accessPoint = sc.getSubConfig(SUBCONFIG_ACCESS_URL);
         Map map = accessPoint.getAttributes();
         Set set = (Set)map.get(ATTR_PRIMARY_SITE_URL);
         urls.add(set.iterator().next());
 
         Set secondary = accessPoint.getSubConfigNames("*");
         if ((secondary != null) && !secondary.isEmpty()) {
             urls.addAll(secondary);
         }
         return urls;
     }
     
     /**
      * Returns the secondary URLs of a site.
      *
      * @param ssoToken Single Sign-On Token which is used to access to the
      *        service management datastore.
      * @param siteName Name of the site.
      * @return the secondary URLs of a site.
      * @throws SMSException if errors access in the service management
      *         datastore.
      * @throws SSOException if the <code>ssoToken</code> is not valid.
      */
     public static Set getSiteSecondaryURLs(SSOToken ssoToken, String siteName)
         throws SMSException, SSOException {
         Set secondaryURLs = new HashSet();
         ServiceConfig rootNode = getRootSiteConfig(ssoToken);
         ServiceConfig sc = rootNode.getSubConfig(siteName);
         ServiceConfig accessPoint = sc.getSubConfig(SUBCONFIG_ACCESS_URL);
 
         Set secondary = accessPoint.getSubConfigNames("*");
         if ((secondary != null) && !secondary.isEmpty()) {
             secondaryURLs.addAll(secondary);
         }
         return secondaryURLs;
     }
 
     /**
      * Sets the primary URL of a site.
      *
      * @param ssoToken Single Sign-On Token which is used to access to the
      *        service management datastore.
      * @param siteName Name of the site.
      * @param siteURL Primary URL of a site.
      * @throws SMSException if errors access in the service management
      *         datastore.
      * @throws SSOException if the <code>ssoToken</code> is not valid.
      */
     public static void setSitePrimaryURL(
         SSOToken ssoToken,
         String siteName,
         String siteURL
     ) throws SMSException, SSOException, ConfigurationException {
         ServiceConfig rootNode = getRootSiteConfig(ssoToken);
         ServiceConfig sc = rootNode.getSubConfig(siteName);
         ServiceConfig accessPoint = sc.getSubConfig(SUBCONFIG_ACCESS_URL);
 
         Map existing = accessPoint.getAttributes();
         Set existingSet = (Set)existing.get(ATTR_PRIMARY_SITE_URL);
         
         if (!existingSet.contains(siteURL)) {
             Set allURLs = getAllSiteURLs(ssoToken);
             
             if (allURLs.contains(siteURL)) {
                 String[] param = {siteURL};
                 throw new ConfigurationException("duplicated.site.url", param);
             }
             
             Map map = new HashMap(2);
             Set set = new HashSet(2);
             set.add(siteURL);
             map.put(ATTR_PRIMARY_SITE_URL, set);
             accessPoint.setAttributes(map);
         }
     }
 
     /**
      * Sets the secondary URLs of a site.
      *
      * @param ssoToken Single Sign-On Token which is used to access to the
      *        service management datastore.
      * @param siteName Name of the site.
      * @param secondaryURLs secondary URLs of a site.
      * @throws SMSException if errors access in the service management
      *         datastore.
      * @throws SSOException if the <code>ssoToken</code> is not valid.
      */
     public static void setSiteSecondaryURLs(
         SSOToken ssoToken,
         String siteName,
         Collection secondaryURLs
     ) throws SMSException, SSOException, ConfigurationException {
         for (Iterator i = secondaryURLs.iterator(); i.hasNext(); ) {
             String url = (String)i.next();
             try {
                 new URL(url);
             } catch (MalformedURLException ex) {
                 String[] param = {url};
                 throw new ConfigurationException("invalid.site.url", param);
             }
         }
         
         ServiceConfig rootNode = getRootSiteConfig(ssoToken);
         ServiceConfig sc = rootNode.getSubConfig(siteName);
         ServiceConfig accessPoint = sc.getSubConfig(SUBCONFIG_ACCESS_URL);
 
         Set secondary = accessPoint.getSubConfigNames("*");
         Set toAdd = new HashSet(secondaryURLs.size());
         toAdd.addAll(secondaryURLs);
         Set toRemove = new HashSet(secondary.size());
       
         if ((secondary != null) && !secondary.isEmpty()) {
             toRemove.addAll(secondary);
             toRemove.removeAll(secondaryURLs);
             toAdd.removeAll(secondary);
         }
         
         Set allURLs = getAllSiteURLs(ssoToken);
         for (Iterator i = toAdd.iterator(); i.hasNext(); ) {
             String url = (String)i.next();
             if (allURLs.contains(url)) {
                 String[] param = {url};
                 throw new ConfigurationException("duplicated.site.url", param);
             }
         }
   
         for (Iterator i = toRemove.iterator(); i.hasNext(); ) {
             String url = (String)i.next();
             accessPoint.removeSubConfig(url);
         }
         
         for (Iterator i = toAdd.iterator(); i.hasNext(); ) {
             String url = (String)i.next();
             Map values = new HashMap(2);
             Set set = new HashSet(2);
             set.add(getNextId(ssoToken));
             values.put(ATTR_SEC_ID, set);
             accessPoint.addSubConfig(url, SUBCONFIG_SEC_URLS, 0, values);
         }
     }
 
     /**
      * Adds the secondary URLs of a site.
      *
      * @param ssoToken Single Sign-On Token which is used to access to the
      *        service management datastore.
      * @param siteName Name of the site.
      * @param secondaryURLs Secondary URLs to be added to site.
      * @throws SMSException if errors access in the service management
      *         datastore.
      * @throws SSOException if the <code>ssoToken</code> is not valid.
      */
     public static void addSiteSecondaryURLs(
         SSOToken ssoToken,
         String siteName,
         Collection secondaryURLs    
     ) throws SMSException, SSOException, ConfigurationException {
         ServiceConfig rootNode = getRootSiteConfig(ssoToken);
         ServiceConfig sc = rootNode.getSubConfig(siteName);
         ServiceConfig accessPoint = sc.getSubConfig(SUBCONFIG_ACCESS_URL);
 
         Set toAdd = new HashSet(secondaryURLs.size() *2);
         toAdd.addAll(secondaryURLs);
 
         Set secondary = accessPoint.getSubConfigNames("*");
         if ((secondary != null) && !secondary.isEmpty()) {
             toAdd.removeAll(secondary);
         }
 
         Set allURLs = getAllSiteURLs(ssoToken);
         for (Iterator i = toAdd.iterator(); i.hasNext(); ) {
             String url = (String)i.next();
             if (allURLs.contains(url)) {
                 String[] param = {url};
                 throw new ConfigurationException("duplicated.site.url", param);
             }
         }
         
         for (Iterator i = toAdd.iterator(); i.hasNext(); ){
             String url = (String)i.next();
             Map values = new HashMap(2);
             Set set = new HashSet(2);
             set.add(getNextId(ssoToken));
             values.put(ATTR_SEC_ID, set);
             accessPoint.addSubConfig(url, SUBCONFIG_SEC_URLS, 0, values);
         }
     }
 
     /**
      * Removes the secondary URLs from a site.
      *
      * @param ssoToken Single Sign-On Token which is used to access to the
      *        service management datastore.
      * @param siteName Name of the site.
      * @param secondaryURLs Secondary URLs to be removed from site.
      * @throws SMSException if errors access in the service management
      *         datastore.
      * @throws SSOException if the <code>ssoToken</code> is not valid.
      */
     public static void removeSiteSecondaryURLs(
         SSOToken ssoToken,
         String siteName,
         Collection secondaryURLs
     
     ) throws SMSException, SSOException {
         ServiceConfig rootNode = getRootSiteConfig(ssoToken);
         ServiceConfig sc = rootNode.getSubConfig(siteName);
         ServiceConfig accessPoint = sc.getSubConfig(SUBCONFIG_ACCESS_URL);
 
         Set secondary = accessPoint.getSubConfigNames("*");
         if ((secondary != null) && !secondary.isEmpty()) {
             for (Iterator i = secondary.iterator(); i.hasNext(); ) {
                 String secName = (String)i.next();
                 if (secondaryURLs.contains(secName)) {
                     accessPoint.removeSubConfig(secName);
                 }
             }
         }
     }
 
     /**
      * Adds a set of server instances to a site.
      *
      * @param ssoToken Single Sign-On Token which is used to access to the
      *        service management datastore.
      * @param siteId Identifier of the site.
      * @param serverInstanceNames Set of server instance names.
      * @throws SMSException if errors access in the service management
      *         datastore.
      * @throws SSOException if the <code>ssoToken</code> is not valid.
      */
     public static void addServersToSite(
         SSOToken ssoToken,
         String siteName,
         Collection serverInstanceNames
     ) throws SMSException, SSOException {
         String siteId = getSiteId(ssoToken, siteName);
 
         if (siteId != null) {
             for (Iterator i = serverInstanceNames.iterator(); i.hasNext(); ) {
                 String svr = (String)i.next();
                 ServerConfiguration.addToSite(ssoToken, svr, siteName);
             }
         }
     }
 
     /**
      * Removes a set of server instances from a site.
      *
      * @param ssoToken Single Sign-On Token which is used to access to the
      *        service management datastore.
      * @param siteId Identifier of the site.
      * @param serverInstanceNames Set of server instance names.
      * @throws SMSException if errors access in the service management
      *         datastore.
      * @throws SSOException if the <code>ssoToken</code> is not valid.
      */
     public static void removeServersFromSite(
         SSOToken ssoToken,
         String siteName,
         Collection serverInstanceNames
     ) throws SMSException, SSOException {
         String siteId = getSiteId(ssoToken, siteName);
 
         if (siteId != null) {
             for (Iterator i = serverInstanceNames.iterator(); i.hasNext();){
                 String svr = (String)i.next();
                 ServerConfiguration.removeFromSite(ssoToken, svr, siteName);
             }
         }
     }
 
     private static String getSiteId(SSOToken ssoToken, String siteName)
         throws SMSException, SSOException {
         String siteId = null;
 
         if (isLegacy(ssoToken)) {
             Set sites = legacyGetSiteInfo(ssoToken);
             if ((sites != null) && !sites.isEmpty()) {
                 for (Iterator i = sites.iterator();
                     i.hasNext() && (siteId == null);
                 ) {
                     String site = (String)i.next();
                     int idx = site.indexOf('|');
                     if (idx != -1) {
                         String name = site.substring(0, idx);
                         if (name.equals(siteName)) {
                             siteId = site.substring(idx+1);
                             idx = siteId.indexOf('|');
 
                             if (idx != -1) {
                                 siteId = siteId.substring(0, idx);
                             }
                         }
                     }
                 }
             }
         } else {
             Set siteIds = getSiteConfigurationIds(
                 ssoToken, null, siteName, true);
             if ((siteIds != null) && !siteIds.isEmpty()) {
                 siteId = (String)siteIds.iterator().next();
             }
         }
 
         return siteId;
     }
 
     /**
      * Returns the server instance names that belong to a site.
      *
      * @param ssoToken Single Sign-On Token which is used to access to the
      *        service management datastore.
      * @param siteId Identifier of the site.
      * @return the server instance names that belong to a site.
      * @throws SMSException if errors access in the service management
      *         datastore.
      * @throws SSOException if the <code>ssoToken</code> is not valid.
      */
     public static Set listServers(
         SSOToken ssoToken,
         String siteName
     ) throws SMSException, SSOException {
         Set members = new HashSet();
         String siteId = getSiteId(ssoToken, siteName);
 
         if (siteId != null) {
             Set allServers = ServerConfiguration.getServers(ssoToken);
 
             for (Iterator i = allServers.iterator(); i.hasNext();){
                 String svr = (String)i.next();
                 if (ServerConfiguration.belongToSite(ssoToken, svr, siteName)) {
                     members.add(svr);
                 }
             }
         }
 
         return members;
     }
 
     /**
      * Returns <code>true</code> if site exists.
      *
      * @param ssoToken Single Sign-On Token which is used to access to the
      *        service management datastore.
      * @param siteName Name of the site.
      * @return <code>true</code> if site exists.
      * @throws SMSException if errors access in the service management
      *         datastore.
      * @throws SSOException if the <code>ssoToken</code> is not valid.
      */
     public static boolean isSiteExist(
         SSOToken ssoToken,
         String siteName
     ) throws SMSException, SSOException {
         Set sites = getSites(ssoToken);
         return sites.contains(siteName);
     }
     
     private static Set getAllSiteURLs(SSOToken ssoToken)
         throws SMSException, SSOException {
         Set urls = new HashSet();
         Set sites = getSites(ssoToken);
         for (Iterator i = sites.iterator(); i.hasNext(); ) {
             String siteName = (String)i.next();
             urls.addAll(getSiteURLs(ssoToken, siteName));
         }
         return urls;
     }
 
     /**
      * Returns site name where the given URL is either its primary or
      * secondary URL.
      * 
      * @param ssoToken Single Sign-On Token which is used to access to the
      *        service management datastore.
      * @param URL Lookup URL.
      * @return site name.
      * @throws SMSException if errors access in the service management
      *         datastore.
      * @throws SSOException if the <code>ssoToken</code> is not valid.
      */
     public static String getSiteIdByURL(SSOToken ssoToken, String url) 
         throws SMSException, SSOException {
         String siteName = null;
         Set sites = getSites(ssoToken);
         for (Iterator i = sites.iterator(); i.hasNext() && (siteName == null);){
             String name = (String)i.next();
             Set urls = getSiteURLs(ssoToken, name);
             if (urls.contains(url)) {
                 siteName = name;
             }
         }
         return siteName;
     }
 }
