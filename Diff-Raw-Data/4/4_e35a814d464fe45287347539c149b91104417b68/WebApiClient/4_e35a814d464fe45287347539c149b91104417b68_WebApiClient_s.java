 /**
  * *****************************************************************************
  * Copyright 2012-2013 University of Trento - Department of Information
  * Engineering and Computer Science (DISI)
  *
  * All rights reserved. This program and the accompanying materials are made
  * available under the terms of the GNU Lesser General Public License (LGPL)
  * version 2.1 which accompanies this distribution, and is available at
  *
  * http://www.gnu.org/licenses/lgpl-2.1.html
  *
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  *
  *****************************************************************************
  */
 /**
  * WebApiClient
  *
  * Version: 1.0
  *
  * Date: Jan 26, 2010
  *
  */
 package it.unitn.disi.smatch.webapi.client;
 
 import it.unitn.disi.smatch.webapi.client.exceptions.WebApiException;
 import it.unitn.disi.smatch.webapi.model.Configuration;
 import it.unitn.disi.smatch.webapi.client.methods.MatchMethods;
 import it.unitn.disi.smatch.webapi.model.smatch.Correspondence;
 import java.util.List;
 import java.util.Locale;
 import java.util.logging.Level;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
 import org.apache.log4j.Logger;
 
 /**
  * S-Match version of the generic SWeb API client part realization
  *
  * @author Sergey Kanshin kanshin@disi.unitn.it
  *
  * @author Moaz Reyad <reyad@disi.unitn.it>
  * @date Jul 11, 2013
  *
  */
 public class WebApiClient implements IApiClient {
 
     private static WebApiClient webApiClient;
     private HttpClient httpClient;
     private Locale locale;
     private String serverPath;
    private String host;
    private String port;
     private static final int MILLISECONDS = 1000;
 
     protected WebApiClient(Locale locale, String host, String port) {
         MultiThreadedHttpConnectionManager mgr =
                 new MultiThreadedHttpConnectionManager();
         String tmp = Configuration.getString("smatch.webapi.idle.timeout");
         if (tmp != null) {
             mgr.closeIdleConnections(Long.parseLong(tmp));
         }
         tmp = Configuration.getString("smatch.webapi.read.timeout");
         if (tmp == null) {
             tmp = "240000";
         }
         int timeout = Integer.parseInt(tmp);
         this.httpClient = new HttpClient(mgr);
         if (Boolean.parseBoolean(Configuration.getString("smatch.webapi.proxy"))) {
             String proxyHost = Configuration.getString("smatch.webapi.proxy.host");
             int proxyPort = Integer.parseInt(Configuration.getString(
                     "smatch.webapi.proxy.port"));
             httpClient.getHostConfiguration().setProxy(proxyHost, proxyPort);
         }
 
         httpClient.getParams().setSoTimeout(timeout);
         mgr.getParams().setSoTimeout(timeout);
         mgr.getParams().setConnectionTimeout(timeout);
         this.locale = locale;
         this.serverPath = "http://" + host + ":" + port;
         this.host = host;
         this.port = port;
         Logger.getLogger(getClass()).info("Web API client started on "
                 + serverPath + ", connection timeout is " + (httpClient.getParams().getSoTimeout() / MILLISECONDS) + " s");
     }
 
     protected WebApiClient(Locale locale) {
         this(locale, Configuration.getString("smatch.webapi.host"), Configuration.getString("smatch.webapi.port"));
     }
 
     /**
      * Returns instance of web API client for given locale. This method use
      * default connection parameters from property configuration file.
      *
      * @param locale language.
      * @return instance of web API client for given locale.
      */
     public static WebApiClient getInstance(Locale locale) {
         if (null == webApiClient) {
             webApiClient = new WebApiClient(locale);
         }
         return webApiClient;
     }
 
     /**
      * Returns instance of web API client for given locale and connection
      * properties.
      *
      * @param locale language.
      * @param host Sweb web API server host.
      * @param port Sweb web API server port.
      * @return instance of web API client for given locale and connection
      * properties.
      */
     public static WebApiClient getInstance(Locale locale, String host, int port) {
         if (null == webApiClient) {
             webApiClient = new WebApiClient(locale, host, "" + port);
         }
         return webApiClient;
     }
 
     /**
      * Returns the HTTP client of the Web API.
      *
      * @return the HTTP Client
      */
     public HttpClient getHttpClient() {
         return httpClient;
     }
 
     /**
      * Returns the language locale which is used in the client.
      * 
      * @return - the locale
      */
     @Override
     public Locale getLocale() {
         return locale;
     }
 
     /**
      * Returns a string path like "http://host:port"
      *
      * @return a string path like "http://host:port"
      */
     @Override
     public String getServerPath() {
         return serverPath;
     }
 
     /**
      * Returns the correspondence between the source and the target contexts
      * 
      * @param sourceName - The name of the root node in the source tree
      * @param sourceNodes - Names of the source nodes under the source root node
      * @param targetName - The name of the root node in the target tree
      * @param targetNodes -Names of the target nodes under the target root node
      * 
      * @return - the correspondence
      */
     @Override
     public Correspondence match(String sourceName, List<String> sourceNodes,
             String targetName, List<String> targetNodes) {
         MatchMethods method = new MatchMethods(httpClient, locale, serverPath);
 
         Correspondence correspondace = null;
 
         try {
             correspondace = method.match(sourceName, sourceNodes, targetName, targetNodes);
         } catch (WebApiException ex) {
             java.util.logging.Logger.getLogger(WebApiClient.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         return correspondace;
     }
 }
