 package uk.ac.ebi.arrayexpress.servlets;
 
 /*
  * Copyright 2009-2010 European Molecular Biology Laboratory
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 import org.apache.commons.httpclient.Header;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import uk.ac.ebi.arrayexpress.app.ApplicationServlet;
 import uk.ac.ebi.arrayexpress.utils.RegexHelper;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.*;
 import java.nio.CharBuffer;
 import java.util.Enumeration;
 
 public class HttpProxyServlet extends ApplicationServlet
 {
     // logging machinery
     private final Logger logger = LoggerFactory.getLogger(getClass());
 
     private final RegexHelper MATCH_URL_REGEX = new RegexHelper("servlets/proxy/+(.+)", "i");
    private final RegexHelper TEST_HOST_IN_URL_REGEX = new RegexHelper("^http\\:/{2}([^/]+)/", "i");
     private final RegexHelper SQUARE_BRACKETS_REGEX = new RegexHelper("\\[\\]", "g");
 
     private final int PROXY_BUFFER_SIZE = 64000;
 
     protected boolean canAcceptRequest( HttpServletRequest request, RequestType requestType )
     {
         return (requestType == RequestType.GET);
     }
 
     // Respond to HTTP requests from browsers.
     protected void doRequest( HttpServletRequest request, HttpServletResponse response, RequestType requestType )
             throws ServletException, IOException
     {
         logRequest(logger, request, requestType);
 
         String url = MATCH_URL_REGEX.matchFirst(request.getRequestURL().toString());
        url = url.replaceFirst("http:/{1,2}", "http://");   // stupid hack as tomcat 6.0 removes second forward slash
         String queryString = request.getQueryString();
 
         if (0 < url.length()) {
             if (!TEST_HOST_IN_URL_REGEX.test(url)) { // no host here, will self
                 url = "http://localhost:" + String.valueOf(request.getLocalPort()) + "/" + url;
             }
             logger.debug("Will access [{}]", url);
 
             HttpClient httpClient = new HttpClient();
             GetMethod getMethod = new GetMethod(url);
 
             if (null != queryString) {
                 queryString = SQUARE_BRACKETS_REGEX.replace(queryString, "%5B%5D");
                 getMethod.setQueryString(queryString);
             }
 
             Enumeration requestHeaders = request.getHeaderNames();
             while (requestHeaders.hasMoreElements()) {
                 String name = (String) requestHeaders.nextElement();
                 String value = request.getHeader(name);
                 if (null != value) {
                     getMethod.setRequestHeader(name, value);
                 }
             }
 
             try {
                 // establish a connection within 5 seconds
                 httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
 
                 httpClient.executeMethod(getMethod);
 
                 int statusCode = getMethod.getStatusCode();
                 long contentLength = getMethod.getResponseContentLength();
                 logger.debug("Got response [{}], length [{}]", statusCode, contentLength);
 
                 Header[] responseHeaders = getMethod.getResponseHeaders();
                 for (Header responseHeader : responseHeaders) {
                     String name = responseHeader.getName();
                     String value = responseHeader.getValue();
                     if ( null != name
                             && null != value
                             && !(name.equals("Server") || name.equals("Date") || name.equals("Transfer-Encoding"))
                             ) {
                         response.setHeader(responseHeader.getName(), responseHeader.getValue());
                     }
                 }
 
                 if (200 != statusCode) {
                     response.setStatus(statusCode);
                 }
 
                 InputStream inStream = getMethod.getResponseBodyAsStream();
                 if (null != inStream) {
                     BufferedReader in = new BufferedReader(
                             new InputStreamReader(inStream));
 
                     BufferedWriter out = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
 
                     CharBuffer buffer = CharBuffer.allocate(PROXY_BUFFER_SIZE);
                     while ( in.read(buffer) >= 0 ) {
                         buffer.flip();
                         out.append(buffer);
                         buffer.clear();
                     }
 
                     in.close();
                     out.close();
                 }
             } catch ( Exception x ) {
                 if (x.getClass().getName().equals("org.apache.catalina.connector.ClientAbortException")) {
                     logger.warn("Client aborted connection");
                 } else {
                     logger.error("Caught an exception:", x);
                     response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, x.getMessage());
                 }
             } finally {
                 getMethod.releaseConnection();
             }
         }
     }
 }
