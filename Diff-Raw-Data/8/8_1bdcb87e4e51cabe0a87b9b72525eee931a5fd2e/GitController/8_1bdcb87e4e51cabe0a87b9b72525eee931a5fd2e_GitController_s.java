 /* Copyright 2013 Lyor Goldstein
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
 package net.community.chest.gitcloud.facade.frontend.git;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.logging.Level;
 
 import javax.inject.Inject;
 import javax.management.JMException;
 import javax.management.MBeanInfo;
 import javax.management.MBeanServer;
 import javax.management.ObjectName;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.community.chest.gitcloud.facade.ServletUtils;
 
 import org.apache.commons.collections15.SetUtils;
 import org.apache.commons.io.HexDumpOutputStream;
 import org.apache.commons.io.input.TeeInputStream;
 import org.apache.commons.io.output.LineLevelAppender;
 import org.apache.commons.io.output.TeeOutputStream;
 import org.apache.commons.lang3.ArrayUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.Validate;
 import org.apache.commons.logging.ExtendedLogUtils;
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpMessage;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.ProtocolException;
 import org.apache.http.StatusLine;
 import org.apache.http.client.RedirectStrategy;
 import org.apache.http.client.methods.CloseableHttpResponse;
 import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.conn.HttpClientConnectionManager;
 import org.apache.http.entity.InputStreamEntity;
 import org.apache.http.impl.client.CloseableHttpClient;
 import org.apache.http.impl.client.HttpClientBuilder;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.util.EntityUtils;
 import org.eclipse.jgit.http.server.GitSmartHttpTools;
 import org.eclipse.jgit.lib.Constants;
 import org.springframework.beans.factory.DisposableBean;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.RefreshedContextAttacher;
 import org.springframework.stereotype.Controller;
 import org.springframework.util.SystemPropertyUtils;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  * @author Lyor Goldstein
  * @since Sep 12, 2013 1:17:34 PM
  */
 @Controller // TODO make it a @ManagedObject and expose internal configuration values for JMX management (Read/Write)
 public class GitController extends RefreshedContextAttacher implements DisposableBean {
     public static final Set<String> ALLOWED_SERVICES=
             Collections.unmodifiableSet(
                     new TreeSet<String>(
                             Arrays.asList(GitSmartHttpTools.UPLOAD_PACK, GitSmartHttpTools.RECEIVE_PACK)));
     public static final String  LOOP_DETECT_TIMEOUT="gitcloud.frontend.git.controller.loop.detect.timeout";
         public static final long    DEFAULT_LOOP_DETECT_TIMEOUT=0L; // disabled
         private static final String LOOP_DETECT_TIMEOUT_VALUE=SystemPropertyUtils.PLACEHOLDER_PREFIX
                                             + LOOP_DETECT_TIMEOUT
                                             + SystemPropertyUtils.VALUE_SEPARATOR
                                             + DEFAULT_LOOP_DETECT_TIMEOUT
                                             + SystemPropertyUtils.PLACEHOLDER_SUFFIX;
     // TODO move this to some 'util' artifact
     public static final RedirectStrategy    NO_REDIRECTION=new RedirectStrategy() {
             @Override
             public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
                 return false;
             }
             
             @Override
             public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
                 throw new ProtocolException("getRedirect(" + request + ")[" + response + "] N/A");
             }
         };
 
     private final MBeanServer   mbeanServer;
     private final CloseableHttpClient   client;
     private final long  loopRetryTimeout;
     private volatile long    initTimestamp=System.currentTimeMillis();
     private volatile boolean    loopDetected;
 
     @Inject
     public GitController(MBeanServer localMbeanServer,
             HttpClientConnectionManager connectionsManager,
             @Value(LOOP_DETECT_TIMEOUT_VALUE) long loopDetectTimeout) {
         mbeanServer = Validate.notNull(localMbeanServer, "No MBean server", ArrayUtils.EMPTY_OBJECT_ARRAY);
         
         client = HttpClientBuilder.create()
                     .setConnectionManager(Validate.notNull(connectionsManager, "No connections manager", ArrayUtils.EMPTY_OBJECT_ARRAY))
                     .setRedirectStrategy(NO_REDIRECTION)
                     .build()
                     ;
         loopRetryTimeout = loopDetectTimeout;
     }
 
     @Override
     public void destroy() throws Exception {
         logger.info("destroy()");
         client.close();
     }
 
     @Override
     protected void onContextInitialized(ApplicationContext context) {
         super.onContextInitialized(context);
         initTimestamp = System.currentTimeMillis();
 
         logger.info("MBeanServer default domain: " + mbeanServer.getDefaultDomain());
         
         String[]    domains=mbeanServer.getDomains();
         if (!ArrayUtils.isEmpty(domains)) {
             for (String d : domains) {
                 logger.info("MBeanServer extra domain: " + d);
             }
         }
     }
 
     @RequestMapping(method=RequestMethod.GET)
     public void serveGetRequests(HttpServletRequest req, HttpServletResponse rsp) throws IOException, ServletException {
         serveRequest(RequestMethod.GET, req, rsp);
     }
     
     @RequestMapping(method=RequestMethod.POST)
     public void servePostRequests(HttpServletRequest req, HttpServletResponse rsp) throws IOException, ServletException {
         serveRequest(RequestMethod.POST, req, rsp);
     }
 
     private void serveRequest(RequestMethod method, HttpServletRequest req, HttpServletResponse rsp) throws IOException, ServletException {
         if (logger.isDebugEnabled()) {
             logger.debug("serveRequest(" + method + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]");
         }
 
         if ((loopRetryTimeout > 0L) && (!loopDetected)) {
             long    now=System.currentTimeMillis(), diff=now - initTimestamp;
             if ((diff > 0L) && (diff < loopRetryTimeout)) {
                 try {
                     MBeanInfo   mbeanInfo=
                             mbeanServer.getMBeanInfo(new ObjectName("net.community.chest.gitcloud.facade.backend.git:name=BackendRepositoryResolver"));
                     if (mbeanInfo != null) {
                         logger.info("serveRequest(" + method + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]"
                                   + " detected loop: " + mbeanInfo.getClassName() + "[" + mbeanInfo.getDescription() + "]");
                         loopDetected = true;
                     }
                 } catch(JMException e) {
                     if (logger.isDebugEnabled()) {
                         logger.debug("serveRequest(" + method + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]"
                                 + " failed " + e.getClass().getSimpleName()
                                 + " to detect loop: " + e.getMessage());
                     }
                 }
             }
         }
 
         /*
          * NOTE: this feature requires enabling cross-context forwarding.
          * In Tomcat, the 'crossContext' attribute in 'Context' element of
          * 'TOMCAT_HOME\conf\context.xml' must be set to true, to enable cross-context 
          */
         URI uri=resolveTargetRepository(method, req);
         if (loopDetected) {
             // TODO see if can find a more efficient way than splitting and re-constructing
             ServletContext  curContext=req.getServletContext();
             String          urlPath=uri.getPath(), urlQuery=uri.getQuery();
             String[]        comps=StringUtils.split(urlPath, '/');
             String          appName=comps[0];
             ServletContext  loopContext=Validate.notNull(curContext.getContext("/" + appName), "No cross-context for %s", appName);
             // build the relative path in the re-directed context
             StringBuilder   sb=new StringBuilder(urlPath.length() + 1 + (StringUtils.isEmpty(urlQuery) ? 0 : urlQuery.length()));
             for (int index=1; index < comps.length; index++) {
                 sb.append('/').append(comps[index]);
             }
             if (!StringUtils.isEmpty(urlQuery)) {
                 sb.append('?').append(urlQuery);
             }
             
             String              redirectPath=sb.toString();
             RequestDispatcher   dispatcher=Validate.notNull(loopContext.getRequestDispatcher(redirectPath), "No dispatcher for %s", redirectPath);
             dispatcher.forward(req, rsp);
             if (logger.isDebugEnabled()) {
                 logger.debug("serveRequest(" + method + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]"
                            + " forwarded to " + loopContext.getContextPath() + "/" + redirectPath);
             }
         } else {
             executeRemoteRequest(method, uri, req, rsp);
         }
     }
 
     private void executeRemoteRequest(RequestMethod method, URI uri, HttpServletRequest req, HttpServletResponse rsp) throws IOException {
         if (logger.isDebugEnabled()) {
             logger.debug("executeRemoteRequest(" + method + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]"
                        + " redirected to " + uri.toASCIIString());
         }
 
         HttpRequestBase request=resolveRequest(method, uri);
         executeRemoteRequest(request, req, rsp);
     }
 
     private StatusLine executeRemoteRequest(HttpRequestBase request, HttpServletRequest req, HttpServletResponse rsp) throws IOException {
         copyRequestHeadersValues(req, request);
 
         final CloseableHttpResponse  response;
         if (HttpPost.METHOD_NAME.equalsIgnoreCase(request.getMethod())) {
             response = transferPostedData((HttpEntityEnclosingRequestBase) request, req);
         } else {
             response = client.execute(request);
         }
         
         try {
             HttpEntity  rspEntity=response.getEntity();
             StatusLine  statusLine=response.getStatusLine();
             int         statusCode=statusLine.getStatusCode();
             if ((statusCode < HttpServletResponse.SC_OK) || (statusCode >= 300)) {
                 String  reason=StringUtils.trimToEmpty(statusLine.getReasonPhrase());
                 logger.warn("executeRemoteRequest(" + req.getMethod() + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]"
                          +  " bad response (" + statusCode + ") from remote end: " + reason);
                 EntityUtils.consume(rspEntity);
                 rsp.sendError(statusCode, reason);
             } else {
                 rsp.setStatus(statusCode);
                 copyResponseHeadersValues(req, response, rsp);
                 transferBackendResponse(req, rspEntity, rsp);
             }
             
             return statusLine;
         } finally {
             response.close();
         }
     }
 
     private CloseableHttpResponse transferPostedData(HttpEntityEnclosingRequestBase postRequest, final HttpServletRequest req)
             throws IOException {
         InputStream postData=req.getInputStream();
         try {
             if (logger.isTraceEnabled()) {
                 LineLevelAppender   appender=new LineLevelAppender() {
                         @Override
                         @SuppressWarnings("synthetic-access")
                         public void writeLineData(CharSequence lineData) throws IOException {
                             logger.trace("transferPostedData(" + req.getMethod() + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "] C: " + lineData);
                         }
                         
                         @Override
                         public boolean isWriteEnabled() {
                             return true;
                         }
                     };
                 postData = new TeeInputStream(postData, new HexDumpOutputStream(appender), true);
             }
             postRequest.setEntity(new InputStreamEntity(postData));
             return client.execute(postRequest);
         } finally {
             postData.close();
         }
     }
 
     private void transferBackendResponse(final HttpServletRequest req, HttpEntity rspEntity, HttpServletResponse rsp)
                     throws IOException {
         final String    method=req.getMethod();
         OutputStream    rspTarget=rsp.getOutputStream();
         try {
             if (logger.isTraceEnabled()) {
                 LineLevelAppender   appender=new LineLevelAppender() {
                         @Override
                         @SuppressWarnings("synthetic-access")
                         public void writeLineData(CharSequence lineData) throws IOException {
                             logger.trace("transferBackendResponse(" + method + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]"
                                        + " S: " + lineData);
                         }
 
                         @Override
                         public boolean isWriteEnabled() {
                             return true;
                         }
                     };
                 rspTarget = new TeeOutputStream(rspTarget, new HexDumpOutputStream(appender));
             }
 
             rspEntity.writeTo(rspTarget);
         } finally {
             rspTarget.close();
         }
     }
 
     private URI resolveTargetRepository(RequestMethod method, HttpServletRequest req) throws IOException {
         String  op=StringUtils.trimToEmpty(req.getParameter("service")), uriPath=req.getPathInfo();
         if (StringUtils.isEmpty(op)) {
             int pos=uriPath.lastIndexOf('/');
             if ((pos > 0) && (pos < (uriPath.length() - 1))) {
                 op = uriPath.substring(pos + 1);
             }
         }
 
         if (!ALLOWED_SERVICES.contains(op)) {
             throw ExtendedLogUtils.thrownLogging(logger, Level.WARNING,
                     "resolveTargetRepository(" + method + " " + uriPath + ")",
                     new UnsupportedOperationException("Unsupported operation: " + op));
         }
         
         String repoName=extractRepositoryName(uriPath);
         if (StringUtils.isEmpty(repoName)) {
             throw ExtendedLogUtils.thrownLogging(logger, Level.WARNING,
                          "resolveTargetRepository(" + method + " " + uriPath + ")",
                          new IllegalArgumentException("Failed to extract repo name from " + uriPath));
         }
 
         // TODO access an injected resolver that returns the back-end location URL
         String  query=req.getQueryString();
         try {
             if (StringUtils.isEmpty(query)) {
                 return new URI("http://localhost:8080/git-backend/git" + uriPath);
             } else {
                 return new URI("http://localhost:8080/git-backend/git" + uriPath + "?" + query);
             }
         } catch(URISyntaxException e) {
             throw new MalformedURLException(e.getClass().getSimpleName() + ": " + e.getMessage());
         }
     }
 
     public static final SortedSet<String>   FILTERED_REQUEST_HEADERS=
             SetUtils.unmodifiableSortedSet(new TreeSet<String>(String.CASE_INSENSITIVE_ORDER) {
                 // we're not serializing it anywhere
                 private static final long serialVersionUID = 1L;
 
                 {
                     // see RequestContent#process method
                     add(HTTP.TRANSFER_ENCODING);
                     add(HTTP.CONTENT_LEN);
                     
                     // other headers we don't want to echo as-is
                     add(HTTP.CONN_DIRECTIVE);
                     add(HTTP.TARGET_HOST);
                 }
             });
     // TODO move this to some generic util location
     private Map<String,String> copyRequestHeadersValues(HttpServletRequest req, HttpRequestBase request) {
         Map<String,String>  hdrsMap=ServletUtils.getRequestHeaders(req);
         // NOTE: headers name must be case insensitive as per HTTP requirements
         Set<String>         removedHeaders=new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
         for (Map.Entry<String,String> hdrEntry : hdrsMap.entrySet()) {
             String  hdrName=hdrEntry.getKey(), hdrValue=StringUtils.trimToEmpty(hdrEntry.getValue());
             if (StringUtils.isEmpty(hdrValue)) {
                 logger.warn("copyRequestHeadersValues(" + req.getMethod() + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]"
                           + " no value for header " + hdrName);
                 
             }
 
             if (FILTERED_REQUEST_HEADERS.contains(hdrName)) {
                 removedHeaders.add(hdrName);
                 if (logger.isTraceEnabled()) {
                     logger.trace("copyRequestHeadersValues(" + req.getMethod() + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]"
                                + " filtered " + hdrName + ": " + hdrValue);
                 }
             } else {
                 if (logger.isTraceEnabled()) {
                     logger.trace("copyRequestHeadersValues(" + req.getMethod() + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]"
                                + " " + hdrName + ": " + hdrValue);
                 }
                 request.addHeader(hdrName, hdrValue);
                 hdrsMap.put(hdrName, hdrValue);
             }
         }
 
         if (removedHeaders.size() > 0) {
             for (String hdrName : removedHeaders) {
                 hdrsMap.remove(hdrName);
             }
         }
 
         return hdrsMap;
     }
 
     // TODO move this to some generic util location
     public static final SortedSet<String>   FILTERED_RESPONSE_HEADERS=
             SetUtils.unmodifiableSortedSet(new TreeSet<String>(String.CASE_INSENSITIVE_ORDER) {
                 // we're not serializing it anywhere
                 private static final long serialVersionUID = 1L;
 
                 {
                     // other headers we don't want to echo as-is
                     add(HTTP.SERVER_HEADER);
                 }
             });
     private Map<String,String> copyResponseHeadersValues(HttpServletRequest req, HttpMessage response, HttpServletResponse rsp) {
         Header[]  hdrs=response.getAllHeaders();
         if (ArrayUtils.isEmpty(hdrs)) {
             logger.warn("copyResponseHeadersValues(" + req.getMethod() + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "] no headers");
             return Collections.emptyMap();
         }
 
         // NOTE: map must be case insensitive as per HTTP requirements
         Map<String,String>  hdrsMap=new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
         for (Header hdrEntry : hdrs) {
             // TODO add support for multi-valued headers
             String  hdrName=ServletUtils.capitalizeHttpHeaderName(hdrEntry.getName()), hdrValue=StringUtils.trimToEmpty(hdrEntry.getValue());
             if (FILTERED_RESPONSE_HEADERS.contains(hdrName)) {
                 if (logger.isTraceEnabled()) {
                     logger.trace("copyResponseHeadersValues(" + req.getMethod() + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]"
                                + " filtered " + hdrName + ": " + hdrValue);
                 }
                 continue;
             }
 
             if (StringUtils.isEmpty(hdrValue)) {
                 logger.warn("copyResponseHeadersValues(" + req.getMethod() + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]"
                           + " no value for header " + hdrName);
                 rsp.setHeader(hdrName, "");
                 continue;
             }
 
             rsp.setHeader(hdrName, hdrValue);
             hdrsMap.put(hdrName, hdrValue);
         }
         
         if (logger.isTraceEnabled()) {
             for (Map.Entry<String,String> hdrEntry : hdrsMap.entrySet()) {
                 String  hdrName=hdrEntry.getKey(), hdrValue=hdrEntry.getValue();
                 logger.trace("copyResponseHeadersValues(" + req.getMethod() + ")[" + req.getRequestURI() + "][" + req.getQueryString() + "]"
                            + " " + hdrName + ": " + hdrValue);
             }
         }
 
         return hdrsMap;
     }
 
     static final HttpRequestBase resolveRequest (RequestMethod method, URI uri) {
         if (RequestMethod.GET.equals(method)) {
             return new HttpGet(uri);
         } else if (RequestMethod.POST.equals(method)) {
             return new HttpPost(uri);
         } else { // TODO add support for HEAD, OPTIONS, TRACE, PUT if necessary
             throw new UnsupportedOperationException(uri.toString()+ " - unknown method: " + method);
         }
     }
 
     // TODO move this to some generic util location
     public static final String extractRepositoryName(String uriPath) {
         if (StringUtils.isEmpty(uriPath)) {
             return null;
         }
         
         int gitPos=uriPath.indexOf(Constants.DOT_GIT_EXT);
         if (gitPos <= 0) {
             return null;
         }
         
         int startPos=gitPos;
         for ( ; startPos >= 0; startPos--) {
             if (uriPath.charAt(startPos) == '/') {
                 startPos++;
                 break;
             }
         }
         
         if (startPos < 0) {
             startPos = 0;   // in case did not start with '/'
         }
 
         int endPos=gitPos;
         for ( ; endPos < uriPath.length(); endPos++) {
             if (uriPath.charAt(endPos) == '/') {
                 break;
             }
         }
         
         String  pureName=uriPath.substring(startPos, endPos);
         if (Constants.DOT_GIT_EXT.equals(pureName)) {
             return null;
         } else {
             return pureName;
         }
     }
 }
