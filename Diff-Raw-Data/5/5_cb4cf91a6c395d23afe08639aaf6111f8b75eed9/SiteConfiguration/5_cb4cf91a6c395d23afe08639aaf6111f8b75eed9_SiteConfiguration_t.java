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
 * $Id: SiteConfiguration.java,v 1.2 2008-01-05 01:33:28 veiming Exp $
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
                 
         Set failovers = accessPoint.getSubConfigNames("*");
         if ((failovers != null) && !failovers.isEmpty()) {
             for (Iterator i = failovers.iterator(); i.hasNext(); ) {
                 String foName = (String)i.next();
                 ServiceConfig s = accessPoint.getSubConfig(foName);
                 Map mapValues = s.getAttributes();
                setId = (Set)mapValues.get(ATTR_FAILOVER_ID);
                 info.add(foName + "|" + (String)setId.iterator().next()); 
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
      * @param failoverURLs failover URLs of the site.
      * @throws SMSException if errors access in the service management
      *         datastore.
      * @throws SSOException if the <code>ssoToken</code> is not valid.
      * @throws ConfigurationException if site url is invalid.
      */
     public static boolean createSite(
         SSOToken ssoToken,
         String siteName,
         String siteURL,
         Collection failoverURLs
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
                     failoverURLs);
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
      * @param failoverURLs failover URLs of the site.
      * @throws SMSException if errors access in the service management
      *         datastore.
      * @throws SSOException if the <code>ssoToken</code> is not valid.
      */
     private static boolean createSite(
         SSOToken ssoToken,
         String siteName,
         String siteURL,
         String siteId,
         Collection failoverURLs
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
 
             if ((failoverURLs != null) && !failoverURLs.isEmpty()) {
                 setSiteFailoverURLs(ssoToken, siteName, failoverURLs);
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
      * Returns the failover URLs of a site.
      *
      * @param ssoToken Single Sign-On Token which is used to access to the
      *        service management datastore.
      * @param siteName Name of the site.
      * @return the failover URLs of a site.
      * @throws SMSException if errors access in the service management
      *         datastore.
      * @throws SSOException if the <code>ssoToken</code> is not valid.
      */
     public static Set getSiteFailoverURLs(SSOToken ssoToken, String siteName)
         throws SMSException, SSOException {
         Set failoverURLs = new HashSet();
         ServiceConfig rootNode = getRootSiteConfig(ssoToken);
         ServiceConfig sc = rootNode.getSubConfig(siteName);
         ServiceConfig accessPoint = sc.getSubConfig(SUBCONFIG_ACCESS_URL);
 
         Set failovers = accessPoint.getSubConfigNames("*");
         if ((failovers != null) && !failovers.isEmpty()) {
             for (Iterator i = failovers.iterator(); i.hasNext(); ) {
                 failoverURLs.add(i.next());
             }
         }
         return failoverURLs;
 
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
     ) throws SMSException, SSOException {
         ServiceConfig rootNode = getRootSiteConfig(ssoToken);
         ServiceConfig sc = rootNode.getSubConfig(siteName);
         ServiceConfig accessPoint = sc.getSubConfig(SUBCONFIG_ACCESS_URL);
 
         Map map = new HashMap(2);
         Set set = new HashSet(2);
         set.add(siteURL);
         map.put(ATTR_PRIMARY_SITE_URL, set);
         accessPoint.setAttributes(map);
     }
 
     /**
      * Sets the failover URLs of a site.
      *
      * @param ssoToken Single Sign-On Token which is used to access to the
      *        service management datastore.
      * @param siteName Name of the site.
      * @param failoverURLs Failover URLs of a site.
      * @throws SMSException if errors access in the service management
      *         datastore.
      * @throws SSOException if the <code>ssoToken</code> is not valid.
      */
     public static void setSiteFailoverURLs(
         SSOToken ssoToken,
         String siteName,
         Collection failoverURLs
     ) throws SMSException, SSOException, ConfigurationException {
         ServiceConfig rootNode = getRootSiteConfig(ssoToken);
         ServiceConfig sc = rootNode.getSubConfig(siteName);
         ServiceConfig accessPoint = sc.getSubConfig(SUBCONFIG_ACCESS_URL);
 
         Set failovers = accessPoint.getSubConfigNames("*");
         if ((failovers != null) && !failovers.isEmpty()) {
             for (Iterator i = failovers.iterator(); i.hasNext(); ) {
                 String foName = (String)i.next();
                 accessPoint.removeSubConfig(foName);
             }
         }
 
         for (Iterator i = failoverURLs.iterator(); i.hasNext(); ) {
             String url = (String)i.next();
             try {
                 new URL(url);
             } catch (MalformedURLException ex) {
                 String[] param = {url};
                 throw new ConfigurationException("invalid.site.url", param);
             }
         }
         
         for (Iterator i = failoverURLs.iterator(); i.hasNext(); ) {
             String url = (String)i.next();
             Map values = new HashMap(2);
             Set set = new HashSet(2);
             set.add(getNextId(ssoToken));
             values.put(ATTR_FAILOVER_ID, set);
             accessPoint.addSubConfig(url, SUBCONFIG_FAILOVERS, 0, values);
         }
     }
 
     /**
      * Adds the failover URLs of a site.
      *
      * @param ssoToken Single Sign-On Token which is used to access to the
      *        service management datastore.
      * @param siteName Name of the site.
      * @param failoverURLs Failover URLs to be added to site.
      * @throws SMSException if errors access in the service management
      *         datastore.
      * @throws SSOException if the <code>ssoToken</code> is not valid.
      */
     public static void addSiteFailoverURLs(
         SSOToken ssoToken,
         String siteName,
         Collection failoverURLs
     ) throws SMSException, SSOException {
         ServiceConfig rootNode = getRootSiteConfig(ssoToken);
         ServiceConfig sc = rootNode.getSubConfig(siteName);
         ServiceConfig accessPoint = sc.getSubConfig(SUBCONFIG_ACCESS_URL);
 
         Set toAdd = new HashSet(failoverURLs.size() *2);
         toAdd.addAll(failoverURLs);
 
         Set failovers = accessPoint.getSubConfigNames("*");
         if ((failovers != null) && !failovers.isEmpty()) {
             for (Iterator i = toAdd.iterator(); i.hasNext(); ) {
                 String foName = (String)i.next();
                 if (failovers.contains(foName)) {
                     i.remove();
                 }
             }
         }
 
         for (Iterator i = toAdd.iterator(); i.hasNext(); ){
             String url = (String)i.next();
             Map values = new HashMap(2);
             Set set = new HashSet(2);
             set.add(getNextId(ssoToken));
             values.put(ATTR_FAILOVER_ID, set);
             accessPoint.addSubConfig(url, SUBCONFIG_FAILOVERS, 0, values);
         }
     }
 
     /**
      * Removes the failover URLs from a site.
      *
      * @param ssoToken Single Sign-On Token which is used to access to the
      *        service management datastore.
      * @param siteName Name of the site.
      * @param failoverURLs Failover URLs to be removed from site.
      * @throws SMSException if errors access in the service management
      *         datastore.
      * @throws SSOException if the <code>ssoToken</code> is not valid.
      */
     public static void removeSiteFailoverURLs(
         SSOToken ssoToken,
         String siteName,
         Collection failoverURLs
     ) throws SMSException, SSOException {
         ServiceConfig rootNode = getRootSiteConfig(ssoToken);
         ServiceConfig sc = rootNode.getSubConfig(siteName);
         ServiceConfig accessPoint = sc.getSubConfig(SUBCONFIG_ACCESS_URL);
 
         Set failovers = accessPoint.getSubConfigNames("*");
         if ((failovers != null) && !failovers.isEmpty()) {
             for (Iterator i = failovers.iterator(); i.hasNext(); ) {
                 String foName = (String)i.next();
                 if (failoverURLs.contains(foName)) {
                     accessPoint.removeSubConfig(foName);
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
                 boolean added = false;
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
 }
