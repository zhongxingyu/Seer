 /*
  * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     bstefanescu
  */
 package org.nuxeo.ide.studio.connector.internal;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 
import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpException;
 import org.apache.http.HttpHost;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpRequestInterceptor;
 import org.apache.http.HttpResponse;
 import org.apache.http.auth.AuthScheme;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.AuthState;
 import org.apache.http.auth.Credentials;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.CredentialsProvider;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.impl.auth.BasicScheme;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.ExecutionContext;
 import org.apache.http.protocol.HttpContext;
 
 /**
  * Should be reinstantiated each time URL or login changes.
  *
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  *
  */
 public class StudioConnector {
 
     protected URL url;
 
     protected DefaultHttpClient http;
 
     protected HttpHost host;
 
     protected String base64;
 
     public StudioConnector(URL url, String user, String passwd) {
         this.url = url;
         init(new UsernamePasswordCredentials(user, passwd));
     }
 
     protected void init(UsernamePasswordCredentials upc) {
        base64 = "Basic "+Base64.encode(upc.getUserName()+":"+upc.getPassword()); //"test1:test1"
         http = new DefaultHttpClient();
         http.getCredentialsProvider().setCredentials(
                 new AuthScope(url.getHost(), url.getPort()), upc);
 
         host = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
 
         BasicHttpContext localcontext = new BasicHttpContext();
 
         // Generate BASIC scheme object and stick it to the local
         // execution context
         BasicScheme basicAuth = new BasicScheme();
         localcontext.setAttribute("preemptive-auth", basicAuth);
 
         // Add as the first request interceptor
         http.addRequestInterceptor(new PreemptiveAuth(), 0);
     }
 
     public void shutdown() {
         // When HttpClient instance is no longer needed,
         // shut down the connection manager to ensure
         // immediate deallocation of all system resources
         ClientConnectionManager connectionManager = http.getConnectionManager();
         connectionManager.shutdown();
     }
 
     public String getFeatures(String projectId) throws Exception {
         return doGetAsString(url.getPath() + "/projects/" + projectId
                 + "/features");
     }
 
     public String getProjects() throws Exception {
         return doGetAsString(url.getPath() + "/list");
     }
 
     public File getJar(String projectId) throws Exception {
         File file = doGetAsFile(url.getPath() + "/projects/" + projectId
                 + "/features");
         File f = new File(file.getParentFile(), projectId+".jar");
         f.delete();
         file.renameTo(f);
         return file;
     }
 
 
     protected String doGetAsString(String path) throws Exception {
         InputStream in = doGet(path);
         if (in != null) {
             try {
                 return readStream(in);
             } finally {
                 in.close();
             }
         }
         return null;
     }
 
     protected File doGetAsFile(String path) throws Exception {
         InputStream in = doGet(path);
         if (in != null) {
             try {
                 return saveStream(in);
             } finally {
                 in.close();
             }
         }
         return null;
     }
     protected InputStream doGet(String path) throws Exception {
         HttpGet get = new HttpGet(url.getPath()+path);
        get.setHeader("Authorization", base64);
         HttpResponse response = http.execute(host, get);
         HttpEntity entity = response.getEntity();
         if (entity != null) {
             return entity.getContent();
         }
         return null;
     }
 
     private static String readStream(InputStream in) throws IOException {
         int r;
         StringBuilder buf = new StringBuilder();
         byte[] tmp = new byte[4096*4];
         while ((r = in.read(tmp)) != -1) {
             buf.append(new String(tmp, 0, r, "UTF-8"));
         }
         return buf.toString();
     }
 
     private static File saveStream(InputStream in) throws IOException {
         File file = File.createTempFile("studio-stream", ".tmp");
         FileOutputStream out = new FileOutputStream(file);
         try {
             int r;
             byte[] tmp = new byte[1024*1024];
             while ((r = in.read(tmp)) != -1) {
                 out.write(tmp, 0, r);
             }
         } finally {
             out.close();
         }
         return file;
     }
 
     static class PreemptiveAuth implements HttpRequestInterceptor {
 
         public void process(final HttpRequest request, final HttpContext context)
                 throws HttpException, IOException {
 
             AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);
 
             // If no auth scheme avaialble yet, try to initialize it
             // preemptively
             if (authState.getAuthScheme() == null) {
                 AuthScheme authScheme = (AuthScheme) context.getAttribute("preemptive-auth");
                 CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(ClientContext.CREDS_PROVIDER);
                 HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
                 if (authScheme != null) {
                     Credentials creds = credsProvider.getCredentials(new AuthScope(
                             targetHost.getHostName(), targetHost.getPort()));
                     if (creds == null) {
                         throw new HttpException(
                                 "No credentials for preemptive authentication");
                     }
                     authState.setAuthScheme(authScheme);
                     authState.setCredentials(creds);
                 }
             }
 
         }
 
     }
 
     public static void main(String[] args) throws Exception {
         StudioConnector con = new StudioConnector(new URL("http://localhost:8080/nuxeo/site/studio"), "b", "b");
         System.out.println(con.doGetAsString("/list"));
 
     }
 
 }
