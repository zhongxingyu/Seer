 // Copyright (C) 2000, 2001, 2002, 2003 Philip Aston
 // Copyright (C) 2000 Phil Dawes
 // Copyright (C) 2001 Kalle Burbeck
 // Copyright (C) 2003 Bill Schnellinger
 // Copyright (C) 2003, 2004 Bertrand Ave
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
 
 import java.io.BufferedWriter;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.text.DateFormat;
 
 import org.apache.oro.text.regex.MalformedPatternException;
 import org.apache.oro.text.regex.MatchResult;
 import org.apache.oro.text.regex.Pattern;
 import org.apache.oro.text.regex.PatternCompiler;
 import org.apache.oro.text.regex.Perl5Compiler;
 import org.apache.oro.text.regex.Perl5Matcher;
 
 import HTTPClient.Codecs;
 import HTTPClient.NVPair;
 import HTTPClient.ParseException;
 
 import net.grinder.common.GrinderBuild;
 import net.grinder.tools.tcpproxy.ConnectionDetails;
 import net.grinder.tools.tcpproxy.TCPProxyFilter;
 
 
 /**
  * {@link TCPProxyFilter} that outputs session in a form that can be
  * reused by the HTTP plugin.
  *
  * <p>Bugs:
  * <ul>
  * <li>Assumes Request-Line (GET ...) is first line of packet, and that
  * every packet that starts with such a line is the start of a request.
  * <li>Should filter chunked transfer coding from POST data.
  * <li>Doesn't handle line continuations.
  * <li>Doesn't parse correctly if lines are broken accross message
  * fragments.
  * </ul>
  *
  * @author Philip Aston
  * @author Bertrand Ave
  * @version $Revision$
  */
 public class HTTPPluginTCPProxyFilter2 implements TCPProxyFilter {
 
   private static final String INITIAL_TEST_PROPERTY = "initialTest";
   private static final String FILENAME_V1_PROPERTY = "scriptFileV1";
   private static final String FILENAME_V2_PROPERTY = "scriptFileV2";
   private static final String USERAGENT_PROPERTY = "userAgent";
   private static final String USERAGENT_DEFAULT_VALUE =
     "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)";
   private static final String DEBUG_PROPERTY = "debug";
   private static final String COOKIENAME_PROPERTY = "cookieName";
   private static final String SEARCHEDPAGEPATTERN_PROPERTY =
     "searchedPagePattern";
   private static final String EXCLUDERESOURCE_PROPERTY = "excludeResource";
   private static final String RESOURCES_PROPERTY = "resources";
   private static final String SLEEPTIME_PROPERTY = "sleepTime";
   private static final String DISCARD_PATTERN = "discardPattern";
   private static final int DISCARD_PATTERN_LENGTH = 10;
 
   private boolean m_extractJsession = false;
   private static final String s_userAgent =
     System.getProperty(USERAGENT_PROPERTY, USERAGENT_DEFAULT_VALUE);
   private static final String s_debug =
     System.getProperty(DEBUG_PROPERTY, "off");
   private static final String s_cookieName =
     System.getProperty(COOKIENAME_PROPERTY, "JSESSIONID");
   private static final String s_searchedPagePattern =
     System.getProperty(SEARCHEDPAGEPATTERN_PROPERTY, "^.*?home.jsp$");
   private static String[] s_resource;
   private static final String s_excludeResource =
     System.getProperty(EXCLUDERESOURCE_PROPERTY, "off");
   private static final long s_sleepTime =
     Integer.getInteger(SLEEPTIME_PROPERTY, -1).intValue();
   private static final String[] s_discardPattern =
     new String[DISCARD_PATTERN_LENGTH];
 
   private static final String s_newLine =
     System.getProperty("line.separator");
   private static final String s_indent = "    ";
 
   private static long s_lastResponseTime = 0;
 
   /**
    * A list of headers which we record and replay. HTTPConnection
    * sets up many things for us (Host, Connection, User-Agent,
    * Cookie, Content-Length and so on).
    */
   private static final String[] s_mirroredHeaders = {
     "Accept",
     "Accept-Charset",
     "Accept-Encoding",
     "Accept-Language",
     "Cache-Control",
     "If-Modified-Since",
     "Referer",
     "User-Agent",
   };
 
   private static final int s_length = 100;
 
   private final PrintWriter m_out;
   private final String m_scriptFileName;
   private final PrintWriter m_scriptFileWriter;
   private final String m_testFileName;
   private final PrintWriter m_testFileWriter;
   private final String m_recordedScenarioFileName;
   private final PrintWriter m_recordedScenarioFileWriter;
 
   private final Pattern m_basicAuthorizationHeaderPattern;
   private final Pattern m_contentTypePattern;
   private final Pattern m_contentLengthPattern;
   private final Pattern m_messageBodyPattern;
   private final Pattern[] m_mirroredHeaderPatterns =
     new Pattern[s_mirroredHeaders.length];
   private final Pattern m_lastURLPathElementPattern;
   private final Pattern m_requestLinePattern;
   private final Pattern m_searchedPagePattern;
   private final Pattern[] m_discardPattern =
     new Pattern[DISCARD_PATTERN_LENGTH];
 
   /**
    * Map of {@link ConnectionDetails} to handlers.
    */
   private final Map m_handlers = new HashMap();
 
   private int m_currentRequestNumber;
   private int m_currentPageNumber;
   private int m_initialPageNumber;
   private boolean m_discardingAssociatedResources = false;
 
   private final Map m_previousHeaders =
     Collections.synchronizedMap(new HashMap());
   private static long[] s_sleepTimes = new long[s_length];
 
 
   /**
    * Constructor.
    *
    * @param outputPrintWriter a <code>PrintWriter</code> value
    * @exception IOException If an I/O error occurs.
    * @exception MalformedPatternException If a regular expression
    * could not be compiled.
    */
   public HTTPPluginTCPProxyFilter2(PrintWriter outputPrintWriter)
     throws IOException, MalformedPatternException {
 
     m_out = outputPrintWriter;
 
     m_initialPageNumber = Integer.getInteger(INITIAL_TEST_PROPERTY, 0)
       .intValue();
     m_currentRequestNumber = m_initialPageNumber - 1;
     m_currentPageNumber = m_currentRequestNumber;
 
     s_resource = extractResources(System.getProperty(RESOURCES_PROPERTY,
       ".gif;.css;.js;.jpg;.ico"));
 
     int i = 0;
     String discardPattern = System.getProperty(DISCARD_PATTERN + i);
     while (i < DISCARD_PATTERN_LENGTH && discardPattern != null) {
       s_discardPattern[i] = discardPattern;
       i++;
       discardPattern = System.getProperty(DISCARD_PATTERN + i);
     }
 
     // displays the value of all system properties used by this filter
     debug ("debug: " + s_debug);
     debug ("userAgent: " + s_userAgent);
     debug ("cookieName: " + s_cookieName);
     debug ("searchedPagePattern: " + s_searchedPagePattern);
     debug ("excludeResource: " + s_excludeResource);
     debug ("sleepTime: " + s_sleepTime);
     debug ("resource: " + s_resource.toString());
     debug ("discard pattern: " + s_discardPattern);
 
     final PatternCompiler compiler = new Perl5Compiler();
 
     m_messageBodyPattern =
       compiler.compile(
         "\\r\\n\\r\\n(.*)",
         Perl5Compiler.READ_ONLY_MASK | Perl5Compiler.SINGLELINE_MASK);
 
     // From RFC 2616:
     //
     // Request-Line = Method SP Request-URI SP HTTP-Version CRLF
     // HTTP-Version = "HTTP" "/" 1*DIGIT "." 1*DIGIT
     // http_URL = "http:" "//" host [ ":" port ] [ abs_path [ "?" query ]]
     // ";" can also be used as a query separator.
     //
     // We're flexible about SP and CRLF, see RFC 2616, 19.3.
 
     m_requestLinePattern =
       compiler.compile(
         "^([A-Z]+)[ \\t]+([^\\?;]+)([\\?;].+)?[ \\t]+HTTP/\\d.\\d[ \\t]*\\r?$",
         Perl5Compiler.READ_ONLY_MASK | Perl5Compiler.MULTILINE_MASK);
 
     m_contentLengthPattern =
       compiler.compile(
         getHeaderExpression("Content-Length"),
         Perl5Compiler.READ_ONLY_MASK |
         Perl5Compiler.MULTILINE_MASK |
         Perl5Compiler.CASE_INSENSITIVE_MASK // Sigh...
         );
 
     m_contentTypePattern =
       compiler.compile(
         getHeaderExpression("Content-Type"),
         Perl5Compiler.READ_ONLY_MASK |
         Perl5Compiler.MULTILINE_MASK |
         Perl5Compiler.CASE_INSENSITIVE_MASK // and sigh again.
         );
 
     for (i = 0; i < s_mirroredHeaders.length; i++) {
       m_mirroredHeaderPatterns[i] =
         compiler.compile(
           getHeaderExpression(s_mirroredHeaders[i]),
           Perl5Compiler.READ_ONLY_MASK |
           Perl5Compiler.MULTILINE_MASK);
     }
 
     m_basicAuthorizationHeaderPattern =
       compiler.compile(
         "^Authorization:[ \\t]*Basic[  \\t]*([a-zA-Z0-9+/]*=*).*\\r?$",
         Perl5Compiler.READ_ONLY_MASK | Perl5Compiler.MULTILINE_MASK);
 
     // Ignore maximum amount of stuff thats not a '?' followed by
     // a '/', then grab the next until the first '?'.
     m_lastURLPathElementPattern =
       compiler.compile(
         "^[^\\?]*/([^\\?]*)",
         Perl5Compiler.READ_ONLY_MASK | Perl5Compiler.SINGLELINE_MASK);
 
     m_searchedPagePattern =
       compiler.compile(s_searchedPagePattern,
       Perl5Compiler.READ_ONLY_MASK | Perl5Compiler.SINGLELINE_MASK);
 
     i = 0;
     while (i < DISCARD_PATTERN_LENGTH && s_discardPattern[i] != null) {
       m_discardPattern[i] =
         compiler.compile(s_discardPattern[i],
         Perl5Compiler.READ_ONLY_MASK | Perl5Compiler.SINGLELINE_MASK);
       i++;
     }
 
     m_scriptFileName = System.getProperty (FILENAME_V1_PROPERTY, "httpscript") +
       ".py";
 
     m_scriptFileWriter =
       new PrintWriter(
         new BufferedWriter(new FileWriter(m_scriptFileName)), false);
 
     final String testFileNamePrefix = System.getProperty (FILENAME_V1_PROPERTY,
       "httpscript") + "_tests";
     m_testFileName = testFileNamePrefix + ".py";
 
     m_testFileWriter =
       new PrintWriter(
         new BufferedWriter(new FileWriter(m_testFileName)), false);
 
     m_recordedScenarioFileName = System.getProperty (FILENAME_V2_PROPERTY,
       "RecordedScenario") + ".py";
     m_recordedScenarioFileWriter =
       new PrintWriter(
         new BufferedWriter(new FileWriter(m_recordedScenarioFileName)), false);
 
     final String version = GrinderBuild.getVersionString();
 
     addComment(m_scriptFileWriter, version);
     m_scriptFileWriter.println("from " +
       new File (testFileNamePrefix).getName() + " import *");
     m_scriptFileWriter.println(
       "from net.grinder.script.Grinder import grinder");
     m_scriptFileWriter.println();
     m_scriptFileWriter.println("class TestRunner:");
     m_scriptFileWriter.println(s_indent + "def __call__(self):");
 
     addComment(m_testFileWriter, version);
     m_testFileWriter.println("from HTTPClient import NVPair");
     m_testFileWriter.println(
       "from net.grinder.plugin.http import HTTPRequest");
     m_testFileWriter.println("from net.grinder.script import Test");
     m_testFileWriter.println();
     m_testFileWriter.println("tests = {}");
 
     addComment(m_recordedScenarioFileWriter, version);
     m_recordedScenarioFileWriter.println();
     m_recordedScenarioFileWriter.println("from HTTPClient import NVPair");
     m_recordedScenarioFileWriter.println(
       "from net.grinder.script.Grinder import grinder");
     m_recordedScenarioFileWriter.println("from net.grinder.script import Test");
     m_recordedScenarioFileWriter.println("from net.grinder.plugin.http " +
       "import HTTPRequest");
     m_recordedScenarioFileWriter.println("from WebUtils " +
       "import extractJSESSION");
     m_recordedScenarioFileWriter.println();
     m_recordedScenarioFileWriter.println("headers1 = ( NVPair('Accept', " +
       "'text/html, image/png, image/jpeg, image/gif, image/x-xbitmap, */*" +
       ";q=0.1'), ");
     m_recordedScenarioFileWriter.println(s_indent +
       "NVPair('Accept-Charset', 'windows-1252, utf-8, utf-16, iso-8859-1;" +
       "q=0.6, *;q=0.1'), ");
     m_recordedScenarioFileWriter.println(s_indent +
       "NVPair('Accept-Encoding', 'deflate, gzip, x-gzip, identity, *;q=0'), ");
     m_recordedScenarioFileWriter.println(s_indent +
       "NVPair('Accept-Language', 'en;q=1.0'), ");
     m_recordedScenarioFileWriter.println(s_indent +
       "NVPair('User-Agent', '" + s_userAgent + "'), )");
     m_recordedScenarioFileWriter.println();
     m_recordedScenarioFileWriter.println("request = " +
       "HTTPRequest(headers = headers1)");
     m_recordedScenarioFileWriter.println();
     m_recordedScenarioFileWriter.println("class TestRunner:");
     m_recordedScenarioFileWriter.println();
     m_recordedScenarioFileWriter.println(s_indent + "# jsession value");
     m_recordedScenarioFileWriter.println(s_indent + "jsess = ''");
 
     m_out.println("\nScript will be generated to the files:\n'" +
                   m_scriptFileName + "' and '" + m_testFileName +
                   "' (old version)\nand '" + m_recordedScenarioFileName +
                   "' (new version)\n");
     m_out.flush();
   }
 
   /**
    * Called when any response activity is detected. Because the test
    * script is a single thread of control we need to calculate the
    * sleep deltas using the last time any activity occurred on any
    * connection.
    */
   public static final synchronized void markLastResponseTime() {
     s_lastResponseTime = System.currentTimeMillis();
   }
 
   /**
    * The main handler method called by the sniffer engine.
    *
    * <p>NOTE, this is called for message fragments; don't assume
    * that its passed a complete HTTP message at a time.</p>
    *
    * @param connectionDetails The TCP connection.
    * @param buffer The message fragment buffer.
    * @param bytesRead The number of bytes of buffer to process.
    * @return Filters can optionally return a <code>byte[]</code> which
    * will be transmitted to the server instead of <code>buffer</code>.
    * @exception IOException if an error occurs
    */
   public byte[] handle(ConnectionDetails connectionDetails, byte[] buffer,
                        int bytesRead) throws IOException {
     getHandler(connectionDetails).handle(buffer, bytesRead);
     return null;
   }
 
   /**
    * A connection has been opened.
    *
    * @param connectionDetails a <code>ConnectionDetails</code> value
    */
   public void connectionOpened(ConnectionDetails connectionDetails) {
     getHandler(connectionDetails);
   }
 
   /**
    * A connection has been closed.
    *
    * @param connectionDetails a <code>ConnectionDetails</code> value
    * @exception IOException if an error occurs
    */
   public void connectionClosed(ConnectionDetails connectionDetails)
     throws IOException {
     removeHandler(connectionDetails).endMessage();
   }
 
   /**
    * Called just before stop.
    */
   public final void stop() {
     final StringBuffer recordedScenarioOutput = new StringBuffer();
 
     if (getPageNumber(true) >= 0) {
       appendNewLineAndIndent(recordedScenarioOutput, 2);
       recordedScenarioOutput.append("return HtmlResult");
 
       recordedScenarioOutput.append(s_newLine);
       appendNewLineAndIndent(recordedScenarioOutput, 1);
       recordedScenarioOutput.append("def __init__(self):");
       appendNewLineAndIndent(recordedScenarioOutput, 2);
       for (int i = m_initialPageNumber; i <= getPageNumber(false); i++) {
         recordedScenarioOutput.append("self.page");
         recordedScenarioOutput.append(i);
         recordedScenarioOutput.append("Test = Test(");
         recordedScenarioOutput.append(i);
         recordedScenarioOutput.append(", 'Page");
         recordedScenarioOutput.append(i);
         recordedScenarioOutput.append("').wrap(self.page");
         recordedScenarioOutput.append(i);
         recordedScenarioOutput.append(")");
         appendNewLineAndIndent(recordedScenarioOutput, 2);
       }
 
       recordedScenarioOutput.append(s_newLine);
       appendNewLineAndIndent(recordedScenarioOutput, 1);
       recordedScenarioOutput.append("def __call__(self):");
       appendNewLineAndIndent(recordedScenarioOutput, 2);
 
       for (int i = m_initialPageNumber; i <= getPageNumber(false); i++) {
         // sleep times bigger than 10 ms
         if (s_sleepTimes[i - m_initialPageNumber] > 0) {
           recordedScenarioOutput.append("grinder.sleep(");
           recordedScenarioOutput.append(s_sleepTimes[i - m_initialPageNumber]);
           recordedScenarioOutput.append(")");
           appendNewLineAndIndent(recordedScenarioOutput, 2);
         }
         recordedScenarioOutput.append("self.page");
         recordedScenarioOutput.append(i);
         recordedScenarioOutput.append("Test()");
         appendNewLineAndIndent(recordedScenarioOutput, 2);
       }
 
       m_recordedScenarioFileWriter.print(recordedScenarioOutput.toString());
       m_recordedScenarioFileWriter.flush();
     }
   }
 
   /**
    * add a comment.
    *
    * @return void
    */
   private void addComment(PrintWriter out, String version) {
     out.println("#");
     out.println("# The Grinder version " + version);
     out.println("#");
     out.println("# Script recorded by the TCPProxy at " +
       DateFormat.getDateTimeInstance().format(Calendar.getInstance()
       .getTime()));
     out.println("#");
     out.println();
   }
 
   /**
    * extracts tokens (separator ;).
    *
    * @param src
    * @return String[]
    */
   private String[] extractResources(String src) {
     if (src == null) {
       return null;
     }
     final StringTokenizer st = new StringTokenizer(src, ";");
     final String[] result = new String[st.countTokens()];
     int i = 0;
     while (st.hasMoreTokens()) {
       String value = st.nextToken();
       if (value.trim().length() > 0) {
         result[i] = value;
         i++;
       }
     }
     return result;
   }
 
   /**
    * display a debug information.
    *
    * @param message message to display
    * @return void
    */
   private void debug(String message) {
     if ("on".equals(s_debug)) {
       m_out.println("# DEBUG: " + message);
       m_out.flush();
     }
   }
 
   private Handler getHandler(ConnectionDetails connectionDetails) {
     synchronized (m_handlers) {
       final Handler oldHandler =
         (Handler)m_handlers.get(connectionDetails);
 
       if (oldHandler != null) {
         return oldHandler;
       }
       else {
         final Handler newHandler = new Handler(connectionDetails);
         m_handlers.put(connectionDetails, newHandler);
         return newHandler;
       }
     }
   }
 
   private Handler removeHandler(ConnectionDetails connectionDetails) {
     final Handler handler;
 
     synchronized (m_handlers) {
       handler = (Handler)m_handlers.remove(connectionDetails);
     }
 
     if (handler == null) {
       throw new IllegalArgumentException(
         "Unknown connection " + connectionDetails);
     }
 
     return handler;
   }
 
   private synchronized int getRequestNumber() {
     return m_currentRequestNumber;
   }
 
   private synchronized int incrementRequestNumber() {
     return ++m_currentRequestNumber;
   }
 
   private synchronized int getPageNumber(boolean useOffset) {
     if (useOffset) {
       return m_currentPageNumber - m_initialPageNumber;
     }
     else {
       return m_currentPageNumber;
     }
   }
 
   private synchronized int incrementPageNumber() {
     return ++m_currentPageNumber;
   }
 
   private synchronized boolean isDiscardingAssociatedResources () {
     return m_discardingAssociatedResources;
   }
 
   private synchronized void setDiscardingAssociatedResources (boolean value) {
     this.m_discardingAssociatedResources = value;
   }
 
   /**
    * Factory for regular expression patterns that match HTTP headers.
    *
    * @param headerName a <code>String</code> value
    * @return The expression.
    */
   private String getHeaderExpression(String headerName) {
     return "^" + headerName + ":[ \\t]*(.*)\\r?$";
   }
 
   /**
    * Class that handles a particular connection.
    *
    * <p>Multithreaded calls for a given connection are
    * serialised.</p>
    **/
   private final class Handler {
     private final ConnectionDetails m_connectionDetails;
 
     // Parse state.
     private boolean m_parsingHeaders = false;
     private boolean m_handlingBody = false;
 
     // Parse data.
     private long m_time;
     private String m_method = null;
     private String m_url = null;
     private String m_queryString = null;
     private final List m_headers = new ArrayList();
     private int m_contentLength = -1;
     private String m_contentType = null;
     private final ByteArrayOutputStream m_entityBodyByteStream =
       new ByteArrayOutputStream();
 
     private final Perl5Matcher m_matcher = new Perl5Matcher();
 
     public Handler(ConnectionDetails connectionDetails) {
       m_connectionDetails = connectionDetails;
 
       /*
         m_out.print(s_newLine + s_indent + s_indent +
         "# New connection: " +
         connectionDetails.getDescription());
       */
     }
 
     public synchronized void handle(byte[] buffer, int length)
       throws IOException {
 
       // String used to parse headers - header names are US-ASCII
       // encoded and anchored to start of line.
       final String asciiString = new String(buffer, 0, length, "US-ASCII");
 
       debug("HTTP Request/Response :" + asciiString);
 
       if (m_matcher.contains(asciiString, m_requestLinePattern)) {
         // Packet is start of new request message.
 
         final MatchResult matchResult = m_matcher.getMatch();
 
         // Grab match results because endMessage plays with
         // out Matcher.
         final String newMethod = matchResult.group(1);
         final String newURL = matchResult.group(2);
         final String newQueryString = matchResult.group(3);
 
         endMessage();
 
         m_method = newMethod;
         m_parsingHeaders = true;
 
         if (m_method.equals("GET") ||
             m_method.equals("HEAD")) {
           m_handlingBody = false;
           m_url = newURL;
           m_queryString = newQueryString == null ? "" : newQueryString;
         }
         else if (m_method.equals("DELETE") ||
                  m_method.equals("TRACE")) {
           m_handlingBody = false;
 
           if (newQueryString != null) {
             m_url = newURL + newQueryString;
            m_queryString = "";
           }
           else {
             m_url = newURL;
           }
         }
         else if (m_method.equals("OPTIONS") ||
                  m_method.equals("PUT") ||
                  m_method.equals("POST")) {
           m_handlingBody = true;
           m_entityBodyByteStream.reset();
           m_contentLength = -1;
           m_contentType = null;
 
           if (newQueryString != null) {
             m_url = newURL + newQueryString;
            m_queryString = "";
           }
           else {
             m_url = newURL;
           }
         }
         else {
           warn("Ignoring '" + m_method + "' from " + m_connectionDetails);
           return;
         }
 
         if (!m_url.startsWith("http")) {
           // Relative URL given, calculate absolute URL.
           m_url = m_connectionDetails.getURLBase("http") + m_url;
         }
 
         // Stuff we do at start of request only.
         m_time = s_lastResponseTime > 0 ?
           System.currentTimeMillis() - s_lastResponseTime : 0;
 
         m_headers.clear();
       }
 
       // Stuff we do whatever.
 
       if (m_parsingHeaders) {
         for (int i = 0; i < s_mirroredHeaders.length; i++) {
           if (m_matcher.contains(asciiString,
                                  m_mirroredHeaderPatterns[i])) {
 
             m_headers.add(
               new NVPair(s_mirroredHeaders[i],
                          m_matcher.getMatch().group(1).trim()));
           }
         }
         /*
           if (m_matcher.contains(asciiString,
           m_basicAuthorizationHeaderPattern)) {
 
           final String decoded =
           Codecs.base64Decode(
           m_matcher.getMatch().group(1).trim());
 
           final int colon = decoded.indexOf(":");
 
           if (colon < 0) {
           warn("Could not decode Authorization header");
           }
           else {
           outputProperty(
           "parameter.basicAuthenticationUser",
           decoded.substring(0, colon));
 
           outputProperty(
           "parameter.basicAuthenticationPassword",
           decoded.substring(colon+1));
 
           outputProperty(
           "parameter.basicAuthenticationRealm",
           HTTPPluginTCPProxyResponseFilter.
           getLastAuthenticationRealm());
           }
           }
         */
 
         if (m_handlingBody) {
           // Look for the content length and type in the header.
           if (m_matcher.contains(asciiString,
                                  m_contentLengthPattern)) {
             m_contentLength =
               Integer.parseInt(
                 m_matcher.getMatch().group(1).trim());
           }
 
           if (m_matcher.contains(asciiString,
                                  m_contentTypePattern)) {
             m_contentType = m_matcher.getMatch().group(1).trim();
             m_headers.add(
               new NVPair("Content-Type", m_contentType));
           }
 
           if (m_matcher.contains(asciiString,
                                  m_messageBodyPattern)) {
 
             m_parsingHeaders = false;
             final MatchResult matchResult = m_matcher.getMatch();
             final int beginOffset = matchResult.beginOffset(1);
             final int endOffset = matchResult.endOffset(1);
             addToEntityBody(buffer, beginOffset, endOffset - beginOffset);
           }
         }
       }
       else {
         if (m_handlingBody) {
           addToEntityBody(buffer, 0, length);
         }
         else {
           warn("UNEXPECTED - Not parsing headers or handling POST");
         }
       }
     }
 
     private void addToEntityBody(byte[] bytes, int start, int length)
       throws IOException {
 
       int l = length;
 
       if (m_contentLength != -1 &&
           l > m_contentLength - m_entityBodyByteStream.size()) {
 
         warn("Expected content length exceeded, truncating to content length");
         l = m_contentLength - m_entityBodyByteStream.size();
       }
 
       m_entityBodyByteStream.write(bytes, start, l);
 
       // We flush our entity data output now if we've reached the
       // specified Content-Length. If no contentLength was specified
       // we rely on next message or connection close event to flush
       // the data.
       if (m_contentLength != -1 &&
           m_entityBodyByteStream.size() >= m_contentLength) {
 
         endMessage();
       }
     }
 
     /**
      * display a warning information.
      *
      * @param message message to display
      * @return void
      */
     private void warn(String message) {
       m_out.println("# WARNING: " + message);
       m_out.flush();
     }
 
     /**
      * display a debug information.
      *
      * @param message message to display
      * @return void
      */
     private void debug(String message) {
       if ("on".equals(s_debug)) {
         m_out.println("# DEBUG: " + message);
         m_out.flush();
       }
     }
 
     /**
      * checks if a string ends with one of the suffix in the string array.
      *
      * @param url
      * @param suffixes
      * @return boolean
      * @author ave bertrand
      */
     private boolean endsWith(String url, String[] suffixes) {
       boolean result = false;
 
       if (suffixes == null) {
         return result;
       }
       for (int i = 0; i < suffixes.length; i++) {
         if (m_url.endsWith(suffixes[i])) {
           result = true;
           break;
         }
       }
       return result;
     }
 
     private synchronized void generateRecordedScenario(
                                           boolean isNewPage,
                                           boolean parsedFormData,
                                           StringBuffer formDataOutput,
                                           StringBuffer queryStringOutput,
                                           String contentTypeValue)
                                           throws IOException {
       final String dataParameter = "data" + getPageNumber(false);
       final String fileName = dataParameter + ".dat";
       final StringBuffer recordedScenarioOutput = new StringBuffer();
 
       debug("URL: [" + m_url + "]");
       debug("QueryString: [" + m_queryString + "]");
       debug ("Main page/Resource: " + isNewPage);
 
       final int remainder = (getPageNumber(true) + 1) % s_length;
       if (remainder == 0) {
         // need to extend array
         final int result = (getPageNumber(true) + 1) / s_length;
         final long[] tmp = new long[s_length * result];
         System.arraycopy(s_sleepTimes, 0, tmp, 0, s_length * result);
 
         s_sleepTimes = new long[s_length * (result + 1)];
         System.arraycopy(tmp, 0, s_sleepTimes, 0, s_length * result);
       }
 
       if (isNewPage) {
         if (getPageNumber(false) > m_initialPageNumber) {
           appendNewLineAndIndent(recordedScenarioOutput, 2);
           recordedScenarioOutput.append("return HtmlResult");
         }
         recordedScenarioOutput.append(s_newLine);
         recordedScenarioOutput.append(s_newLine);
 
         appendNewLineAndIndent(recordedScenarioOutput, 1);
         recordedScenarioOutput.append("def page");
         recordedScenarioOutput.append(getPageNumber(false));
         recordedScenarioOutput.append("(self):");
         appendNewLineAndIndent(recordedScenarioOutput, 2);
         if (isNewPage) {
           if (m_time > 10) {
             s_sleepTimes[getPageNumber(true)] = s_sleepTime > 0 ? s_sleepTime :
               m_time;
           }
           else {
             s_sleepTimes[getPageNumber(true)] = 0;
           }
         }
 
         if (m_matcher.contains(m_url + m_queryString, m_searchedPagePattern)) {
           m_extractJsession = true;
         }
         else {
           m_extractJsession = false;
         }
 
         if (m_handlingBody && !parsedFormData) {
           final byte[] bytes = m_entityBodyByteStream.toByteArray();
 
           recordedScenarioOutput.append("request.addHeader ");
           recordedScenarioOutput.append("('Content-Type', '");
           recordedScenarioOutput.append(contentTypeValue);
           recordedScenarioOutput.append("')");
           appendNewLineAndIndent(recordedScenarioOutput, 2);
 
           if (bytes.length > 0x400) {
             final FileOutputStream dataStream = new FileOutputStream(fileName);
             dataStream.write(bytes, 0, bytes.length);
             dataStream.close();
 
             recordedScenarioOutput.append("request.setDataFromFile('");
             recordedScenarioOutput.append(fileName);
             recordedScenarioOutput.append("')");
             appendNewLineAndIndent(recordedScenarioOutput, 2);
           }
           else {
             recordedScenarioOutput.append(dataParameter);
             recordedScenarioOutput.append(" = ( ");
 
             for (int i = 0; i < bytes.length; ++i) {
               final int x = bytes[i] < 0 ? (int) bytes[i] + 0x100 :
                 (int) bytes[i];
               recordedScenarioOutput.append(Integer.toString(x));
               recordedScenarioOutput.append(", ");
             }
 
             recordedScenarioOutput.append(")");
             appendNewLineAndIndent(recordedScenarioOutput, 2);
 
             formDataOutput.append(", ");
             formDataOutput.append(dataParameter);
           }
         }
 
         recordedScenarioOutput.append("HtmlResult = request.");
         recordedScenarioOutput.append(m_method);
         recordedScenarioOutput.append("('");
 
         final int pos = m_url.indexOf(s_cookieName + "=");
         boolean addFormDataOutput = true;
         if (pos != -1) {
           final int pos2 = m_url.indexOf("?", pos);
           if ((pos2 != -1) && (!"".equals (m_url.substring(pos2)))) {
             m_url = m_url.substring(0, pos + s_cookieName.length() + 1) +
               "' + self.jsess + '" + m_url.substring(pos2);
           }
           else {
             m_url = m_url.substring(0, pos + s_cookieName.length() + 1) +
               "' + self.jsess";
             addFormDataOutput = false;
           }
         }
         recordedScenarioOutput.append(m_url);
         recordedScenarioOutput.append(queryStringOutput);
         if (addFormDataOutput) {
           recordedScenarioOutput.append(formDataOutput);
         }
         recordedScenarioOutput.append(")");
 
         if (m_extractJsession) {
           appendNewLineAndIndent(recordedScenarioOutput, 2);
           recordedScenarioOutput.append("self.jsess = extractJSESSION ('");
           recordedScenarioOutput.append(s_cookieName);
           recordedScenarioOutput.append("', HtmlResult, grinder, 0)");
         }
 
         if (m_handlingBody && !parsedFormData) {
           appendNewLineAndIndent(recordedScenarioOutput, 2);
           recordedScenarioOutput.append(
             "request.deleteHeader ('Content-Type')");
         }
       }
       else {
         // ressource case
         // add ressource request to the current generated function
         // we do not generate sleep time for ressource only between main pages
         appendNewLineAndIndent(recordedScenarioOutput, 2);
         recordedScenarioOutput.append("request.");
         recordedScenarioOutput.append(m_method);
         recordedScenarioOutput.append("('");
         recordedScenarioOutput.append(m_url);
         recordedScenarioOutput.append("')");
       }
       m_recordedScenarioFileWriter.print(recordedScenarioOutput.toString());
       m_recordedScenarioFileWriter.flush();
     }
 
     private synchronized boolean parseBody (StringBuffer testOutput,
                                             StringBuffer scriptOutput,
                                             StringBuffer formDataOutput,
                                             int requestNumber,
                                             String requestVariable)
                                             throws IOException {
       final String dataParameter = "data" + requestNumber;
       final String fileName = dataParameter + ".dat";
       boolean parsedFormData = false;
 
       if ("application/x-www-form-urlencoded".equals(m_contentType)) {
         try {
           final String nameValueString =
             parseNameValueString(m_entityBodyByteStream.toString("US-ASCII"),
                                  4, false);
 
           parsedFormData = true;
 
           scriptOutput.append(",");
           appendNewLineAndIndent(scriptOutput, 3);
           scriptOutput.append("  ( ");
           scriptOutput.append(nameValueString);
           scriptOutput.append(")");
 
           formDataOutput.append(",");
           appendNewLineAndIndent(formDataOutput, 3);
           formDataOutput.append("( ");
           formDataOutput.append(nameValueString);
           formDataOutput.append(")");
         }
         catch (ParseException e) {
           // Failed to parse form data as name-value pairs, we'll
           // treat it as raw data instead.
         }
       }
 
       if (!parsedFormData) {
 
         testOutput.append(s_newLine);
 
         final byte[] bytes = m_entityBodyByteStream.toByteArray();
 
         if (bytes.length > 0x400) {
 
           // Large amount of data, use a file.
           final FileOutputStream dataStream = new FileOutputStream(fileName);
           dataStream.write(bytes, 0, bytes.length);
           dataStream.close();
 
           testOutput.append(requestVariable);
           testOutput.append(".setDataFromFile('");
           testOutput.append(fileName);
           testOutput.append("')");
           testOutput.append(s_newLine);
         }
         else {
           testOutput.append(dataParameter);
           testOutput.append(" = ( ");
 
           for (int i = 0; i < bytes.length; ++i) {
             final int x =
               bytes[i] < 0 ? (int)bytes[i] + 0x100 : (int)bytes[i];
 
             testOutput.append(Integer.toString(x));
             testOutput.append(", ");
           }
 
           testOutput.append(")");
 
           scriptOutput.append(", ");
           scriptOutput.append(dataParameter);
         }
       }
       return parsedFormData;
     }
 
     /**
      * Called when end of message is reached.
      */
     public synchronized void endMessage() throws IOException {
       if (m_method == null) {
         return;
       }
 
       debug("URL: " + m_url);
       boolean isNewPage = false;
       if (endsWith(m_url, s_resource)) {
         // exclude resources (gif, css, js, ...)
         if ("on".equals(s_excludeResource)) {
           return;
         }
         isNewPage = false;
       }
       else {
         isNewPage = true;
         setDiscardingAssociatedResources (false);
       }
 
       final StringBuffer scriptOutput = new StringBuffer();
       final StringBuffer testOutput = new StringBuffer();
       final StringBuffer formDataOutput = new StringBuffer();
       final StringBuffer queryStringOutput = new StringBuffer();
 
       if (m_time > 10) {
         appendNewLineAndIndent(scriptOutput, 2);
         scriptOutput.append("grinder.sleep(");
         scriptOutput.append(Long.toString(m_time));
         scriptOutput.append(")");
       }
 
       testOutput.append(s_newLine);
 
       final int requestNumber = incrementRequestNumber();
       final String requestVariable = "request" + requestNumber;
 
       // Assigned below to any new headers, if any.
       String newHeaderString = null;
       String newHeaderVariable = null;
       String contentTypeValue = "";
 
       final String headerAssignment;
 
       if (m_headers.size() > 0) {
         final StringBuffer headerStringBuffer = new StringBuffer();
 
         final Iterator iterator = m_headers.iterator();
         boolean first = true;
 
         while (iterator.hasNext()) {
           final NVPair entry = (NVPair)iterator.next();
           // save Content-Type for later use
           if ("Content-Type".equals(entry.getName())) {
             contentTypeValue = entry.getValue();
           }
 
           if (!first) {
             appendNewLineAndIndent(headerStringBuffer, 3);
           }
           else {
             first = false;
           }
 
           appendNVPair(headerStringBuffer, entry, false);
         }
 
         final String headerString = headerStringBuffer.toString();
         String headerVariable = (String)m_previousHeaders.get(headerString);
 
         if (headerVariable == null) {
           headerVariable = "headers" + requestNumber;
 
           testOutput.append(s_newLine);
           testOutput.append(headerVariable);
           testOutput.append(" = ( ");
           testOutput.append(headerString);
           testOutput.append(")");
 
           // We have new headers, but don't add them to
           // m_previousHeaders until we've flushed the header array to
           // the script. Otherwise there's a race condition where
           // another Handler can refer to the header array before its
           // declared.
           newHeaderString = headerString;
           newHeaderVariable = headerVariable;
         }
 
         headerAssignment = "headers = " + headerVariable;
       }
       else {
         headerAssignment = "";
       }
 
       testOutput.append(s_newLine);
       testOutput.append(requestVariable);
       testOutput.append(" = HTTPRequest(");
       testOutput.append(headerAssignment);
       testOutput.append(")");
 
       // Base default description on method and URL.
       final String description;
 
       if (m_matcher.contains(m_url, m_lastURLPathElementPattern)) {
         description = m_method + " " + m_matcher.getMatch().group(1);
       }
       else {
         description = m_method;
       }
 
       testOutput.append(s_newLine);
       testOutput.append("tests[");
       testOutput.append(requestNumber);
       testOutput.append("] = Test(");
       testOutput.append(requestNumber);
       testOutput.append(", '");
       testOutput.append(description);
       testOutput.append("').wrap(request");
       testOutput.append(requestNumber);
       testOutput.append(")");
 
       testOutput.append(s_newLine);
 
       appendNewLineAndIndent(scriptOutput, 2);
       scriptOutput.append("tests[");
       scriptOutput.append(Integer.toString(requestNumber));
       scriptOutput.append("].");
       scriptOutput.append(m_method);
       scriptOutput.append("('");
       scriptOutput.append(m_url);
 
       if (m_queryString.length() > 1) {
 
         try {
           int pos = m_queryString.indexOf("?");
           String otherQueryStringAsNameValuePairs = null;
           if (pos != -1 && m_queryString.length() > pos + 1) {
             otherQueryStringAsNameValuePairs =
               parseNameValueString(m_queryString.substring(pos + 1), 3, true);
           }
 
           String queryStringAsNameValuePairs =
             parseNameValueString(m_queryString.substring(1), 3, false);
 
           scriptOutput.append("'");
           scriptOutput.append(",");
           appendNewLineAndIndent(scriptOutput, 3);
           scriptOutput.append("  ( ");
           scriptOutput.append(queryStringAsNameValuePairs);
           scriptOutput.append(")");
           queryStringOutput.append("'");
 
           queryStringOutput.append(",");
           appendNewLineAndIndent(queryStringOutput, 2);
           queryStringOutput.append("( ");
           if (pos == -1) {
             pos = m_queryString.length();
           }
           if (pos > 1) {
             queryStringAsNameValuePairs =
               parseNameValueString(m_queryString.substring(1, pos), 3, true);
           }
           queryStringOutput.append(queryStringAsNameValuePairs);
           if (otherQueryStringAsNameValuePairs != null) {
             appendNewLineAndIndent(queryStringOutput, 3);
             queryStringOutput.append(otherQueryStringAsNameValuePairs);
           }
           queryStringOutput.append(")");
         }
         catch (ParseException e) {
           // Failed to split query string into name-value pairs. Oh
           // well, bolt it back onto the URL.
           debug("Failed to parse this queryString=[" +
                 m_queryString.substring(1) + "]");
 
           scriptOutput.append(m_queryString);
           scriptOutput.append("'");
 
           queryStringOutput.append(m_queryString);
           queryStringOutput.append("'");
         }
       }
       else {
         scriptOutput.append("'");
         formDataOutput.append("'");
       }
 
       boolean parsedFormData = false;
 
       if (!m_parsingHeaders && m_handlingBody) {
         parsedFormData = parseBody (testOutput,
                                     scriptOutput,
                                     formDataOutput,
                                     requestNumber,
                                     requestVariable);
       }
 
       scriptOutput.append(")");
       scriptOutput.append(s_newLine);
 
       int i = 0;
       boolean discardThisPage = false;
       while (i < DISCARD_PATTERN_LENGTH && m_discardPattern[i] != null) {
         if (m_matcher.contains (m_url + m_queryString, m_discardPattern[i])) {
           discardThisPage = true;
           setDiscardingAssociatedResources (true);
           break;
         }
         i++;
       }
       if (!discardThisPage && !isDiscardingAssociatedResources()) {
         int pageNumber;
         if (isNewPage) {
           pageNumber = incrementPageNumber();
         }
         generateRecordedScenario (isNewPage,
                                   parsedFormData,
                                   formDataOutput,
                                   queryStringOutput,
                                   contentTypeValue);
       }
 
       m_scriptFileWriter.print(scriptOutput.toString());
       m_scriptFileWriter.flush();
 
       m_testFileWriter.print(testOutput.toString());
       m_testFileWriter.flush();
 
       if (newHeaderString != null) {
         // We created a new header array, stash for use by other
         // Handlers.
         m_previousHeaders.put(newHeaderString, newHeaderVariable);
       }
 
       m_method = null;
     }
   }
 
   /**
    *
    * add a new line to a buffer and indents next line.
    *
    * @param resultBuffer buffer
    * @param indentLevel number of indentations
    * @return void
    */
   private void appendNewLineAndIndent(StringBuffer resultBuffer,
                                       int indentLevel) {
     resultBuffer.append(s_newLine);
 
     for (int k = 0; k < indentLevel; ++k) {
       resultBuffer.append(s_indent);
     }
   }
   /**
    * create a list of NVPair from an input (querystring),
    * append each element of this list into a buffer
    * and return this buffer.
    *
    * @param input a querystring
    * @param indentLevel
    * @param handleSessionId
    * @return String
    * @throws IOException
    * @throws ParseException if unable to parse input
    */
   private String parseNameValueString(String input, int indentLevel,
     boolean handleSessionId) throws IOException, ParseException {
 
     final StringBuffer result = new StringBuffer();
 
     final NVPair[] pairs = Codecs.query2nv(input);
 
     for (int i = 0; i < pairs.length; ++i) {
       if (i != 0) {
         appendNewLineAndIndent(result, indentLevel);
       }
 
       appendNVPair(result, pairs[i], handleSessionId);
     }
 
     return result.toString();
   }
 
   /**
    * add "NVPair (name, value), " to a buffer.
    *
    * @param resultBuffer buffer
    * @param pair couple (name, value) to add
    * @param boolean handleSessionId
    * @return void
    */
   private void appendNVPair(StringBuffer resultBuffer, NVPair pair,
     boolean handleSessionId) {
     resultBuffer.append("NVPair(");
     quoteForScript(resultBuffer, pair.getName());
     resultBuffer.append(", ");
 
     if (handleSessionId && pair.getName().equals(s_cookieName)) {
       resultBuffer.append("self.jsess");
     }
     else if (pair.getName().equals("User-Agent")) {
       quoteForScript(resultBuffer, s_userAgent == null ?
         USERAGENT_DEFAULT_VALUE : s_userAgent);
     }
     else {
       quoteForScript(resultBuffer, pair.getValue());
     }
     resultBuffer.append("), ");
   }
 
   private void quoteForScript(StringBuffer resultBuffer, String value) {
     final String quotes = value.indexOf("\n") > -1 ? "'''" : "'";
 
     resultBuffer.append(quotes);
 
     final int length = value.length();
 
     for (int i = 0; i < length; ++i) {
       final char c = value.charAt(i);
 
       switch (c) {
       case '\'':
       case '\\':
         resultBuffer.append('\\');
         // fall through.
       default:
         resultBuffer.append(c);
       }
     }
 
     resultBuffer.append(quotes);
   }
 }
