 package ch.rollis.emma.request;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 /**
  * HTTP protocol parser.
  * <p>
  * Reads in data from an input stream and tries to setup a valid request object.
  * HTTP protocol versions 0.9, 1.0 and 1.1 are supported.
  * 
  * @author mrolli
  */
 public class HttpProtocolParser {
     /**
      * Input stream decorated by a BufferedReader.
      */
     private final BufferedReader reader;
 
     /**
      * Default character buffer size.
      */
     private static final int CHAR_BUFFER_SIZE = 1024;
 
     /**
      * The request object built while parsing.
      */
     private Request request;
 
     /**
      * Major version number of HTTP protocol version.
      */
     private int majorVersion = 0;
 
     /**
      * Minor version number of HTTP protocol version.
      */
     private int minorVersion = 9;
 
     /**
      * Constant representing a single space.
      */
     private static final String SP = " ";
 
     /**
      * Constant representing a horizontal tab.
      */
     private static final String HT = "\t";
 
     /**
      * Constant representing a end of line character enforced by rfc1945.
      */
     private static final String CRLF = "\r\n";
 
     /**
      * Class constructor that "installs" a HttpProtocolParser in front of an
      * input stream.
      * 
      * @param input
      *            The input stream to read in a request from
      */
     public HttpProtocolParser(final InputStream input) {
         reader = new BufferedReader(new InputStreamReader(input));
     }
 
     /**
      * Reads in from the InputStream and parses the byets received into a
      * request object. After validating the request object it is returned.
      * 
      * @return The parsed and validated request object
      * @throws HttpProtocolException
      *             In case the request does not adhere to the HTTP request
      *             specification
      * @throws IOException
      *             If reading from the input stream is not possible
      */
     public Request parse() throws HttpProtocolException, IOException {
         // reset state
         majorVersion = 0;
         minorVersion = 9;
         request = new Request();
 
         parseRequestLine();
         if (request.isFullRequest()) {
             parseHeaders();
             parseEntity();
         }
         validateRequest();
 
         return request;
     }
 
     /**
      * Reads in the request line and parses for method, request URI and protocol
      * version.
      * <p>
      * The method knows about Simple-Request and Full-Request request lines and
      * parses the protocol version into two integer fields (majorVersion and
      * minorVersion). The request URI is parsed using a java.net.URI object.
      * 
      * @throws HttpProtocolException
      *             In case the request does not contain a valid request line
      * @throws IOException
      *             If reading from the input stream is not possible
      */
     private void parseRequestLine() throws HttpProtocolException, IOException {
         String reqLine = reader.readLine();
         String[] reqParts = reqLine.split(SP + "+");
 
         if (reqParts.length == 2) {
             // Simple-Request
             try {
                 HttpMethod method = HttpMethod.valueOf(reqParts[0]);
                 // HTTP/0.9 only allows GET requests
                 if (method.compareTo(HttpMethod.GET) != 0) {
                     throw new HttpProtocolException("Illegl method " + method + ".");
                 }
                 request.setMethod(method);
             } catch (IllegalArgumentException e) {
                 throw new HttpProtocolException("Unknown method " + reqParts[0] + ".");
             }
             request.setProtocol(String.format("HTTP/" + majorVersion + "." + minorVersion));
         } else if (reqParts.length == 3) {
             // Full-Request
             try {
                 HttpMethod method = HttpMethod.valueOf(reqParts[0]);
                 request.setMethod(method);
             } catch (IllegalArgumentException e) {
                 throw new HttpProtocolException("Unknown method " + reqParts[0] + ".");
             }
             Matcher m = Pattern.compile("HTTP/(\\d+).(\\d+)").matcher(reqParts[2]);
             if (m.matches()) {
                 majorVersion = Integer.parseInt(m.group(1));
                 minorVersion = Integer.parseInt(m.group(2));
                 request.setProtocol(String.format("HTTP/" + majorVersion + "." + minorVersion));
             } else {
                 throw new HttpProtocolException("Invalid protocol " + reqParts[2] + ".");
             }
         } else {
             // Bad Request
             throw new HttpProtocolException("Malformed request line.");
         }
 
         try {
             request.setRequestURI(new URI(reqParts[1]));
         } catch (URISyntaxException e) {
             throw new HttpProtocolException("Illegal URI.", e);
         }
     }
 
     /**
      * Reads in any header fields available in the request.
      * <p>
      * The order of headers sent by the client is gracefully ignored. Any header
      * field that is neither a general header field nor a request header field
      * is stored as a entity header field. Extended header values are supported.
      * 
      * @throws HttpProtocolException
      *             In case an invalid header value is found
      * @throws IOException
      *             If reading from the input stream is not possible
      */
     private void parseHeaders() throws HttpProtocolException, IOException {
         String line = null;
         String currentHeader = null;
         String currentValue = null;
 
         while ((line = reader.readLine()) != null) {
             // do we reach end of header section of request
             if (line.equals("")) {
                 break;
             }
 
             int colonPos = line.indexOf(":");
             if (colonPos > 0) {
                 // new header found - store previous if we have one
                 if (currentHeader != null) {
                    request.setHeader(currentHeader, currentValue.trim());
                     currentHeader = null;
                     currentValue = null;
                 }
 
                 // process new current
                 currentHeader = line.substring(0, colonPos).trim();
                currentValue = line.substring(colonPos + 1);
             } else {
                 // extend header field
                 if (currentHeader == null || !(line.startsWith(SP) || line.startsWith(HT))) {
                     throw new HttpProtocolException("Invalid extended header");
                 } else {
                     currentValue = currentValue.concat(CRLF + line.trim());
                 }
             }
         }
         // !store the last as it has not been stored previously!
         if (currentHeader != null) {
            request.setHeader(currentHeader, currentValue.trim());
         }
     }
 
     /**
      * Reads in the entity contained in a request if there is one.
      * <p>
      * The method take the entity header Content-Length" into account when
      * deciding if an entity has to be parsed. If an entity is available it is
      * stored within the request object.
      * 
      * @throws IOException
      *             If reading from the input stream is not possible
      */
     private void parseEntity() throws IOException {
         String cl = request.getHeader("Content-Length");
         if (cl == null || Integer.parseInt(cl) == 0) {
             // no entity to parse
             return;
         }
 
         int length = Integer.parseInt(cl);
         StringBuilder sb = new StringBuilder();
 
         while (sb.length() < length) {
             char[] charBuffer = new char[CHAR_BUFFER_SIZE];
             reader.read(charBuffer);
             sb.append(charBuffer);
         }
         request.setEntity(sb.toString());
     }
 
     /**
      * Performs some protocol checks not related to parsing.
      * <p>
      * The following checks are performed:
      * <ul>
      * <li>A request header of type Host is expected if the request claims to
      * support HTTP/1.1</li>
      * <li>If the request is HTTP/1.0 only GET, POST and HEAD are allowed.</li>
      * </ul>
      * 
      * @throws HttpProtocolException
      *             In case the request does not adhere to HTTP protocol
      */
     private void validateRequest() throws HttpProtocolException {
         if (majorVersion >= 1 && minorVersion >= 1 && request.getHeader("Host") == null) {
             throw new HttpProtocolException("No Host header present.");
         }
 
         // check methods
         if (majorVersion >= 1) {
             if (minorVersion == 0 && !(request.isGet() || request.isPost() || request.isHead())) {
                 throw new HttpProtocolException("Invalid method for protcol "
                         + request.getProtocol());
             }
         }
     }
 }
 
