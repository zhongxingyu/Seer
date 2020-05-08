 // Copyright (C) 2005 - 2008 Philip Aston
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
 // COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.plugin.http.tcpproxyfilter;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.xmlbeans.XmlObject;
 import org.picocontainer.Disposable;
 
 import HTTPClient.ParseException;
 import HTTPClient.URI;
 
 import net.grinder.common.GrinderBuild;
 import net.grinder.common.Logger;
 import net.grinder.plugin.http.xml.BaseURIType;
 import net.grinder.plugin.http.xml.CommonHeadersType;
 import net.grinder.plugin.http.xml.HTTPRecordingType;
 import net.grinder.plugin.http.xml.HeaderType;
 import net.grinder.plugin.http.xml.HeadersType;
 import net.grinder.plugin.http.xml.HttpRecordingDocument;
 import net.grinder.plugin.http.xml.PageType;
 import net.grinder.plugin.http.xml.ParsedURIPartType;
 import net.grinder.plugin.http.xml.RelativeURIType;
 import net.grinder.plugin.http.xml.RequestType;
 import net.grinder.plugin.http.xml.ResponseType;
 import net.grinder.plugin.http.xml.TokenReferenceType;
 import net.grinder.plugin.http.xml.TokenType;
 import net.grinder.tools.tcpproxy.ConnectionDetails;
 import net.grinder.tools.tcpproxy.EndPoint;
 import net.grinder.util.URIParser;
 
 
 /**
  * Contains common state for HTTP recording.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public class HTTPRecordingImplementation implements HTTPRecording, Disposable {
 
   /**
    * Headers which are likely to have common values.
    */
   private static final Set COMMON_HEADERS = new HashSet(Arrays.asList(
     new String[] {
       "Accept",
       "Accept-Charset",
       "Accept-Encoding",
       "Accept-Language",
       "Cache-Control",
       "Referer", // Deliberate misspelling to match specification.
       "User-Agent",
     }
   ));
 
   private final HttpRecordingDocument m_recordingDocument =
     HttpRecordingDocument.Factory.newInstance();
   private final Logger m_logger;
   private final HTTPRecordingResultProcessor m_resultProcessor;
   private final RegularExpressions m_regularExpressions;
   private final URIParser m_uriParser;
 
   private final IntGenerator m_bodyFileIDGenerator = new IntGenerator();
   private final BaseURLMap m_baseURLMap = new BaseURLMap();
   private final CommonHeadersMap m_commonHeadersMap = new CommonHeadersMap();
   private final RequestList m_requestList = new RequestList();
   private final TokenMap m_tokenMap = new TokenMap();
 
   private long m_lastResponseTime = 0;
 
   /**
    * Constructor.
    *
    * @param resultProcessor
    *          Component which handles result.
    * @param logger
    *          A logger.
    * @param regularExpressions
    *          Compiled regular expressions.
    * @param uriParser
    *          A URI parser.
    */
   public HTTPRecordingImplementation(
     HTTPRecordingResultProcessor resultProcessor,
     Logger logger,
     RegularExpressions regularExpressions,
     URIParser uriParser) {
 
     m_resultProcessor = resultProcessor;
     m_logger = logger;
     m_regularExpressions = regularExpressions;
     m_uriParser = uriParser;
 
     final HTTPRecordingType.Metadata httpRecording =
       m_recordingDocument.addNewHttpRecording().addNewMetadata();
 
     httpRecording.setVersion("The Grinder " + GrinderBuild.getVersionString());
     httpRecording.setTime(Calendar.getInstance());
   }
 
   /**
    * Add a new request to the recording.
    *
    * <p>
    * The request is returned to allow the caller to add things it doesn't know
    * yet, e.g. headers, body, response.
    * </p>
    *
    * @param connectionDetails
    *          The connection used to make the request.
    * @param method
    *          The HTTP method.
    * @param relativeURI
    *          The URI.
    * @return The request.
    */
   public RequestType addRequest(
     ConnectionDetails connectionDetails, String method, String relativeURI) {
 
     final RequestType request = m_requestList.add();
     request.setTime(Calendar.getInstance());
 
     synchronized (this) {
       if (m_lastResponseTime > 0) {
         // We only want to record a sleep time for the first request after
         // a response.
         final long time = System.currentTimeMillis() - m_lastResponseTime;
 
         if (time > 10) {
           request.setSleepTime(time);
         }
       }
 
       m_lastResponseTime = 0;
     }
 
     request.addNewHeaders();
 
     request.setMethod(RequestType.Method.Enum.forString(method));
 
     String unescapedURI;
 
     try {
       unescapedURI = URI.unescape(relativeURI, null);
     }
     catch (ParseException e) {
       unescapedURI = relativeURI;
     }
 
     final Matcher lastPathElementMatcher =
       m_regularExpressions.getLastPathElementPathPattern().matcher(
         unescapedURI);
 
     final String description;
 
     if (lastPathElementMatcher.find()) {
       final String element = lastPathElementMatcher.group(1);
 
       if (element.trim().length() != 0) {
         description = method + " " + element;
       }
       else {
         description = method + " /";
       }
     }
     else {
       description = method + " " + relativeURI;
     }
 
     request.setDescription(description);
 
     final RelativeURIType uri = request.addNewUri();
 
     uri.setUnparsed(unescapedURI);
 
     uri.setExtends(
       m_baseURLMap.getBaseURL(
         connectionDetails.isSecure() ?
           BaseURIType.Scheme.HTTPS : BaseURIType.Scheme.HTTP,
         connectionDetails.getRemoteEndPoint()).getUriId());
 
     final ParsedURIPartType parsedPath = uri.addNewPath();
     final ParsedURIPartType parsedQueryString = uri.addNewQueryString();
     final String[] fragment = new String[1];
 
     // Look for tokens in path parameters and query string.
     m_uriParser.parse(relativeURI, new URIParser.AbstractParseListener() {
 
       public boolean path(String path) {
         parsedPath.addText(path);
         return true;
       }
 
       public boolean pathParameterNameValue(String name, String value) {
         setTokenReference(
           name, value, parsedPath.addNewTokenReference());
         return true;
       }
 
       public boolean queryString(String queryString) {
         parsedQueryString.addText(queryString);
         return true;
       }
 
       public boolean queryStringNameValue(String name, String value) {
         setTokenReference(
           name, value, parsedQueryString.addNewTokenReference());
         return true;
       }
 
       public boolean fragment(String theFragment) {
         fragment[0] = theFragment;
         return true;
       }
     });
 
     if (parsedQueryString.getTokenReferenceArray().length == 0 &&
         parsedQueryString.getTextArray().length ==  0) {
       uri.unsetQueryString();
     }
 
     if (fragment[0] != null) {
       uri.setFragment(fragment[0]);
     }
 
     return request;
   }
 
   /**
    * Called when a response message starts. Because the test script represents a
    * single thread of control we need to calculate the sleep deltas using the
    * last time any response was received on any connection.
    */
   public void markLastResponseTime() {
     synchronized (this) {
       m_lastResponseTime = System.currentTimeMillis();
     }
   }
 
   /**
    * Fill in token reference details, creating the token if necessary.
    *
    * <p>
    * The reference source is cached for use by
    * {@link #tokenReferenceExists(String, String)}, so it should be set before
    * this method is called.
    * </p>
    *
    * @param name
    *          The name.
    * @param value
    *          The value.
    * @param tokenReference
    *          This reference is set with the appropriate token ID, and the new
    *          value is set if appropriate.
    */
   public void setTokenReference(
     String name, String value, TokenReferenceType tokenReference) {
     m_tokenMap.add(name, value, tokenReference);
   }
 
   /**
    * Return the last value recorded for the given token.
    *
    * @param name The token name.
    * @return The last value, or <code>null</code> if no token reference
    * for this token has been seen.
    */
   public String getLastValueForToken(String name) {
     return m_tokenMap.getLastValue(name);
   }
 
   /**
    * Check for existence of token. The token must have at least one previous
    * reference with a source type of <code>source</code>.
    *
    * @param name
    *          Token name.
    * @param source
    *          Token source.
    * @return <code>true</code> if a token with name <code>name</code>
    *         exists, and has at least one reference with a source type of
    *         <code>source</code>.
    */
   public boolean tokenReferenceExists(String name, String source) {
     return m_tokenMap.exists(name, source);
   }
 
   /**
    * Create a new file name for body data.
    *
    * @return The file name.
    */
   public File createBodyDataFileName() {
     return new File("http-data-" + m_bodyFileIDGenerator.next() + ".dat");
   }
 
   /**
    * Called after the component has been stopped.
    */
   public void dispose() {
     final HttpRecordingDocument result;
 
     synchronized (m_recordingDocument) {
       result = (HttpRecordingDocument)m_recordingDocument.copy();
     }
 
     m_requestList.record(result.getHttpRecording());
 
     // Extract default headers that are present in all common headers.
     final CommonHeadersType[] commonHeaders =
       result.getHttpRecording().getCommonHeadersArray();
 
     final Map defaultHeaders = new HashMap();
     final Set notDefaultHeaders = new HashSet();
 
     for (int i = 0; i < commonHeaders.length; ++i) {
       final HeaderType[] headers = commonHeaders[i].getHeaderArray();
 
       for (int j = 0; j < headers.length; ++j) {
         final String name = headers[j].getName();
         final String value = headers[j].getValue();
 
         if (notDefaultHeaders.contains(name)) {
           continue;
         }
 
         final String existing = (String)defaultHeaders.put(name, value);
 
         if (existing != null && !value.equals(existing) ||
             existing == null && i > 0) {
           defaultHeaders.remove(name);
           notDefaultHeaders.add(name);
         }
       }
     }
 
     if (defaultHeaders.size() > 0) {
       final CommonHeadersType[] newCommonHeaders =
         new CommonHeadersType[commonHeaders.length + 1];
 
       System.arraycopy(
         commonHeaders, 0, newCommonHeaders, 1, commonHeaders.length);
 
       final String defaultHeadersID = "defaultHeaders";
       newCommonHeaders[0] = CommonHeadersType.Factory.newInstance();
       newCommonHeaders[0].setHeadersId(defaultHeadersID);
 
       final Iterator iterator = defaultHeaders.entrySet().iterator();
       while (iterator.hasNext()) {
         final Entry entry = (Entry)iterator.next();
         final HeaderType header = newCommonHeaders[0].addNewHeader();
         header.setName((String)entry.getKey());
         header.setValue((String)entry.getValue());
       }
 
       for (int i = 0; i < commonHeaders.length; ++i) {
         final HeaderType[] headers = commonHeaders[i].getHeaderArray();
         for (int j = headers.length - 1; j >= 0; --j) {
           if (defaultHeaders.containsKey(headers[j].getName())) {
             commonHeaders[i].removeHeader(j);
           }
         }
 
         commonHeaders[i].setExtends(defaultHeadersID);
       }
 
       result.getHttpRecording().setCommonHeadersArray(newCommonHeaders);
     }
 
     try {
       m_resultProcessor.process(result);
     }
     catch (IOException e) {
       m_logger.error(e.getMessage());
       e.printStackTrace(m_logger.getErrorLogWriter());
     }
   }
 
   private final class BaseURLMap {
     private final Map m_map = new HashMap();
     private final IntGenerator m_idGenerator = new IntGenerator();
 
     public BaseURIType getBaseURL(
       BaseURIType.Scheme.Enum scheme, EndPoint endPoint) {
 
       final Object key = scheme.toString() + "://" + endPoint;
 
       synchronized (m_map) {
         final BaseURIType existing = (BaseURIType)m_map.get(key);
 
         if (existing != null) {
           return existing;
         }
 
         final BaseURIType result;
 
         synchronized (m_recordingDocument) {
           result = m_recordingDocument.getHttpRecording().addNewBaseUri();
         }
 
         result.setUriId("url" + m_idGenerator.next());
         result.setScheme(scheme);
         result.setHost(endPoint.getHost());
         result.setPort(endPoint.getPort());
 
         m_map.put(key, result);
 
         return result;
       }
     }
   }
 
   private static final class CommonHeadersMap {
     private final Map m_map = new HashMap();
     private final IntGenerator m_idGenerator = new IntGenerator();
 
     public void extractCommonHeaders(
       HTTPRecordingType httpRecording, RequestType request) {
 
       final CommonHeadersType commonHeaders =
         CommonHeadersType.Factory.newInstance();
       final HeadersType newRequestHeaders = HeadersType.Factory.newInstance();
 
       final XmlObject[] children = request.getHeaders().selectPath("./*");
 
       for (int i = 0; i < children.length; ++i) {
         if (children[i] instanceof HeaderType) {
           final HeaderType header = (HeaderType)children[i];
 
           if (COMMON_HEADERS.contains(header.getName())) {
             commonHeaders.addNewHeader().set(header);
           }
           else {
             newRequestHeaders.addNewHeader().set(header);
           }
         }
         else {
           newRequestHeaders.addNewAuthorization().set(children[i]);
         }
       }
 
       // Key that ignores ID.
       final Object key =
         Arrays.asList(commonHeaders.getHeaderArray()).toString();
 
       synchronized (m_map) {
         final CommonHeadersType existing = (CommonHeadersType)m_map.get(key);
 
         if (existing != null) {
           newRequestHeaders.setExtends(existing.getHeadersId());
         }
         else {
           commonHeaders.setHeadersId("headers" + m_idGenerator.next());
 
           httpRecording.addNewCommonHeaders().set(commonHeaders);
 
           m_map.put(key, commonHeaders);
 
           newRequestHeaders.setExtends(commonHeaders.getHeadersId());
         }
       }
 
       request.setHeaders(newRequestHeaders);
     }
   }
 
   private final class RequestList {
     private final List m_requests = new ArrayList();
     private final Pattern m_resourcePathPattern = Pattern.compile(
       ".*(?:\\.css|\\.gif|\\.ico|\\.jpe?g|\\.js|\\.png)(?:\\?.*)?$",
       Pattern.CASE_INSENSITIVE);
 
     public RequestType add() {
       final RequestType request = RequestType.Factory.newInstance();
 
       synchronized (m_requests) {
         m_requests.add(request);
       }
 
       return request;
     }
 
     public void record(HTTPRecordingType httpRecording) {
       synchronized (m_requests) {
         final Iterator iterator = m_requests.iterator();
 
         String lastBaseURI = null;
         boolean lastResponseWasRedirect = false;
 
         PageType currentPage = null;
 
         while (iterator.hasNext()) {
           final RequestType request = (RequestType)iterator.next();
 
           final ResponseType response = request.getResponse();
 
           if (response == null) {
             continue;
           }
 
           m_commonHeadersMap.extractCommonHeaders(httpRecording, request);
 
           synchronized (m_recordingDocument) {
             // Crude but effective pagination heuristics.
             if (!request.getUri().getExtends().equals(lastBaseURI) ||
                 request.isSetBody() ||
                 !(m_resourcePathPattern.matcher(request.getUri().getUnparsed())
                      .matches() ||
                   lastResponseWasRedirect) ||
                 currentPage == null) {
               currentPage = httpRecording.addNewPage();
             }
 
             lastBaseURI = request.getUri().getExtends();
 
             switch (response.getStatusCode()) {
               case HttpURLConnection.HTTP_MOVED_PERM:
               case HttpURLConnection.HTTP_MOVED_TEMP:
               case 307:
                 lastResponseWasRedirect = true;
 
                 request.setAnnotation(
                   "Expecting " + response.getStatusCode() +
                   " '" + response.getReasonPhrase() + "'");
                 break;
               default:
                 lastResponseWasRedirect = false;
             }
 
             currentPage.addNewRequest().set(request);
           }
         }
       }
     }
   }
 
   /**
    * Responsible for tokens at the recording level. Generates unique token
    * names. Tracks the last value for a particular token name, allowing the
    * "newValue" attribute of token references to be set appropriately. Token
    * names are deemed to have global (recording) scope; a simple model that
    * might not be right for every use case.
    */
   private final class TokenMap {
     private final Map m_map = new HashMap();
     private final Map m_uniqueTokenIDs = new HashMap();
 
     public void add(
       String name, String value, TokenReferenceType tokenReference) {
 
       final TokenLastValuePair tokenValuePair;
 
       synchronized (m_map) {
         final TokenLastValuePair existing = (TokenLastValuePair)m_map.get(name);
 
         if (existing == null) {
           final TokenType newToken;
 
           synchronized (m_recordingDocument) {
             newToken = m_recordingDocument.getHttpRecording().addNewToken();
           }
 
           // Build a tokenID that is also a reasonable identifier.
           final StringBuffer tokenID = new StringBuffer();
           tokenID.append("token_");
 
           for (int i = 0; i < name.length(); ++i) {
             final char c = name.charAt(i);
 
            if (Character.isJavaIdentifierPart(c)) {
               tokenID.append(c);
             }
           }
 
           final String partToken = tokenID.toString();
           final Integer existingValue =
             (Integer)m_uniqueTokenIDs.get(partToken);
 
           if (existingValue != null) {
             tokenID.append(existingValue);
             m_uniqueTokenIDs.put(partToken,
                               new Integer(existingValue.intValue() + 1));
           }
           else {
             m_uniqueTokenIDs.put(partToken, new Integer(2));
           }
 
           newToken.setTokenId(tokenID.toString());
           newToken.setName(name);
 
           tokenValuePair = new TokenLastValuePair(newToken);
           m_map.put(name, tokenValuePair);
         }
         else {
           tokenValuePair = existing;
         }
       }
 
       tokenReference.setTokenId(tokenValuePair.getToken().getTokenId());
 
       if (!value.equals(tokenValuePair.getLastValue())) {
         tokenReference.setNewValue(value);
         tokenValuePair.setLastValue(value);
       }
 
       tokenValuePair.addSource(tokenReference.getSource());
     }
 
     public String getLastValue(String name) {
 
       final TokenLastValuePair existing;
 
       synchronized (m_map) {
         existing = (TokenLastValuePair)m_map.get(name);
       }
 
       return existing != null ? existing.getLastValue() : null;
     }
 
     public boolean exists(String name, String source) {
 
       final TokenLastValuePair existing;
 
       synchronized (m_map) {
         existing = (TokenLastValuePair)m_map.get(name);
       }
 
       return existing != null && existing.hasAReferenceWithSource(source);
     }
   }
 
   private static final class TokenLastValuePair {
     private final TokenType m_token;
     private final Set m_sources = new HashSet();
     private String m_lastValue;
 
     public TokenLastValuePair(TokenType token) {
       m_token = token;
     }
 
     public TokenType getToken() {
       return m_token;
     }
 
     public void setLastValue(String lastValue) {
       m_lastValue = lastValue;
     }
 
     public String getLastValue() {
       return m_lastValue;
     }
 
     public void addSource(String source) {
       m_sources.add(source);
     }
 
     public boolean hasAReferenceWithSource(String source) {
       return m_sources.contains(source);
     }
   }
 }
