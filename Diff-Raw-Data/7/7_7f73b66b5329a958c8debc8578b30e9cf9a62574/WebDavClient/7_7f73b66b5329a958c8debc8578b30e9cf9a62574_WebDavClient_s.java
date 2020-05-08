 package de.cismet.security;
 
import de.cismet.security.Proxy;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import org.apache.commons.httpclient.Credentials;
 import org.apache.commons.httpclient.HostConfiguration;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpConnectionManager;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
 import org.apache.commons.httpclient.UsernamePasswordCredentials;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
 import org.apache.commons.httpclient.methods.RequestEntity;
 import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
 import org.apache.jackrabbit.webdav.client.methods.DavMethod;
 import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;
 import org.apache.jackrabbit.webdav.client.methods.PutMethod;
 import org.apache.log4j.Logger;
 
 /**
  * Communicates with a web dav server
  * @author therter
  */
 public class WebDavClient {
     private static final Logger log = Logger.getLogger(WebDavClient.class);
     private static final int MAX_HOST_CONNECTIONS = 20;
     private String username;
     private String password;
     private HttpClient client = null;
     private String currentHost = null;
     private Proxy proxy = null;
 
 
     /**
      *
      * @param proxy the currently used proxy or null, if no proxy is used
      * @param username can be null, if no authentication is required
      * @param password can be null, if no authentication is required
      */
     public WebDavClient(Proxy proxy, String username, String password) {
         this.username = username;
         this.password = password;
         this.proxy = proxy;
     }
     
     /**
      * initialises the http client for the given host
      * 
      * @param host
      */
     public void init(final String host) {
         log.debug("initialise WebDavClient");
         HostConfiguration hostConfig = new HostConfiguration();
         hostConfig.setHost(host);
         HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
         HttpConnectionManagerParams params = new HttpConnectionManagerParams();
         params.setMaxConnectionsPerHost(hostConfig, MAX_HOST_CONNECTIONS);
         connectionManager.setParams(params);
         client = new HttpClient(connectionManager);
         client.setHostConfiguration(hostConfig);
 
         if (username != null && password != null) {
             Credentials creds = new UsernamePasswordCredentials(username, password);
             client.getState().setCredentials(AuthScope.ANY, creds);
         }
 
         if (proxy != null && proxy.isEnabled()) {
             log.debug("use proxy");
             client.getHostConfiguration().setProxy(proxy.getHost(), proxy.getPort());
 
             if (proxy.getUsername() != null) {
                 AuthScope scope = new AuthScope(proxy.getHost(), proxy.getPort());
                UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
                        proxy.getUsername(), proxy.getPassword());
                 client.getState().setProxyCredentials(scope, credentials);
             }
         }
         currentHost = host;
     }
 
 
     /**
      * delete the given path
      * @param path
      * @throws MalformedURLException
      * @throws IOException
      * @throws HttpException
      */
     public void delete(String path) throws MalformedURLException, IOException, HttpException {
         lazyInitialise(path);
         log.debug("delete: " + path);
         DavMethod put = new DeleteMethod(path);
         client.executeMethod(put);
 
         put.releaseConnection();
     }
 
 
     /**
      * @param path
      * @return an InputStream from the given path
      * @throws MalformedURLException
      * @throws IOException
      * @throws HttpException
      */
     public InputStream getInputStream(String path) throws MalformedURLException, IOException, HttpException {
         lazyInitialise(path);
         log.debug("get: " + path);
         GetMethod get = new GetMethod(path);
         client.executeMethod(get);
 
         return get.getResponseBodyAsStream();
     }
 
 
     /**
      * copies the content of the given InputStream to the given path
      * @param path
      * @param input
      * @throws MalformedURLException
      * @throws IOException
      * @throws HttpException
      */
     public void put(String path, InputStream input) throws MalformedURLException, IOException, HttpException {
         lazyInitialise(path);
         log.debug("put: " + path);
         PutMethod put = new PutMethod(path);
         RequestEntity requestEntity = new InputStreamRequestEntity(input);
 
         put.setRequestEntity(requestEntity);
         client.executeMethod(put);
         put.releaseConnection();
     }
 
     
     /**
      * checks if the HttpClient is initialised with the host of the given path and initialises
      * the client if required.
      * @param path
      * @throws MalformedURLException
      */
     private void lazyInitialise(String path) throws MalformedURLException {
         int startIndex = path.indexOf( "://" ) + "://".length();
         String host = null;
         
         if (startIndex != -1) {
             int endIndex = path.indexOf("/", startIndex);
             if (endIndex == -1) {
                 endIndex = path.length();
             }
             host = path.substring(0, endIndex);
         } else {
             throw new MalformedURLException("Protocol not found in url " + path);
         }
 
         if (log.isDebugEnabled()) {
             log.debug("WebDav host: " + host);
         }
         
         //initialises the Httpclient if it is not initialised, yet, or the host was changed
         if (client == null || (currentHost != null && !currentHost.equals(host))) {
             init(host);
         }
     }
 }
