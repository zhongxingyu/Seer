 // Copyright (C) 2000, 2001, 2002, 2003, 2004 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.plugin.http;
 
 import junit.framework.TestCase;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 
 import org.apache.oro.text.regex.MatchResult;
 import org.apache.oro.text.regex.Pattern;
 import org.apache.oro.text.regex.PatternCompiler;
 import org.apache.oro.text.regex.PatternMatcher;
 import org.apache.oro.text.regex.Perl5Compiler;
 import org.apache.oro.text.regex.Perl5Matcher;
 
 import HTTPClient.HTTPResponse;
 import HTTPClient.NVPair;
 import HTTPClient.ParseException;
 
 import net.grinder.common.LoggerStubFactory;
 import net.grinder.common.SSLContextFactory;
 import net.grinder.plugininterface.PluginProcessContext;
 import net.grinder.plugininterface.PluginThreadContext;
 import net.grinder.script.Grinder.ScriptContext;
 import net.grinder.script.Statistics;
 import net.grinder.statistics.StatisticsIndexMap;
 import net.grinder.testutility.AssertUtilities;
 import net.grinder.testutility.CallData;
 import net.grinder.testutility.CallRecorder;
 import net.grinder.testutility.RandomStubFactory;
 
 
 /**
  * Unit test case for <code>HTTPRequest</code>.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public class TestHTTPRequest extends TestCase {
 
   private static final PatternMatcher s_matcher = new Perl5Matcher();
   private static final Pattern s_contentLengthPattern;
 
   static {
     final PatternCompiler compiler = new Perl5Compiler();
 
     try {
       s_contentLengthPattern = compiler.compile(
         "^Content-Length:[ \\t]*(.*)\\r?$",
         Perl5Compiler.MULTILINE_MASK  |
         Perl5Compiler.READ_ONLY_MASK |
         Perl5Compiler.CASE_INSENSITIVE_MASK);
     }
     catch (Exception e) {
       throw new ExceptionInInitializerError(e);
     }
   }
 
   private final RandomStubFactory m_scriptContextStubFactory =
     new RandomStubFactory(ScriptContext.class);
 
   private final RandomStubFactory m_statisticsStubFactory =
     new RandomStubFactory(Statistics.class);
 
   protected void setUp() throws Exception {
     final PluginThreadContext threadContext =
       (PluginThreadContext)
       new RandomStubFactory(PluginThreadContext.class).getStub();
 
     final SSLContextFactory sslContextFactory =
       (SSLContextFactory)
       new RandomStubFactory(SSLContextFactory.class).getStub();
 
     final HTTPPluginThreadState threadState =
       new HTTPPluginThreadState(threadContext, sslContextFactory);
 
     m_statisticsStubFactory.setResult("availableForUpdate", Boolean.FALSE);
     final Statistics statistics =
       (Statistics)m_statisticsStubFactory.getStub();
 
     m_scriptContextStubFactory.setResult("getStatistics", statistics);
     final ScriptContext scriptContext =
       (ScriptContext)m_scriptContextStubFactory.getStub();
 
     final RandomStubFactory pluginProcessContextStubFactory =
       new RandomStubFactory(PluginProcessContext.class);
     pluginProcessContextStubFactory.setResult("getPluginThreadListener",
                                               threadState);
     pluginProcessContextStubFactory.setResult("getScriptContext",
                                               scriptContext);
 
     final PluginProcessContext pluginProcessContext =
       (PluginProcessContext)pluginProcessContextStubFactory.getStub();
 
     HTTPPlugin.getPlugin().initialize(pluginProcessContext);
   }
 
   public void testSetUrl() throws Exception {
     final HTTPRequest httpRequest = new HTTPRequest();
     
     assertNull(httpRequest.getUrl());
 
     try {
       httpRequest.setUrl("foo/bah");
       fail("Expected URLException");
     }
     catch (URLException e) {
     }
 
     assertNull(httpRequest.getUrl());
 
     try {
       httpRequest.setUrl("http://foo:bah/blah");
       fail("Expected ParseException");
     }
     catch (ParseException e) {
     }
 
     assertNull(httpRequest.getUrl());
 
     httpRequest.setUrl("http://foo/bah");
 
     assertEquals("http://foo/bah", httpRequest.getUrl());
   }
 
   public void testSetHeaders() {
     final HTTPRequest httpRequest = new HTTPRequest();
 
     assertEquals(0, httpRequest.getHeaders().length);
 
     final NVPair[] newHeaders = new NVPair[] {
       new NVPair("name", "value"),
       new NVPair("another name", "another value"),
     };
     
     httpRequest.setHeaders(newHeaders);
     AssertUtilities.assertArraysEqual(newHeaders, httpRequest.getHeaders());
   }
 
   public void testAddHeader() {
 
     final HTTPRequest httpRequest = new HTTPRequest();
 
     final NVPair[] newHeaders = new NVPair[] {
       new NVPair("name", "value"),
       new NVPair("another name", "another value"),
     };
 
     httpRequest.setHeaders(newHeaders);
     
     httpRequest.addHeader("name", "value");
     httpRequest.addHeader("foo", "bah");
 
     AssertUtilities.assertArraysEqual(
       new NVPair[] {
         new NVPair("name", "value"),
         new NVPair("another name", "another value"),
         new NVPair("name", "value"),
         new NVPair("foo", "bah"),
       },
       httpRequest.getHeaders());
   }
 
   public void testDeleteHeader() {
 
     final HTTPRequest httpRequest = new HTTPRequest();
 
     final NVPair[] newHeaders = new NVPair[] {
       new NVPair("name", "value"),
       new NVPair("another name", "another value"),
       new NVPair("name", "value"),
       new NVPair("some more stuff", "value"),
     };
 
     httpRequest.setHeaders(newHeaders);
     
     httpRequest.deleteHeader("name");
 
     AssertUtilities.assertArraysEqual(
       new NVPair[] {
         new NVPair("another name", "another value"),
         new NVPair("some more stuff", "value"),
       },
       httpRequest.getHeaders());
 
     httpRequest.deleteHeader("some more stuff");
 
     AssertUtilities.assertArraysEqual(
       new NVPair[] {
         new NVPair("another name", "another value"),
       },
       httpRequest.getHeaders());
   }
 
   /**
    * Active class that accepts a connection on a socket, reads an HTTP
    * request, and returns a response who's body is the text of the
    * request.
    */
   private final class HTTPRequestHandler implements Runnable {
 
     private final ServerSocket m_serverSocket;
     private String m_lastRequestHeaders;
     private byte[] m_lastRequestBody;
     private String m_statusString = "200 OK";
 
     public HTTPRequestHandler() throws Exception {
       m_serverSocket = new ServerSocket(0);
       new Thread(this, getClass().getName()).start();
     }
 
     public void shutdown() throws Exception {
       m_serverSocket.close();
     }
 
     public void setStatusString(String statusString) {
       m_statusString = statusString;
     }
 
     public String getURL() {
       return "http://localhost:" + m_serverSocket.getLocalPort();
     }
 
     public String getLastRequestHeaders() {
       return m_lastRequestHeaders;
     }
 
     public byte[] getLastRequestBody() {
       return m_lastRequestBody;
     }
 
     public String getRequestFirstHeader() {
       final String text = getLastRequestHeaders();
 
       final int i = text.indexOf("\r\n");
       assertTrue("Has at least one line", i>=0);
       return text.substring(0, i);
     }
 
     public void assertRequestContainsHeader(String line) {
       final String text = getLastRequestHeaders();
 
       int start = 0;
       int i;
     
       while((i = text.indexOf("\r\n", start)) != -1) {
         if (text.substring(start, i).equals(line)) {
           return;
         }
 
         start = i + 2;
       }
 
       if (text.substring(start).equals(line)) {
         return;
       }
     
       fail(text + " does not contain " + line);
     }
 
  
     public void run() {
       try {
         while (true) {
           final Socket localSocket;
 
           try {
             localSocket = m_serverSocket.accept();
           }
           catch (SocketException e) {
             // Socket's been closed, lets quit.
             break;
           }
 
           final InputStream in = localSocket.getInputStream();
 
           final StringBuffer headerBuffer = new StringBuffer();
           final byte[] buffer = new byte[1000];
           int n;
           int bodyStart = -1;
 
           READ_HEADERS:
           while ((n = in.read(buffer, 0, buffer.length)) != -1) {
 
             for (int i=0; i<n-3; ++i) {
               if (buffer[i] == '\r' &&
                   buffer[i+1] == '\n' &&
                   buffer[i+2] == '\r' &&
                   buffer[i+3] == '\n') {
 
                 headerBuffer.append(new String(buffer, 0, i));
                 bodyStart = i + 4;
                 break READ_HEADERS;
               }
             }
 
             headerBuffer.append(new String(buffer, 0, n));
           }
 
           if (bodyStart == -1) {
             throw new IOException("No header boundary");
           }
 
           m_lastRequestHeaders = headerBuffer.toString();
 
           if (s_matcher.contains(m_lastRequestHeaders,
                                  s_contentLengthPattern)) {
             final MatchResult matchResult = s_matcher.getMatch();
 
             final int contentLength =
               Integer.parseInt(s_matcher.getMatch().group(1).trim());
 
             m_lastRequestBody = new byte[contentLength];
 
             int bodyBytes = n - bodyStart;
 
             System.arraycopy(buffer, bodyStart, m_lastRequestBody, 0,
                              bodyBytes);
 
             while (bodyBytes < m_lastRequestBody.length) {
               final int bytesRead =
                 in.read(m_lastRequestBody, bodyBytes,
                         m_lastRequestBody.length - bodyBytes);
 
               if (bytesRead == -1) {
                 throw new IOException("Content-length too large");
               }
               
               bodyBytes += bytesRead;
             }
 
             if (in.available() > 0) {
               throw new IOException("Content-length too small");
             }
           }
           else {
             m_lastRequestBody = null;
           }
 
           final OutputStream out = localSocket.getOutputStream();
 
           final StringBuffer response = new StringBuffer();
           response.append("HTTP/1.0 " + m_statusString + "\r\n");
           response.append("\r\n");
           out.write(response.toString().getBytes());
           out.flush();
 
           localSocket.close();
         }
       }
       catch (Exception e) {
         e.printStackTrace();
       }
       finally {
         try {
           m_serverSocket.close();
         }
         catch (IOException e) {
           // Whatever.
         }
       }
     }
   }
 
   public void testDELETE() throws Exception {
     final HTTPRequestHandler handler = new HTTPRequestHandler();
     final HTTPRequest request = new HTTPRequest();
 
     try {
       request.DELETE();
       fail("Expected URLException");
     }
     catch (URLException e) {
     }
 
     try {
       request.DELETE("/partial");
       fail("Expected URLException");
     }
     catch (URLException e) {
     }
 
     final HTTPResponse response = request.DELETE(handler.getURL());
     assertEquals(200, response.getStatusCode());
     assertEquals("DELETE / HTTP/1.1", handler.getRequestFirstHeader());
 
     request.setUrl(handler.getURL());
     final HTTPResponse response2 = request.DELETE("/foo");
     assertEquals(200, response2.getStatusCode());
     assertEquals("DELETE /foo HTTP/1.1", handler.getRequestFirstHeader());
 
     final HTTPResponse response3 = request.DELETE();
     assertEquals(200, response3.getStatusCode());
     assertEquals("DELETE / HTTP/1.1", handler.getRequestFirstHeader());
 
     final NVPair[] headers4 = {
       new NVPair("x", "212"),
       new NVPair("y", "321"),
     };
 
     final HTTPResponse response4 = request.DELETE("/", headers4);
     assertEquals(200, response4.getStatusCode());
     assertEquals("DELETE / HTTP/1.1", handler.getRequestFirstHeader());
     handler.assertRequestContainsHeader("x: 212");
     handler.assertRequestContainsHeader("y: 321");
 
     handler.shutdown();
   }
 
   public void testGET() throws Exception {
     final HTTPRequestHandler handler = new HTTPRequestHandler();
     final HTTPRequest request = new HTTPRequest();
 
     try {
       request.GET();
       fail("Expected URLException");
     }
     catch (URLException e) {
     }
 
     try {
       request.GET("#partial");
       fail("Expected URLException");
     }
     catch (URLException e) {
     }
 
     final HTTPResponse response = request.GET(handler.getURL());
     assertEquals(200, response.getStatusCode());
     assertEquals("GET / HTTP/1.1", handler.getRequestFirstHeader());
 
     request.setUrl(handler.getURL());
     final HTTPResponse response2 = request.GET("/foo");
     assertEquals(200,  response2.getStatusCode());
     assertEquals("GET /foo HTTP/1.1", handler.getRequestFirstHeader());
 
     final HTTPResponse response3 = request.GET();
     assertEquals(200, response3.getStatusCode());
     assertEquals("GET / HTTP/1.1", handler.getRequestFirstHeader());
 
     final NVPair[] parameters4 = {
       new NVPair("some", "header"),
       new NVPair("y", "321"),
     };
 
     final HTTPResponse response4 = request.GET("/lah/de/dah", parameters4);
     assertEquals(200, response4.getStatusCode());
     assertEquals("GET /lah/de/dah?some=header&y=321 HTTP/1.1",
                  handler.getRequestFirstHeader());
 
     final NVPair[] parameters5 = {
       new NVPair("another", "header"),
       new NVPair("y", "331"),
     };
 
     request.setUrl(handler.getURL() + "/lah/");
     final HTTPResponse response5 = request.GET(parameters5);
     assertEquals(200, response5.getStatusCode());
     assertEquals("GET /lah/?another=header&y=331 HTTP/1.1",
                  handler.getRequestFirstHeader());
 
     final NVPair[] headers6 = {
       new NVPair("key", "value"),
     };
 
     request.setHeaders(headers6);
     final HTTPResponse response6 = request.GET();
     assertEquals(200, response6.getStatusCode());
     assertEquals("GET /lah/ HTTP/1.1", handler.getRequestFirstHeader());
     handler.assertRequestContainsHeader("key: value");
 
     handler.shutdown();
   }
 
   public void testHEAD() throws Exception {
     final HTTPRequestHandler handler = new HTTPRequestHandler();
 
     final HTTPRequest request = new HTTPRequest();
 
     try {
       request.HEAD();
       fail("Expected URLException");
     }
     catch (URLException e) {
     }
 
     try {
       request.HEAD("?partial");
       fail("Expected URLException");
     }
     catch (URLException e) {
     }
 
     final HTTPResponse response = request.HEAD(handler.getURL());
     assertEquals(200, response.getStatusCode());
     assertEquals("HEAD / HTTP/1.1", handler.getRequestFirstHeader());
 
     request.setUrl(handler.getURL());
     final HTTPResponse response2 = request.HEAD("/foo");
     assertEquals(200, response2.getStatusCode());
     assertEquals("HEAD /foo HTTP/1.1", handler.getRequestFirstHeader());
 
     final HTTPResponse response3 = request.HEAD();
     assertEquals(200, response3.getStatusCode());
     assertEquals("HEAD / HTTP/1.1", handler.getRequestFirstHeader());
 
     final NVPair[] parameters4 = {
       new NVPair("some", "header"),
       new NVPair("y", "321"),
     };
 
     final HTTPResponse response4 = request.HEAD("/lah/de/dah", parameters4);
     assertEquals(200, response4.getStatusCode());
     assertEquals("HEAD /lah/de/dah?some=header&y=321 HTTP/1.1",
                  handler.getRequestFirstHeader());
 
     final NVPair[] parameters5 = {
       new NVPair("another", "header"),
       new NVPair("y", "331"),
     };
 
     request.setUrl(handler.getURL() + "/lah/");
     final HTTPResponse response5 = request.HEAD(parameters5);
     assertEquals(200, response5.getStatusCode());
     assertEquals("HEAD /lah/?another=header&y=331 HTTP/1.1",
                  handler.getRequestFirstHeader());
 
     final NVPair[] headers6 = {
       new NVPair("key", "value"),
     };
 
     request.setHeaders(headers6);
     final HTTPResponse response6 = request.HEAD();
     assertEquals(200, response6.getStatusCode());
     assertEquals("HEAD /lah/ HTTP/1.1", handler.getRequestFirstHeader());
     handler.assertRequestContainsHeader("key: value");
 
     handler.shutdown();
   }
 
   public void testOPTIONS() throws Exception {
     final HTTPRequestHandler handler = new HTTPRequestHandler();
     final HTTPRequest request = new HTTPRequest();
 
     try {
       request.OPTIONS();
       fail("Expected URLException");
     }
     catch (URLException e) {
     }
 
     try {
       request.OPTIONS("///::partial");
       fail("Expected URLException");
     }
     catch (URLException e) {
     }
 
     final HTTPResponse response = request.OPTIONS(handler.getURL());
     assertEquals(200, response.getStatusCode());
     assertEquals("OPTIONS / HTTP/1.1", handler.getRequestFirstHeader());
 
     request.setUrl(handler.getURL());
     final HTTPResponse response2 = request.OPTIONS("/foo");
     assertEquals(200, response2.getStatusCode());
     assertEquals("OPTIONS /foo HTTP/1.1", handler.getRequestFirstHeader());
 
     final HTTPResponse response3 = request.OPTIONS();
     assertEquals(200, response3.getStatusCode());
     assertEquals("OPTIONS / HTTP/1.1", handler.getRequestFirstHeader());
 
     final byte[] data4 = { 0, 1, 2, 3, 4, };
 
     final HTTPResponse response4 = request.OPTIONS("/blah", data4);
     assertEquals(200, response4.getStatusCode());
     assertEquals("OPTIONS /blah HTTP/1.1", handler.getRequestFirstHeader());
     AssertUtilities.assertArraysEqual(data4, handler.getLastRequestBody());
 
     final byte[] data5 = { 23, 45, -21, -124 , 9, 44, 2 };
 
     request.setUrl(handler.getURL() + "/lah/");
     request.setData(data5);
     final HTTPResponse response5 = request.OPTIONS("/blah");
     assertEquals(200, response5.getStatusCode());
     assertEquals("OPTIONS /blah HTTP/1.1", handler.getRequestFirstHeader());
     AssertUtilities.assertArraysEqual(data5, handler.getLastRequestBody());
 
     final NVPair[] headers6 = {
       new NVPair("key", "value"),
     };
 
     request.setHeaders(headers6);
     final HTTPResponse response6 = request.OPTIONS();
     assertEquals(200, response6.getStatusCode());
     assertEquals("OPTIONS /lah/ HTTP/1.1", handler.getRequestFirstHeader());
     handler.assertRequestContainsHeader("key: value");
 
     handler.shutdown();
   }
 
   public void testPOST() throws Exception {
     final HTTPRequestHandler handler = new HTTPRequestHandler();
     final HTTPRequest request = new HTTPRequest();
 
     try {
       request.POST();
       fail("Expected URLException");
     }
     catch (URLException e) {
     }
 
     try {
       request.POST("#:/partial");
       fail("Expected URLException");
     }
     catch (URLException e) {
     }
 
     final HTTPResponse response = request.POST(handler.getURL());
     assertEquals(200, response.getStatusCode());
     assertEquals("POST / HTTP/1.1", handler.getRequestFirstHeader());
 
     request.setUrl(handler.getURL());
     final HTTPResponse response2 = request.POST("/foo");
     assertEquals(200, response2.getStatusCode());
     assertEquals("POST /foo HTTP/1.1", handler.getRequestFirstHeader());
 
     final HTTPResponse response3 = request.POST();
     assertEquals(200, response3.getStatusCode());
     assertEquals("POST / HTTP/1.1", handler.getRequestFirstHeader());
 
     final byte[] data4 = { 0, 1, 2, 3, 4, };
 
     final HTTPResponse response4 = request.POST("/blah", data4);
     assertEquals(200, response4.getStatusCode());
     assertEquals("POST /blah HTTP/1.1", handler.getRequestFirstHeader());
     AssertUtilities.assertArraysEqual(data4, handler.getLastRequestBody());
 
     final byte[] data5 = { 23, 45, -21, -124 , 9, 44, 2 };
 
     request.setUrl(handler.getURL() + "/lah/");
     request.setData(data5);
     final HTTPResponse response5 = request.POST("/blah");
     assertEquals(200, response5.getStatusCode());
     assertEquals("POST /blah HTTP/1.1", handler.getRequestFirstHeader());
     AssertUtilities.assertArraysEqual(data5, handler.getLastRequestBody());
 
     final NVPair[] headers6 = {
       new NVPair("key", "value"),
     };
 
     request.setHeaders(headers6);
     final HTTPResponse response6 = request.POST();
     assertEquals(200, response6.getStatusCode());
     assertEquals("POST /lah/ HTTP/1.1", handler.getRequestFirstHeader());
     handler.assertRequestContainsHeader("key: value");
 
     final NVPair[] formData7 = {
       new NVPair("Vessel", "Grace of Lefkas"),
     };
 
     final HTTPResponse response7 = request.POST("/foo?abc=def", formData7);
     assertEquals(200, response7.getStatusCode());
     assertEquals("POST /foo?abc=def HTTP/1.1",
                  handler.getRequestFirstHeader());
     handler.assertRequestContainsHeader("key: value");
     final String bodyText7 = new String(handler.getLastRequestBody());
     assertTrue(bodyText7.indexOf("Vessel=Grace+of+Lefkas") > -1);
 
     final NVPair[] formData8 = {
       new NVPair("LOA", "12.3m"),
       new NVPair("Draught", "1.7"),
     };
 
     request.setFormData(formData8);
 
     final HTTPResponse response8 = request.POST();
     assertEquals(200, response8.getStatusCode());
     assertEquals("POST /lah/ HTTP/1.1", handler.getRequestFirstHeader());
     handler.assertRequestContainsHeader("key: value");
     AssertUtilities.assertArraysEqual(data5, handler.getLastRequestBody());
 
     request.setData(null);
 
     final HTTPResponse response9 = request.POST();
     assertEquals(200, response9.getStatusCode());
     assertEquals("POST /lah/ HTTP/1.1", handler.getRequestFirstHeader());
     handler.assertRequestContainsHeader("key: value");
     final String bodyText9 = new String(handler.getLastRequestBody());
     assertTrue(bodyText9.indexOf("LOA=12.3m") > -1);
 
     final HTTPResponse response10 = request.POST(formData7);
     assertEquals(200, response10.getStatusCode());
     assertEquals("POST /lah/ HTTP/1.1", handler.getRequestFirstHeader());
     final String bodyText10 = new String(handler.getLastRequestBody());
     assertTrue(bodyText10.indexOf("Vessel=Grace+of+Lefkas") > -1);
 
     handler.shutdown();
   }
 
   public void testPUT() throws Exception {
     final HTTPRequestHandler handler = new HTTPRequestHandler();
     final HTTPRequest request = new HTTPRequest();
 
     try {
       request.PUT();
       fail("Expected URLException");
     }
     catch (URLException e) {
     }
 
     try {
       request.PUT("?:/partial");
       fail("Expected URLException");
     }
     catch (URLException e) {
     }
 
     final HTTPResponse response = request.PUT(handler.getURL());
     assertEquals(200, response.getStatusCode());
     assertEquals("PUT / HTTP/1.1", handler.getRequestFirstHeader());
 
     request.setUrl(handler.getURL());
     final HTTPResponse response2 = request.PUT("/foo");
     assertEquals(200, response2.getStatusCode());
     assertEquals("PUT /foo HTTP/1.1", handler.getRequestFirstHeader());
 
     final HTTPResponse response3 = request.PUT();
     assertEquals(200, response3.getStatusCode());
     assertEquals("PUT / HTTP/1.1", handler.getRequestFirstHeader());
 
     final byte[] data4 = { 0, 1, 2, 3, 4, };
 
     final HTTPResponse response4 = request.PUT("/blah", data4);
     assertEquals(200, response4.getStatusCode());
     assertEquals("PUT /blah HTTP/1.1", handler.getRequestFirstHeader());
     AssertUtilities.assertArraysEqual(data4, handler.getLastRequestBody());
 
     final byte[] data5 = { 23, 45, -21, -124 , 9, 44, 2 };
 
     request.setUrl(handler.getURL() + "/lah/");
     request.setData(data5);
     final HTTPResponse response5 = request.PUT("/blah");
     assertEquals(200, response5.getStatusCode());
     assertEquals("PUT /blah HTTP/1.1", handler.getRequestFirstHeader());
     AssertUtilities.assertArraysEqual(data5, handler.getLastRequestBody());
 
     final NVPair[] headers6 = {
       new NVPair("key", "value"),
     };
 
     request.setHeaders(headers6);
     final HTTPResponse response6 = request.PUT();
     assertEquals(200, response6.getStatusCode());
     assertEquals("PUT /lah/ HTTP/1.1", handler.getRequestFirstHeader());
     handler.assertRequestContainsHeader("key: value");
 
     handler.shutdown();
   }
 
   public void testTRACE() throws Exception {
     final HTTPRequestHandler handler = new HTTPRequestHandler();
     final HTTPRequest request = new HTTPRequest();
 
     try {
       request.TRACE();
       fail("Expected URLException");
     }
     catch (URLException e) {
     }
 
     try {
       request.TRACE("??partial");
       fail("Expected URLException");
     }
     catch (URLException e) {
     }
 
     final HTTPResponse response = request.TRACE(handler.getURL());
     assertEquals(200, response.getStatusCode());
     assertEquals("TRACE / HTTP/1.1", handler.getRequestFirstHeader());
 
     request.setUrl(handler.getURL());
     final HTTPResponse response2 = request.TRACE("/foo");
     assertEquals(200, response2.getStatusCode());
     assertEquals("TRACE /foo HTTP/1.1", handler.getRequestFirstHeader());
 
     final HTTPResponse response3 = request.TRACE();
     assertEquals(200, response3.getStatusCode());
     assertEquals("TRACE / HTTP/1.1", handler.getRequestFirstHeader());
 
     final byte[] data4 = { 0, 1, 2, 3, 4, };
 
     final NVPair[] headers4 = {
       new NVPair("key", "value"),
     };
 
     request.setUrl(handler.getURL() + "/lah/");
     request.setHeaders(headers4);
     final HTTPResponse response4 = request.TRACE();
     assertEquals(200, response4.getStatusCode());
     assertEquals("TRACE /lah/ HTTP/1.1", handler.getRequestFirstHeader());
     handler.assertRequestContainsHeader("key: value");
 
     handler.shutdown();
   }
 
   public final void testToString() throws Exception {
     final HTTPRequest request = new HTTPRequest();
 
     assertEquals("<Undefined URL>\n", request.toString());
 
     request.setUrl("http://grinder.sf.net/");
     assertEquals("http://grinder.sf.net/\n", request.toString());
 
     request.setHeaders(new NVPair[] {
                          new NVPair("home", "end"),
                          new NVPair("pause", "insert"),
                        });
 
     assertEquals("http://grinder.sf.net/\nhome: end\npause: insert\n",
                  request.toString());
   }
 
   public final void testSetDataFromFile() throws Exception {
 
     final File file = File.createTempFile("testing", "123");
     file.deleteOnExit();
 
     final OutputStream out = new FileOutputStream(file);
 
     final byte[] data5 = { 23, 45, -21, -124 , 9, 44, 2 };
 
     out.write(data5);
     out.close();
 
     final HTTPRequest request = new HTTPRequest();
     request.setDataFromFile(file.getPath());
 
     AssertUtilities.assertArraysEqual(data5, request.getData());
 
   }
 
   public final void testResponseProcessing() throws Exception {
     final HTTPRequestHandler handler = new HTTPRequestHandler();
     final HTTPRequest request = new HTTPRequest();
 
     final LoggerStubFactory loggerStubFactory = new LoggerStubFactory();
 
     m_scriptContextStubFactory.setResult("getLogger",
                                          loggerStubFactory.getLogger());
 
     request.GET(handler.getURL());
 
     final CallData loggerCall = loggerStubFactory.getCallData();
     assertEquals("output", loggerCall.getMethodName());
     final String message = (String)loggerCall.getParameters()[0];
     assertTrue(message.indexOf("200") >= 0);
     assertEquals(-1, message.indexOf("Redirect"));
     loggerStubFactory.assertNoMoreCalls();
 
     final CallData callData1 =
       m_statisticsStubFactory.assertSuccess("availableForUpdate");
     assertEquals(Boolean.FALSE, callData1.getResult());
     m_statisticsStubFactory.assertNoMoreCalls();
 
     handler.setStatusString("302 Moved Temporarily");
     m_statisticsStubFactory.setResult("availableForUpdate", Boolean.TRUE);
 
     final HTTPResponse response2 = request.GET(handler.getURL());
 
     final CallData loggerCall2 = loggerStubFactory.getCallData();
     assertEquals("output", loggerCall2.getMethodName());
     final String message2 = (String)loggerCall2.getParameters()[0];
     assertTrue(message2.indexOf("302") >= 0);
     assertTrue(message2.indexOf("Redirect") >= 0);
     loggerStubFactory.assertNoMoreCalls();
 
     final HTTPPlugin httpPlugin = HTTPPlugin.getPlugin();
 
     final CallData callData2 =
       m_statisticsStubFactory.assertSuccess("availableForUpdate");
     assertEquals(Boolean.TRUE, callData2.getResult());
 
     m_statisticsStubFactory.assertSuccess(
       "addValue", httpPlugin.getResponseLengthIndex(), new Long(0));
 
     m_statisticsStubFactory.assertSuccess(
       "setValue", httpPlugin.getResponseStatusIndex(), new Long(302));
 
     m_statisticsStubFactory.assertSuccess(
       "addValue", StatisticsIndexMap.LongIndex.class, Long.class);
 
     try {
       m_statisticsStubFactory.assertSuccess(
         "addValue", StatisticsIndexMap.LongIndex.class, Long.class);
 
       m_statisticsStubFactory.assertSuccess(
         "addValue", StatisticsIndexMap.LongIndex.class, Long.class);
     }
     catch (java.util.NoSuchElementException e) {
       // Whatever.
     }
 
     m_statisticsStubFactory.assertNoMoreCalls();
 
     handler.setStatusString("400 Bad Request");
 
     final HTTPResponse response3 = request.GET(handler.getURL());
 
     final CallData loggerCall3 = loggerStubFactory.getCallData();
     assertEquals("output", loggerCall3.getMethodName());
     final String message3 = (String)loggerCall3.getParameters()[0];
     assertTrue(message3.indexOf("400") >= 0);
     loggerStubFactory.assertNoMoreCalls();
 
     final CallData callData3 = 
       m_statisticsStubFactory.assertSuccess("availableForUpdate");
     assertEquals(Boolean.TRUE, callData3.getResult());
 
     m_statisticsStubFactory.assertSuccess(
       "addValue", httpPlugin.getResponseLengthIndex(), new Long(0));
 
     m_statisticsStubFactory.assertSuccess(
       "setValue", httpPlugin.getResponseStatusIndex(), new Long(400));
 
     m_statisticsStubFactory.assertSuccess(
       "addValue", StatisticsIndexMap.LongIndex.class, Long.class);
 
     try {
       m_statisticsStubFactory.assertSuccess(
         "addValue", StatisticsIndexMap.LongIndex.class, Long.class);
 
       m_statisticsStubFactory.assertSuccess(
         "addValue", StatisticsIndexMap.LongIndex.class, Long.class);

      m_statisticsStubFactory.assertSuccess(
        "addValue", httpPlugin.getResponseErrorsIndex(), new Long(1));
     }
     catch (java.util.NoSuchElementException e) {
       // Whatever.
     }
 
     m_statisticsStubFactory.assertNoMoreCalls();
   }
 
   public final void testSubclassProcessResponse() throws Exception {
 
     final HTTPRequestHandler handler = new HTTPRequestHandler();
 
     final Object[] resultHolder = new Object[1];
 
     final HTTPRequest request = new HTTPRequest() {
         public void processResponse(HTTPResponse response) {
           resultHolder[0] = response;
         }
       };
 
     final HTTPResponse response = request.GET(handler.getURL());
 
     assertSame(response, resultHolder[0]);
   }
 }
