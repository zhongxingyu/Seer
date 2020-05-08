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
 * $Id: WebtopNaming.java,v 1.16 2008-06-04 00:01:20 manish_rustagi Exp $
  *
  * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.iplanet.services.naming;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import com.sun.identity.common.GeneralTaskRunnable;
 import com.sun.identity.common.SystemTimer;
 import com.sun.identity.common.TimerPool;
 import com.iplanet.am.util.SystemProperties;
 import com.iplanet.services.comm.client.PLLClient;
 import com.iplanet.services.comm.client.SendRequestException;
 import com.iplanet.services.comm.share.Request;
 import com.iplanet.services.comm.share.RequestSet;
 import com.iplanet.services.comm.share.Response;
 import com.iplanet.services.naming.service.NamingService;
 import com.iplanet.services.naming.share.NamingBundle;
 import com.iplanet.services.naming.share.NamingRequest;
 import com.iplanet.services.naming.share.NamingResponse;
 import com.sun.identity.shared.Constants;
 import com.sun.identity.shared.debug.Debug;
 
 
 /**
  * The <code>WebtopNaming</code> class is used to get URLs for various
  * services such as session, profile, logging etc. The lookup is based on the
  * service name and the host name. The Naming Service shall contain URLs for all
  * services on all servers. For instance, two machines might host session
  * services. The Naming Service profile may look like the following:
  * 
  * <pre>
  *      host1.session.URL=&quot;http://host1:8080/SessionServlet&quot;
  *      host2.session.URL=&quot;https://host2:9090/SessionServlet&quot;
  * </pre>
  */
 
 public class WebtopNaming {
 
     public static final String NAMING_SERVICE = "com.iplanet.am.naming";
 
     public static final String NODE_SEPARATOR = "|";
 
     private static final String AM_NAMING_PREFIX = "iplanet-am-naming-";
 
     private static final String FAM_NAMING_PREFIX = "sun-naming-";
 
     private static Hashtable namingTable = null;
 
     private static Hashtable serverIdTable = null;
 
     private static Hashtable siteIdTable = null;
 
     //This is created for storing server id and lbcookievalue mapping
     //key:serverid | value:lbcookievalue      
     private static Hashtable lbCookieValuesTable = null;
 
     private static Vector platformServers = new Vector();
 
     //This is created for ignore case comparison
     private static Vector lcPlatformServers = new Vector();
 
     private static String namingServiceURL[] = null;
 
     private static Vector platformServerIDs = new Vector();
 
     protected static Debug debug;
 
     private static boolean serverMode;
 
     private static String amServerProtocol = null;
 
     private static String amServer = null;
 
     private static String amServerPort = null;
     
     private static String amServerURI;
 
     private static SiteMonitor monitorThread = null;
 
     static {
         serverMode = Boolean.valueOf(
                 System.getProperty(Constants.SERVER_MODE, SystemProperties.get(
                         Constants.SERVER_MODE, "false"))).booleanValue();
         try {
             getAMServer();
             debug = Debug.getInstance("amNaming");
         } catch (Exception ex) {
             debug.error("Failed to initialize server properties", ex);
         }
     }
 
     public static boolean isServerMode() {
         return serverMode;
     }
 
     public static boolean isSiteEnabled(
         String protocol,
         String host,
         String port, 
         String uri
     ) throws Exception {
         String serverid = getServerID(protocol, host, port, uri);
         return isSiteEnabled(serverid);
     }
 
     public static boolean isSiteEnabled(String serverid) throws Exception {
         String siteid = (String) siteIdTable.get(serverid);
         return (!serverid.equals(siteid));
     }
 
     public static String getAMServerID() throws ServerEntryNotFoundException {
         return getServerID(amServerProtocol, amServer, amServerPort, 
             amServerURI);
     }
 
     private static void getAMServer() {
         amServer = SystemProperties.get(Constants.AM_SERVER_HOST);
         amServerPort = SystemProperties.get(Constants.AM_SERVER_PORT);
         amServerProtocol = SystemProperties.get(Constants.AM_SERVER_PROTOCOL);
         amServerURI = SystemProperties.get(
             Constants.AM_SERVICES_DEPLOYMENT_DESCRIPTOR);
     }
 
     private static void initializeNamingService() {
         try {
             // Initilaize the list of naming URLs
             getNamingServiceURL();
             if (!serverMode && (namingServiceURL.length > 1)) {
                 startSiteMonitor(namingServiceURL);
             }
         } catch (Exception ex) {
             debug.error("Failed to initialize naming service", ex);
         }
     }
 
     /**
      * This method returns the URL of the specified service on the specified
      * host.
      * 
      * @param service
      *            The name of the service.
      * @param protocol
      *            The service protocol
      * @param host
      *            The service host name
      * @param port
      *            The service listening port
      * @return The URL of the specified service on the specified host.
      */
     public static URL getServiceURL(String service, String protocol,
             String host, String port, String uri) throws URLNotFoundException {
         return (getServiceURL(service, protocol, host, port, uri, serverMode));
     }
 
         /**
      * Returns the URL of the specified service on the specified host.
      * 
      * @param service Name of the service.
      * @param protocol Service protocol.
      * @param host Service host name.
      * @param port Service listening port.
      * @param uri Deployment URI.
      * @param validate Validate the protocol, host and port of AM server
      * @return The URL of the specified service on the specified host.
      */
     public static URL getServiceURL(String service, URL url, boolean validate)
         throws URLNotFoundException {
         return getServiceURL(service, url.getProtocol(), 
             url.getHost(), Integer.toString(url.getPort()), url.getPath(),
             validate);
     }
 
     /**
      * Returns the URL of the specified service on the specified host.
      * 
      * @param service Name of the service.
      * @param protocol Service protocol.
      * @param host Service host name.
      * @param port Service listening port.
      * @param validate Validate the protocol, host and port of AM server
      * @return The URL of the specified service on the specified host.
      */
     public static URL getServiceURL(
         String service, 
         String protocol,
         String host,
         String port,
         boolean validate
     ) throws URLNotFoundException {
         String namingURL = SystemProperties.get(Constants.AM_NAMING_URL);
         try {
             String uri = getURI(new URL(namingURL));
             return getServiceURL(service, protocol, host, port, uri, validate);
         } catch (MalformedURLException ex) {
             throw new URLNotFoundException(ex.getMessage());
         }
         
     }
     
     /**
      * Returns the URL of the specified service on the specified host.
      * 
      * @param service Name of the service.
      * @param protocol Service protocol.
      * @param host Service host name.
      * @param port Service listening port.
      * @param uri Deployment URI.
      * @param validate Validate the protocol, host and port of AM server
      * @return The URL of the specified service on the specified host.
      */
     public static URL getServiceURL(
         String service, 
         String protocol,
         String host,
         String port,
         String uri,
         boolean validate
     ) throws URLNotFoundException {
         try {
             // check before the first naming table update to avoid deadlock
             // uri can be empty string for pre-FAM 8.0 releases
             if ((protocol == null) || (host == null) || (port == null) ||
                 (uri == null) || (protocol.length() == 0) ||
                 (host.length() == 0) || (port.length() == 0)
             ) {
                 throw new Exception(NamingBundle.getString("noServiceURL")
                         + service);
             }
 
             if (namingTable == null) {
                 getNamingProfile(false);
             }
 
             String url = null;
             String name = AM_NAMING_PREFIX + service.toLowerCase() + "-url";
             url = (String)namingTable.get(name);
             if (url == null) {
                 name = FAM_NAMING_PREFIX + service.toLowerCase() + "-url";
                 url = (String)namingTable.get(name);
             }
 
             if (url != null) {
                 // If replacement is required, the protocol, host, and port
                 // validation is needed against the server list
                 // (iplanet-am-platform-server-list)
                 if (validate && url.indexOf("%") != -1) {
                     validate(protocol, host, port, uri);
                 }
                 // %protocol processing
                 int idx;
                 if ((idx = url.indexOf("%protocol")) != -1) {
                     url = url.substring(0, idx)
                             + protocol
                             + url.substring(idx + "%protocol".length(), url
                                     .length());
                 }
 
                 // %host processing
                 if ((idx = url.indexOf("%host")) != -1) {
                     url = url.substring(0, idx)
                             + host
                             + url.substring(idx + "%host".length(), url
                                     .length());
                 }
 
                 // %port processing
                 if ((idx = url.indexOf("%port")) != -1) {
                     // plugin the server name
                     url = url.substring(0, idx)
                             + port
                             + url.substring(idx + "%port".length(), url
                                     .length());
                 }
                 
                 // %uri processing
                 // uri can be null for previous releases.
                 if ((uri != null) && ((idx = url.indexOf("%uri")) != -1)) {
                     int test = uri.lastIndexOf('/');
                     while (test > 0) {
                         uri = uri.substring(0, test);
                         test = uri.lastIndexOf('/');
                     }
 
                     url = url.substring(0, idx) + uri + 
                         url.substring(idx + "%uri".length(), url.length());
                 }
 
                 return new URL(url);
             } else {
                 throw new Exception(NamingBundle.getString("noServiceURL")
                         + service);
             }
         } catch (Exception e) {
             throw new URLNotFoundException(e.getMessage());
         }
     }
 
     /**
      * This method returns all the URLs of the specified service based on the
      * servers in platform server list.
      * 
      * @param service
      *            The name of the service.
      * @return The URL of the specified service on the specified host.
      */
     public static Vector getServiceAllURLs(String service)
             throws URLNotFoundException {
         Vector allurls = null;
 
         try {
             if (namingTable == null) {
                 getNamingProfile(false);
             }
 
             String name = AM_NAMING_PREFIX + service.toLowerCase() + "-url";
             String url = (String)namingTable.get(name);
             if (url == null) {
                 name = FAM_NAMING_PREFIX + service.toLowerCase() + "-url";
                 url = (String)namingTable.get(name);
             }
 
             if (url != null) {
                 allurls = new Vector();
                 if (monitorThread == null) {
                     allurls.add(getServiceURL(service, amServerProtocol,
                         amServer, amServerPort, amServerURI));
                 } else {
                     if (url.indexOf("%") != -1) {
                         Vector servers =  SiteMonitor.getAvailableSites();
                         Iterator it = servers.iterator();
                         while (it.hasNext()) {
                             String server = getServerFromID((String)it.next());
                             URL serverURL = new URL(server);
                             allurls.add(getServiceURL(service,
                                 serverURL.getProtocol(), serverURL.getHost(),
                                 String.valueOf(serverURL.getPort()), 
                                 serverURL.getPath()));
                         }
                     } else {
                         allurls.add(new URL(url));
                     }
                 }
             }
 
             return allurls;
         } catch (Exception e) {
             throw new URLNotFoundException(e.getMessage());
         }
     }
 
     /**
      * This method returns all the platform servers note: This method should be
      * used only for the remote sdk, calling this method may cause performance
      * abuse, as it involves xml request over the wire.An alternative to this
      * method is use SMS getNamingProfile(true) is called over here if there's
      * any change in server list dynamically at the server side.
      */
     public static Vector getPlatformServerList() throws Exception {
          return getPlatformServerList(true);
     }
 
      public static Vector getPlatformServerList(boolean update)
              throws Exception {
          getNamingProfile(update);
          return platformServers;
      }
 
     /**
      * This method returns key value from a hashtable, ignoring the case of the
      * key.
      */
     private static String getValueFromTable(Hashtable table, String key) {
         if (table.contains(key)) {
             return (String) table.get(key);
         }
         for (Enumeration keys = table.keys(); keys.hasMoreElements();) {
             String tmpKey = (String) keys.nextElement();
             if (tmpKey.equalsIgnoreCase(key)) {
                 return (String) table.get(tmpKey);
             }
         }
         return null;
     }
 
     /**
      * This method returns local server name from naming table. 
      * @return server name opensso is deployed. 
      */
     public static String getLocalServer() {
         String server = null;
         
         try {
             server = getServerFromID(getAMServerID());
         } catch (ServerEntryNotFoundException e) {
             debug.error("Failed to get local server entry.", e);
         }
         
         return server;
     }
 
     /**
      * This function gets the server id that is there in the platform server
      * list for a corresponding server. One use of this function is to keep this
      * server id in our session id.
      */
     public static String getServerID(
         String protocol,
         String host,
         String port,
         String uri)
         throws ServerEntryNotFoundException {
         return getServerID(protocol, host, port, uri, true);
     }
 
     public static String getServerID(
         String protocol, 
         String host, 
         String port,
         String uri,
         boolean updatetbl
     ) throws ServerEntryNotFoundException {
         try {
             // check before the first naming table update to avoid deadlock
             if (protocol == null || host == null || port == null ||
                 protocol.length() == 0 || host.length() == 0 ||
                 port.length() == 0) {
                 debug.error("WebtopNaming.getServerId():noServerId");
                 throw new Exception(NamingBundle.getString("noServerID"));
             }
 
             String serverWithoutURI = protocol + ":" + "//" + host + ":" + port;
             String server = (uri != null) ?
                 protocol + ":" + "//" + host + ":" + port + uri : 
                 serverWithoutURI;
             
             String serverID = null;
             if (serverIdTable != null) {
                 serverID = getValueFromTable(serverIdTable, server);
                 
                 if (serverID == null) {
                     //try without URI, this is for prior release of FAM 8.0
                     serverID = getValueFromTable(serverIdTable, 
                         serverWithoutURI);
                 }
             }
             //update the naming table and as well as server id table
             //if it can not find it
             if (( serverID == null ) && (updatetbl == true)) {
                 getNamingProfile(true);
                 serverID = getValueFromTable(serverIdTable, server);
                 if (serverID == null) {
                     //try without URI, this is for prior release of FAM 8.0
                     serverID = getValueFromTable(serverIdTable, 
                         serverWithoutURI);
                 }
             }
 
             if (serverID == null) {
                 debug.error("WebtopNaming.getServerId():serverId null");
                 throw new ServerEntryNotFoundException(
                     NamingBundle.getString("noServerID"));
             }
             return serverID;
         } catch (Exception e) {
             debug.error("WebtopNaming.getServerId()", e);
             throw new ServerEntryNotFoundException(e);
         }
     }
 
     /**
      * This function returns server from the id.
      */
 
     public static String getServerFromID(String serverID)
             throws ServerEntryNotFoundException {
         String server = null;
         try {
             // refresh local naming table in case the key is not found
             if (namingTable != null) {
                 server = getValueFromTable(namingTable, serverID);
             }
             if (server == null) {
                 getNamingProfile(true);
                 server = getValueFromTable(namingTable, serverID);
             }
             if (server == null) {
                 throw new ServerEntryNotFoundException(NamingBundle
                         .getString("noServer"));
             }
 
         } catch (Exception e) {
             throw new ServerEntryNotFoundException(e);
         }
         return server;
     }
 
     public static Vector getAllServerIDs() throws Exception  {
         if (namingTable == null) {
             getNamingProfile(false);
         }
 
         return platformServerIDs;
     }
 
     private static void updateLBCookieValueMappings() {
         lbCookieValuesTable = new Hashtable();
         String serverSet = (String) namingTable.get(
                            Constants.SERVERID_LBCOOKIEVALUE_LIST);
 
         if ((serverSet == null) || (serverSet.length() == 0)) {
             return;
         }
 
         StringTokenizer tok = new StringTokenizer(serverSet, ",");
         while (tok.hasMoreTokens()) {
             String serverid = tok.nextToken();
             String lbCookieValue = serverid;
             int idx = serverid.indexOf(NODE_SEPARATOR);
             if (idx != -1) {
                 lbCookieValue = serverid.substring(idx + 1, serverid.length());
                 serverid = serverid.substring(0, idx);
             }
             lbCookieValuesTable.put(serverid, lbCookieValue);
         }
 
         if (debug.messageEnabled()) {
             debug.message("WebtopNaming.updateLBCookieValueMappings():" +
                 "LBCookieValues table -> " + lbCookieValuesTable.toString());
         }
 
         return;
     }
 
     /**
      * Returns the lbCookieValue corresponding to serverid
      * 
      * @param serverid
      *            The server id
      * @return The lbCookieValue corresponding to serverid
      */     
     public static String getLBCookieValue(String serverid) {
         String lbCookieValue = null;
 
        if (lbCookieValuesTable == null || serverid == null) {
             return null;
         }
 
         lbCookieValue = (String) lbCookieValuesTable.get(serverid);
 
         if (debug.messageEnabled()) {
             debug.message("WebtopNaming.getLBCookieValue(): lbCookieValue"
             + "for " + serverid + " is "  + lbCookieValue);
         }
 
         return lbCookieValue;
     }
 
     /**
      * This function gets the server id that is there in the platform server
      * list for a corresponding server. One use of this function is to keep this
      * server id in our session id.
      */
     public static String getSiteID(
         String protocol,
         String host,
         String port, 
         String uri
     ) throws ServerEntryNotFoundException {
         String serverid = getServerID(protocol, host, port, uri);
         return getSiteID(serverid);
     }
 
     /**
      * Extended ServerID syntax : localserverID | lbID-1 | lbID-2 | lbID-3 | ...
      * It returns lbID-1
      */
     public static String getSiteID(String serverid) {
         String primary_site = null;
         String sitelist = null;
 
         if (siteIdTable == null) {
             return null;
         }
 
         sitelist = (String) siteIdTable.get(serverid);
         StringTokenizer tok = new StringTokenizer(sitelist, NODE_SEPARATOR);
         if (tok != null) {
             primary_site = tok.nextToken();
         }
 
         if (debug.messageEnabled()) {
             debug.message("WebtopNaming : SiteID for " + serverid + " is "
                     + primary_site);
         }
 
         return primary_site;
     }
 
     /**
      * Extended ServerID syntax : localserverID | lbID-1 | lbID-2 | lbID-3 | ...
      * It returns lbID-2 | lbID-3 | ...
      */
     public static String getSecondarySites(String serverid) {
         String sitelist = null;
         String secondarysites = null;
 
         if (siteIdTable == null) {
             return null;
         }
 
         sitelist = (String) siteIdTable.get(serverid);
         if (sitelist == null) {
             return null;
         }
 
         int index = sitelist.indexOf(NODE_SEPARATOR);
         if (index != -1) {
             secondarysites = sitelist.substring(index + 1, sitelist.length());
         }
 
         if (debug.messageEnabled()) {
             debug.message("WebtopNaming : SecondarySites for " + serverid
                     + " is " + secondarysites);
         }
 
         return secondarysites;
     }
 
     /**
      * This method returns all the node id for the site
      * 
      * @param one
      *            of node id, it can be serverid or lb's serverid
      * @return HashSet has all the node is for the site.
      */
     public static Set getSiteNodes(String serverid) throws Exception {
         HashSet nodeset = new HashSet();
 
         if (namingTable == null) {
             getNamingProfile(false);
         }
 
         String siteid = getSiteID(serverid);
 
         Enumeration e = siteIdTable.keys();
         while (e.hasMoreElements()) {
             String node = (String) e.nextElement();
             if (siteid.equalsIgnoreCase(node)) {
                 continue;
             }
 
             if (siteid.equalsIgnoreCase(getSiteID(node))) {
                 nodeset.add(node);
             }
         }
 
         return nodeset;
     }
 
     /**
      * This method returns the class of the specified service.
      * 
      * @param service
      *            The name of the service.
      * @return The class name of the specified service.
      */
     public static String getServiceClass(String service)
             throws ClassNotFoundException {
         try {
             if (namingTable == null) {
                 getNamingProfile(false);
             }
             String cls = null;
             String name = AM_NAMING_PREFIX + service.toLowerCase()
                     + "-class";
             cls = (String) namingTable.get(name);
             if (cls == null) {
                 name = FAM_NAMING_PREFIX + service.toLowerCase() + "-class";
                 cls = (String)namingTable.get(name);
             }
             if (cls == null) {
                 throw new Exception(NamingBundle.getString("noServiceClass")
                         + service);
             }
             return cls;
         } catch (Exception e) {
             throw new ClassNotFoundException(e.getMessage());
         }
     }
 
     /**
      * This method returns the URL of the notification service on the local
      * host.
      */
     public synchronized static URL getNotificationURL()
             throws URLNotFoundException {
         try {
             String url = System.getProperty(Constants.CLIENT_NOTIFICATION_URL,
                     SystemProperties.get(Constants.CLIENT_NOTIFICATION_URL));
             if (url == null) {
                 throw new URLNotFoundException(NamingBundle
                         .getString("noNotificationURL"));
             }
             return new URL(url);
         } catch (Exception e) {
             throw new URLNotFoundException(e.getMessage());
         }
     }
 
     private synchronized static void getNamingProfile(boolean update)
             throws Exception {
         if (update || namingTable == null) {
             updateNamingTable();
         }
     }
 
     private static void updateServerProperties(URL url) {
         amServerProtocol = url.getProtocol();
         amServer = url.getHost();
         amServerPort = Integer.toString(url.getPort());
         amServerURI = url.getPath();
         amServerURI = amServerURI.replaceAll("//", "/");
         int idx = amServerURI.lastIndexOf("/");
         while (idx > 0) {
             amServerURI = amServerURI.substring(0, idx);
             idx = amServerURI.lastIndexOf("/");
         }
 
         SystemProperties.initializeProperties(Constants.AM_SERVER_PROTOCOL,
             amServerProtocol);
         SystemProperties.initializeProperties(Constants.AM_SERVER_HOST,
             amServer);
         SystemProperties.initializeProperties(Constants.AM_SERVER_PORT,
             amServerPort);
         SystemProperties.initializeProperties(
             Constants.AM_SERVICES_DEPLOYMENT_DESCRIPTOR, amServerURI);
         if (debug.messageEnabled()) {
             debug.message("Server Properties are changed : ");
             debug.message(Constants.AM_SERVER_PROTOCOL + " : "
                 + SystemProperties.get(Constants.AM_SERVER_PROTOCOL, null));
             debug.message(Constants.AM_SERVER_HOST + " : "
                 + SystemProperties.get(Constants.AM_SERVER_HOST, null));
             debug.message(Constants.AM_SERVER_PORT + " : "
                 + SystemProperties.get(Constants.AM_SERVER_PORT, null));
             debug.message(Constants.AM_SERVICES_DEPLOYMENT_DESCRIPTOR + " : "
                 + SystemProperties.get(
                     Constants.AM_SERVICES_DEPLOYMENT_DESCRIPTOR, null));
         }
     }
 
     private static Hashtable getNamingTable(URL nameurl) throws Exception {
         Hashtable nametbl = null;
         NamingRequest nrequest = new NamingRequest(NamingRequest.reqVersion);
         Request request = new Request(nrequest.toXMLString());
         RequestSet set = new RequestSet(NAMING_SERVICE);
         set.addRequest(request);
         Vector responses = null;
 
         try {
             responses = PLLClient.send(nameurl, set);
             if (responses.size() != 1) {
                 throw new Exception(NamingBundle
                         .getString("unexpectedResponse"));
             }
 
             Response res = (Response) responses.elementAt(0);
             NamingResponse nres = NamingResponse.parseXML(res.getContent());
             if (nres.getException() != null) {
                 throw new Exception(nres.getException());
             }
             nametbl = nres.getNamingTable();
         } catch (SendRequestException sre) {
             debug.error("Naming service connection failed for " + nameurl, sre);
         } catch (Exception e) {
             debug.error("getNamingTable: ", e);
         }
 
         return nametbl;
     }
 
     private static void updateNamingTable() throws Exception {
 
         if (!serverMode) {
             if (namingServiceURL == null) {
                 initializeNamingService();
             }
 
             // Try for the primary server first, if it fails and then
             // for the second server. We get connection refused error
             // if it doesn't succeed.
             namingTable = null;
             URL tempNamingURL = null;
             for (int i = 0; ((namingTable == null) && 
                     (i < namingServiceURL.length)); i++) {
                 tempNamingURL = new URL(namingServiceURL[i]);
                 namingTable = getNamingTable(tempNamingURL);
             }
 
             if (namingTable == null) {
                 debug.error("updateNamingTable : "
                         + NamingBundle.getString("noNamingServiceAvailable"));
                 throw new Exception(NamingBundle
                         .getString("noNamingServiceAvailable"));
             }
 
             updateServerProperties(tempNamingURL);
         } else {
             namingTable = NamingService.getNamingTable();
         }
 
         String servers = (String) namingTable.get(Constants.PLATFORM_LIST);
 
         if (servers != null) {
             StringTokenizer st = new StringTokenizer(servers, ",");
             platformServers.clear();
             lcPlatformServers.clear();
             while (st.hasMoreTokens()) {
                 String svr = st.nextToken();
                 lcPlatformServers.add(svr.toLowerCase());
                 platformServers.add(svr);
             }
         }
         updateServerIdMappings();
         updateSiteIdMappings();
         updatePlatformServerIDs();
         updateLBCookieValueMappings();
 		
         if (debug.messageEnabled()) {
             debug.message("Naming table -> " + namingTable.toString());
             debug.message("Platform Servers -> " + platformServers.toString());
             debug.message("Platform Server IDs -> "
                           + platformServerIDs.toString());
         }
     }
 
     /*
      * this method is to update the servers and their ids in a seprate hash and
      * will get updated each time when the naming table gets updated note: this
      * table will have all the entries in naming table but in a reverse order
      * except the platform server list We can just as well keep only server id
      * mappings, but we need to exclude each other entry which is there in.
      */
     private static void updateServerIdMappings() {
         serverIdTable = new Hashtable();
         Enumeration e = namingTable.keys();
         while (e.hasMoreElements()) {
             String key = (String) e.nextElement();
             String value = (String) namingTable.get(key);
             if ((key == null) || (value == null)) {
                 continue;
             }
             // If the key is server list skip it, since it would
             // have the same value
             if (key.equals(Constants.PLATFORM_LIST)) {
                 continue;
             }
             serverIdTable.put(value, key);
         }
     }
 
     private static void updateSiteIdMappings() {
         siteIdTable = new Hashtable();
         String serverSet = (String) namingTable.get(Constants.SITE_ID_LIST);
 
         if ((serverSet == null) || (serverSet.length() == 0)) {
             return;
         }
 
         StringTokenizer tok = new StringTokenizer(serverSet, ",");
         while (tok.hasMoreTokens()) {
             String serverid = tok.nextToken();
             String siteid = serverid;
             int idx = serverid.indexOf(NODE_SEPARATOR);
             if (idx != -1) {
                 siteid = serverid.substring(idx + 1, serverid.length());
                 serverid = serverid.substring(0, idx);
             }
             siteIdTable.put(serverid, siteid);
         }
 
         if (debug.messageEnabled()) {
             debug.message("SiteID table -> " + siteIdTable.toString());
         }
 
         return;
     }
 
     private static void updatePlatformServerIDs()
         throws MalformedURLException, ServerEntryNotFoundException {
         Iterator it = platformServers.iterator();
         while (it.hasNext()) {
             String plaformURL = (String) it.next();
             URL url = new URL(plaformURL);
             String serverID = getServerID(url.getProtocol(), url.getHost(),
                 Integer.toString(url.getPort()), url.getPath());
             if (!platformServerIDs.contains(serverID)) {
                 platformServerIDs.add(serverID);
             }
         }
     }
 
     private static void validate(
         String protocol, 
         String host, 
         String port,
         String uri
     ) throws URLNotFoundException {
         String server = (uri != null) ?
             protocol + "://" + host + ":" + port + uri :
             protocol + "://" + host + ":" + port;
         server = server.toLowerCase();
         
         try {
             // first check if this is the local server, proto, and port,
             // if it is there is no need to
             // validate that it is in the trusted server platform server list
             if (protocol.equalsIgnoreCase(amServerProtocol) &&
                 host.equalsIgnoreCase(amServer) && 
                 port.equals(amServerPort) &&
                 ((uri == null) || uri.equalsIgnoreCase(amServerURI))
             ) {
                 return;
             }
             if (debug.messageEnabled()) {
                 debug.message("WebtopNaming.validate: platformServers= " + 
                     platformServers);
             }
 
             if (!lcPlatformServers.contains(server)) {
                 getNamingProfile(true);
                 if (!platformServers.contains(server)) {
                     throw new URLNotFoundException(NamingBundle
                             .getString("invalidServiceHost")
                             + " " + server);
                 }
             }
         } catch (Exception e) {
             debug.error("platformServers: " + platformServers, e);
             throw new URLNotFoundException(e.getMessage());
         }
     }
 
     /**
      * This method returns the list URL of the naming service url
      * 
      * @return Array of naming service url.
      * @throws Exception -
      *             if there is no configured urls or any problem in getting urls
      */
     public synchronized static String[] getNamingServiceURL() throws Exception {
         if (!serverMode && (namingServiceURL == null)) {
             // Initilaize the list of naming URLs
             ArrayList urlList = new ArrayList();
 
             // Get the naming service URLs from properties files
             String configURLListString =
                                SystemProperties.get(Constants.AM_NAMING_URL);
             if (configURLListString != null) {
                 StringTokenizer stok = new StringTokenizer(configURLListString);
                 while (stok.hasMoreTokens()) {
                     String nextURL = stok.nextToken();
                     if (urlList.contains(nextURL)) {
                         if (debug.warningEnabled()) {
                             debug.warning(
                                 "Duplicate naming service URL specified "
                                 + nextURL + ", will be ignored.");
                         }
                     } else {
                         urlList.add(nextURL);
                     }
                 }
             }
 
             if (urlList.size() == 0) {
                 throw new Exception(
                     NamingBundle.getString("noNamingServiceURL"));
             } else {
                 if (debug.messageEnabled()) {
                     debug.message("Naming service URL list: " + urlList);
                 }
             }
 
             namingServiceURL = new String[urlList.size()];
             System.arraycopy(urlList.toArray(), 0, namingServiceURL, 0,
                 urlList.size());
         }
 
         return namingServiceURL;
     }
 
     private static synchronized void startSiteMonitor(String[] urlList) {
         // Site monitor is already started.
         if (monitorThread != null) {
             return;
         }
 
         // Start naming service monitor if more than 1 naming URLs are found
         if (urlList.length > 1) {
             monitorThread = new SiteMonitor(urlList);
             SystemTimer.getTimer().schedule(monitorThread, new Date(
                 System.currentTimeMillis() / 1000 * 1000));
         } else {
             if (debug.messageEnabled()) {
                 debug.message("Only one naming service URL specified."
                     + " NamingServiceMonitor will be disabled.");
             }
         }
     }
 
     static public void removeFailedSite(String server) {
         if (monitorThread != null) {
             try {
                 URL url = new URL(server);
                 removeFailedSite(url);
             } catch (MalformedURLException e) {
                 debug.error("Server URL is not valid : ", e);
             }
         }
 
         return;
     }
 
     static public void removeFailedSite(URL url) {
         if (monitorThread != null) {
             try {
                 String serverid = getServerID(url.getProtocol(),
                     url.getHost(), String.valueOf(url.getPort()), 
                     url.getPath());
                 SiteMonitor.removeFailedSite(serverid);
             } catch (ServerEntryNotFoundException e) {
                 debug.error("Can not find server ID : ", e);
             }
         }
 
         return;
     }
 
     /**
      * Returns the uri of the specified URL.
      * 
      * @param URL that includes uri.
      * @return uri of the specified <code>URL</code>.
      */
     public static String getURI(URL url) {
         String uri = url.getPath();
         int idx = uri.lastIndexOf('/');
         while (idx > 0) {
             uri = uri.substring(0, idx);
             idx = uri.lastIndexOf('/');
         }
         
         return uri;
     }
     
 static public class SiteMonitor extends GeneralTaskRunnable {
     static long sleepInterval;
     static Vector availableSiteList = new Vector();
     static String currentSiteID = null;
     static SiteStatusCheck siteChecker = null;
     static String[] siteUrlList = null;
     static public boolean keepMonitoring = false;
 
     static {
         try {
             String checkClass =
                 SystemProperties.get(Constants.SITE_STATUS_CHECK_CLASS,
                     "com.iplanet.services.naming.SiteStatusCheckThreadImpl");
             if (debug.messageEnabled()) {
                 debug.message("SiteMonitor : SiteStatusCheck class = "
                         + checkClass);
             }
             siteChecker =
                 (SiteStatusCheck) Class.forName(checkClass).newInstance();
             sleepInterval = Long.valueOf(SystemProperties.
                     get(Constants.MONITORING_INTERVAL, "60000")).longValue();
             getNamingProfile(false);
             currentSiteID = getServerID(amServerProtocol, amServer, 
                 amServerPort, amServerURI);
         } catch (Exception e) {
             debug.message("SiteMonitor initialization failed : ", e);
         }
     }
 
     public SiteMonitor(String[] urlList) {
         siteUrlList = urlList;
     }
 
     public boolean addElement(Object obj) {
         return false;
     }
     
     public boolean removeElement(Object obj) {
         return false;
     }
     
     public boolean isEmpty() {
         return true;
     }
     
     public long getRunPeriod() {
         return sleepInterval;
     }
     
     public void run() {
         keepMonitoring = true;
         try {
             runCheckValidSite();
         } catch (Exception e) {
             debug.error("SiteMonitor run failed : ", e);
         }
     }
 
     static void runCheckValidSite() {
         Vector siteList = checkAvailableSiteList();
         updateSiteList(siteList);
         updateCurrentSite(siteList);
     }
 
     public static boolean checkSiteStatus(URL siteurl) {
         return siteChecker.doCheckSiteStatus(siteurl);
     }
 
     private static Vector checkAvailableSiteList() {
         Vector siteList = new Vector();
         for (int i = 0; i < siteUrlList.length; i++) {
             try {
                 URL siteurl = new URL(siteUrlList[i]);
                 if (siteChecker.doCheckSiteStatus(siteurl) == false) {
                     continue;
                 }
 
                 String serverid = getServerID(
                     siteurl.getProtocol(), siteurl.getHost(),
                     String.valueOf(siteurl.getPort()), siteurl.getPath());
                 siteList.add(serverid);
             } catch (MalformedURLException ex) {
                 if (debug.messageEnabled()) {
                     debug.message("SiteMonitor: Site URL "
                          + siteUrlList[i] + " is not valid.", ex);
                 }
             } catch (ServerEntryNotFoundException ex) {
                 if (debug.messageEnabled()) {
                     debug.message("SiteMonitor: Site URL "
                          + siteUrlList[i] + " is not available.", ex);
                 }
             }
         }
 
         return siteList;
     }
 
     public static boolean isAvailable(URL url) throws Exception {
         if ((namingTable == null) || (keepMonitoring == false)) {
             return true;
         }
 
         String serverID = null;
         try {
             serverID = getServerID(url.getProtocol(), url.getHost(),
                 Integer.toString(url.getPort()), url.getPath(), false);
         } catch (ServerEntryNotFoundException e) {
             if (debug.messageEnabled()) {
                 debug.message("URL is not part of AM setup.");
             }
             return true;
         }
 
         Vector sites = getAvailableSites();
         boolean available = false;
         Iterator it = sites.iterator();
         while (it.hasNext()) {
             String server = (String)it.next();
             if (serverID.equalsIgnoreCase(server)) {
                 available = true;
                 break;
             }
         }
 
         if (debug.messageEnabled()) {
             debug.message("In SiteMonitor.isAvailable()");
             if (available) {
                 debug.message("SiteID " + url.toString() + " is UP.");
             } else {
                 debug.message("SiteID " + url.toString() + " is DOWN.");
             }
         }
 
         return available;
     }
     
     public static boolean isCurrentSite(URL url) throws Exception {
         if ((namingTable == null) || !keepMonitoring) {
             return true;
         }
 
         String serverID = null;
         try {
             serverID = getServerID(url.getProtocol(), url.getHost(),
                 Integer.toString(url.getPort()), url.getPath(), false);
         } catch (ServerEntryNotFoundException e) {
             if (debug.messageEnabled()) {
                 debug.message("URL is not part of AM setup.");
             }
             return true;
         }
 
         Vector sites = getAvailableSites();
         boolean isCurrent = false;
         if (!sites.isEmpty()) {
             String serverid = (String)sites.firstElement();
             if (serverid != null) {
                 isCurrent = serverid.equalsIgnoreCase(serverID);
             }
         }
 
         return isCurrent;
     }
 
     static Vector getAvailableSites() throws Exception {
         Vector sites = null;
         if (availableSiteList.size() == 0) {
             String[] namingURLs = getNamingServiceURL();
             for (int i = 0; i < namingURLs.length; i++) {
                 URL url = new URL(namingURLs[i]);
                 availableSiteList.add(getServerID(
                     url.getProtocol(), url.getHost(), 
                     String.valueOf(url.getPort()), url.getPath()));
             }
 
             updateCurrentSite(availableSiteList);
         }
 
         sites = new Vector(availableSiteList);
         if (debug.messageEnabled()) {
             debug.message("In SiteMonitor.getAvailableSites()");
             debug.message("availableSiteList : " + sites.toString());
         }
 
         return sites;
     }
 
     static void removeFailedSite(String site) {
         if ((keepMonitoring == true) && (availableSiteList.contains(site))) {
             availableSiteList.remove(site);
         }
 
         return;
     }
 
     private static void updateSiteList(Vector list) {
         availableSiteList = list;
 
         if (debug.messageEnabled()) {
             debug.message("In SiteMonitor.updateSiteList()");
             debug.message("availableSiteList : "
                     + availableSiteList.toString());
         }
         return;
     }
 
     private static void updateCurrentSite(Vector list) {
         if (serverMode) {
             return;
         }
 
         if ((list == null) || (list.size() == 0)) {
             return;
         }
 
         String sid = (String)list.firstElement();
         if (!currentSiteID.equalsIgnoreCase(sid)) {
             if (debug.messageEnabled()) {
                 debug.message("Invoke updateServerProperties() : " +
                         "Server properties are changed for service failover");
             }
 
             try {
                 currentSiteID = sid;
                 String serverurl = getServerFromID(currentSiteID);
                 updateServerProperties(new URL(serverurl));
             } catch (Exception e) {
                 debug.error("SiteMonitor: ", e);
             }
         }
 
         return;
     }
 }
 
 /**
  * The interface <code>SiteStatusCheck</code> provides
  * method that will be used by SiteMonitor to check each site is alive.
  * Each implementation class has to implement doCheckSiteStatus method.
  */
 public interface SiteStatusCheck {
     public boolean doCheckSiteStatus(URL siteurl);
 }
 }
