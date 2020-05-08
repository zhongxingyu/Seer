 /*
  * $Id$
  *
  * Copyright 2003-2006 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.servlet.container;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.FileNameMap;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.URLConnection;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.servlet.ServletException;
 
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.PropertyConfigurator;
 import org.apache.log4j.helpers.NullEnumeration;
 import org.xins.common.Library;
 
 import org.xins.common.Log;
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.Utils;
 import org.xins.common.collections.PropertyReader;
 
 /**
  * HTTP server used to invoke the XINS servlet.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:anthony.goubard@orange-ft.com">Anthony Goubard</a>
  * @author <a href="mailto:ernst.dehaan@orange-ft.com">Ernst de Haan</a>
  */
 public class HTTPServletHandler {
 
    // TODO: This class should be cleaned up further
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Initializes the logging subsystem with fallback default settings.
     */
    private static final void configureLoggerFallback() {
       Properties settings = new Properties();
       settings.setProperty("log4j.rootLogger",                                "ALL, console");
       settings.setProperty("log4j.appender.console",                          "org.apache.log4j.ConsoleAppender");
       settings.setProperty("log4j.appender.console.layout",                   "org.apache.log4j.PatternLayout");
       settings.setProperty("log4j.appender.console.layout.ConversionPattern", "%16x %6c{1} %-6p %m%n");
       settings.setProperty("log4j.logger.org.xins.",                          "INFO");
       PropertyConfigurator.configure(settings);
    }
 
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The default port number is 8080.
     */
    public final static int DEFAULT_PORT_NUMBER = 8080;
 
    /**
     * The map containing the MIME type information. Never <code>null</code>
     */
    private final static FileNameMap MIME_TYPES_MAP = URLConnection.getFileNameMap();
 
 
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
 
    /**
     * Creates a new HTTPSevletHandler with no Servlet. Use the addServlet
     * methods to add the WAR files or the Servlets.
     *
     * @param port
     *    The port of the servlet server.
     *
     * @param daemon
     *    <code>true</code> if the thread listening to connection should be a
     *    daemon thread, <code>false</code> otherwise.
     *
     * @throws IOException
     *    if the servlet container cannot be started.
     */
    public HTTPServletHandler(int port, boolean daemon) throws IOException {
 
       // Configure log4j if not already done.
       Enumeration appenders = LogManager.getLoggerRepository().getRootLogger().getAllAppenders();
       if (appenders instanceof NullEnumeration) {
          configureLoggerFallback();
       }
 
       // Start the HTTP server.
       startServer(port, daemon);
    }
 
    /**
     * Creates a new <code>HTTPServletHandler</code>. This servlet handler
     * starts a web server on port 8080 and wait for calls from the
     * <code>XINSServiceCaller</code>.
     *
     * <p>Note that all the libraries used by this WAR file should already be
     * in the classpath.
     *
     * @param warFile
     *    the war file of the application to deploy, cannot be
     *    <code>null</code>.
     *
     * @throws ServletException
     *    if the servlet cannot be initialized.
     *
     * @throws IOException
     *    if the servlet container cannot be started.
     */
    public HTTPServletHandler(File warFile)
    throws ServletException, IOException {
       this(DEFAULT_PORT_NUMBER, true);
       addWAR(warFile, "/");
    }
 
    /**
     * Creates a new <code>HTTPSevletHandler</code>. This servlet handler
     * starts a web server on the specified port and waits for calls from the XINSServiceCaller.
     * Note that all the libraries used by this WAR file should already be in
     * the classpath.
     *
     * @param warFile
     *    the war file of the application to deploy, cannot be
     *    <code>null</code>.
     *
     * @param port
     *    The port of the servlet server.
     *
     * @param daemon
     *    <code>true</code> if the thread listening to connection should be a
     *    daemon thread, <code>false</code> otherwise.
     *
     * @throws ServletException
     *    if the servlet cannot be initialized.
     *
     * @throws IOException
     *    if the servlet container cannot be started.
     */
    public HTTPServletHandler(File warFile, int port, boolean daemon)
    throws ServletException, IOException {
       this(port, daemon);
       addWAR(warFile, "/");
    }
 
    /**
     * Creates a new HTTPSevletHandler. This Servlet handler starts a web server
     * and wait for calls from the XINSServiceCaller.
     *
     * @param servletClassName
     *    The name of the servlet's class to load, cannot be <code>null</code>.
     *
     * @throws ServletException
     *    if the servlet cannot be initialized.
     *
     * @throws IOException
     *    if the servlet container cannot be started.
     */
    public HTTPServletHandler(String servletClassName) throws ServletException, IOException {
       this(DEFAULT_PORT_NUMBER, true);
       addServlet(servletClassName, "/");
    }
 
    /**
     * Creates a new HTTPSevletHandler. This Servlet handler starts a web server
     * and wait for calls from the XINSServiceCaller.
     *
     * @param servletClassName
     *    The name of the servlet's class to load, cannot be <code>null</code>.
     *
     * @param port
     *    The port of the servlet server.
     *
     * @param daemon
     *    <code>true</code> if the thread listening to connection should be a
     *    daemon thread, <code>false</code> otherwise.
     *
     * @throws ServletException
     *    if the servlet cannot be initialized.
     *
     * @throws IOException
     *    if the servlet container cannot be started.
     */
    public HTTPServletHandler(String servletClassName, int port, boolean daemon) throws ServletException, IOException {
       this(port, daemon);
       addServlet(servletClassName, "/");
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The web server.
     */
    private ServerSocket _serverSocket;
 
    /**
     * The thread that waits for connections from the client.
     */
    private SocketAcceptor _acceptor;
 
    /**
     * Flag indicating if the server should wait for other connections or stop.
     */
    private boolean _running;
 
    /**
     * Mapping between the path and the servlet.
     */
    private Map _servlets = new HashMap();
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Adds a WAR file to the server.
     * The servlet with the virtual path "/" will be the default one.
     * Note that all the libraries used by this WAR file should already be in
     * the classpath.
     *
     * @param warFile
     *    The war file of the application to deploy, cannot be <code>null</code>.
     *
     * @param virtualPath
     *    The virtual path of the HTTP server that links to this WAR file, cannot be <code>null</code>.
     *
     * @throws ServletException
     *    if the servlet cannot be initialized.
     */
    public void addWAR(File warFile, String virtualPath) throws ServletException {
       LocalServletHandler servlet = new LocalServletHandler(warFile);
       _servlets.put(virtualPath, servlet);
    }
 
    /**
     * Adds a new servlet.
     * The servlet with the virtual path "/" will be the default one.
     *
     * @param servletClassName
     *    The name of the servlet's class to load, cannot be <code>null</code>.
     *
     * @param virtualPath
     *    The virtual path of the HTTP server that links to this WAR file, cannot be <code>null</code>.
     *
     * @throws ServletException
     *    if the servlet cannot be initialized.
     */
    public void addServlet(String servletClassName, String virtualPath) throws ServletException{
       LocalServletHandler servlet = new LocalServletHandler(servletClassName);
       _servlets.put(virtualPath, servlet);
    }
 
    /**
     * Remove a servlet from the server.
     *
     * @param virtualPath
     *    The virtual path of the servlet to remove, cannot be <code>null</code>.
     */
    public void removeServlet(String virtualPath) {
       LocalServletHandler servlet = (LocalServletHandler) _servlets.get(virtualPath);
       servlet.close();
       _servlets.remove(virtualPath);
    }
 
    /**
     * Starts the web server.
     *
     * @param port
     *    the port of the servlet server.
     *
     * @param daemon
     *    <code>true</code> if the thread listening to connection should be a
     *    daemon thread, <code>false</code> otherwise.
     *
     * @throws IOException
     *    if the web server cannot be started.
     */
    public void startServer(int port, boolean daemon) throws IOException {
       // Create the server socket
       _serverSocket = new ServerSocket(port, 5);
       _running = true;
 
       _acceptor = new SocketAcceptor(daemon);
       _acceptor.start();
    }
 
    /**
     * Returns the port the server is accepting connections on.
     *
     * @return
     *    the server socket, e.g. <code>8080</code>.
     *
     * @throws IllegalStateException
     *    if the port cannot be determined, for example because the server is
     *    not started.
     *
     * @since XINS 1.5.0
     */
    public int getPort() throws IllegalStateException {
       int port;
       try {
          port = _serverSocket.getLocalPort();
       } catch (NullPointerException exception) {
          port = -1;
       }
 
       if (port < 0) {
          throw new IllegalStateException("Unable to determine port.");
       }
 
       return port;
    }
 
    /**
     * Disposes the servlet and stops the web server.
     */
    public void close() {
       _running = false;
       Iterator itServlets = _servlets.values().iterator();
       while (itServlets.hasNext()) {
          LocalServletHandler servlet = (LocalServletHandler) itServlets.next();
          servlet.close();
       }
       try {
          _serverSocket.close();
       } catch (IOException ioe) {
          Log.log_1502(ioe);
       }
    }
 
    /**
     * This method is invoked when a client connects to the server.
     *
     * @param client
     *    the connection with the client, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>client == null</code>.
     *
     * @throws IOException
     *    if the query is not handled correctly.
     */
    public void serviceClient(Socket client)
    throws IllegalArgumentException, IOException {
 
       // Check argument
       MandatoryArgumentChecker.check("client", client);
 
       BufferedReader       inbound  = null;
       BufferedOutputStream outbound = null;
       try {
          // Acquire the streams for IO
          inbound  = new BufferedReader(new InputStreamReader(client.getInputStream()));
          outbound = new BufferedOutputStream(client.getOutputStream());
 
          httpQuery(inbound, outbound);
 
       } finally{
 
          // Clean up
          if (inbound != null) {
             try {
                inbound.close();
             } catch (Throwable exception) {
                // ignore
             }
          }
 
          if (outbound != null) {
             try {
                outbound.close();
             } catch (Throwable exception) {
                // ignore
             }
          }
       }
    }
 
    /**
     * This method parses the data sent from the client to get the input
     * parameters and format the result as a compatible HTTP result.
     * This method will used the servlet associated with the passed virtual
     * path. If no servlet is associated with the virtual path, the servlet with
     * the virtual path "/" is used as default. If there is no servlet then with
     * the virtual path "/" is found then HTTP 404 is returned.
     *
     * @param input
     *    the input character stream that contains the request sent by the
     *    client.
     *
     * @param outbound
     *    the output byte stream that must be fed the response towards the
     *    client.
     *
     * @throws IOException
     *    if the query is not handled correctly.
     */
    public void httpQuery(BufferedReader input,
                          BufferedOutputStream outbound) throws IOException {
 
       // Read the input
       String url = null;
       char[] contentData = null;
       Map inHeaders = new HashMap();
       int contentLength = -1;
       String inContentType = null;
       boolean inputRead = false;
       String encoding = "ISO-8859-1";
       String method;
       String httpResult;
       boolean getMethod = false;
 
       // Read the first line
       String inputLine = input.readLine();
       if (inputLine == null || inputLine.length() < 1) {
          httpResult = "HTTP/1.1 400 Bad Request\r\n";
          byte[] bytes = httpResult.getBytes(encoding);
          outbound.write(bytes, 0, bytes.length);
          outbound.flush();
          return;
       }
 
       // Find the space
       int spaceIndex = inputLine.indexOf(' ');
       if (spaceIndex < 1) {
          httpResult = "HTTP/1.1 400 Bad Request\r\n";
          byte[] bytes = httpResult.getBytes(encoding);
          outbound.write(bytes, 0, bytes.length);
          outbound.flush();
          return;
       }
 
       // Determine the method
       method = inputLine.substring(0, spaceIndex).toUpperCase();
 
       url = inputLine.substring(spaceIndex + 1);
       if ("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method)) {
          url = url.replace(',', '&');
          getMethod = true;
       }
 
       while (!inputRead && (inputLine = input.readLine()) != null) {
 
          // Read the HTTP headers
          if (inputLine.indexOf(": ") > 0) {
             int colonPos = inputLine.indexOf(": ");
             String headerKey = inputLine.substring(0, colonPos);
             String headerValue = inputLine.substring(colonPos + 2);
             inHeaders.put(headerKey, headerValue);
            if (headerKey.equals("Content-Length")) {
                contentLength = Integer.parseInt(headerValue);
            } else if (headerKey.equals("Content-Type")) {
                inContentType = headerValue;
             }
 
          // Headers read for HTTP GET
          } else if (getMethod && inputLine.equals("")) {
             inputRead = true;
 
          // Headers read for HTTP POST, then read the content
          } else if (contentLength != -1 && inputLine.equals("")) {
             if (inContentType == null) {
                input.readLine();
             }
             contentData = new char[contentLength];
             input.read(contentData);
             inputRead = true;
          }
       }
 
 
       // Normalize the URL (removing the " HTTP/1.1" suffix)
       if (url != null && url.indexOf(' ') != -1) {
          url = url.substring(0, url.indexOf(' '));
          if (url.endsWith("/") && getClass().getResource(url + "index.html") != null) {
             url += "index.html";
          }
       }
 
       if (url == null) {
          httpResult = "HTTP/1.1 400 Bad Request\r\n";
 
       // Handle the case that a web page is requested
       } else if (getMethod && url.indexOf('?') == -1 && !url.endsWith("/") && !"*".equals(url)) {
          httpResult = readWebPage(url);
       } else {
 
          if ((inContentType == null || inContentType.startsWith("application/x-www-form-urlencoded")) && contentData != null) {
             url += '?' + new String(contentData);
             contentData = null;
          }
 
          // Locate the path of the URL
          String virtualPath = url;
          if (virtualPath.indexOf('?') != -1) {
             virtualPath = virtualPath.substring(0, url.indexOf('?'));
          }
          if (virtualPath.endsWith("/") && virtualPath.length() > 1) {
             virtualPath = virtualPath.substring(0, virtualPath.length() - 1);
          }
 
          // Get the Servlet according to the path
          LocalServletHandler servlet = (LocalServletHandler) _servlets.get(virtualPath);
 
          // If not found the root Servlet is used
          if (servlet == null) {
             servlet = (LocalServletHandler) _servlets.get("/");
          }
 
          // If no servlet is found return 404
          if (servlet == null) {
             httpResult = "HTTP/1.1 404 Not Found\r\n";
          } else {
 
             // Query the Servlet
             XINSServletResponse response = servlet.query(method, url, contentData, inHeaders);
 
             // Create the HTTP answer
             httpResult = "HTTP/1.1 " + response.getStatus() + " " +
                   HttpStatus.getStatusText(response.getStatus()) + "\r\n";
             PropertyReader headers = response.getHeaders();
             Iterator itHeaderNames = headers.getNames();
             while (itHeaderNames.hasNext()) {
                String nextHeader = (String) itHeaderNames.next();
                String headerValue = headers.get(nextHeader);
                if (headerValue != null) {
                   httpResult += nextHeader + ": " + headerValue + "\r\n";
                   //System.out.println(": " + nextHeader + ": " + headerValue);
                }
             }
 
             String result = response.getResult();
             if (result != null) {
                encoding = response.getCharacterEncoding();
                int length = result.getBytes(encoding).length + 1;
                httpResult += "Content-Length: " + length + "\r\n";
                httpResult += "Connection: close\r\n";
                httpResult += "\r\n";
                httpResult += result + "\n";
             }
             httpResult += "\n";
          }
       }
 
       byte[] bytes = httpResult.getBytes(encoding);
       outbound.write(bytes, 0, bytes.length);
       outbound.flush();
    }
 
    /**
     * Reads the content of a web page.
     *
     * @param url
     *    the location of the content, cannot be <code>null</code>.
     *
     * @return
     *    the HTTP response to return, never <code>null</code>.
     *
     * @throws IOException
     *    if an error occcurs when reading the URL.
     */
    private String readWebPage(String url) throws IOException {
       String httpResult = null;
       if (getClass().getResource(url) != null) {
          InputStream urlInputStream = getClass().getResourceAsStream(url);
          ByteArrayOutputStream contentOutputStream = new ByteArrayOutputStream();
          byte[] buf = new byte[8192];
          int len;
          while ((len = urlInputStream.read(buf)) > 0) {
             contentOutputStream.write(buf, 0, len);
          }
          contentOutputStream.close();
          urlInputStream.close();
          String content = contentOutputStream.toString("ISO-8859-1");
 
          httpResult = "HTTP/1.1 200 OK\r\n";
          String fileName = url.substring(url.lastIndexOf('/') + 1);
          httpResult += "Content-Type: " + MIME_TYPES_MAP.getContentTypeFor(fileName) + "\r\n";
          int length = content.getBytes("ISO-8859-1").length + 1;
          httpResult += "Content-Length: " + length + "\r\n";
          httpResult += "Connection: close\r\n";
          httpResult += "\r\n";
          httpResult += content + "\n";
          httpResult += "\n";
       } else {
 System.err.println("Web page \"" + url + "\" not found.");
          httpResult = "HTTP/1.1 404 Not Found\r\n";
       }
       return httpResult;
    }
 
    /**
     * Thread waiting for connection from the client.
     */
    private class SocketAcceptor extends Thread {
 
       /**
        * Create the thread.
        *
        * @param daemon
        *    <code>true</code> if the server should be a daemon thread,$
        *    <code>false</code> otherwise.
        */
       public SocketAcceptor(boolean daemon) {
          setDaemon(daemon);
          setName("XINS " + Library.getVersion() + " Servlet container.");
       }
 
       /**
        * Executes the thread.
        */
       public void run() {
          Log.log_1500(_serverSocket.getLocalPort());
          try {
             while (_running) {
                // Wait for a connection
                Socket clientSocket = _serverSocket.accept();
 
                try {
                   // Service the connection
                   serviceClient(clientSocket);
                } catch (Exception ex) {
                   // If anything goes wrong still continue accepting clients
                   Utils.logIgnoredException("SocketAcceptor", "serviceClient", "SocketAcceptor", "run", ex);
                } finally {
                   try {
                      clientSocket.close();
                   } catch (Throwable exception) {
                      // ignore
                   }
                }
             }
          } catch (SocketException ie) {
             // fall through
          } catch (IOException ioe) {
             Log.log_1501(ioe);
          }
       }
    }
 }
