 /*
  * Copyright 2010 Kodapan
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package se.kodapan.io.http;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.conn.params.ConnManagerPNames;
 import org.apache.http.conn.params.ConnPerRoute;
 import org.apache.http.conn.routing.HttpRoute;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.cyberneko.html.parsers.DOMParser;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import se.kodapan.html.NekoHtmlTool;
 import se.kodapan.net.ssl.SSLUtilities;
 
 import java.io.*;
 import java.net.URI;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.LinkedHashSet;
 import java.util.UUID;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Downloads stuff via HTTP, optionally following redirects via HTTP 3xx, HTML meta redirects, etc.
  * Works pretty well but I would prefer a remote controlled Firefox or so.
  *
  * @author kalle
  * @since 2010-jan-05 15:38:17
  */
 public class HttpAccessor {
 
   private static final Logger log = LoggerFactory.getLogger(HttpAccessor.class);
 
 
   static {
     SSLUtilities.trustAllHostnames();
     SSLUtilities.trustAllHttpsCertificates();
   }
 
   private DefaultHttpClient httpClient;
 
   private File temporaryContentFilePath;
 
   public HttpAccessor() {
   }
 
   private boolean open = false;
 
   public synchronized void open() throws IOException {
 
     if (open) {
       return;
     }
 
     if (temporaryContentFilePath == null) {
       temporaryContentFilePath = File.createTempFile(HttpAccessor.class.getSimpleName(), "contentFiles");
       temporaryContentFilePath.delete();
     }
     if (!temporaryContentFilePath.exists()) {
       if (!temporaryContentFilePath.mkdirs()) {
         throw new IOException("Could not create directory: " + temporaryContentFilePath.getAbsolutePath());
       }
     } else if (!temporaryContentFilePath.isDirectory()) {
       throw new IOException("Not a directory: " + temporaryContentFilePath.getAbsolutePath());
     }
 
 
     HttpParams params = new BasicHttpParams();
 
     params.setIntParameter("http.socket.timeout", 10000); // 10 seconds
     params.setIntParameter("http.connection.timeout", 10000); // 10 seconds
     /*
        'http.conn-manager.timeout':  defines the timeout in milliseconds used
        when retrieving an instance of ManagedClientConnection from the ClientConnectionManager
        This parameter expects a value of type java.lang.Long.
        If this parameter is not set connection requests will not time out (infinite timeout).
 
     */
     params.setLongParameter("http.conn-manager.timeout", 240000); // 4 minutes
 
     params.setIntParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 50);
     params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRoute() {
       @Override
       public int getMaxForRoute(HttpRoute httpRoute) {
         return 10;
       }
     });
 
     SchemeRegistry registry = new SchemeRegistry();
     registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
     registry.register(new Scheme("https", org.apache.http.conn.ssl.SSLSocketFactory.getSocketFactory(), 443));
     ThreadSafeClientConnManager connManager = new ThreadSafeClientConnManager(params, registry);
 
     httpClient = new DefaultHttpClient(connManager, params);
 
 //    // http proxy!
 //
 //    String proxyHost = "localhost";
 //    int proxyPort = 8123;
 //    String proxyUsername = null;
 //    String proxyPassword = null;
 //
 //    final HttpHost hcProxyHost = new HttpHost(proxyHost, proxyPort, "http");
 //    httpClient.getCredentialsProvider().setCredentials(
 //                            new AuthScope(proxyHost, proxyPort),
 //                            new UsernamePasswordCredentials(proxyUsername, proxyPassword));
 //    httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, hcProxyHost);
 
 
     open = true;
 
   }
 
   /**
    * Deletes any downloaded file and relases the connection manager.
    *
    * @throws IOException
    */
   public void close() throws IOException {
     httpClient.getConnectionManager().shutdown();
     FileUtils.deleteDirectory(temporaryContentFilePath);
   }
 
   public Response sendRequest(String url) throws IOException {
     return sendRequest(new HttpGet(url));
   }
 
   public Response sendRequest(URI url) throws IOException {
     return sendRequest(new HttpGet(url));
   }
 
   public Response sendRequest(HttpRequestBase method) throws IOException {
     return sendRequest(method, true);
   }
 
   public Response sendRequest(HttpRequestBase method, boolean followRedirects) throws IOException {
 
     // lazy opening
     if (!open) {
       open();
     }
 
 
     // Mimics a Firefox on OS X.
 
     if (method.getHeaders("User-Agent").length == 0) {
       method.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.5; sv-SE; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2");
     }
     if (method.getHeaders("Accept").length == 0) {
       method.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
     }
     if (method.getHeaders("Accept-Language").length == 0) {
       method.addHeader("Accept-Language", "sv-se,sv;q=0.8,en-us;q=0.5,en;q=0.3");
     }
     if (method.getHeaders("Accept-Charset").length == 0) {
       method.addHeader("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
     }
 
 
     Response response = new Response();
     URI url = method.getURI();
     response.finalURL = url;
 
 
     if (log.isDebugEnabled()) {
       log.debug("Requesting " + url);
     }
 
     String tmp = url.toString().toLowerCase();
     if (!(tmp.startsWith("http://") || tmp.startsWith("https://"))) {
       log.error("Unsupported protocol in url " + url);
       return response;
     }
 
 
     if (followRedirects) {
       try {
         response.success = downloadFollowRedirects(new Request(method), response);
       } catch (SAXException e) {
         log.error("Attempting to follow redirection chain", e);
         return response;
       }
     } else {
       response.success = download(new Request(method), response);
     }
 
     return response;
 
   }
 
 
   private static final Pattern charsetPattern = Pattern.compile("^.+;\\s*charset\\s*=\\s*(.+)\\s*$", Pattern.CASE_INSENSITIVE);
 
   /**
    * Downloads an url and insert it as a new document in the database,
    * no matter if an instance that looks exactly the same already exists.
    *
    * @param request
    * @param response
    * @return true if successfully downloaded
    * @throws java.io.IOException
    */
   private boolean download(Request request, Response response) throws IOException {
 
 
     if (log.isDebugEnabled()) {
       log.debug("Downloading " + request.method.getURI());
     }
 
 
     request.method.getParams().setBooleanParameter("http.protocol.handle-redirects", false);
     HttpResponse httpResponse = httpClient.execute(request.method);
     response.httpResponse = httpResponse;
 
     return downloadDocumentContent(request, response);
 
   }
 
   /**
    * @param request
    * @param response
    * @return false if failed
    * @throws IOException
    */
   private boolean downloadDocumentContent(Request request, Response response) throws IOException {
     Header contentType = response.httpResponse.getFirstHeader("Content-type");
     if (contentType != null) {
       response.contentType = contentType.getValue();
     }
 
     if (response.httpResponse.getEntity().getContentEncoding() != null) {
       response.contentEncoding = response.httpResponse.getEntity().getContentEncoding().getValue();
     } else if (response.contentType == null) {
       log.debug("Could not figure out the content encoding for " + request.method.getURI());
       // todo pick up from somewhere else? meta tags or so?
     } else {
       Matcher matcher = charsetPattern.matcher(response.contentType);
       if (matcher.matches()) {
         response.contentEncoding = matcher.group(1);
       } else {
         log.debug("Could not figure out the content encoding for " + request.method.getURI());
         // todo pick up from somewhere else? meta tags or so?
       }
     }
 
     InputStream is = response.httpResponse.getEntity().getContent();
     if (is == null) {
       response.httpResponse.getEntity().consumeContent();
       return false;
     }
 
     response.contentFile = new File(temporaryContentFilePath, response.uuid);
     OutputStream out = new FileOutputStream(response.contentFile);
 
     final MessageDigest md;
     try {
       md = MessageDigest.getInstance("SHA1");
     } catch (NoSuchAlgorithmException e) {
       throw new RuntimeException(e);
     }
 
     int contentLength = 0;
     int length;
     byte[] buf = new byte[49152];
     while ((length = is.read(buf)) > -1) {
       out.write(buf, 0, length);
       md.update(buf, 0, length);
       contentLength += length;
     }
 
     // out.close requires checksum and content length set as it writes these values to the meta.xml!
     response.contentChecksum = md.digest();
     response.contentLength = contentLength;
 
     out.close();
 
     is.close();
     response.httpResponse.getEntity().consumeContent();
 

    return response.httpResponse.getStatusLine().getStatusCode() >= 200
        && response.httpResponse.getStatusLine().getStatusCode() <= 299;
   }
 
 
   private static final Pattern pattern = Pattern.compile("^([0-9]+)(\\s*;\\s*url\\s*=\\s*(.+))?$", Pattern.CASE_INSENSITIVE);
 
   private boolean downloadFollowRedirects(Request request, Response response) throws IOException, SAXException {
     return downloadFollowRedirects(null, request, response);
   }
 
   // todo use referer, send to server as http head!
 
   private boolean downloadFollowRedirects(final URI referer, final Request request, final Response response) throws IOException, SAXException {
 
     final URI requestURL = request.method.getURI();
 
     response.finalURL = requestURL;
 
     if (log.isDebugEnabled()) {
       if (referer != null) {
         log.debug("Redirecting from " + referer + " to " + requestURL);
       }
     }
 
     if (!response.redirectChain.add(requestURL)) {
       throw new IOException("Circular redirection");
     }
 
     if (response.redirectChain.size() > 10) {
       throw new IOException("Breaking at link depth " + response.redirectChain.size());
     }
 
     if (!download(request, response)) {
       return false;
     }
 
     if (response.httpResponse.getStatusLine().getStatusCode() >= 300
         && response.httpResponse.getStatusLine().getStatusCode() <= 399) {
       URI redirectURL = requestURL.resolve(response.httpResponse.getFirstHeader("Location").getValue());
 
       // http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
 
       if (response.httpResponse.getStatusLine().getStatusCode() == 303
           && !(request.method instanceof HttpGet)) {
         // 303:s should be redirected as GET
         HttpGet get = new HttpGet(redirectURL);
         for (Header header : request.method.getAllHeaders()) {
           get.addHeader(header);
         }
         request.method = get;
       } else {
         request.method.setURI(redirectURL);
       }
 
 
       return downloadFollowRedirects(requestURL, request, response);
     }
 
 
     // attempt to follow html meta refresh redirect
 
     if (response.contentType != null
         && response.contentType.toLowerCase().startsWith("text/html")) {
 
 
       InputSource inputSource;
       if (response.contentEncoding != null) {
         inputSource = new InputSource(new InputStreamReader(new FileInputStream(response.contentFile), response.contentEncoding));
       } else {
         inputSource = new InputSource(new FileInputStream(response.contentFile));
       }
       DOMParser parser = new DOMParser();
       parser.parse(inputSource);
       response.htmlDom = parser.getDocument();
 
 
       return NekoHtmlTool.visitNodes(response.htmlDom, requestURL, new NekoHtmlTool.Visitor<Boolean>() {
         public Boolean visit(Node node, URI documentURI) {
           if ("META".equals(node.getLocalName())) {
             Node httpEquivNode = node.getAttributes().getNamedItem("http-equiv");
             if (httpEquivNode != null) {
               String tmp = httpEquivNode.getTextContent();
               if ("refresh".equalsIgnoreCase(tmp)) {
                 node = node.getAttributes().getNamedItem("content");
                 if (node != null) {
                   tmp = node.getTextContent();
                   Matcher matcher = pattern.matcher(tmp);
                   if (matcher.matches()) {
                     // 0;url=
                     int seconds = Integer.valueOf(matcher.group(1));
                     URI redirectUrl;
                     redirectUrl = requestURL.resolve(matcher.group(3));
                     if (!redirectUrl.equals(requestURL)) {
                       try {
                         HttpGet get = new HttpGet(redirectUrl);
                         for (Header header : request.method.getAllHeaders()) {
                           get.addHeader(header);
                         }
                         request.method = get;
                         if (!downloadFollowRedirects(requestURL, request, response)) {
                           log.error("Expected a document as we have been redirected to it, but the new URL could not be retrieved. " + requestURL + "  --> " + redirectUrl);
                           return false;
                         }
                         return true;
 
                       } catch (Exception e) {
                         throw new RuntimeException(e);
                       }
                     }
                   }
                 }
               }
             }
           }
           return true;
         }
       });
     }
 
     return true;
 
   }
 
 
   private static class Request {
     private HttpRequestBase method;
 
     private Request(HttpRequestBase method) {
       this.method = method;
     }
   }
 
   public static class Response {
 
     private URI finalURL;
 
     private String uuid = UUID.randomUUID().toString();
 
     private boolean success = false;
 
     private LinkedHashSet<URI> redirectChain = new LinkedHashSet<URI>();
 
     private HttpResponse httpResponse;
 
     private String contentEncoding;
     private String contentType;
     private long contentLength;
 
     private File contentFile;
 
     private byte[] contentChecksum;
 
     private Document htmlDom;
 
     /**
      * removes content file from disk
      */
     public void cleanup() {
       if (contentFile != null && contentFile.exists()) {
         if (!contentFile.delete()) {
           log.warn("Was not able to delete content file " + contentFile.getAbsolutePath());
         }
       }
     }
 
     @Override
     protected void finalize() throws Throwable {
       try {
         cleanup();
       } finally {
         super.finalize();
       }
     }
 
     public Reader getContentReader() throws IOException {
       if (contentEncoding == null) {
         throw new IOException("Unknown content encoding");
       }
       return new InputStreamReader(new FileInputStream(contentFile), contentEncoding);
     }
 
     public InputStream getContentInputStream() throws IOException {
       return new FileInputStream(contentFile);
     }
 
     public String getContentString() throws IOException {
       Reader reader;
       if (contentEncoding != null) {
         reader = getContentReader();
       } else {
         reader = new InputStreamReader(getContentInputStream(), "UTF8");
       }
 
       StringBuilder sb = new StringBuilder((int) contentLength);
       char[] buf = new char[49152];
       int read;
       while ((read = reader.read(buf)) > -1) {
         sb.append(buf, 0, read);
       }
       return sb.toString();
     }
 
     public boolean isPDF() {
       if ("application/pdf".equalsIgnoreCase(contentType)) {
         return true;
       }
 
       // PDF magic number
       // 0x25 0x50 0x44 0x46
 
       byte[] buf = new byte[4];
       InputStream is;
       try {
         is = getContentInputStream();
         if (is.read(buf) != 4) {
           is.close();
           return false;
         }
         is.close();
       } catch (IOException e) {
         log.error("Could not retrieve the magic number", e);
       }
 
       return buf[0] == 0x25 && buf[1] == 0x50 && buf[2] == 0x44 && buf[3] == 0x46;
 
     }
 
     public URI getFinalURL() {
       return finalURL;
     }
 
     public String getUuid() {
       return uuid;
     }
 
     public boolean isSuccess() {
       return success;
     }
 
     public LinkedHashSet<URI> getRedirectChain() {
       return redirectChain;
     }
 
     public HttpResponse getHttpResponse() {
       return httpResponse;
     }
 
     public String getContentEncoding() {
       return contentEncoding;
     }
 
     public String getContentType() {
       return contentType;
     }
 
     public long getContentLength() {
       return contentLength;
     }
 
     public File getContentFile() {
       return contentFile;
     }
 
     public byte[] getContentChecksum() {
       return contentChecksum;
     }
 
     public Document getHtmlDom() {
       return htmlDom;
     }
 
     public void setFinalURL(URI finalURL) {
       this.finalURL = finalURL;
     }
 
     public void setUuid(String uuid) {
       this.uuid = uuid;
     }
 
     public void setSuccess(boolean success) {
       this.success = success;
     }
 
     public void setRedirectChain(LinkedHashSet<URI> redirectChain) {
       this.redirectChain = redirectChain;
     }
 
     public void setHttpResponse(HttpResponse httpResponse) {
       this.httpResponse = httpResponse;
     }
 
     public void setContentEncoding(String contentEncoding) {
       this.contentEncoding = contentEncoding;
     }
 
     public void setContentType(String contentType) {
       this.contentType = contentType;
     }
 
     public void setContentLength(long contentLength) {
       this.contentLength = contentLength;
     }
 
     public void setContentFile(File contentFile) {
       this.contentFile = contentFile;
     }
 
     public void setContentChecksum(byte[] contentChecksum) {
       this.contentChecksum = contentChecksum;
     }
 
     public void setHtmlDom(Document htmlDom) {
       this.htmlDom = htmlDom;
     }
   }
 
 
 }
