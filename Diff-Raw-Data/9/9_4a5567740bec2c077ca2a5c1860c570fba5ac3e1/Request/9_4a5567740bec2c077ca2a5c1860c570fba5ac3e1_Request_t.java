 import java.io.*;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * This class represents a HTTP request.
  * @author Benjamin Russell (brr1922@rit.edu)
  */
 public class Request {
 
     // CONSTANTS ///////////////////////////////////////////////////////////
 
     /**
      * Valid request methods
      */
     enum RequestMethod {
         POST,
         GET,
         HEAD
     }
 
     public static final int MAX_HTTP_MAJOR = 1;
     public static final int MAX_HTTP_MINOR = 0;
 
     // MEMBER VARIABLES ////////////////////////////////////////////////////
     private String requestBody;
 
     private int majorVersion;
     private int minorVersion;
     private RequestMethod requestMethod;
     private String requestUri;
     private String requestQuery;
     private String host;
     private String remoteIp;
 
     private String contentType;
     private String contentLength;
 
     private SiteConfiguration siteConfiguration = null;
 
     private String requestContent;
 
     // CONSTRUCTOR /////////////////////////////////////////////////////////
 
     /**
      * Creates a request object based on the text of the HTTP request.
      * Validates the protocol version, and the request method.
      * @param requestString     The text of the HTTP request
      * @throws RequestException Thrown if the request is via an unsupported
      *                          protocol version, the method is unsupported, or
      *                          if the method line is malformed.
      */
     public Request(String requestString, String remoteIp){
         // Store the request
         this.requestBody = requestString;
         this.remoteIp = remoteIp;
     }
 
     // METHODS /////////////////////////////////////////////////////////////
 
     /**
      * Processes the request and generates are response for output
      * @return  A response that can easily be outputted to the socket
      */
     public Response process() {
         // Process the header
         try {
             processHeader();
         } catch(RequestException e) {
             return new ErrorResponse(e.getCode(), e.getMessage(), e.getFriendlyMessage(), requestUri);
         }
 
         // Make sure that the request has a configuration to use for processing
         if(this.siteConfiguration == null) {
             throw new NullPointerException("Cannot process request if site configuration is undefined.");
         }
 
         // Extract the URI based on the style of URI we received
         String uri = "";
         if(requestUri.startsWith("http://")) {
             StringTokenizer tok = new StringTokenizer(requestUri, "/", true);
             tok.nextToken();
             tok.nextToken();
             tok.nextToken();
             tok.nextToken();
             while(tok.hasMoreTokens()) {
                 uri += tok.nextToken();
             }
         } else if(requestUri.startsWith("/")) {
             uri = requestUri;
         } else {
             // It's an invalid URI
             return processError(400, "Bad Request", "The request does not match the format specified by HTTP/1.0");
         }
 
         // Break off the page to load
         StringTokenizer tok = new StringTokenizer(uri, "?");
         if(tok.countTokens() > 2) {
             // There's > 1 ? in the uri. that's invalid
             return processError(400, "Bad Request", "The request does not match the format specified by HTTP/1.0");
         }
         String page = tok.nextToken();
        Pattern queryP = Pattern.compile("(.*)\\?(.*)");
        Matcher queryM = queryP.matcher(uri);
        if(queryM.matches()) {
            requestQuery = queryM.group(2);
         }
 
         // Extract the page to load from the request uri
         try {
             // Declare the bytes for the page
             byte[] pageBytes;
             String contentType;
 
             // Grab the extension of the file
             Pattern extPat = Pattern.compile(".*\\.(.*)");
             Matcher m = extPat.matcher(page);
 
             Response r;
 
             // Determine if a CGI should be ran
             if(m.matches()) {
                 String extension = m.group(1);
                 String extensionHandler = siteConfiguration.getCgiHandler(extension);
                 if(extensionHandler != null) {
                     // CGI away!
                     r = new CgiResponse(runCgiRequest(page, extensionHandler));
                 } else {
                     // No handler for the type, load the file statically
                     r = new Response(siteConfiguration.getPage(page), 200, "OK");
                     r.contentType = siteConfiguration.getPageContentType(page);
                 }
             } else {
                 // No extension, load the file statically
                 r = new Response(siteConfiguration.getPage(page), 200, "OK");
                 r.contentType = siteConfiguration.getPageContentType(page);
             }
 
             // Set the header method if we're doing header stuff
             if(RequestMethod.HEAD.equals(requestMethod)) {
                 r.setHeadOnly(true);
             }
 
 
             return r;
 
         } catch(FileNotFoundException e) {
             return processError(404, "File Not Found", "The requested page could not be found on this server.");
         } catch(Exception e) {
             return processError(500, "Internal Server Error", "An internal server error has occurred.");
         }
     }
 
     // PRIVATE METHODS /////////////////////////////////////////////////////
 
     /**
      * Processes the header of the request internally.
      * @throws RequestException Thrown if there is a problem with the request
      */
     private void processHeader() throws RequestException {
         // Create a string reader
         Scanner requestReader = new Scanner(this.requestBody);
 
         // Make sure we at least have one line
         if(!requestReader.hasNextLine()) {
             throw new RequestException(501, "Incomplete Method", "The HTTP header provided is incomplete.");
         }
 
         // First line must be the method, request URI, and protocol version
         Pattern p = Pattern.compile("^(.*) (.*) HTTP/([0-9])\\.([0-9])$");
         Matcher m = p.matcher(requestReader.nextLine());
         if(!m.matches()) {
             throw new RequestException(501, "Incomplete Method", "The HTTP header provided is incomplete.");
         }
 
         // Store the method
         try {
             requestMethod = RequestMethod.valueOf(m.group(1));
         } catch(IllegalArgumentException e) {
             throw new RequestException(501, "Unsupported Method", "The HTTP request method is unsupported.");
         }
 
         // Store the URI
         requestUri = m.group(2);
 
         // Store the protocol
         majorVersion = Integer.parseInt(m.group(3));
         minorVersion = Integer.parseInt(m.group(4));
         if(majorVersion > MAX_HTTP_MAJOR || (majorVersion == MAX_HTTP_MAJOR && minorVersion > MAX_HTTP_MINOR)) {
             throw new RequestException(505, "HTTP Version Not Supported", "This HTTP protocol version is not supported");
         }
 
         // Read the rest of the header stuff and process what we can
         while(requestReader.hasNextLine()) {
             // Lines are always formatted: Attribute: value
             String line = requestReader.nextLine();
             StringTokenizer tok = new StringTokenizer(line, ":");
             if(line.toLowerCase().startsWith("content-type:")) {
                 tok.nextToken();
                 this.contentType = tok.nextToken().trim();
             } else if(line.toLowerCase().startsWith("content-length:")) {
                 tok.nextToken();
                 this.contentLength = tok.nextToken().trim();
             }
 
             // We reached the end of the header
             if(line.equals("\r\n")) {
                 break;
             }
         }
 
         // Read the rest of the request into the content
         while(requestReader.hasNextLine()) {
             requestContent += requestReader.nextLine();
         }
 
         if(contentLength == null && requestContent != null) {
             contentLength = (new Integer(requestContent.length())).toString();
         } else {
             contentLength = (new Integer(0)).toString();
             requestContent = "";
         }
         if(contentType == null) {
             contentType = "text/html";
         }
     }
 
     /**
      * Generates an error page for it the error. If an error handler for
      * the error is defined in the site configuration, the page for the error is
      * retrieved. If that page does not exist, the default error page is returned.
      * @param code          The HTTP code for the error that occurred.
      * @param message       The HTTP message for the error that occurred.
      * @param friendlyError A friendlier error message to be included with the error page
      * @return  An error response
      */
     private Response processError(int code, String message, String friendlyError) {
         // Grab the page for the error code, if it exists
         String path = siteConfiguration.getErrorHandlerPath(code);
         if(path == null) {
             return new ErrorResponse(code, message, friendlyError, requestUri);
         } else {
             try {
                 // Return the error handler page
                 return new Response(siteConfiguration.getPage(path), code, message);
             } catch(Exception e) {
                 friendlyError += "<br/>An additional error 404 was incurred while attempting to ";
                 friendlyError += "find the appropriate page for this error";
                 return new ErrorResponse(code, message, friendlyError, requestUri);
             }
         }
     }
 
     private String runCgiRequest(String file, String handler) {
         // Create a process builder and grab the environment variables
         ProcessBuilder p = new ProcessBuilder(handler, siteConfiguration.getRoot() + file);
         Map<String, String> env = p.environment();
 
         // Set up the environment variables
         env.put("REDIRECT_STATUS", "true");         // Required for php 5.3.3 to play nice
         env.put("CONTENT_LENGTH", contentLength);
         env.put("CONTENT_TYPE", contentType);
         env.put("GATEWAY_INTERFACE", "CGI/1.1");
         if(requestQuery != null) {
            env.put("QUERY_STRING", requestQuery);
         }
         env.put("REMOTE_ADDR", remoteIp);
         env.put("REQUEST_METHOD", requestMethod.name());
         env.put("SCRIPT_FILENAME", siteConfiguration.getRoot() + file);
         env.put("SERVER_PORT", (Integer.toString(siteConfiguration.getPort())));
         env.put("SERVER_PROTOCOL", "HTTP/" + MAX_HTTP_MAJOR + "." + MAX_HTTP_MINOR);
         env.put("SERVER_SOFTWARE", "DCN2-Web Server");
 
         // Set the working directory to the directory of the file
         File f = new File(siteConfiguration.getRoot() + file);
         p.directory(f.getParentFile());
 
         // Execute!
         try {
             // Start it up
             Process cgi = p.start();
 
             // Grab stdin and out
             OutputStream stdIn = cgi.getOutputStream();
             InputStream stdOut = cgi.getInputStream();
 
             // Dump the content to the cgi handler
             if(requestContent.length() > 0) {
                 stdIn.write(requestContent.getBytes());
                 stdIn.flush();
             }
 
             // Read in the stdOut
             StreamGobbler sg = new StreamGobbler(stdOut);
             sg.start();
             cgi.waitFor();
             return sg.getBody();
         } catch(Exception e) {
             throw new RequestException(500, "Internal Server Error", "Executing script failed.");
         }
     }
 
     // SETTERS /////////////////////////////////////////////////////////////
 
     /**
      * Sets the site configuration
      * @param config    The site configuration the request will be processed with
      * @throws NullPointerException Thrown if the configuration passed in is null
      */
     public void setSiteConfiguration(SiteConfiguration config) throws NullPointerException {
         // Verify that the site configuration is valid
         if(config == null) {
             throw new NullPointerException("Site configuration cannot be null");
         }
 
         this.siteConfiguration = config;
     }
 
     public void setRemoteIp(String ip) { this.remoteIp = ip; }
 
     // GETTERS /////////////////////////////////////////////////////////////
     public String getHost() {
         return host;
     }
 
     public String toString() {
         StringBuilder b = new StringBuilder();
         b.append("Protocol Version: ");
         b.append(majorVersion);
         b.append(".");
         b.append(minorVersion);
         b.append("\n");
         b.append("Method: ");
         b.append(requestMethod);
         b.append("\n");
         b.append("URI: ");
         b.append(requestUri);
         b.append("\n");
 
         return b.toString();
     }
 }
